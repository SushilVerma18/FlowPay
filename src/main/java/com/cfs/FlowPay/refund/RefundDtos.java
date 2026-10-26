package com.cfs.FlowPay.refund;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RefundDtos {

    public record Create(@NotBlank String paymentId, @Min(1) @Max(10000000) long amount)
    {

    }
}
