package com.cfs.FlowPay.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public class BaseEntity {

    @Id
    @Column
    public String id;

    @Column(nullable = false,updatable = false)
    public Instant createdAt = Instant.now();
}
