package io.pillopl.eventsource.debts;

import io.pillopl.eventsource.debts.event.Event;
import io.pillopl.eventsource.debts.payment.PaymentsUpdater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableBinding(Processor.class)
public class Application  {

    private final PaymentsUpdater payments;

    @Autowired
    public Application(PaymentsUpdater payments) {
        this.payments = payments;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }

    @StreamListener(Sink.INPUT)
    public void eventStream(Event event) {
        log.info("Received: " + event);
        payments.handle(event);
    }

}
