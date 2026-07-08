# cloud-itonami-isic-7490

Open Business Blueprint for **ISIC Rev.5 7490**: Other professional,
scientific and technical activities n.e.c..

This repository publishes a professional-services actor -- engagement
intake, per-jurisdiction professional-services regulatory assessment,
chain-of-title screening, professional-credential screening and
deliverable/attestation issuance -- as an OSS business that any
qualified, licensed operator can fork, deploy, run, improve and sell,
so a community or independent professional never surrenders client
data and ledgers to a closed SaaS.

Built on this workspace's
[`langgraph`](https://github.com/kotoba-lang/langgraph)
StateGraph runtime (portable `.cljc`, supervised superstep loop,
interrupts, Datomic/in-mem checkpoints) -- the same actor pattern as
every prior actor in this fleet
([`cloud-itonami-isic-6511`](https://github.com/cloud-itonami/cloud-itonami-isic-6511),
[`6512`](https://github.com/cloud-itonami/cloud-itonami-isic-6512),
[`6621`](https://github.com/cloud-itonami/cloud-itonami-isic-6621),
[`6622`](https://github.com/cloud-itonami/cloud-itonami-isic-6622),
[`6629`](https://github.com/cloud-itonami/cloud-itonami-isic-6629),
[`6520`](https://github.com/cloud-itonami/cloud-itonami-isic-6520),
[`6530`](https://github.com/cloud-itonami/cloud-itonami-isic-6530),
[`6820`](https://github.com/cloud-itonami/cloud-itonami-isic-6820),
[`6612`](https://github.com/cloud-itonami/cloud-itonami-isic-6612),
[`6492`](https://github.com/cloud-itonami/cloud-itonami-isic-6492),
[`6920`](https://github.com/cloud-itonami/cloud-itonami-isic-6920),
[`6611`](https://github.com/cloud-itonami/cloud-itonami-isic-6611),
[`7120`](https://github.com/cloud-itonami/cloud-itonami-isic-7120),
[`8620`](https://github.com/cloud-itonami/cloud-itonami-isic-8620),
[`8530`](https://github.com/cloud-itonami/cloud-itonami-isic-8530),
[`9200`](https://github.com/cloud-itonami/cloud-itonami-isic-9200),
[`7500`](https://github.com/cloud-itonami/cloud-itonami-isic-7500),
[`9603`](https://github.com/cloud-itonami/cloud-itonami-isic-9603),
[`9521`](https://github.com/cloud-itonami/cloud-itonami-isic-9521),
[`9321`](https://github.com/cloud-itonami/cloud-itonami-isic-9321),
[`8730`](https://github.com/cloud-itonami/cloud-itonami-isic-8730),
[`9102`](https://github.com/cloud-itonami/cloud-itonami-isic-9102),
[`9103`](https://github.com/cloud-itonami/cloud-itonami-isic-9103),
[`9602`](https://github.com/cloud-itonami/cloud-itonami-isic-9602),
[`9000`](https://github.com/cloud-itonami/cloud-itonami-isic-9000),
[`8890`](https://github.com/cloud-itonami/cloud-itonami-isic-8890),
[`8610`](https://github.com/cloud-itonami/cloud-itonami-isic-8610),
[`9311`](https://github.com/cloud-itonami/cloud-itonami-isic-9311),
[`8510`](https://github.com/cloud-itonami/cloud-itonami-isic-8510),
[`9412`](https://github.com/cloud-itonami/cloud-itonami-isic-9412),
[`6491`](https://github.com/cloud-itonami/cloud-itonami-isic-6491),
[`8720`](https://github.com/cloud-itonami/cloud-itonami-isic-8720),
[`8521`](https://github.com/cloud-itonami/cloud-itonami-isic-8521),
[`6619`](https://github.com/cloud-itonami/cloud-itonami-isic-6619),
[`3600`](https://github.com/cloud-itonami/cloud-itonami-isic-3600),
[`6190`](https://github.com/cloud-itonami/cloud-itonami-isic-6190),
[`3030`](https://github.com/cloud-itonami/cloud-itonami-isic-3030),
[`3830`](https://github.com/cloud-itonami/cloud-itonami-isic-3830),
[`7020`](https://github.com/cloud-itonami/cloud-itonami-isic-7020),
[`9420`](https://github.com/cloud-itonami/cloud-itonami-isic-9420),
[`9491`](https://github.com/cloud-itonami/cloud-itonami-isic-9491),
[`2610`](https://github.com/cloud-itonami/cloud-itonami-isic-2610),
[`3512`](https://github.com/cloud-itonami/cloud-itonami-isic-3512),
[`8810`](https://github.com/cloud-itonami/cloud-itonami-isic-8810),
[`8691`](https://github.com/cloud-itonami/cloud-itonami-isic-8691),
[`8569`](https://github.com/cloud-itonami/cloud-itonami-isic-8569),
[`6419`](https://github.com/cloud-itonami/cloud-itonami-isic-6419),
[`7310`](https://github.com/cloud-itonami/cloud-itonami-isic-7310),
[`7320`](https://github.com/cloud-itonami/cloud-itonami-isic-7320),
[`7210`](https://github.com/cloud-itonami/cloud-itonami-isic-7210),
[`7410`](https://github.com/cloud-itonami/cloud-itonami-isic-7410),
[`8710`](https://github.com/cloud-itonami/cloud-itonami-isic-8710),
[`8541`](https://github.com/cloud-itonami/cloud-itonami-isic-8541),
[`8690`](https://github.com/cloud-itonami/cloud-itonami-isic-8690),
[`9601`](https://github.com/cloud-itonami/cloud-itonami-isic-9601),
[`6420`](https://github.com/cloud-itonami/cloud-itonami-isic-6420),
[`7420`](https://github.com/cloud-itonami/cloud-itonami-isic-7420),
[`9609`](https://github.com/cloud-itonami/cloud-itonami-isic-9609),
[`8550`](https://github.com/cloud-itonami/cloud-itonami-isic-8550),
[`7010`](https://github.com/cloud-itonami/cloud-itonami-isic-7010),
[`8790`](https://github.com/cloud-itonami/cloud-itonami-isic-8790),
[`8542`](https://github.com/cloud-itonami/cloud-itonami-isic-8542),
[`6411`](https://github.com/cloud-itonami/cloud-itonami-isic-6411)) --
here it is **ProServ-LLM ⊣ Professional Services Governor**.

> **Why an actor layer at all?** An LLM is great at drafting an
> engagement-intake summary, normalizing records, and checking
> whether an engagement's own recorded professional credential
> actually stays current -- but it has **no notion of which
> jurisdiction's professional-services law is official, no license to
> issue a real deliverable or attestation, and no way to know on its
> own whether an IP asset's own chain-of-title concern has actually
> stayed resolved**. Letting it issue a deliverable directly invites
> fabricated regulatory citations, a deliverable being issued on top
> of an unresolved chain-of-title concern, and a not-current
> professional credential being quietly overlooked -- and liability,
> and professional-malpractice/title risk, for whoever runs it. This
> project seals the ProServ-LLM into a single node and wraps it with
> an independent **Professional Services Governor**, a human
> **approval workflow**, and an immutable **audit ledger**.

## Scope: what this actor does and does not do

This actor covers engagement intake through professional-services
regulatory assessment, chain-of-title screening, professional-
credential screening and deliverable/attestation issuance. It does
**not**, by itself, hold any license required to operate as a
translator/interpreter, appraiser or patent broker in a given
jurisdiction, and it does not claim to. It also does not perform the
actual translation/appraisal/brokering work itself, or judge its
quality -- `proserv.governor`'s checks read the engagement's own
recorded boolean fields directly, not a professional-quality review.
Whoever deploys and operates a live instance (a licensed professional-
services provider) supplies any jurisdiction-specific license, the
real professional work and the real practice-management-system
integrations, and bears that jurisdiction's liability -- the software
supplies the governed, spec-cited, audited execution scaffold so that
provider does not have to build the compliance layer from scratch.

### Actuation

**Issuing a real deliverable or attestation to a client is never
autonomous, at any phase, by construction.** Two independent layers
enforce this (`proserv.governor`'s `:actuation/issue-deliverable`
high-stakes gate and `proserv.phase`'s phase table, which never puts
`:actuation/issue-deliverable` in any phase's `:auto` set) -- see
`proserv.phase`'s docstring and `test/proserv/phase_test.clj`'s
`issue-deliverable-never-auto-at-any-phase`. The actor may draft,
check and recommend; a human licensed professional is always the one
who actually issues a deliverable. Matching `leasing`'s/
`underwriting`'s/`testlab`'s/`clinic`'s/`veterinary`'s/`funeral`'s/
`parksafety`'s/`salon`'s/`entertainment`'s/`facility`'s/`consulting`'s/
`advertising`'s/`polling`'s/`research`'s/`design`'s/`sports`'s/
`alliedhealth`'s/`photo`'s/`personalservice`'s/`edsupport`'s/
`cultural`'s single-actuation shape, grounded directly in this
blueprint's own README text ("No automated proposal, by itself, can
complete the following without governor approval and audit evidence:
issuing a deliverable or attestation to a client") -- following
`sports`/8541's either/or-naming precedent, this build treats
"deliverable or attestation" as ONE conceptual act. A POSITIVE
actuation (issuing a real record), matching this fleet's majority
actuation shape (`3600`/`6190` are the fleet's two NEGATIVE-actuation
exceptions).

## The core contract

```
engagement intake + jurisdiction facts (proserv.facts, spec-cited)
        |
        v
   ┌───────────────────────┐   proposal      ┌───────────────────────┐
   │ ProServ-LLM           │ ─────────────▶ │ Professional Services         │  (independent system)
   │ (sealed)              │  + citations    │ Governor:                    │
   └───────────────────────┘                 │ spec-basis · evidence-       │
          │                 commit ◀┼ incomplete · chain-of-title-      │
          │                         │ unresolved (unconditional, NEW)     │
    record + ledger        escalate ┼ · credential-not-current              │
          │              (ALWAYS for│ (unconditional, honest reuse) ·        │
          │               :actuation│ already-issued                          │
          │               /issue-   │                                           │
          ▼               deliverable)└───────────────────────┘
      human approval
```

**The ProServ-LLM never issues a deliverable the Professional
Services Governor would reject, and never does so without a human
sign-off.** Hard violations (fabricated regulatory requirements;
unsupported evidence; an unresolved chain-of-title concern; a not-
current professional credential; a double issuance) force **hold**
and *cannot* be approved past; a clean issuance proposal still always
routes to a human.

## Run

```bash
clojure -M:dev:run     # walk one clean single-actuation lifecycle + four HARD-hold cases through the actor
clojure -M:dev:test    # governor contract · phase invariants · store parity · registry conformance · facts coverage
clojure -M:lint        # clj-kondo (errors fail; CI mirrors this)
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot
performs the physical domain work**. Here a document-courier robot
handles physical deliverable handoff where used, under the actor,
gated by the independent **Professional Services Governor**. The
governor never dispatches hardware itself; `:high`/`:safety-critical`
actions require human sign-off.

## Open business

This repository is not only source code. It is a public, forkable
business model:

| Layer | What is open |
|---|---|
| OSS core | Actor runtime, Professional Services Governor, deliverable-issuance draft records, audit ledger |
| Business blueprint | Customer, offer, pricing, unit economics, sales motion |
| Operator playbook | How to fork, license, deploy and support the service in a jurisdiction |
| Trust controls | Governance, security reporting, actuation invariant, audit requirements |

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md) to start this as an
open business on itonami.cloud, and
[`docs/adr/0001-architecture.md`](docs/adr/0001-architecture.md) for the
full architecture and decision record.

## Capability layer

This blueprint resolves its technology stack via
[`kotoba-lang/industry`](https://github.com/kotoba-lang/industry) (ISIC
`7490`). This vertical's engagement records are practice-specific
rather than a shared cross-operator data contract, so `proserv.*` runs
on the generic robotics/identity/forms/dmn/bpmn/audit-ledger stack
only -- no bespoke domain capability lib to reference at all.

## Layout

| File | Role |
|---|---|
| `src/proserv/store.cljc` | **Store** protocol -- `MemStore` ‖ `DatomicStore` (`langchain.db`) + append-only audit ledger + deliverable-issuance history. No dynamically-filed sub-record -- the actuation op acts directly on a pre-seeded engagement, and the double-actuation guard checks a dedicated `:deliverable-issued?` boolean rather than a `:status` value |
| `src/proserv/registry.cljc` | Deliverable-issuance draft records. Intentionally 'plain': this build's two distinctive checks are both boolean flags evaluated directly by the governor, not registry-level numeric/temporal predicates |
| `src/proserv/facts.cljc` | Per-jurisdiction professional-services catalog with an official spec-basis citation per entry, honest coverage reporting |
| `src/proserv/proservadvisor.cljc` | **ProServ-LLM** -- `mock-advisor` ‖ `llm-advisor`; intake/deliverable-scope-verification/chain-of-title-screening/credential-screening/deliverable-issuance proposals |
| `src/proserv/governor.cljc` | **Professional Services Governor** -- 5 HARD checks (spec-basis · evidence-incomplete · chain-of-title-unresolved, unconditional evaluation, GENUINELY NEW, the 55th grounding of this discipline · credential-not-current, unconditional evaluation, an HONEST reuse of this fleet's long-established credential-currency concept, not claimed as new · already-issued guard) + 1 soft (confidence/actuation gate) |
| `src/proserv/phase.cljc` | **Phase 0→3** -- read-only → assisted intake → assisted verify → supervised (deliverable issuance always human; engagement intake is the ONLY auto-eligible op, no direct capital risk) |
| `src/proserv/operation.cljc` | **OperationActor** -- langgraph-clj StateGraph |
| `src/proserv/sim.cljc` | demo driver |
| `test/proserv/*_test.clj` | governor contract · phase invariants · store parity · registry conformance · facts coverage |

## Business-process coverage (honest)

This actor covers engagement intake through professional-services
regulatory assessment, chain-of-title screening, professional-
credential screening and deliverable/attestation issuance -- the
core governed lifecycle this blueprint's own `docs/business-model.md`
names as its Offer:

| Covered | Not covered (out of scope for this R0) |
|---|---|
| Engagement intake + per-jurisdiction evidence checklisting, HARD-gated on an official spec-basis citation (`:engagement/intake`/`:engagement/verify`) | Real practice-management-system integration, real translation/appraisal/brokering work itself (see `proserv.facts`'s docstring) |
| Chain-of-title screening, evaluated unconditionally so the screening op itself can HARD-hold on its own finding (`:chainoftitle/screen`) | Any professional-quality judgment itself -- deliberately outside this actor's competence |
| Credential screening, evaluated unconditionally (`:credential/screen`) | |
| Deliverable/attestation issuance, HARD-gated on full evidence, a resolved chain-of-title status and a current credential, plus a double-issuance guard (`:actuation/issue-deliverable`) | |
| Immutable audit ledger for every intake/verification/screening/issuance decision | |

Extending coverage is additive: add the next gate (e.g. a conflict-
of-interest-in-valuation check) as its own governed op with its own
HARD checks and tests, following the SAME "an independent governor
re-verifies against the actor's own records before any real-world
act" pattern this repo's flagship op already establishes.

## Jurisdiction coverage (honest)

`proserv.facts/coverage` reports how many requested jurisdictions
actually have an official spec-basis in `proserv.facts/catalog` --
currently 4 seeded (JPN, USA, GBR, DEU) out of ~194 jurisdictions
worldwide. This is a starting catalog to prove the governor contract
end-to-end, not a claim of global coverage. Adding a jurisdiction is
additive: one map entry in `proserv.facts/catalog`, citing a real
official source -- never fabricate a jurisdiction's requirements to
make coverage look bigger.

## Maturity

`:implemented` -- `ProServ-LLM` + `Professional Services Governor`
run as real, tested code (see `Run` above), promoted from the
originally-published `:blueprint`-tier scaffold, modeled closely on
the sixty-nine prior actors' architecture. See `docs/adr/0001-
architecture.md` for the history and design.

## License

Code and implementation templates are AGPL-3.0-or-later.
