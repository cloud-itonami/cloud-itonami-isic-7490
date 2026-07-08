(ns proserv.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a clean engagement
  through intake -> deliverable-scope verification -> chain-of-title
  screening -> credential screening -> deliverable-issuance proposal
  (always escalates) -> human approval -> commit, then shows four
  HARD holds (a jurisdiction with no spec-basis, an engagement whose
  own recorded chain-of-title concern has NOT been resolved [screened
  directly via `:chainoftitle/screen` -- never via an actuation op
  against an unscreened engagement -- see this actor's own governor
  ns docstring / the lesson `parksafety`'s ADR-2607071922 Decision 5,
  `eldercare`'s, `museum`'s, `conservation`'s, `salon`'s,
  `entertainment`'s, `casework`'s, `hospital`'s, `facility`'s,
  `school`'s, `association`'s, `leasing`'s, `behavioral`'s,
  `secondary`'s, `card`'s, `water`'s, `telecom`'s, `aerospace`'s,
  `recovery`'s, `consulting`'s, `union`'s, `congregation`'s, `fab`'s,
  `energy`'s, `care`'s, `navigator`'s, `learning`'s, `banking`'s,
  `advertising`'s, `polling`'s, `research`'s, `design`'s, `nursing`'s,
  `sports`'s, `alliedhealth`'s, `laundry`'s, `holdco`'s, `photo`'s,
  `personalservice`'s, `edsupport`'s, `headoffice`'s, `residential`'s,
  `cultural`'s and `reserve`'s ADR-0001s already recorded], a not-
  current professional credential screened directly, and a double
  issuance of an already-processed engagement) that never reach a
  human at all, and prints the audit ledger + the draft deliverable-
  issuance records."
  (:require [langgraph.graph :as g]
            [proserv.store :as store]
            [proserv.operation :as op]))

(def operator {:actor-id "op-1" :actor-role :licensed-professional :phase 3})

(defn- exec! [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn -main [& _]
  (let [db (store/seed-db)
        actor (op/build db)]
    (println "== engagement/intake engagement-1 (JPN, clean; chain of title resolved, credential current) ==")
    (println (exec! actor "t1" {:op :engagement/intake :subject "engagement-1"
                                :patch {:id "engagement-1" :client-name "Sato Kenji"}} operator))

    (println "== engagement/verify engagement-1 (escalates -- human approves) ==")
    (println (exec! actor "t2" {:op :engagement/verify :subject "engagement-1"} operator))
    (println (approve! actor "t2"))

    (println "== chainoftitle/screen engagement-1 (clean; escalates -- human approves) ==")
    (println (exec! actor "t3" {:op :chainoftitle/screen :subject "engagement-1"} operator))
    (println (approve! actor "t3"))

    (println "== credential/screen engagement-1 (clean; escalates -- human approves) ==")
    (println (exec! actor "t4" {:op :credential/screen :subject "engagement-1"} operator))
    (println (approve! actor "t4"))

    (println "== actuation/issue-deliverable engagement-1 (always escalates -- actuation/issue-deliverable) ==")
    (let [r (exec! actor "t5" {:op :actuation/issue-deliverable :subject "engagement-1"} operator)]
      (println r)
      (println "-- human licensed professional approves --")
      (println (approve! actor "t5")))

    (println "== engagement/verify engagement-2 (no spec-basis -> HARD hold) ==")
    (println (exec! actor "t6" {:op :engagement/verify :subject "engagement-2" :no-spec? true} operator))

    (println "== chainoftitle/screen engagement-3 (unresolved -> HARD hold, never reaches a human) ==")
    (println (exec! actor "t7" {:op :chainoftitle/screen :subject "engagement-3"} operator))

    (println "== credential/screen engagement-4 (not current -> HARD hold, never reaches a human) ==")
    (println (exec! actor "t8" {:op :credential/screen :subject "engagement-4"} operator))

    (println "== actuation/issue-deliverable engagement-1 AGAIN (double-issuance -> HARD hold) ==")
    (println (exec! actor "t9" {:op :actuation/issue-deliverable :subject "engagement-1"} operator))

    (println "== audit ledger ==")
    (doseq [f (store/ledger db)] (println f))

    (println "== draft deliverable-issuance records ==")
    (doseq [r (store/deliverable-history db)] (println r))))
