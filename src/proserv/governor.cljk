(ns proserv.governor
  "Professional Services Governor -- the independent compliance layer
  that earns the ProServ-LLM the right to commit. The LLM has no
  notion of jurisdictional professional-services law, whether an
  engagement's own recorded chain-of-title concern has actually
  stayed unresolved, whether an engagement's own recorded credential
  has actually stayed current, or when an act stops being a draft and
  becomes a real-world deliverable/attestation issuance, so this MUST
  be a separate system able to *reject* a proposal and fall back to
  HOLD -- the professional-services analog of `cloud-itonami-isic-
  8620`'s ClinicGovernor.

  Five checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them (you don't get to approve your way
  past a fabricated jurisdiction spec-basis, incomplete evidence, an
  unresolved chain-of-title concern, a not-current professional
  credential, or a double issuance). The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `proserv.phase`: for `:stake
  :actuation/issue-deliverable` (a real deliverable/attestation
  issuance) NO phase ever allows auto-commit either. Two independent
  layers agree that actuation is always a human call.

    1. Spec-basis                  -- did the deliverable proposal
                                       cite an OFFICIAL source
                                       (`proserv.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:actuation/issue-
                                       deliverable`, has the
                                       engagement actually been
                                       assessed with a full client-
                                       engagement-consent-record/
                                       deliverable-scope-record/
                                       chain-of-title-verification-
                                       record/credential-verification-
                                       record evidence checklist on
                                       file?
    3. Chain-of-title unresolved   -- reported by THIS proposal itself
                                       (a `:chainoftitle/screen` that
                                       just found an unresolved
                                       concern), or already on file for
                                       the engagement (`:chainoftitle/
                                       screen`/`:actuation/issue-
                                       deliverable`). Evaluated
                                       UNCONDITIONALLY (not scoped to a
                                       specific op) so the screening op
                                       itself can HARD-hold on its own
                                       finding. A GENUINELY NEW concept
                                       in this fleet (grep-verified
                                       absent -- no dedicated chain-of-
                                       title CHECK FUNCTION exists
                                       anywhere else in this fleet),
                                       the 55th distinct application of
                                       the unconditional-evaluation
                                       discipline overall (`casualty.
                                       governor/sanctions-violations`'s
                                       original fix; most recently
                                       `reserve.governor/
                                       correspondent-banking-due-
                                       diligence-unresolved-
                                       violations` at 54th). Grounded
                                       in real IP-transfer due-
                                       diligence practice (USPTO 37 CFR
                                       Part 3 assignment recordation,
                                       UK Patents Act 1977 section 33,
                                       Germany's Patentgesetz section
                                       30, WIPO IP due-diligence
                                       guidance).
    4. Credential not current      -- reported by THIS proposal itself
                                       (a `:credential/screen` that
                                       just found a lapsed credential),
                                       or already on file for the
                                       engagement (`:credential/
                                       screen`/`:actuation/issue-
                                       deliverable`). Evaluated
                                       UNCONDITIONALLY, the SAME
                                       discipline as check 3 above --
                                       an HONEST reuse of this fleet's
                                       long-established credential-
                                       currency concept (`clinic.
                                       governor/credential-not-current-
                                       violations` and many siblings
                                       since, e.g. `hospital`/
                                       `eldercare`/`veterinary`/
                                       `nursing`), not claimed as new.
                                       Grounded in real professional-
                                       certification practice (ATA
                                       certification for translators/
                                       interpreters, USPAP/RICS
                                       credential-currency standards
                                       for appraisers).
    5. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:actuation/issue-
                                       deliverable` (a REAL deliverable/
                                       attestation issuance) ->
                                       escalate.

  One more guard, double-issuance prevention, is enforced but NOT
  listed as a numbered HARD check above because it needs no upstream
  comparison at all -- `already-issued-violations` refuses to issue a
  deliverable for the SAME engagement twice, off a dedicated
  `:deliverable-issued?` fact (never a `:status` value) -- the SAME
  'check a dedicated boolean, not status' discipline every prior
  sibling governor's guards establish, informed by `cloud-itonami-
  isic-6492`'s status-lifecycle bug (ADR-2607071320)."
  (:require [proserv.facts :as facts]
            [proserv.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Issuing a real deliverable or attestation is the ONE real-world
  actuation event this actor performs -- a single-member set,
  matching `cloud-itonami-isic-6511`'s/`6621`'s/`6629`'s/`6612`'s/
  `6492`'s/`7120`'s/`8620`'s/`edsupport`'s single-actuation shape."
  #{:actuation/issue-deliverable})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:engagement/verify` (or `:actuation/issue-deliverable`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's professional-services requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:engagement/verify :actuation/issue-deliverable} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は納品基準として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:actuation/issue-deliverable`, the jurisdiction's required
  client-engagement-consent-record/deliverable-scope-record/chain-of-
  title-verification-record/credential-verification-record evidence
  must actually be satisfied -- do not trust the advisor's self-
  reported confidence alone."
  [{:keys [op subject]} st]
  (when (= op :actuation/issue-deliverable)
    (let [e (store/engagement st subject)
          deliverable (store/deliverable-of st subject)]
      (when-not (and deliverable
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist deliverable)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(依頼者同意記録/納品範囲記録/権原確認記録/資格確認記録等)が充足していない状態での提案"}]))))

(defn- chain-of-title-unresolved-violations
  "An unresolved chain-of-title concern -- reported by THIS proposal
  (e.g. a `:chainoftitle/screen` that itself just found an unresolved
  concern), or already on file in the store for the engagement
  (`:chainoftitle/screen`/`:actuation/issue-deliverable`) -- is a
  HARD, un-overridable hold. Evaluated UNCONDITIONALLY (not scoped to
  a specific op) so the screening op itself can HARD-hold on its own
  finding."
  [{:keys [op subject]} proposal st]
  (let [hit-in-proposal? (true? (get-in proposal [:value :chain-of-title-unresolved?]))
        engagement-id (when (contains? #{:chainoftitle/screen :actuation/issue-deliverable} op) subject)
        hit-on-file? (and engagement-id (true? (:chain-of-title-unresolved? (store/engagement st engagement-id))))]
    (when (or hit-in-proposal? hit-on-file?)
      [{:rule :chain-of-title-unresolved
        :detail "権原(チェーン・オブ・タイトル)が未解決の状態での納品提案は進められない"}])))

(defn- credential-not-current-violations
  "A not-current professional credential -- reported by THIS proposal
  (e.g. a `:credential/screen` that itself just found a lapsed
  credential), or already on file in the store for the engagement
  (`:credential/screen`/`:actuation/issue-deliverable`) -- is a HARD,
  un-overridable hold. Evaluated UNCONDITIONALLY (not scoped to a
  specific op) so the screening op itself can HARD-hold on its own
  finding."
  [{:keys [op subject]} proposal st]
  (let [hit-in-proposal? (true? (get-in proposal [:value :credential-not-current?]))
        engagement-id (when (contains? #{:credential/screen :actuation/issue-deliverable} op) subject)
        hit-on-file? (and engagement-id (true? (:credential-not-current? (store/engagement st engagement-id))))]
    (when (or hit-in-proposal? hit-on-file?)
      [{:rule :credential-not-current
        :detail "専門資格が最新でない状態での納品提案は進められない"}])))

(defn- already-issued-violations
  "For `:actuation/issue-deliverable`, refuses to issue a deliverable
  for the SAME engagement twice, off a dedicated `:deliverable-
  issued?` fact (never a `:status` value)."
  [{:keys [op subject]} st]
  (when (= op :actuation/issue-deliverable)
    (when (store/engagement-already-issued? st subject)
      [{:rule :already-issued
        :detail (str subject " は既に納品済み")}])))

(defn check
  "Censors a ProServ-LLM proposal against the governor rules. Returns
  {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (chain-of-title-unresolved-violations request proposal st)
                           (credential-not-current-violations request proposal st)
                           (already-issued-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
