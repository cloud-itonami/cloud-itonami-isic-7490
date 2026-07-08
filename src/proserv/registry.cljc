(ns proserv.registry
  "Pure-function deliverable/attestation-issuance record construction
  -- an append-only professional-services book-of-record draft.

  Like every sibling actor's registry, there is no single
  international check-digit standard for a deliverable/attestation
  reference number -- every practice/jurisdiction assigns its own
  reference format. This namespace does NOT invent one; it builds a
  jurisdiction-scoped sequence number and validates the record's
  required fields, the same honest, non-fabricating discipline
  `proserv.facts` uses.

  Unlike siblings whose distinctive check is a numeric/temporal
  ground-truth recompute (a registry-level pure predicate), this
  build's TWO distinctive checks (`chain-of-title-unresolved?` and
  `credential-not-current?`) are both BOOLEAN flags read directly off
  the engagement's own record by `proserv.governor` -- the same shape
  `photo.governor`'s `minor-subject-guardian-consent-unresolved-
  violations` and `residential.governor`'s `mandatory-reporting-
  obligation-unresolved-violations` use, neither of which needed a
  dedicated registry-level predicate either. This namespace is
  therefore intentionally 'plain': record construction only, no
  distinctive check function.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real patent-registry/practice-management system. It
  builds the RECORD a professional-services operator would keep, not
  the act of issuing the deliverable or attestation itself (that is
  `proserv.operation`'s `:actuation/issue-deliverable`, always human-
  gated -- see README `Actuation`)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is the
  professional-services operator's own act, not this actor's. See
  README `Actuation`."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn register-deliverable-issuance
  "Validate + construct the DELIVERABLE-ISSUANCE registration DRAFT --
  the professional-services operator's own act of issuing a real
  deliverable or attestation to a client. Pure function -- does not
  touch any real patent-registry/practice-management system; it
  builds the RECORD an operator would keep. `proserv.governor`
  independently re-verifies the engagement's own chain-of-title and
  credential-currency status, and blocks a double-issuance for the
  same engagement, before this is ever allowed to commit."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "deliverable-issuance: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "deliverable-issuance: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "deliverable-issuance: sequence must be >= 0" {})))
  (let [deliverable-number (str (str/upper-case jurisdiction) "-DLV-" (zero-pad sequence 6))
        record {"record_id" deliverable-number
                "kind" "deliverable-issuance-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "deliverable_number" deliverable-number
     "certificate" (unsigned-certificate "DeliverableIssuance" deliverable-number deliverable-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
