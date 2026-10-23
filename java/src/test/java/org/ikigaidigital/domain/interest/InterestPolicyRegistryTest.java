package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterestPolicyRegistryTest {

    private final InterestPolicyRegistry defaults = InterestPolicyRegistry.defaults();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "basic,   org.ikigaidigital.domain.interest.BasicInterestPolicy",
            "student, org.ikigaidigital.domain.interest.StudentInterestPolicy",
            "premium, org.ikigaidigital.domain.interest.PremiumInterestPolicy",
    })
    void defaultsResolveEachKnownPlanTypeToItsPolicy(String planType, Class<?> expected) {
        assertThat(defaults.policyFor(planType)).isExactlyInstanceOf(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> NoInterestPolicy")
    @ValueSource(strings = {"gold", "BASIC", ""})
    void unknownPlanTypesFallBackToNoInterest(String planType) {
        assertThat(defaults.policyFor(planType)).isInstanceOf(NoInterestPolicy.class);
    }

    @Test
    void nullPlanTypeThrowsNullPointerExceptionAsTheLegacyCodeDid() {
        assertThatThrownBy(() -> defaults.policyFor(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void customPolicySuppliedThroughTheConstructorIsUsed() {
        // The public constructor is the extension seam: a caller can register its own policies.
        InterestPolicy custom = deposit -> 42.0;
        InterestPolicyRegistry registry = new InterestPolicyRegistry(Map.of(PlanType.BASIC, custom));

        assertThat(registry.policyFor("basic")).isSameAs(custom);
        assertThat(registry.policyFor("basic").monthlyInterest(new TimeDeposit(1, "basic", 1000.00, 31)))
                .isEqualTo(42.0);
    }
}
