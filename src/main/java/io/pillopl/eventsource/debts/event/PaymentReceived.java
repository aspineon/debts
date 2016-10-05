package io.pillopl.eventsource.debts.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentReceived implements Event {

    private UUID uuid;
    private Instant when;

    @Override
    public UUID uuid() {
        return uuid;
    }
}
