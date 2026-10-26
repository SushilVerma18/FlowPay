package com.cfs.FlowPay.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Component
@ConditionalOnProperty(name = {"app.jobs-enabled","app.kafka-enabled"},havingValue = "true")
public class OutBoxJobs {


    private static final Logger log= LoggerFactory.getLogger(OutBoxJobs.class);
    private final EventRepository repo;
    private final OutBoxPublisher publisher;

    public OutBoxJobs(EventRepository repo, OutBoxPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 3000)
    public void publish()
    {
        for(Event e: repo.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
        {
            try {
                publisher.publish(e.id);
            } catch (Exception ex) {
                log.info("Event {} remains pending  {} ",e.id,ex.getClass().getSimpleName());
                break;
            }
        }
    }
}
