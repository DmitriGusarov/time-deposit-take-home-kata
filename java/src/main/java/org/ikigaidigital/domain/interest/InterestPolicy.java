package org.ikigaidigital.domain.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Extension point for interest calculation – one implementation per plan type.
 * <p>
 * {@link org.ikigaidigital.TimeDepositCalculator#updateBalance(java.util.List)} keeps its
 * signature and is a thin loop that resolves the policy for each deposit's plan type
 * and applies it. Adding a new plan then means adding one class, not editing a nested if.
 * <p>
 * Contract (the results are pinned by {@code TimeDepositCalculatorCharacterizationTest}):
 * <ul>
 *   <li>an implementation applies only its own plan's rule and returns the <em>unrounded</em>
 *       monthly interest, or 0 when that rule pays nothing</li>
 *   <li>interest is computed in {@code double}, in the order {@code balance * annualRate / 12};
 *       a different order or {@code BigDecimal} arithmetic can change the rounded result</li>
 *   <li>the 30-day grace period and the rounding ({@code new BigDecimal(double)
 *       .setScale(2, HALF_UP)}) are common to all plans and live in the calculator, not here</li>
 *   <li>unknown plan types are handled by {@link InterestPolicyRegistry}, which falls back to
 *       {@link NoInterestPolicy}</li>
 * </ul>
 */
public interface InterestPolicy {

    /**
     * @return the unrounded monthly interest under this plan's rule, or 0 if the rule pays nothing.
     */
    double monthlyInterest(TimeDeposit deposit);
}
