package org.ikigaidigital.domain.interest;

import java.util.Map;

/**
 * Resolves the {@link InterestPolicy} for a plan type.
 * <p>
 * Plan types are matched exactly (case-sensitive), as in the legacy {@code equals} chain;
 * anything not registered earns no interest. The map is immutable, so a {@code null} plan
 * type still fails with a {@link NullPointerException}, as the legacy code did.
 */
public final class InterestPolicyRegistry {

    private static final InterestPolicy NO_INTEREST = new NoInterestPolicy();

    private final Map<String, InterestPolicy> policies;

    public InterestPolicyRegistry(Map<String, InterestPolicy> policies) {
        this.policies = Map.copyOf(policies);
    }

    public static InterestPolicyRegistry defaults() {
        return new InterestPolicyRegistry(Map.of(
                "basic", new BasicInterestPolicy(),
                "student", new StudentInterestPolicy(),
                "premium", new PremiumInterestPolicy()));
    }

    public InterestPolicy policyFor(String planType) {
        return policies.getOrDefault(planType, NO_INTEREST);
    }
}
