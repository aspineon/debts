package io.pillopl.eventsource.debts.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIsDue implements Event {

    public static final String TYPE = "item.payment.timeout";

    private UUID uuid;
    private Instant when;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }
}
