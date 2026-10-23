package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Extension point for interest calculation – one implementation per plan type.
 * <p>
 * {@link org.ikigaidigital.TimeDepositCalculator#updateBalance(java.util.List)} keeps its
 * signature and is a thin loop that resolves the policy for each deposit's plan type
 * and applies it. Adding a new plan then means adding one class, not editing a nested if.
 * <p>
 * Behaviour that the implementations must preserve exactly (pinned by
 * {@code TimeDepositCalculatorCharacterizationTest}):
 * <ul>
 *   <li>no interest at all while {@code days <= 30}, for every plan</li>
 *   <li>interest is computed in {@code double} as {@code balance * annualRate / 12},
 *       then rounded with {@code new BigDecimal(double).setScale(2, HALF_UP)} – not
 *       {@code BigDecimal.valueOf(double)}, which rounds differently on some inputs</li>
 *   <li>unknown plan types earn nothing (no exception)</li>
 * </ul>
 */
public interface InterestPolicy {

    /**
     * @return the (unrounded) monthly interest for the given deposit, or 0 if none applies.
     */
    double monthlyInterest(TimeDeposit deposit);
}
