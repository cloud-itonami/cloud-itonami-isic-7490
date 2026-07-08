# ADR-0001: ProServ-LLM ⊣ Professional Services Governor architecture

## Status

Accepted. `cloud-itonami-isic-7490` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-7490` publishes an OSS business blueprint for
other professional, scientific and technical activities not elsewhere
classified: translation/interpretation, non-real-estate/non-insurance
appraisal, and patent brokering. Like every prior actor in this
fleet, the blueprint alone is not an implementation: this ADR records
the governed-actor architecture that promotes it to real, tested
code, following the same langgraph StateGraph + independent Governor
+ Phase 0→3 rollout pattern established by `cloud-itonami-isic-6511`
(life insurance) and applied across sixty-nine prior siblings, most
recently `cloud-itonami-isic-6411` (central banking).

## Decision

### Decision 1: single-actuation shape

This blueprint's own README/business-model.md/operator-guide.md
consistently name only ONE real-world act: "issuing a deliverable or
attestation to a client." Following `sports`/8541's precedent for
this same either/or-naming ambiguity, this build treats "deliverable
or attestation" as ONE conceptual act. Matching `leasing`/
`underwriting`/`testlab`/`clinic`/`veterinary`/`funeral`/
`parksafety`/`salon`/`entertainment`/`facility`/`consulting`/
`advertising`/`polling`/`research`/`design`/`sports`/`alliedhealth`/
`photo`/`personalservice`/`edsupport`/`cultural`'s single-actuation
shape, `high-stakes` here is a one-member set, `#{:actuation/issue-
deliverable}`.

### Decision 2: entity and op shape

The primary entity is an `engagement`, matching the business-
model.md's own Offer language ("engagement intake"). Five ops:
`:engagement/intake` (directory upsert, no capital risk),
`:engagement/verify` (per-jurisdiction professional-services evidence
checklist, never auto), `:chainoftitle/screen` (chain-of-title
screening, unconditional-evaluation discipline, never auto),
`:credential/screen` (professional-credential screening,
unconditional-evaluation discipline, never auto), and `:actuation/
issue-deliverable` (POSITIVE, high-stakes -- issuing a real
deliverable or attestation).

### Decision 3: `chain-of-title-unresolved-violations` -- the 55th unconditional-evaluation screening grounding, a genuinely new concept

Before writing this check, every prior sibling's governor/registry
namespaces were grepped for "chain-of-title" -- zero hits, confirming
this is a genuinely new unconditional-evaluation concept, avoiding
the false-precedent-claim risk `leasing`'s ADR-0001 documents.
`chain-of-title-unresolved-violations` reuses the unconditional-
evaluation DISCIPLINE (`casualty.governor/sanctions-violations`'s
original fix) for the 55th distinct application overall, continuing
the count established across this fleet's builds (most recently
`reserve.governor/correspondent-banking-due-diligence-unresolved-
violations` at 54th). Grounded in real IP-transfer due-diligence
practice: USPTO 37 CFR Part 3 assignment recordation, UK Patents Act
1977 §33, Germany's Patentgesetz §30, and WIPO IP due-diligence
guidance. Gates `:chainoftitle/screen` and the actuation. Genuinely
distinct from `design`/7410's `ip-licensing-conflict-unresolved?`
(a deliverable-scope-vs-licensed-scope conflict), since chain-of-
title concerns ownership verification, not licensing scope.

### Decision 4: `credential-not-current-violations` -- an honest reuse, not claimed as new

This exact concept (a licensed professional's credential/license
currency, evaluated unconditionally so the screening op itself can
HARD-hold on its own finding) was already established by `clinic.
governor/credential-not-current-violations` and has since been reused
literally by many siblings (`hospital`/`eldercare`/`veterinary`/
`nursing` among others). `proserv.governor/credential-not-current-
violations` is NOT claimed as new -- grounded in real professional-
certification practice (ATA certification for translators/
interpreters, USPAP/RICS credential-currency standards for
appraisers). Gates `:credential/screen` and the actuation.

### Decision 5: dedicated double-actuation-guard boolean

`:deliverable-issued?` is a dedicated boolean on the `engagement`
record, never a single `:status` value -- the same discipline every
prior sibling governor's guards establish, informed by `cloud-
itonami-isic-6492`'s status-lifecycle bug (ADR-2607071320).

### Decision 6: Store protocol, MemStore + DatomicStore parity

`proserv.store/Store` is implemented by both `MemStore` (atom-
backed, default for dev/tests/demo) and `DatomicStore` (`langchain.
db`-backed), proven to satisfy the same contract in `test/proserv/
store_contract_test.clj` -- the same seam every sibling actor uses so
swapping the SSoT backend is a configuration change, not a rewrite.
The protocol's per-entity accessor is named `engagement` directly --
not a Clojure special form, so no `-of` suffix workaround was needed.

### Decision 7: Phase 0→3 rollout

Phase 3's `:auto` set has exactly one member, `:engagement/intake`
(no capital risk). `:engagement/verify`, `:chainoftitle/screen` and
`:credential/screen` are never auto-eligible at any phase (matching
every sibling's screening/verification-op posture), and `:actuation/
issue-deliverable` is permanently excluded from every phase's `:auto`
set -- a structural fact, not a rollout milestone, enforced by BOTH
`proserv.phase` and `proserv.governor`'s `high-stakes` set
independently.

### Decision 8: no bespoke domain capability lib

This blueprint's own `:itonami.blueprint/required-technologies` names
no domain-specific capability beyond the generic robotics/identity/
forms/dmn/bpmn/audit-ledger stack -- there was no capability-lib
decision to make at all.

### Decision 9: mock + LLM advisor pair

`proserv.proservadvisor` provides `mock-advisor` (deterministic,
default everywhere -- the actor graph and governor contract run
offline) and `llm-advisor` (backed by `langchain.model/ChatModel`,
with a defensive EDN-proposal parser so a malformed LLM response
degrades to a safe low-confidence noop rather than ever auto-issuing
a deliverable).

### Decision 10: no `blueprint.edn` field-sync fixes needed

Matching `photo`/7420's, `personalservice`/9609's, `edsupport`/8550's,
`headoffice`/7010's, `residential`/8790's, `cultural`/8542's and
`reserve`/6411's own experience, this repo's `blueprint.edn` already
had the correct `isic-` prefixed `:id` and correctly populated
`:required-technologies`/`:optional-technologies` matching the
`kotoba-lang/industry` registry's own entry for `"7490"` exactly --
only the `:maturity` field itself needed adding.

## Alternatives considered

- **A dual-actuation shape** (splitting "deliverable" and
  "attestation" into two acts). Rejected: the blueprint's own text
  consistently names only ONE real-world act; following `sports`/
  8541's precedent, this either/or naming is treated as one
  conceptual act, not grounds for inventing a second.
- **Reusing `design`/7410's `ip-licensing-conflict-unresolved?`
  concept for the chain-of-title concern.** Rejected: IP/licensing
  conflict (does a deliverable's own elements exceed a licensed
  scope) and chain-of-title (does the asset's own ownership chain
  verify clearly) are distinct real-world concerns -- confirmed via
  grep to have zero prior instances under either name for this
  specific concept.
- **Claiming credential-not-current as a new concept.** Rejected:
  this exact concept is already well-established across many
  siblings; honestly characterizing the reuse (rather than inventing
  a novel name for the same idea) matches this fleet's precedent-
  verification discipline.

## Consequences

- Seventieth actor in this fleet (69 implemented before this build).
- Establishes a genuinely NEW unconditional-evaluation-screening
  concept (chain-of-title-unresolved), grep-verified absent from
  every prior sibling before the claim was finalized.
- Documents an honest reuse of the long-established credential-not-
  current concept, not claimed as new.
- `MemStore` ‖ `DatomicStore` parity is proven by `test/proserv/
  store_contract_test.clj`, the same `:db-api`-driven swap pattern
  every sibling actor uses.
- `blueprint.edn` required no field-sync fixes this time (already
  correct) -- only the `:maturity` flip itself.

## References

- `orgs/cloud-itonami/cloud-itonami-isic-7490/README.md`
- `orgs/cloud-itonami/cloud-itonami-isic-7490/docs/business-model.md`
- `orgs/kotoba-lang/industry/resources/kotoba/industry/registry.edn` (entry `"7490"`)
