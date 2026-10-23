# AI-assisted development: setup and summary

## 1. Tools and how I used them

| Tool | What I used it for |
|------|--------------------|
| **Claude (claude.ai, Fable 5.1 / Opus 5.5)** | Design-level work: challenging my reading of the kata, enumerating risks, drafting boilerplate (project skeleton, migrations, test matrix) to my specification, reviewing the pushed repository with a fresh eye. |
| **Claude Code (CLI, Opus 5.5)** | Execution inside the repository under `CLAUDE.md`: scoped changes, running the test suite after every step, reporting diffs and any deviation from the brief. |
| **IntelliJ IDEA** | Reading code, running the application and Swagger UI locally, manual checks. |

No other LLMs, agent frameworks, MCP servers or IDE AI assistants were used.

## 2. Setup (reproducible)

1. Install Claude Code: https://docs.claude.com/en/docs/claude-code (requires Node.js).
2. Clone the repository and run `claude` in its root. `CLAUDE.md` is loaded automatically and is
   the only configuration. It encodes what I decided must never change (`TimeDeposit`, the
   `updateBalance` signature and its observable behaviour, the two-endpoint limit, the contract-first
   API), plus the architecture rules, test commands and commit conventions. No hooks, sub-agents
   or custom settings.
3. Working loop. I split the work into steps that each have a verifiable outcome. For each step I
   write a brief: scope, invariants, which tests prove it, what to report back. Claude Code executes
   it, runs the tests and shows the diff. I review the diff and the report, decide, and commit.
   Anything I want a second opinion on goes to Claude in chat before I decide.
4. Guardrails that make agent-driven changes safe on a "do not change behaviour" task:
    - `TimeDepositCalculatorCharacterizationTest`: expected values produced by executing the original
      code, plus a 20,000-input equivalence test against a verbatim copy of the legacy calculator.
      A refactoring that changes a single rounding result fails the build.
    - `CLAUDE.md` forbids edits to `TimeDeposit`, to the `updateBalance` signature and extra endpoints.
    - CI (`.github/workflows/ci.yml`) runs unit and Testcontainers tests on every push.

## 3. How the solution was built, and what was AI-assisted

**Analysis.** I started by reading the kata and the legacy code, then used Claude to stress-test my
reading and list risks. Three mattered. The `new BigDecimal(double)` rounding path, which a "clean"
`BigDecimal` rewrite would silently change. The "exactly two endpoints" limit, which rules out a
create endpoint and therefore requires seed data. And the `withdrawals` field in the GET schema
versus the requirement to leave `TimeDeposit` untouched. Each risk was confirmed by running the
original code on boundary inputs before it shaped the design.

**Design.** The design decisions are mine: hexagonal layering with the dependency rule
`adapters → application → domain`, a contract-first API where one `openapi.yaml` drives both code
generation and Swagger UI, a read model that composes withdrawals next to the shared class instead
of extending it, `POST …/balances` with `204` because the operation is not idempotent, Java 17 as
the minimum the kata names, and keeping the legacy rounding and case-sensitive plan matching,
because changing behaviour is a business decision, not a refactoring side effect.

**Characterization first.** Before any production change I had the current behaviour pinned. Claude
drafted the test matrix from my list of boundaries. Every expected value was produced by executing
the original code, never taken from a model's arithmetic. The 18.00 → 18.01 case (where
`BigDecimal.valueOf` gives 18.02) came from a brute-force search I asked for over inputs on which
the two rounding paths disagree.

**Build-out.** Claude drafted the skeleton (build, packages, ports, adapters, migrations, contract,
end-to-end test) to the structure above. I reviewed every file for the dependency rule, for JPA
staying out of the domain, and for the contract being the single source of truth, then had Claude
Code verify the build, run the application and exercise both endpoints through Swagger UI.

**Refactoring.** The interest-policy refactoring was done by Claude Code from a brief that fixed the
target shape and the arithmetic that must not change (section 4), in small steps, each gated by the
characterization suite and reviewed as a diff before commit.

| Part | AI-assisted? | How I checked it |
|------|--------------|------------------|
| Risk analysis | Claude (chat) as a sparring partner | Each risk reproduced by running the original code on boundary inputs. |
| Characterization tests | Matrix drafted by Claude from my boundary list, **expected values from executing the original code** | 18.00 → 18.01 found by brute force, equivalence test against a verbatim legacy copy. |
| Project structure: build, packages, ports, adapters, migrations, OpenAPI contract | Drafted by Claude to my design | Reviewed per file. Build, app run and Swagger UI verified by Claude Code. |
| Null plan type characterization test | Edge case raised by Claude Code, pinned on my decision | Committed before any production change. |
| Interest-policy refactoring (`domain/interest`, `TimeDepositCalculator`, `PlanType`) | Claude Code, from my brief | Characterization and equivalence tests unchanged and green. Diff and line endings reviewed before each commit. |
| Policy and registry unit tests | Claude Code | Boundaries 45/46 and 365/366, unknown plan types, null, custom policies through the public constructor. |
| Testcontainers end-to-end test | Drafted by Claude, made to pass by Claude Code | Expected balances follow from the seed data and the characterized values. |
| Naming and documentation cleanup | My review of the pushed repository | Behaviour unchanged, tests green. |
| Commit messages, READMEs, this document | Drafted with Claude, edited by me | Checked against `git log` and the code. |

## 4. Prompts and rules used

- `CLAUDE.md`, the full rules file (committed).
- The brief for the main refactoring, verbatim:

  ```
  Refactor the interest calculation in java/src/main/java/org/ikigaidigital/TimeDepositCalculator.java
  following the InterestPolicy interface that already exists in domain/interest.

  Requirements:
  - Add BasicInterestPolicy (1% p.a.), StudentInterestPolicy (3% p.a., only while days < 366),
    PremiumInterestPolicy (5% p.a., only while days > 45) and NoInterestPolicy (fallback for unknown
    plan types) in org.ikigaidigital.domain.interest.
  - Add InterestPolicyRegistry in the same package with a static defaults() factory mapping "basic",
    "student", "premium" to their policies and returning NoInterestPolicy for anything else.
  - TimeDepositCalculator keeps a public no-arg constructor (using defaults()) and gets a second
    constructor taking a registry. updateBalance(List<TimeDeposit>) keeps its exact signature and
    becomes a loop: if days > 30, ask the registry's policy for the monthly interest, otherwise 0;
    then round with new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP) and add to the
    balance exactly as before.
  - Keep the arithmetic bit-identical: double, written as balance * 0.01 / 12 (same operand order),
    new BigDecimal(double) not BigDecimal.valueOf. Add a comment on the 30-day guard explaining it
    stays in the calculator because it is common to all plans.
  - Do not touch TimeDeposit.java, the tests, or any adapter.

  After the change run `mvn test -Dtest=TimeDepositCalculatorCharacterizationTest` and show me the
  result and the full diff before committing. Do not commit yet.
  ```

- Later briefs had the same shape (scope, invariants, tests to run, "show the diff, do not commit
  yet"): `PlanType` with exact, case-sensitive matching, per-policy and registry tests computed the
  same way as the code, and named constants for the grace period, rates and day thresholds.

## 5. What I did not take on trust, and what I corrected

- **A wrong expectation in a drafted test.** The compounding case was off by one ulp:
  `1000.83 + 0.83` is `1001.6600000000001` in `double`. Caught by executing it. The test now
  asserts with a tolerance and documents why.
- **Wrong HTTP method in the first contract draft.** It used `PUT` and called the update
  "idempotent in intent". Every call compounds interest, so that was false. Switched to `POST`
  with `204`. The contract now states that the operation is not idempotent and why.
- **Swagger UI returned 404.** The drafted `springdoc.api-docs.enabled: false` also disables
  Swagger UI in springdoc 2.8. Found when I exercised the REST adapter manually and fixed in the
  configuration commit. `/v3/api-docs` is therefore exposed as a documentation endpoint, and the
  README says so.
- **Testcontainers vs Docker Engine 29.** Spring Boot 3.5 manages Testcontainers 1.21.3, whose
  Docker client API is rejected by Docker Engine 29. Surfaced on the first `mvn verify`. Pinned
  1.21.4 with the reason in `pom.xml`.
- **Null plan type.** Raised by Claude Code before the refactoring: the legacy code reads the plan
  type only after the 30-day check, so `null` passes up to day 30 and throws after it, and a
  `HashMap` lookup would silently turn that into "no interest". I pinned it with a test first. The
  registry uses `Map.copyOf`.
- **A justified deviation from my brief.** For `PlanType.fromString` I had written "empty for
  anything else". Claude Code made `null` throw instead, because returning empty would have changed
  the pinned behaviour, and flagged the deviation. I checked the reasoning and accepted it: the
  constraint outranks the letter of the brief.
- **Review of the pushed repository.** The 30-day literal, the legacy `a2d` variable, and an
  `InterestPolicy` Javadoc that attributed the grace period and rounding to policies instead of
  the calculator. Fixed in three small commits. `/ 12` stays a literal on purpose: it is a unit
  conversion, not a business parameter.
- **Rejected "improvements".** Rewriting the money arithmetic in `BigDecimal`, normalising plan-type
  case, and raising the Java version were all on the table. I kept the legacy behaviour and Java 17
  because none of them is a refactoring decision: the first two change results, the third adds
  nothing the code needs.