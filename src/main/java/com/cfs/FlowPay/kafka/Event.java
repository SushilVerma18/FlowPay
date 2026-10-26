package com.cfs.FlowPay.kafka;

import com.cfs.FlowPay.common.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(nullable = false,length = 48)
    public String merchantId;

    @Column(nullable = false,length = 60)
    public String type;

    @Column(nullable = false, columnDefinition="TEXT")
    public String payload;

    public Instant publishedAt;

    public Instant consumedAt;
}
