package com.cfs.FlowPay.order;

import com.cfs.FlowPay.merchant.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {


    private final OrderService service;
    private final MerchantService merchants;

    public OrderController(OrderService service, MerchantService merchants) {
        this.service = service;
        this.merchants = merchants;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentOrder create(@RequestHeader("X-Api-Key") String key,@Valid @RequestBody OrderDtos.Create r)
    {
        return service.create(merchants.authenticate(key).id,r);
    }


    @GetMapping
    public List<PaymentOrder> list(@RequestHeader("X-Api-Key") String key)
    {
        return service.list(merchants.authenticate(key).id);
    }

    @GetMapping("/{id}")
    public PaymentOrder list(@RequestHeader("X-Api-Key") String key,@PathVariable String id)
    {
        return service.get(merchants.authenticate(key).id,id);
    }
}
