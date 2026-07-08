(ns proserv.governor-contract-test
  "The governor contract as executable tests -- the professional-
  services analog of `cloud-itonami-isic-6512`'s `casualty.governor-
  contract-test`. The single invariant under test:

    ProServ-LLM never issues a deliverable the Professional Services
    Governor would reject, `:actuation/issue-deliverable` NEVER auto-
    commits at any phase, `:engagement/intake` (no direct capital
    risk) MAY auto-commit when clean, and every decision (commit OR
    hold) leaves exactly one ledger fact."
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [proserv.store :as store]
            [proserv.operation :as op]))

(defn- fresh []
  (let [db (store/seed-db)]
    [db (op/build db)]))

(def operator {:actor-id "op-1" :actor-role :licensed-professional :phase 3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- verify!
  "Walks `subject` through verify -> approve, leaving a deliverable-
  scope assessment on file. Uses distinct thread-ids per call site by
  suffixing `tid-prefix`."
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-verify") {:op :engagement/verify :subject subject} operator)
  (approve! actor (str tid-prefix "-verify")))

(deftest clean-intake-auto-commits
  (let [[db actor] (fresh)
        res (exec-op actor "t1"
                  {:op :engagement/intake :subject "engagement-1"
                   :patch {:id "engagement-1" :client-name "Sato Kenji"}} operator)]
    (is (= :commit (get-in res [:state :disposition])))
    (is (= "Sato Kenji" (:client-name (store/engagement db "engagement-1"))) "SSoT actually updated")
    (is (= 1 (count (store/ledger db))))))

(deftest engagement-verify-always-needs-approval
  (testing "verify is never in any phase's :auto set -- always human approval, even when clean"
    (let [[db actor] (fresh)
          res (exec-op actor "t2" {:op :engagement/verify :subject "engagement-1"} operator)]
      (is (= :interrupted (:status res)))
      (let [r2 (approve! actor "t2")]
        (is (= :commit (get-in r2 [:state :disposition])))
        (is (some? (store/deliverable-of db "engagement-1")))))))

(deftest fabricated-jurisdiction-is-held
  (testing "an engagement/verify proposal with no official spec-basis -> HOLD, never reaches a human"
    (let [[db actor] (fresh)
          res (exec-op actor "t3"
                    {:op :engagement/verify :subject "engagement-1" :no-spec? true} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:no-spec-basis} (-> (store/ledger db) first :basis)))
      (is (nil? (store/deliverable-of db "engagement-1")) "no deliverable scope written"))))

(deftest issue-deliverable-without-deliverable-scope-is-held
  (testing "actuation/issue-deliverable before any engagement verification -> HOLD (evidence incomplete)"
    (let [[db actor] (fresh)
          res (exec-op actor "t4" {:op :actuation/issue-deliverable :subject "engagement-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:evidence-incomplete} (-> (store/ledger db) first :basis))))))

(deftest chain-of-title-unresolved-is-held-and-unoverridable
  (testing "an unresolved chain-of-title concern on an engagement -> HOLD, and never reaches request-approval -- exercised via :chainoftitle/screen DIRECTLY, not via the actuation op against an unscreened engagement (see this actor's governor ns docstring / parksafety's ADR-2607071922 Decision 5 / eldercare's, museum's, conservation's, salon's, entertainment's, casework's, hospital's, facility's, school's, association's, leasing's, behavioral's, secondary's, card's, water's, telecom's, aerospace's, recovery's, consulting's, union's, congregation's, fab's, energy's, care's, navigator's, learning's, banking's, advertising's, polling's, research's, design's, nursing's, sports's, alliedhealth's, laundry's, holdco's, photo's, personalservice's, edsupport's, headoffice's, residential's, cultural's and reserve's ADR-0001s)"
    (let [[db actor] (fresh)
          res (exec-op actor "t5" {:op :chainoftitle/screen :subject "engagement-3"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:chain-of-title-unresolved} (-> (store/ledger db) first :basis)))
      (is (nil? (store/chainoftitle-screen-of db "engagement-3")) "no clearance written"))))

(deftest credential-not-current-is-held-and-unoverridable
  (testing "a not-current professional credential on an engagement -> HOLD, and never reaches request-approval -- exercised via :credential/screen DIRECTLY"
    (let [[db actor] (fresh)
          res (exec-op actor "t6" {:op :credential/screen :subject "engagement-4"} operator)]
      (is (= :hold (get-in res [:state :disposition])) "settles immediately, no interrupt")
      (is (not= :interrupted (:status res)))
      (is (some #{:credential-not-current} (-> (store/ledger db) first :basis)))
      (is (nil? (store/credential-screen-of db "engagement-4")) "no clearance written"))))

(deftest issue-deliverable-always-escalates-then-human-decides
  (testing "a clean, fully-assessed engagement still ALWAYS interrupts for human approval -- actuation/issue-deliverable is never auto"
    (let [[db actor] (fresh)
          _ (verify! actor "t7pre" "engagement-1")
          r1 (exec-op actor "t7" {:op :actuation/issue-deliverable :subject "engagement-1"} operator)]
      (is (= :interrupted (:status r1)) "pauses for human approval even when governor-clean")
      (testing "approve -> commit, deliverable-issuance record drafted"
        (let [r2 (approve! actor "t7")]
          (is (= :commit (get-in r2 [:state :disposition])))
          (is (true? (:deliverable-issued? (store/engagement db "engagement-1"))))
          (is (= 1 (count (store/deliverable-history db))) "one draft issuance record"))))))

(deftest double-issuance-is-held
  (testing "issuing the same engagement's deliverable twice -> HOLD on the second attempt"
    (let [[db actor] (fresh)
          _ (verify! actor "t8pre" "engagement-1")
          _ (exec-op actor "t8a" {:op :actuation/issue-deliverable :subject "engagement-1"} operator)
          _ (approve! actor "t8a")
          res (exec-op actor "t8" {:op :actuation/issue-deliverable :subject "engagement-1"} operator)]
      (is (= :hold (get-in res [:state :disposition])))
      (is (some #{:already-issued} (-> (store/ledger db) last :basis)))
      (is (= 1 (count (store/deliverable-history db))) "still only the one earlier issuance"))))

(deftest every-decision-leaves-one-ledger-fact
  (testing "write-only-through-ledger: N operations -> N ledger facts"
    (let [[db actor] (fresh)]
      (exec-op actor "a" {:op :engagement/intake :subject "engagement-1"
                          :patch {:id "engagement-1" :client-name "Sato Kenji"}} operator)
      (exec-op actor "b" {:op :engagement/verify :subject "engagement-1" :no-spec? true} operator)
      (is (= 2 (count (store/ledger db)))
          "one commit + one hold, both recorded"))))
