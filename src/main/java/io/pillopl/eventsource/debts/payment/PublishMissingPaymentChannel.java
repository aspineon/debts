package io.pillopl.eventsource.debts.payment;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.Publisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PublishMissingPaymentChannel {

    @Publisher(channel = Source.OUTPUT)
    public MarkPaymentTimeout send(MarkPaymentTimeout command, @Header("uuid") UUID aggregateUUID) {
        return command;
    }
}

