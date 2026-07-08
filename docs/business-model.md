# Business Model: Other professional, scientific and technical activities n.e.c.

## Classification

- Repository: `cloud-itonami-isic-7490`
- ISIC Rev.5: `7490`
- Activity: other professional, scientific and technical activities not elsewhere classified (e.g. translation/interpretation, non-real-estate/non-insurance appraisal, patent brokering)
- Social impact: professional standards, data sovereignty, transparent audit

## Customer

- independent professional-service providers
- cooperative service collectives
- community professional-access programs

## Offer

- engagement intake
- deliverable proposal
- certification/attestation proposal
- immutable audit ledger

## Revenue

- self-host setup: one-time implementation fee
- managed hosting: monthly subscription per practice
- support: monthly retainer with SLA
- migration: import from an incumbent practice-management system
- per-engagement fee

## Trust Controls

- no deliverable or attestation is issued to a client without human sign-off
- a fabricated qualification/attestation forces a hold, not an override
- every deliverable path is auditable
- emergency manual override paths remain outside LLM control
- an unresolved chain-of-title concern, or a not-current professional
  credential, forces a hold, not an override
- deliverable issuance is logged and escalated, and cannot be issued
  twice for the same engagement: a double-issuance attempt is held
  off this actor's own engagement facts alone, with no upstream
  comparison needed

## Professional Services Governor: decision rule

`blueprint.edn` fixes `:itonami.blueprint/governor` to
`:professional-services-governor` -- this is not a generic "review
step," it is the one gate the ONE real-world act this business
performs (issuing a real deliverable or attestation to a client) must
pass. The governor sits between the ProServ-LLM and execution, per
the README's Core Contract:

```text
ProServ-LLM -> Professional Services Governor -> hold, proceed, or human approval
```

**Approves**: routine professional-services actions proposed against
an engagement that already has a consented deliverable-scope
checklist on file, satisfied required evidence, a resolved chain-of-
title status, and a current professional credential. These proceed
straight to the engagement ledger.

**Rejects or escalates**: the governor refuses to let the advisor
issue a deliverable on its own authority when any of the following
hold -- a fabricated jurisdiction spec-basis; incomplete evidence; an
unresolved chain-of-title concern; a not-current professional
credential; a double-issuance attempt. A clean issuance proposal
still always routes to a human -- `:actuation/issue-deliverable` is
never auto-committed, at any rollout phase.
