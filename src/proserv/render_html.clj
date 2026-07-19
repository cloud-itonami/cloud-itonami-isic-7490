(ns proserv.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root flagship
  rollout): this repo previously had NO demo page and no generator at
  all. This namespace drives the REAL actor stack (`proserv.operation`
  -> `proserv.governor` -> `proserv.store`) through a scenario adapted
  from this repo's own `proserv.sim` demo driver (`clojure -M:dev:run`,
  confirmed by actually running it and diffing its audit-ledger output
  against `proserv.store/demo-data`'s real engagement ids before this
  file was written -- unlike `cloud-itonami-isic-851`'s broken
  `schoolops.sim`, this repo's own sim driver uses ids that DO match
  `proserv.store/demo-data`, so it was safe to reuse rather than author
  from scratch), trimmed to a representative subset (one auto-commit,
  one escalate->approve lifecycle covering verification through
  deliverable issuance, and three distinct HARD-hold reasons) and
  rendered deterministically -- no invented numbers, no timestamps in
  the page content, byte-identical across reruns against the same seed
  (verified by diffing two consecutive runs).

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [clojure.string :as str]
            [proserv.store :as store]
            [proserv.operation :as op]
            [langgraph.graph :as g]))

(def ^:private operator
  {:actor-id "op-1" :actor-role :licensed-professional :phase 3})

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}}
          {:thread-id tid :resume? true}))

(defn run-demo!
  "Runs a fresh seeded store through a scenario mixing every
  disposition this actor can reach: engagement-1 clears intake
  (auto-commit -- phase 3's ONE auto-eligible op, no capital risk),
  then a professional-services evidence-checklist verification (ALWAYS
  escalates -- approved), then a deliverable-issuance proposal (ALWAYS
  escalates -- approved, `proserv.governor`'s evidence-incomplete
  check is satisfied because the verify step above already committed
  the full JPN checklist); engagement-2 HARD-holds an evidence-
  checklist verification with no spec-basis (jurisdiction forced to
  ATL, absent from `proserv.facts/catalog`); engagement-3 HARD-holds a
  chain-of-title screening on its own unresolved concern
  (`proserv.store/demo-data`'s seeded `:chain-of-title-unresolved?
  true`); engagement-4 HARD-holds a credential screening on its own
  lapsed credential (`:credential-not-current? true`); finally
  engagement-1 HARD-holds a SECOND deliverable-issuance attempt
  (double-issuance guard). Every HARD hold never reaches a human.
  Returns the resulting store -- every field read by `render` below is
  real governor/store output, not a hand-typed copy."
  []
  (let [db (store/seed-db)
        actor (op/build db)]
    (exec! actor "e1-intake" {:op :engagement/intake :subject "engagement-1"
                               :patch {:id "engagement-1" :client-name "Sato Kenji"}})

    (exec! actor "e1-verify" {:op :engagement/verify :subject "engagement-1"})
    (approve! actor "e1-verify")

    (exec! actor "e1-issue" {:op :actuation/issue-deliverable :subject "engagement-1"})
    (approve! actor "e1-issue")

    (exec! actor "e2-verify" {:op :engagement/verify :subject "engagement-2" :no-spec? true})

    (exec! actor "e3-chainoftitle" {:op :chainoftitle/screen :subject "engagement-3"})

    (exec! actor "e4-credential" {:op :credential/screen :subject "engagement-4"})

    (exec! actor "e1-issue-again" {:op :actuation/issue-deliverable :subject "engagement-1"})
    db))

;; ----------------------------- rendering -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- last-fact-for [ledger engagement-id]
  (last (filter #(= (:subject %) engagement-id) ledger)))

(defn- status-cell [ledger engagement-id]
  (let [f (last-fact-for ledger engagement-id)]
    (cond
      (nil? f) "<span class=\"muted\">no activity</span>"
      (= :committed (:t f)) "<span class=\"ok\">committed</span>"
      (= :approval-granted (:t f)) "<span class=\"ok\">approved &amp; committed</span>"
      (= :governor-hold (:t f))
      (let [rule (-> f :violations first :rule)]
        (str "<span class=\"critical\">HARD hold &middot; " (esc (name (or rule :unknown))) "</span>"))
      (= :approval-requested (:t f)) "<span class=\"warn\">awaiting approval</span>"
      :else "<span class=\"muted\">in progress</span>")))

(defn- engagement-row [ledger {:keys [id client-name jurisdiction
                                       chain-of-title-unresolved?
                                       credential-not-current?
                                       deliverable-issued?]}]
  (format "        <tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>"
          (esc id) (esc client-name) (esc jurisdiction)
          (if chain-of-title-unresolved?
            "<span class=\"critical\">unresolved</span>"
            "<span class=\"ok\">resolved</span>")
          (if credential-not-current?
            "<span class=\"critical\">not current</span>"
            "<span class=\"ok\">current</span>")
          (if deliverable-issued?
            "<span class=\"ok\">issued</span>"
            "<span class=\"muted\">not issued</span>")
          (status-cell ledger id)))

(defn- ledger-row [{:keys [t op subject disposition basis]}]
  (format "        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td></tr>"
          (esc (name t)) (esc (name (or op :n-a))) (esc subject)
          (esc (or (some->> basis (map name) (str/join ", ")) (some-> disposition name) ""))))

(def ^:private action-gate-rows
  ;; Static description of this actor's own op contract (README `Ops`
  ;; table, `proserv.governor`/`proserv.phase`) -- documentation of
  ;; fixed behavior, not runtime telemetry, so it is legitimately
  ;; hand-described rather than derived from a live run.
  ["        <tr><td><code>:engagement/intake</code></td><td><span class=\"ok\">auto-commit when clean, no capital risk (phase 3's only auto-eligible op)</span></td></tr>"
   "        <tr><td><code>:engagement/verify</code></td><td><span class=\"warn\">ALWAYS human approval &middot; rejects a fabricated jurisdiction spec-basis</span></td></tr>"
   "        <tr><td><code>:chainoftitle/screen</code></td><td><span class=\"warn\">ALWAYS human approval &middot; HARD-holds un-overridably on an unresolved concern</span></td></tr>"
   "        <tr><td><code>:credential/screen</code></td><td><span class=\"warn\">ALWAYS human approval &middot; HARD-holds un-overridably on a lapsed credential</span></td></tr>"
   "        <tr><td><code>:actuation/issue-deliverable</code></td><td><span class=\"warn\">ALWAYS human approval &middot; evidence checklist re-verified &middot; double-issuance blocked</span></td></tr>"])

(defn render
  "Renders the full operator-console.html document from a store `db`
  that has already run `run-demo!` (or any other real scenario)."
  [db]
  (let [ledger (vec (store/ledger db))
        engagements (->> (store/all-engagements db)
                         (filter #(#{"engagement-1" "engagement-2" "engagement-3" "engagement-4"} (:id %)))
                         (sort-by :id))
        engagement-rows (str/join "\n" (map (partial engagement-row ledger) engagements))
        ledger-rows (str/join "\n" (map ledger-row ledger))]
    (str
     "<html><head><meta charset=\"utf-8\"><title>cloud-itonami-isic-7490 &middot; professional-services (proserv)</title><style>\n"
     "table { width: 100%; border-collapse: collapse; font-size: 14px; }\n"
     ".ok { color: #137a3f; }\n"
     "body { font-family: system-ui,-apple-system,sans-serif; margin: 0; color: #1a1a1a; background: #fafafa; }\n"
     "header.bar { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: #fff; border-bottom: 1px solid #e5e5e5; }\n"
     "th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #f0f0f0; }\n"
     "h2 { margin-top: 0; font-size: 15px; }\n"
     ".warn { color: #b25c00; background: #fff8e1; padding: 2px 6px; border-radius: 4px; }\n"
     "main { max-width: 980px; margin: 24px auto; padding: 0 20px; }\n"
     "header.bar h1 { font-size: 18px; margin: 0; font-weight: 600; }\n"
     ".muted { color: #888; font-size: 13px; }\n"
     ".critical { color: #fff; background: #b3261e; padding: 2px 6px; border-radius: 4px; font-weight: 600; }\n"
     ".card { background: #fff; border: 1px solid #e5e5e5; border-radius: 8px; padding: 16px; margin-bottom: 16px; }\n"
     ".err { color: #b3261e; background: #fbe9e7; padding: 2px 6px; border-radius: 4px; }\n"
     "th { font-weight: 600; color: #555; font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; }\n"
     "header.bar .badge { margin-left: auto; font-size: 12px; color: #666; }\n"
     "code { font-size: 12px; background: #f4f4f4; padding: 1px 4px; border-radius: 3px; }\n"
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Professional services, n.e.c. (ISIC 7490) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · deliverable issuance always human-approved</span>\n"
     "</header>\n"
     "<main>\n"
     "  <section class=\"card\">\n"
     "    <h2>Engagements</h2>\n"
     "    <p class=\"muted\">Demo snapshot — build-time-generated from <code>proserv.store</code> via <code>proserv.render-html</code> (<code>clojure -M:dev:render-html</code>), regenerated nightly.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Engagement</th><th>Client</th><th>Jurisdiction</th><th>Chain of title</th><th>Credential</th><th>Deliverable</th><th>Last op status</th></tr></thead>\n"
     "      <tbody>\n"
     engagement-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Action gate (Professional Services Governor)</h2>\n"
     "    <p class=\"muted\">HARD holds cannot be overridden. A jurisdiction's professional-services requirements are never invented; an unresolved chain-of-title concern or a lapsed credential un-overridably blocks a deliverable, and the same engagement can never be issued twice.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Op</th><th>Gate</th></tr></thead>\n"
     "      <tbody>\n"
     (str/join "\n" action-gate-rows) "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Audit ledger (this run)</h2>\n"
     "    <p class=\"muted\">Append-only decision-fact log — every proposal, hold and commit this scenario produced.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Fact</th><th>Op</th><th>Subject</th><th>Basis</th></tr></thead>\n"
     "      <tbody>\n"
     ledger-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "</main>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        db (run-demo!)
        html (render db)]
    (spit out html)
    (println "wrote" out "(" (count (store/ledger db)) "ledger facts,"
             (count (store/deliverable-history db)) "deliverable issuances )")))
