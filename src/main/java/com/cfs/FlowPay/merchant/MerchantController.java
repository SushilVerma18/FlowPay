package com.cfs.FlowPay.merchant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService service;

    public MerchantController(MerchantService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantDtos.Registered register(@RequestBody MerchantDtos.Register r)
    {
        return service.register(r);
    }

    @GetMapping("/me")
    public Merchant me(@RequestHeader("X-Api-Key") String key)
    {
        return service.authenticate(key);
    }

    @PutMapping("/me")
    public Merchant update(@RequestHeader("X-Api-Key") String key, @Valid @RequestBody MerchantDtos.Update r)
    {
        return service.update(service.authenticate(key).id,r);
    }
}
