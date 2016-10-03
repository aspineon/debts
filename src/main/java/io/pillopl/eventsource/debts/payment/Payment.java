package io.pillopl.eventsource.debts.payment;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Payment {

    private Long id;
    private String uuid;
    private String status;
    private Timestamp when_paid;
    private Timestamp when_payment_timeout;
    private Timestamp when_payment_marked_as_missing;
}
