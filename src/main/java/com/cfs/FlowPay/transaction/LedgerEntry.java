package com.cfs.FlowPay.transaction;


import com.cfs.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class LedgerEntry extends BaseEntity {

    @Column(nullable = false,length = 48)
    public String merchantId;

    @Column(nullable = false,length = 48)
    public String referenceId;

    @Column(nullable = false,length = 20)
    public String type;

    @Column(nullable = false)
    public long amount;

    @Column(nullable = false)
    public long balanceAfter;
}

