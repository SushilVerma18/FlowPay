package com.cfs.FlowPay.payment;

import com.cfs.FlowPay.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"merchantId","idempotencyKey"})
)
public class Payment extends BaseEntity {

    @Column(nullable = false,length = 48)
    public String merchantId;

    @Column(nullable = false,length = 48)
    public String orderId;

    @Column(nullable = false,length = 100)
    public String idempotencyKey;

    @Column(nullable = false,length = 20)
    public String method;

    @Column(nullable = false,length = 30)
    public String scenario;

    @Column(nullable = false,length = 20)
    public String status="CREATED";

    @Column(nullable = false)
    public long amount;

    @Column(nullable = false)
    public long fee;

    @Column(nullable = false)
    public long refunded;

    public Instant resolveAt;


}
