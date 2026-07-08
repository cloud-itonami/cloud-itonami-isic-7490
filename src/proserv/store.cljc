(ns proserv.store
  "SSoT for the professional-services actor, behind a `Store`
  protocol so the backend is a swap, not a rewrite -- the same seam
  every prior `cloud-itonami-isic-*` actor in this fleet uses:

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store (datalog q / pull / upsert). Pure `.cljc`,
                        so it runs offline AND can be pointed at a real
                        Datomic Local or a kotoba-server pod by swapping
                        `langchain.db`'s `:db-api` (see langchain.kotoba-db).

  Both implement the same protocol and pass the same contract
  (test/proserv/store_contract_test.clj), which is the whole point:
  the actor, the Professional Services Governor and the audit ledger
  never know which SSoT they run on.

  Like `clinic.store`'s/`edsupport.store`'s simpler entities, an
  ENGAGEMENT is acted on directly by the ONE actuation op -- no
  dynamically-filed sub-record, and the double-issuance guard checks
  a dedicated `:deliverable-issued?` boolean rather than a `:status`
  value, the same discipline `clinic.governor`'s/`edsupport.
  governor`'s guards establish.

  NOTE on naming: the protocol's per-entity accessor is `engagement`
  directly -- not a Clojure special form, so no `-of` suffix
  workaround was needed.

  The ledger stays append-only on every backend: 'which engagement
  was screened for an unresolved chain-of-title concern, which
  engagement was screened for a not-current professional credential,
  which deliverable was issued, on what jurisdictional basis,
  approved by whom' is always a query over an immutable log -- the
  audit trail a client trusting a professional-services provider
  needs, and the evidence an operator needs if an issuance decision
  is later disputed."
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [proserv.registry :as registry]
            [langchain.db :as d]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (chainoftitle-screen-of [s engagement-id] "committed chain-of-title screening verdict for an engagement, or nil")
  (credential-screen-of [s engagement-id] "committed credential-currency screening verdict for an engagement, or nil")
  (deliverable-of [s engagement-id] "committed deliverable-scope evidence assessment, or nil")
  (ledger [s])
  (deliverable-history [s] "the append-only deliverable-issuance history (proserv.registry drafts)")
  (next-sequence [s jurisdiction] "next deliverable-number sequence for a jurisdiction")
  (engagement-already-issued? [s engagement-id] "has this engagement's deliverable already been issued?")
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory (map id->engagement)"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set so the actor + tests run
  offline."
  []
  {:engagements
   {"engagement-1" {:id "engagement-1" :client-name "Sato Kenji"
                    :chain-of-title-unresolved? false
                    :credential-not-current? false
                    :deliverable-issued? false :jurisdiction "JPN" :status :intake}
    "engagement-2" {:id "engagement-2" :client-name "Atlantis Doe"
                    :chain-of-title-unresolved? false
                    :credential-not-current? false
                    :deliverable-issued? false :jurisdiction "ATL" :status :intake}
    "engagement-3" {:id "engagement-3" :client-name "鈴木花子"
                    :chain-of-title-unresolved? true
                    :credential-not-current? false
                    :deliverable-issued? false :jurisdiction "JPN" :status :intake}
    "engagement-4" {:id "engagement-4" :client-name "田中一郎"
                    :chain-of-title-unresolved? false
                    :credential-not-current? true
                    :deliverable-issued? false :jurisdiction "JPN" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- issue-deliverable!
  "Backend-agnostic `:engagement/mark-issued` -- looks up the
  engagement via the protocol and drafts the deliverable-issuance
  record, and returns {:result .. :engagement-patch ..} for the
  caller to persist."
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-sequence s (:jurisdiction e))
        result (registry/register-deliverable-issuance engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:deliverable-issued? true
                        :deliverable-number (get result "deliverable_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (chainoftitle-screen-of [_ id] (get-in @a [:chainoftitle-screens id]))
  (credential-screen-of [_ id] (get-in @a [:credential-screens id]))
  (deliverable-of [_ engagement-id] (get-in @a [:deliverables engagement-id]))
  (ledger [_] (:ledger @a))
  (deliverable-history [_] (:issuances @a))
  (next-sequence [_ jurisdiction] (get-in @a [:sequences jurisdiction] 0))
  (engagement-already-issued? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :deliverable-issued?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :deliverable-scope/set
      (swap! a assoc-in [:deliverables (first path)] payload)

      :chainoftitle/set
      (swap! a assoc-in [:chainoftitle-screens (first path)] payload)

      :credential/set
      (swap! a assoc-in [:credential-screens (first path)] payload)

      :engagement/mark-issued
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (issue-deliverable! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :issuances registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set. The deterministic
  default."
  []
  (->MemStore (atom (assoc (demo-data)
                           :deliverables {} :chainoftitle-screens {} :credential-screens {}
                           :ledger [] :sequences {} :issuances []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  "DataScript/Datomic-style schema: only constraint attrs are declared.
  Compound values (deliverable-scope/chainoftitle/credential-screen
  payloads, ledger facts, issuance records) are stored as EDN strings
  so `langchain.db` doesn't expand them into sub-entities -- the same
  convention every sibling actor's store uses."
  {:engagement/id                 {:db/unique :db.unique/identity}
   :deliverable/engagement-id      {:db/unique :db.unique/identity}
   :chainoftitle/engagement-id       {:db/unique :db.unique/identity}
   :credential/engagement-id           {:db/unique :db.unique/identity}
   :ledger/seq                           {:db/unique :db.unique/identity}
   :issuance/seq                            {:db/unique :db.unique/identity}
   :sequence/jurisdiction                     {:db/unique :db.unique/identity}})

(defn- enc [v] (pr-str v))
(defn- dec* [s] (when s (edn/read-string s)))

(defn- engagement->tx [{:keys [id client-name chain-of-title-unresolved?
                              credential-not-current? deliverable-issued?
                              jurisdiction status deliverable-number]}]
  (cond-> {:engagement/id id}
    client-name                                    (assoc :engagement/client-name client-name)
    (some? chain-of-title-unresolved?)                (assoc :engagement/chain-of-title-unresolved? chain-of-title-unresolved?)
    (some? credential-not-current?)                     (assoc :engagement/credential-not-current? credential-not-current?)
    (some? deliverable-issued?)                            (assoc :engagement/deliverable-issued? deliverable-issued?)
    jurisdiction                                              (assoc :engagement/jurisdiction jurisdiction)
    status                                                      (assoc :engagement/status status)
    deliverable-number                                           (assoc :engagement/deliverable-number deliverable-number)))

(def ^:private engagement-pull
  [:engagement/id :engagement/client-name :engagement/chain-of-title-unresolved?
   :engagement/credential-not-current? :engagement/deliverable-issued?
   :engagement/jurisdiction :engagement/status :engagement/deliverable-number])

(defn- pull->engagement [m]
  (when (:engagement/id m)
    {:id (:engagement/id m) :client-name (:engagement/client-name m)
     :chain-of-title-unresolved? (boolean (:engagement/chain-of-title-unresolved? m))
     :credential-not-current? (boolean (:engagement/credential-not-current? m))
     :deliverable-issued? (boolean (:engagement/deliverable-issued? m))
     :jurisdiction (:engagement/jurisdiction m) :status (:engagement/status m)
     :deliverable-number (:engagement/deliverable-number m)}))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id %])))
         (sort-by :id)))
  (chainoftitle-screen-of [_ id]
    (dec* (d/q '[:find ?p . :in $ ?eid
                :where [?k :chainoftitle/engagement-id ?eid] [?k :chainoftitle/payload ?p]]
              (d/db conn) id)))
  (credential-screen-of [_ id]
    (dec* (d/q '[:find ?p . :in $ ?eid
                :where [?k :credential/engagement-id ?eid] [?k :credential/payload ?p]]
              (d/db conn) id)))
  (deliverable-of [_ engagement-id]
    (dec* (d/q '[:find ?p . :in $ ?eid
                :where [?a :deliverable/engagement-id ?eid] [?a :deliverable/payload ?p]]
              (d/db conn) engagement-id)))
  (ledger [_]
    (->> (d/q '[:find ?s ?f :where [?e :ledger/seq ?s] [?e :ledger/fact ?f]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (deliverable-history [_]
    (->> (d/q '[:find ?s ?r :where [?e :issuance/seq ?s] [?e :issuance/record ?r]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (next-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :sequence/jurisdiction ?j] [?e :sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (engagement-already-issued? [s engagement-id]
    (boolean (:deliverable-issued? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(engagement->tx value)])

      :deliverable-scope/set
      (d/transact! conn [{:deliverable/engagement-id (first path) :deliverable/payload (enc payload)}])

      :chainoftitle/set
      (d/transact! conn [{:chainoftitle/engagement-id (first path) :chainoftitle/payload (enc payload)}])

      :credential/set
      (d/transact! conn [{:credential/engagement-id (first path) :credential/payload (enc payload)}])

      :engagement/mark-issued
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (issue-deliverable! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:sequence/jurisdiction jurisdiction :sequence/next next-n}
                      {:issuance/seq (count (deliverable-history s)) :issuance/record (enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (d/transact! conn [{:ledger/seq (count (ledger s)) :ledger/fact (enc fact)}])
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv engagement->tx (vals engagements)))) s))

(defn datomic-store
  "A DatomicStore (langchain.db backend) seeded from `data`
  ({:engagements ..}); empty when omitted."
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  "A DatomicStore seeded with the demo engagement set -- the Datomic-
  backed analog of `seed-db`, used to prove protocol parity."
  []
  (datomic-store (demo-data)))
