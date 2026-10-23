package org.ikigaidigital.domain.interest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plan type identifiers are matched exactly, as the legacy {@code equals} chain did. */
class PlanTypeTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "basic,   BASIC",
            "student, STUDENT",
            "premium, PREMIUM",
    })
    void mapsTheLowercaseIdentifiers(String planType, PlanType expected) {
        assertThat(PlanType.fromString(planType)).contains(expected);
    }

    @ParameterizedTest(name = "\"{0}\" -> empty")
    @ValueSource(strings = {"BASIC", "Basic", " basic", "gold", ""})
    void anythingElseIsNotAPlanType(String planType) {
        assertThat(PlanType.fromString(planType)).isEmpty();
    }

    @Test
    void nullThrowsNullPointerExceptionAsTheLegacyCodeDid() {
        assertThatThrownBy(() -> PlanType.fromString(null))
                .isInstanceOf(NullPointerException.class);
    }
}
