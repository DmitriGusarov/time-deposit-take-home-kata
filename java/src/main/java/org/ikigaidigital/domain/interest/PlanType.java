package org.ikigaidigital.domain.interest;

import java.util.Objects;
import java.util.Optional;

/** The plan types that earn interest, keyed by the identifiers stored in {@code TimeDeposit.getPlanType()}. */
public enum PlanType {
    BASIC("basic"),
    STUDENT("student"),
    PREMIUM("premium");

    private final String identifier;

    PlanType(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Matches the identifier exactly (case-sensitive), as the legacy {@code equals} chain did,
     * so {@code "BASIC"} is not a plan type and earns nothing.
     * <p>
     * A {@code null} plan type fails with a {@link NullPointerException}, as the legacy
     * {@code planType.equals(...)} did (pinned by the characterization tests; validation is
     * out of scope).
     */
    public static Optional<PlanType> fromString(String planType) {
        Objects.requireNonNull(planType, "planType");
        for (PlanType type : values()) {
            if (type.identifier.equals(planType)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
