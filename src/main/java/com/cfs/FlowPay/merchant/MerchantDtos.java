package com.cfs.FlowPay.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MerchantDtos {

    public record Register(
            @NotBlank @Size(max=100) String name,  @NotBlank @Email @Size(max=100) String email
    ){};


    public record Update(
            @NotBlank @Size(max=100)   String name,
            @NotBlank @Email @Size(max=100)   String email,
            @Size(max=100) String webhookUrl)
    {}

    public record Registered(Merchant merchant,String apiKey,String webhookSecret){}
}
