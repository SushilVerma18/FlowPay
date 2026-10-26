package com.cfs.FlowPay.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrderDtos {


    public record Create(
            @Min(100) @Max(10000000) long amount,@NotBlank @Size(max = 100) String receipt){}

}
