package org.ikigaidigital.domain.interest;

import java.util.Map;

/**
 * Resolves the {@link InterestPolicy} for a plan type.
 * <p>
 * Plan type strings are resolved through {@link PlanType#fromString(String)}; anything that is
 * not a known plan type, or has no registered policy, earns no interest.
 */
public final class InterestPolicyRegistry {

    private static final InterestPolicy NO_INTEREST = new NoInterestPolicy();

    private final Map<PlanType, InterestPolicy> policies;

    public InterestPolicyRegistry(Map<PlanType, InterestPolicy> policies) {
        this.policies = Map.copyOf(policies);
    }

    public static InterestPolicyRegistry defaults() {
        return new InterestPolicyRegistry(Map.of(
                PlanType.BASIC, new BasicInterestPolicy(),
                PlanType.STUDENT, new StudentInterestPolicy(),
                PlanType.PREMIUM, new PremiumInterestPolicy()));
    }

    public InterestPolicy policyFor(String planType) {
        return PlanType.fromString(planType)
                .map(policies::get)
                .orElse(NO_INTEREST);
    }
}
