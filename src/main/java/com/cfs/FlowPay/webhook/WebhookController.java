package com.cfs.FlowPay.webhook;

import java.util.List;

import com.cfs.FlowPay.merchant.MerchantService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
  private final WebhookService service;
  private final MerchantService merchants;

  public WebhookController(WebhookService s, MerchantService m) {
    service = s;
    merchants = m;
  }

  @GetMapping
  public List<Webhook> list(@RequestHeader("X-Api-Key") String k) {
    return service.list(merchants.authenticate(k).id);
  }

  @PostMapping("/{id}/retry")
  public Webhook retry(@RequestHeader("X-Api-Key") String k, @PathVariable String id) {
    return service.retry(merchants.authenticate(k).id, id);
  }
}
