package org.ikigaidigital.adapters.outbound.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** JPA mapping of the {@code withdrawals} table. */
@Entity
@Table(name = "withdrawals")
public class WithdrawalEntity {

    @Id
    private Integer id;

    @Column(name = "time_deposit_id", nullable = false)
    private Integer timeDepositId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    protected WithdrawalEntity() {
        // JPA
    }

    public WithdrawalEntity(Integer id, Integer timeDepositId, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.timeDepositId = timeDepositId;
        this.amount = amount;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTimeDepositId() {
        return timeDepositId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}
