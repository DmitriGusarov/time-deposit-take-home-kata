package org.ikigaidigital.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** JPA mapping of the {@code time_deposits} table. Persistence-only; never leaves the adapter. */
@Entity
@Table(name = "time_deposits")
public class TimeDepositEntity {

    @Id
    private Integer id;

    @Column(name = "plan_type", nullable = false)
    private String planType;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected TimeDepositEntity() {
        // JPA
    }

    public TimeDepositEntity(Integer id, String planType, Integer days, BigDecimal balance) {
        this.id = id;
        this.planType = planType;
        this.days = days;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public String getPlanType() {
        return planType;
    }

    public Integer getDays() {
        return days;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
