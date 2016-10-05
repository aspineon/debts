package io.pillopl.eventsource.debts.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentExpected implements Event {

    private UUID uuid;
    private Instant paymentTimeoutDate;
    private BigDecimal price;

    @Override
    public UUID uuid() {
        return uuid;
    }
}
