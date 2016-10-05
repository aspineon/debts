package io.pillopl.eventsource.debts.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "item.ordered", value = PaymentExpected.class),
        @JsonSubTypes.Type(name = "item.payment.timeout", value = PaymentIsDue.class),
        @JsonSubTypes.Type(name = "item.paid", value = PaymentReceived.class)
})
public interface Event {

    UUID uuid();
}

