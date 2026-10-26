package com.cfs.FlowPay.webhook;

import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs-enabled", havingValue = "true")
public class WebhookJobs {
  private final WebhookRepository repo;
  private final WebhookService service;

  public WebhookJobs(WebhookRepository r, WebhookService s) {
    repo = r;
    service = s;
  }

  @Scheduled(fixedDelay = 2000)
  public void deliver() {
    for (Webhook w :
        repo.findTop50ByStatusAndNextAttemptAtBeforeOrderByCreatedAtAsc("PENDING", Instant.now()))
      service.deliver(w.id);
  }
}
