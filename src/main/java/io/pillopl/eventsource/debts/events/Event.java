package io.pillopl.eventsource.debts.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = PaymentExpected.TYPE, value = PaymentExpected.class),
        @JsonSubTypes.Type(name = PaymentIsDue.TYPE, value = PaymentIsDue.class),
        @JsonSubTypes.Type(name = PaymentReceived.TYPE, value = PaymentReceived.class)
})
public interface Event {

    String type();
    UUID uuid();
}

