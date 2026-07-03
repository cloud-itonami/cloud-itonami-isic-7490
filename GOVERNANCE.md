# Governance

`cloud-itonami-7490` is an OSS open-business blueprint for other professional, scientific and technical activities not elsewhere classified (e.g. translation/interpretation, non-real-estate/non-insurance appraisal, patent brokering).
Governance covers both the capability layer and the operator model.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- the Professional Services Governor remains independent of the advisor.
- hard policy violations (fabricated spec-basis, conflict of interest, incomplete
  records) cannot be overridden by human approval.
- issuing a deliverable or attestation to a client always escalates to a human -- never automated.
- every hold, approval and delivery path is auditable.
- personal and customer data stay outside Git.

## Decision Records

Architecture decisions live in `docs/adr/`. Changes to the trust model,
storage contract, public business model, operator certification or license
should add or update an ADR.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is a
separate trust mark and should require security, audit and data-flow review.

Certified operators can lose certification for:

- bypassing the Professional Services Governor's policy checks
- mishandling customer data
- misrepresenting certification status
- failing to respond to security incidents
- hiding material changes to customer-facing operation
