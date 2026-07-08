(ns proserv.proservadvisor
  "ProServ-LLM client -- the *contained intelligence node* for the
  professional-services actor.

  It normalizes engagement intake, drafts a per-jurisdiction
  professional-services evidence checklist, screens engagements for
  an unresolved chain-of-title concern and for a not-current
  professional credential, and drafts the deliverable-issuance
  action. CRITICAL: it is a smart-but-untrusted advisor. It returns a
  *proposal* (with a rationale + the fields it cited), never a
  committed record or a real deliverable/attestation issuance. Every
  output is censored downstream by `proserv.governor` before anything
  touches the SSoT, and `:actuation/issue-deliverable` proposals
  NEVER auto-commit at any phase -- see README `Actuation`.

  Like every sibling actor's advisor, this is a deterministic mock so
  the actor graph runs offline and the governor contract is exercised
  end-to-end. In production this calls a real LLM (kotoba-llm or
  equivalent) with the same proposal shape.

  Proposal shape (all kinds):
    {:summary    str            ; human-facing draft / finding
     :rationale  str            ; why -- SCANNED by the spec-basis gate
     :cites      [kw|str ..]    ; facts/sources the LLM used -- SCANNED too
     :effect     kw             ; how a commit would mutate the SSoT
     :stake      kw|nil         ; :actuation/issue-deliverable | nil
     :confidence 0..1}"
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [clojure.string :as str]
            [proserv.facts :as facts]
            [proserv.store :as store]
            [langchain.model :as model]))

(defn- normalize-intake
  "Directory upsert -- the LLM only normalizes/validates the patch; it
  does not invent the engagement, client or jurisdiction. High
  confidence, low stakes."
  [_db {:keys [patch]}]
  {:summary    (str "案件記録更新: " (pr-str (keys patch)))
   :rationale  "入力 patch の正規化のみ。新規事実の生成なし。"
   :cites      (vec (keys patch))
   :effect     :engagement/upsert
   :value      patch
   :stake      nil
   :confidence 0.97})

(defn- verify-engagement
  "Per-jurisdiction professional-services evidence checklist draft.
  `:no-spec?` injects the failure mode we must defend against:
  proposing a checklist for a jurisdiction with NO official spec-basis
  in `proserv.facts` -- the Professional Services Governor must reject
  this (never invent a jurisdiction's requirements)."
  [db {:keys [subject no-spec?]}]
  (let [e (store/engagement db subject)
        iso3 (if no-spec? "ATL" (:jurisdiction e))
        sb (facts/spec-basis iso3)]
    (if (nil? sb)
      {:summary    (str iso3 " の公式spec-basisが見つかりません")
       :rationale  "proserv.facts に未登録の法域。要件を推測で作らない。"
       :cites      []
       :effect     :deliverable-scope/set
       :value      {:jurisdiction iso3 :checklist [] :spec-basis nil}
       :stake      nil
       :confidence 0.9}
      {:summary    (str iso3 " (" (:owner-authority sb) ") 向け必要書類 "
                        (count (:required-evidence sb)) " 件を提案")
       :rationale  (str "公式ソース: " (:provenance sb) " / 法的根拠: " (:legal-basis sb))
       :cites      [(:legal-basis sb) (:provenance sb)]
       :effect     :deliverable-scope/set
       :value      {:jurisdiction iso3
                    :checklist (:required-evidence sb)
                    :spec-basis (:provenance sb)
                    :legal-basis (:legal-basis sb)}
       :stake      nil
       :confidence 0.9})))

(defn- screen-chain-of-title
  "Chain-of-title screening draft. `:chain-of-title-unresolved?` on
  the engagement record injects the failure mode: the Professional
  Services Governor must HOLD, un-overridably, on any unresolved
  concern."
  [db {:keys [subject]}]
  (let [e (store/engagement db subject)]
    (cond
      (nil? e)
      {:summary "対象案件記録が見つかりません" :rationale "no engagement record"
       :cites [] :effect :chainoftitle/set :value {:engagement-id subject :chain-of-title-unresolved? nil}
       :stake nil :confidence 0.0}

      (true? (:chain-of-title-unresolved? e))
      {:summary    (str (:client-name e) ": 権原(チェーン・オブ・タイトル)が未解決")
       :rationale  "スクリーニングが未解決状態を検出。人手確認とホールドが必須。"
       :cites      [:chainoftitle-check]
       :effect     :chainoftitle/set
       :value      {:engagement-id subject :chain-of-title-unresolved? true}
       :stake      nil
       :confidence 0.95}

      :else
      {:summary    (str (:client-name e) ": 権原は解決済み")
       :rationale  "権原スクリーニング完了。"
       :cites      [:chainoftitle-check]
       :effect     :chainoftitle/set
       :value      {:engagement-id subject :chain-of-title-unresolved? false}
       :stake      nil
       :confidence 0.9})))

(defn- screen-credential
  "Professional-credential-currency screening draft.
  `:credential-not-current?` on the engagement record injects the
  failure mode: the Professional Services Governor must HOLD, un-
  overridably, on any not-current credential."
  [db {:keys [subject]}]
  (let [e (store/engagement db subject)]
    (cond
      (nil? e)
      {:summary "対象案件記録が見つかりません" :rationale "no engagement record"
       :cites [] :effect :credential/set :value {:engagement-id subject :credential-not-current? nil}
       :stake nil :confidence 0.0}

      (true? (:credential-not-current? e))
      {:summary    (str (:client-name e) ": 担当者の専門資格が最新でないことを検出")
       :rationale  "スクリーニングが資格の失効を検出。人手確認とホールドが必須。"
       :cites      [:credential-check]
       :effect     :credential/set
       :value      {:engagement-id subject :credential-not-current? true}
       :stake      nil
       :confidence 0.95}

      :else
      {:summary    (str (:client-name e) ": 担当者の専門資格は最新")
       :rationale  "資格スクリーニング完了。"
       :cites      [:credential-check]
       :effect     :credential/set
       :value      {:engagement-id subject :credential-not-current? false}
       :stake      nil
       :confidence 0.9})))

(defn- propose-deliverable-issuance
  "Draft the actual DELIVERABLE-ISSUANCE action -- issuing a real
  deliverable or attestation to a client. ALWAYS `:stake :actuation/
  issue-deliverable` -- this is a REAL-WORLD act, never a draft the
  actor may auto-run. See README `Actuation`: no phase ever adds this
  op to a phase's `:auto` set (`proserv.phase`); the governor also
  always escalates on `:actuation/issue-deliverable`. Two independent
  layers agree, deliberately."
  [db {:keys [subject]}]
  (let [e (store/engagement db subject)
        safe? (and e (not (:chain-of-title-unresolved? e))
                   (not (:credential-not-current? e)))]
    {:summary    (str subject " 向け納品確定提案"
                      (when e (str " (client=" (:client-name e) ")")))
     :rationale  (if e
                   (str "chain-of-title-unresolved?=" (:chain-of-title-unresolved? e)
                        " credential-not-current?=" (:credential-not-current? e))
                   "案件記録が見つかりません")
     :cites      (if e [subject] [])
     :effect     :engagement/mark-issued
     :value      {:engagement-id subject}
     :stake      :actuation/issue-deliverable
     :confidence (if safe? 0.9 0.3)}))

(defn infer
  "Route a request to the right proposal generator.
  request: {:op kw :subject id ...op-specific...}"
  [db {:keys [op] :as request}]
  (case op
    :engagement/intake              (normalize-intake db request)
    :engagement/verify               (verify-engagement db request)
    :chainoftitle/screen             (screen-chain-of-title db request)
    :credential/screen               (screen-credential db request)
    :actuation/issue-deliverable     (propose-deliverable-issuance db request)
    {:summary "未対応の操作" :rationale (str op) :cites []
     :effect :noop :stake nil :confidence 0.0}))

;; ----------------------------- Advisor protocol -----------------------------

(defprotocol Advisor
  (-advise [advisor store request] "store + request -> proposal map"))

(defn mock-advisor
  "The deterministic advisor (the `infer` logic above). Default everywhere."
  [] (reify Advisor (-advise [_ st req] (infer st req))))

(def ^:private system-prompt
  (str "あなたは専門サービス事業(翻訳・通訳・鑑定・特許仲介等)の納品確定"
       "エージェントの助言者です。与えられた事実のみに基づき、提案を1つだけ"
       "EDNマップで返します。説明や前置きは一切書かず、EDNだけを出力します。\n"
       "キー: :summary(人向けドラフト) :rationale(根拠/必ず事実から) "
       ":cites(使った事実キーのベクタ) "
       ":effect(:engagement/upsert|:deliverable-scope/set|:chainoftitle/set|"
       ":credential/set|:engagement/mark-issued) "
       ":stake(:actuation/issue-deliverable か nil) :confidence(0..1)。\n"
       "重要: 登録されていない法域の要件を絶対に創作してはいけません。"
       "spec-basisが無い場合は :cites を空にし confidence を上げないこと。"))

(defn- facts-for [st {:keys [op subject]}]
  (case op
    :engagement/verify               {:engagement (store/engagement st subject)}
    :chainoftitle/screen              {:engagement (store/engagement st subject)}
    :credential/screen                {:engagement (store/engagement st subject)}
    :actuation/issue-deliverable      {:engagement (store/engagement st subject)}
    {:engagement (store/engagement st subject)}))

(defn- parse-proposal
  "Parse the model's EDN proposal defensively. Any parse/shape failure
  yields a safe low-confidence noop so the Professional Services
  Governor escalates/holds -- an LLM hiccup can never auto-issue a
  deliverable."
  [content]
  (let [p (try (edn/read-string (str/trim (str content)))
               (catch #?(:clj Exception :cljs :default) _ nil))]
    (if (map? p)
      (-> p
          (update :cites #(vec (or % [])))
          (update :confidence #(if (number? %) (double %) 0.0))
          (update :effect #(or % :noop)))
      {:summary "LLM応答を解釈できませんでした" :rationale (str content)
       :cites [] :effect :noop :stake nil :confidence 0.0})))

(defn llm-advisor
  "An advisor backed by a `langchain.model/ChatModel` (real inference)."
  ([chat-model] (llm-advisor chat-model {}))
  ([chat-model gen-opts]
   (reify Advisor
     (-advise [_ st req]
       (let [msgs [{:role :system :content system-prompt}
                   {:role :user :content (str "操作: " (:op req)
                                              "\n対象: " (:subject req)
                                              "\n事実: " (pr-str (facts-for st req)))}]
             resp (model/-generate chat-model msgs gen-opts)]
         (parse-proposal (:content resp)))))))

(defn trace
  "Decision-grounded audit record -- persisted to the :audit channel."
  [request proposal]
  {:t          :proservadvisor-proposal
   :op         (:op request)
   :subject    (:subject request)
   :summary    (:summary proposal)
   :rationale  (:rationale proposal)
   :cites      (:cites proposal)
   :confidence (:confidence proposal)})
