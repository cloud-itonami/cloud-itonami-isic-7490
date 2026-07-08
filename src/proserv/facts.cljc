(ns proserv.facts
  "Per-jurisdiction professional-services (translation/interpretation,
  non-real-estate/non-insurance appraisal, patent brokering)
  regulatory catalog -- the G2-style spec-basis table the
  Professional Services Governor checks every `:engagement/verify`
  proposal against ('did the advisor cite an OFFICIAL public source
  for this jurisdiction's professional-services framework, or did it
  invent one?').

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  every sibling actor's `facts` namespace uses: a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.

  Seed values are drawn from each jurisdiction's official patent-
  office/professional-standards authority (see `:provenance`); they
  are a STARTING catalog, not a from-scratch survey of all ~194
  jurisdictions. Extending coverage is additive: add one map to
  `catalog`, cite a real source, done -- never invent a jurisdiction's
  requirements to make coverage look bigger.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the client-
  engagement-consent/deliverable-scope/chain-of-title-verification/
  credential-verification evidence set this blueprint's own Offer
  names; `:legal-basis` / `:owner-authority` / `:provenance` are the
  G2 citation the governor requires before any `:actuation/issue-
  deliverable` proposal can commit."
  {"JPN" {:name "Japan"
          :owner-authority "特許庁 (Japan Patent Office, JPO)"
          :legal-basis "特許法第34条 (Patent Act Art. 34, assignment/licensing recordation) -- 弁理士法 (Patent Attorney Act)"
          :national-spec "特許権譲渡・実施許諾の対抗要件としての移転登録、および弁理士による代理業務基準"
          :provenance "https://www.jpo.go.jp/system/laws/rule/guideline/patent/tukatu_kijun/index.html"
          :required-evidence ["依頼者同意記録 (client-engagement-consent-record)"
                              "納品範囲記録 (deliverable-scope-record)"
                              "権原確認記録 (chain-of-title-verification-record)"
                              "資格確認記録 (credential-verification-record)"]}
   "USA" {:name "United States"
          :owner-authority "United States Patent and Trademark Office (USPTO)"
          :legal-basis "37 CFR Part 3 (Assignment, Recording and Rights of Assignee) -- Uniform Standards of Professional Appraisal Practice (USPAP)"
          :national-spec "Patent-assignment recordation and chain-of-title verification requirements; certified-appraiser independence and credential-currency standards"
          :provenance "https://www.uspto.gov/patents/laws/patent-assignments"
          :required-evidence ["Client engagement-consent record"
                              "Deliverable-scope record"
                              "Chain-of-title-verification record"
                              "Credential-verification record"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "UK Intellectual Property Office (IPO) / Royal Institution of Chartered Surveyors (RICS)"
          :legal-basis "Patents Act 1977 §33 (registration of assignments) -- RICS Valuation -- Global Standards"
          :national-spec "Patent-assignment registration requirement and chartered-surveyor valuation-independence standards"
          :provenance "https://www.gov.uk/guidance/patents-transferring-ownership"
          :required-evidence ["Client engagement-consent record"
                              "Deliverable-scope record"
                              "Chain-of-title-verification record"
                              "Credential-verification record"]}
   "DEU" {:name "Germany"
          :owner-authority "Deutsches Patent- und Markenamt (DPMA)"
          :legal-basis "Patentgesetz (PatG) §30 (Rechtsübergang, assignment recordation) -- Sachverständigenordnung"
          :national-spec "Registereintragung von Patentübertragungen und Unabhängigkeitsanforderungen für öffentlich bestellte Sachverständige"
          :provenance "https://www.dpma.de/patent/patentschutz_im_ausland/uebertragung/index.html"
          :required-evidence ["Einwilligungsprotokoll (client-engagement-consent-record)"
                              "Leistungsumfangsprotokoll (deliverable-scope-record)"
                              "Rechtsübergangsprotokoll (chain-of-title-verification-record)"
                              "Qualifikationsnachweis (credential-verification-record)"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to issue a
  deliverable on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-isic-7490 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `proserv.facts/catalog`, "
                 "never fabricate a jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))
