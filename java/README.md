# XA Bank Time Deposit — Java solution

Spring Boot 3 / Java 17 implementation of the kata, structured as a hexagonal application
with a contract-first REST API, Flyway-managed PostgreSQL schema and Testcontainers tests.

## Run it

Prerequisites: JDK 17, Maven 3.9+, Docker.

```bash
cd java
docker compose up -d          # PostgreSQL 16 on localhost:5432 (db/user/password: timedeposit)
mvn spring-boot:run           # starts on http://localhost:8080, Flyway creates schema + seed data
```

## Trigger the endpoints via the Swagger contract

Open **http://localhost:8080/swagger-ui.html**. The UI renders the hand-written contract
`src/main/resources/static/openapi.yaml` (also served raw at `/openapi.yaml`) — the same file
the server-side API interface is generated from, so UI and implementation cannot drift.
`/swagger-ui.html` and `/v3/api-docs` are documentation endpoints served by springdoc; the
business API consists of exactly the two operations in `openapi.yaml`, and Swagger UI renders
that hand-written contract.

1. **`GET /time-deposits`** → *Try it out* → *Execute*. Returns the 7 seeded deposits with their
   withdrawals (`id, planType, balance, days, withdrawals[]`).
2. **`POST /time-deposits/balances`** → *Try it out* → *Execute*. Applies one month of interest to
   every deposit; responds `204 No Content`. Not idempotent: each call applies another month.
3. Run **`GET /time-deposits`** again to see the updated balances (e.g. deposit 1: 1000.00 → 1000.83).

Equivalent `curl`:

```bash
curl -s http://localhost:8080/time-deposits | jq
curl -s -X POST http://localhost:8080/time-deposits/balances -i
```

## Tests

```bash
mvn test      # unit + characterization tests (no Docker)
mvn verify    # adds the Testcontainers end-to-end test (Docker required)
```

`TimeDepositCalculatorCharacterizationTest` pins the original calculator's behaviour, including
a 20,000-input equivalence check against a verbatim copy of the legacy code
(`src/test/java/org/ikigaidigital/legacy/`). It is the proof that the refactoring changed nothing.
Each interest policy, `PlanType` and the registry also have their own unit tests in
`src/test/java/org/ikigaidigital/domain/interest/`.

## Design

```
adapters/inbound/rest         TimeDepositController implements generated TimeDepositsApi
        ↓ inbound ports
application/service           TimeDepositService (orchestration, transactions)
        ↓ outbound port
adapters/outbound/persistence TimeDepositPersistenceAdapter (Spring Data JPA; schema owned by Flyway)

domain/                       TimeDepositDetails, Withdrawal
domain/interest/              InterestPolicy per plan type, PlanType, InterestPolicyRegistry
TimeDeposit, TimeDepositCalculator   shared legacy classes — unchanged signature/behaviour
```

- **Interest rules** live in `domain/interest` as one `InterestPolicy` per plan type
  (`BasicInterestPolicy`, `StudentInterestPolicy`, `PremiumInterestPolicy`, and `NoInterestPolicy`
  as the fallback). Each returns the unrounded monthly interest for its own rule only.
- **`InterestPolicyRegistry`** is keyed by the `PlanType` enum; `PlanType.fromString` converts the
  `String` plan type of the shared `TimeDeposit`, and anything that is not a known plan type
  resolves to `NoInterestPolicy`.
- **`TimeDepositCalculator.updateBalance(List<TimeDeposit>)`** keeps its signature. It applies the
  30-day grace period and the rounding, which are common to all plans, and asks the registry for
  the policy only once `days > 30`.
- **Extension seam:** the registry's public constructor takes a `Map<PlanType, InterestPolicy>`,
  and the calculator has a constructor taking a registry. A new plan type is one `PlanType` value,
  one policy class and one registry entry.
- **`withdrawals`** are composed next to the shared `TimeDeposit` in a read model
  (`TimeDepositDetails`) rather than added to it, so the shared class stays untouched.

## Assumptions

Each assumption is also noted where the decision is made (code comment, migration or contract).

| Topic | Assumption |
|-------|------------|
| Table/column names | README uses camelCase (`timeDeposits`, `timeDepositId`); Postgres folds unquoted identifiers to lower case, so snake_case (`time_deposits`, `time_deposit_id`) is used instead of quoted identifiers. |
| `withdrawals` in GET response | A list of withdrawal records (`id, amount, date`), not a count or a sum. |
| Update endpoint | `POST /time-deposits/balances`, `204 No Content`. Not idempotent: every call applies one more month of interest, so repeated calls compound and `PUT` would be the wrong method. The client reads results via GET. |
| Seed data | No create endpoint is allowed (exactly two endpoints), so Flyway `V2` seeds rows at the rule boundaries: 30 days, premium 45/46, student 366, plus premium after one year. |
| Rounding | Legacy behaviour is preserved exactly: interest computed in `double`, rounded with `new BigDecimal(double).setScale(2, HALF_UP)`. Balances are normalised to scale 2 when persisted. |
| Plan type matching | Exact and case-sensitive, as the legacy `equals` comparisons were: `"BASIC"` earns nothing. A `null` plan type throws `NullPointerException` once `days > 30`. Both are kept from the legacy code. |
| Unknown plan types | Earn no interest and do not fail (legacy behaviour). |
| Validation / errors | Out of scope per the kata. |

## AI-assisted development

See [`../docs/AI_WORKFLOW.md`](../docs/AI_WORKFLOW.md) for the tools and setup used and which
parts were AI-assisted; the rules the agent worked under are in [`../CLAUDE.md`](../CLAUDE.md).
