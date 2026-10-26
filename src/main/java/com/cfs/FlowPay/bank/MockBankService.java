package com.cfs.FlowPay.bank;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MockBankService {

    public record Result(String status, Instant resolveAt)
    {

    }

    public Result initiate(String scenario)
    {
        return switch (scenario)
        {
            case "SUCCESS" -> new Result("SUCCESS",null);
            case "FAILURE" -> new Result("FAILED",null);
            case "DELAYED_SUCCESS" -> new Result("PENDING",Instant.now().plusSeconds(8));
            case "TIMEOUT" -> new Result("PENDING",Instant.now().plusSeconds(20));
            default -> throw new IllegalArgumentException("Unknown Simulation scenario");
        };


    }


    public String check(Instant resolveAt)
    {
        return resolveAt!=null && !Instant.now().isBefore(resolveAt) ? "SUCCESS" :"PENDING";
    }
}
