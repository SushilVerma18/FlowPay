package com.cfs.FlowPay.webhook;


import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.cfs.FlowPay.common.ApiException;
import com.cfs.FlowPay.kafka.Event;
import com.cfs.FlowPay.kafka.EventRepository;
import com.cfs.FlowPay.merchant.Merchant;
import com.cfs.FlowPay.merchant.MerchantRepository;
import com.cfs.FlowPay.merchant.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {
  private final WebhookRepository repo;
  private final EventRepository events;
  private final MerchantRepository merchants;
  private final MerchantService merchantService;
  private final HttpClient http =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(2))
          .followRedirects(HttpClient.Redirect.NEVER)
          .build();

  public WebhookService(
      WebhookRepository r, EventRepository e, MerchantRepository m, MerchantService s) {
    repo = r;
    events = e;
    merchants = m;
    merchantService = s;
  }

  public static String sign(String secret, String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  public void deliver(String id) {
    Webhook w = repo.lockById(id).orElseThrow();
    if (!w.status.equals("PENDING") || w.nextAttemptAt.isAfter(Instant.now())) return;
    Event e = events.findById(w.eventId).orElseThrow();
    Merchant m = merchants.findById(w.merchantId).orElseThrow();
    w.attempts++;
    try {
      merchantService.validate(w.url);
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(w.url))
              .timeout(Duration.ofSeconds(3))
              .header("Content-Type", "application/json")
              .header("X-FlowPay-Event", e.id)
              .header("X-FlowPay-Signature", sign(m.webhookSecret, e.payload))
              .POST(HttpRequest.BodyPublishers.ofString(e.payload))
              .build();
      int status = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
      if (status < 200 || status >= 300) throw new IllegalStateException("HTTP " + status);
      w.status = "DELIVERED";
      w.lastError = null;
    } catch (Exception ex) {
      if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
      w.lastError =
          ex instanceof IllegalStateException ? ex.getMessage() : ex.getClass().getSimpleName();
      w.status = w.attempts >= 5 ? "FAILED" : "PENDING";
      w.nextAttemptAt = Instant.now().plusSeconds(1L << w.attempts);
    }
  }

  @Transactional
  public Webhook retry(String merchantId, String id) {
    Webhook w = repo.lockById(id).orElseThrow(ApiException::missing);
    if (!w.merchantId.equals(merchantId)) throw ApiException.missing();
    if (w.status.equals("DELIVERED")) throw ApiException.conflict("Webhook already delivered");
    Merchant m = merchants.findById(merchantId).orElseThrow();
    if (m.webhookUrl == null || m.webhookUrl.isBlank())
      throw ApiException.bad("Configure a webhook URL first");
    w.url = m.webhookUrl;
    w.status = "PENDING";
    w.attempts = 0;
    w.nextAttemptAt = Instant.now();
    w.lastError = null;
    return w;
  }

  public List<Webhook> list(String m) {
    return repo.findByMerchantIdOrderByCreatedAtDesc(m);
  }
}
