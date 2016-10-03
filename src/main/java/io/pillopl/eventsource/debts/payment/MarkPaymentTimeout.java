package io.pillopl.eventsource.debts.payment;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class MarkPaymentTimeout {

    private final String type = "item.markPaymentTimeout";
    private final UUID uuid;
    private final Instant when;

}
