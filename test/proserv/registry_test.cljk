(ns proserv.registry-test
  (:require [clojure.test :refer [deftest is]]
            [proserv.registry :as r]))

;; ----------------------------- register-deliverable-issuance -----------------------------

(deftest issuance-is-a-draft-not-a-real-issuance
  (let [result (r/register-deliverable-issuance "engagement-1" "JPN" 0)]
    (is (nil? (get-in result ["certificate" "proof"])))
    (is (= (get-in result ["certificate" "issued_by_registry"]) false))
    (is (= (get-in result ["certificate" "status"]) "draft-unsigned"))))

(deftest issuance-assigns-deliverable-number
  (let [result (r/register-deliverable-issuance "engagement-1" "JPN" 7)]
    (is (= (get result "deliverable_number") "JPN-DLV-000007"))
    (is (= (get-in result ["record" "engagement_id"]) "engagement-1"))
    (is (= (get-in result ["record" "kind"]) "deliverable-issuance-draft"))
    (is (= (get-in result ["record" "immutable"]) true))))

(deftest issuance-validation-rules
  (is (thrown? Exception (r/register-deliverable-issuance "" "JPN" 0)))
  (is (thrown? Exception (r/register-deliverable-issuance "engagement-1" "" 0)))
  (is (thrown? Exception (r/register-deliverable-issuance "engagement-1" "JPN" -1))))

(deftest history-is-append-only
  (let [c1 (r/register-deliverable-issuance "engagement-1" "JPN" 0)
        hist (r/append [] c1)
        c2 (r/register-deliverable-issuance "engagement-2" "JPN" 1)
        hist2 (r/append hist c2)]
    (is (= 2 (count hist2)))
    (is (= "JPN-DLV-000000" (get-in hist2 [0 "record_id"])))
    (is (= "JPN-DLV-000001" (get-in hist2 [1 "record_id"])))))
