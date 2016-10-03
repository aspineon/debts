package io.pillopl.eventsource.debts.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentExpected implements Event {

    public static final String TYPE = "item.bought";

    private UUID uuid;
    private Instant paymentTimeoutDate;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }
}
