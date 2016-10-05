package io.pillopl.eventsource.debts.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Payment {

    private Long id;
    private String item_uuid;
    private String status;
    private BigDecimal amount;
    private Timestamp arrived;
    private Timestamp deadline;
    private Timestamp when_payment_marked_as_missing;
}
