package com.cfs.FlowPay.refund;

import com.cfs.FlowPay.merchant.MerchantService;
import com.cfs.FlowPay.payment.Payment;
import com.cfs.FlowPay.payment.PaymentDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService service;
    private final MerchantService merchants;

    public RefundController(RefundService service, MerchantService merchants) {
        this.service = service;
        this.merchants = merchants;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Refund create(@RequestHeader("X-Api-Key") String key,
                          @RequestHeader("Idempotency-Key") String ik,
                          @Valid @RequestBody RefundDtos.Create r
    )
    {
        return service.create(merchants.authenticate(key).id,ik,r);
    }

    @GetMapping
    public List<Refund> list(@RequestHeader("X-Api-Key") String key)
    {
        return service.list(merchants.authenticate(key).id);
    }

}
