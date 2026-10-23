# CLAUDE.md — project rules for AI-assisted work on this repository

This file is loaded automatically by Claude Code (and read by any other coding agent
pointed at the repo). It encodes the kata's constraints so no AI session can violate
them by accident. Keep it short.

## What this repo is

XA Bank Time Deposit refactoring kata (Ikigai Digital take-home). The solution lives in
`java/`. The other language folders (`c#`, `kotlin`, `python`, `typescript`) are the
untouched originals from the kata repository and must not be modified.

## Hard constraints — never violate, even if asked

1. `java/src/main/java/org/ikigaidigital/TimeDeposit.java` — **do not change**.
   No new fields, no renames, no annotations. Compose around it (`domain/TimeDepositDetails`).
2. `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` — **signature stays exactly as is**
   (public, void, single `List<TimeDeposit>` parameter, mutates in place).
3. Observable behaviour of `updateBalance` must stay **bit-for-bit identical**. The oracle is
   `src/test/java/org/ikigaidigital/legacy/LegacyTimeDepositCalculator.java` (verbatim original).
   `TimeDepositCalculatorCharacterizationTest` must stay green after every change.
   In particular keep: `double` arithmetic `balance * rate / 12`, rounding via
   `new BigDecimal(double).setScale(2, HALF_UP)` (NOT `BigDecimal.valueOf`), unknown plan → 0 interest.
4. **Exactly two HTTP endpoints**: `GET /time-deposits` and `POST /time-deposits/balances`.
   Do not add health, create, delete, or per-id endpoints. Do not add Actuator.
5. The API is **contract-first**: edit `java/src/main/resources/static/openapi.yaml`,
   never hand-write DTOs or controller mappings that are not in the contract.
6. Input validation and error handling are explicitly out of scope — do not add them.

## Architecture (hexagonal)

```
org.ikigaidigital
├── TimeDeposit, TimeDepositCalculator      shared legacy classes (see constraints)
├── domain/                                 pure Java, no Spring, no JPA
│   └── interest/                           InterestPolicy + one implementation per plan type
├── application/
│   ├── port/inbound/                       use-case interfaces (driven by adapters)
│   ├── port/outbound/                      TimeDepositRepository (implemented by adapters)
│   └── service/                            orchestration only, @Transactional lives here
└── adapters/
    ├── inbound/rest/                       controller implementing the generated TimeDepositsApi
    └── outbound/persistence/               JPA entities, Spring Data repos, port implementation
```

Dependency rule: `adapters → application → domain`. Domain and application never import
`org.springframework.data`, `jakarta.persistence` or anything from `adapters`.
(`application.service` may use `@Service`/`@Transactional` — accepted pragmatic exception.)

## Conventions

- Java 17, Spring Boot 3.x, Maven. No Lombok, no MapStruct (project is too small to justify them).
- Tests: `*Test` = unit (no Docker), `*IT` = Testcontainers integration (needs Docker, runs in `mvn verify`).
- Every assumption about an ambiguous requirement goes in a code comment at the point of decision
  (kata rule), and is mirrored in `java/README.md` → "Assumptions".
- Commits are atomic: one logical change per commit, tests pass at every commit, message in
  imperative mood with a one-line subject and a short body explaining *why*.

## Commands

```bash
cd java
mvn test                       # unit tests + characterization tests (fast)
mvn verify                     # + Testcontainers integration test (needs Docker)
docker compose up -d           # local Postgres
mvn spring-boot:run            # http://localhost:8080/swagger-ui.html
```

## How to work with me on this repo

- Before touching `TimeDepositCalculator`, run `mvn test` and keep the characterization tests open.
- When refactoring the calculator: red → green → refactor in small steps; after each step run
  `TimeDepositCalculatorCharacterizationTest` (including the 20k random-input equivalence test).
- Prefer the smallest change that satisfies the requirement. This is a refactoring kata: reviewers
  are grading judgment and restraint, not volume.
- When unsure whether something counts as a "breaking change" or a "third endpoint", stop and ask.
