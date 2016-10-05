package io.pillopl.eventsource.debts.payment;

import io.pillopl.eventsource.debts.event.Event;
import io.pillopl.eventsource.debts.event.PaymentExpected;
import io.pillopl.eventsource.debts.event.PaymentReceived;
import io.pillopl.eventsource.debts.event.PaymentIsDue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentsUpdater {

    private final JdbcUpdater jdbcReadModelUpdater;

    @Autowired
    PaymentsUpdater(JdbcUpdater jdbcReadModelUpdater) {
        this.jdbcReadModelUpdater = jdbcReadModelUpdater;
    }

    public void handle(Event event) {
        if (event instanceof PaymentExpected) {
            final PaymentExpected paymentExpected = (PaymentExpected) event;
            jdbcReadModelUpdater.updateOrCretePendingPayment(event.uuid(), paymentExpected.getPaymentTimeoutDate(), paymentExpected.getPrice());
        } else if (event instanceof PaymentReceived) {
            final PaymentReceived paymentReceived = (PaymentReceived) event;
            jdbcReadModelUpdater.updatePaymentAsPaid(event.uuid(), paymentReceived.getWhen());
        } else if (event instanceof PaymentIsDue) {
            final PaymentIsDue itemPaymentTimeout = (PaymentIsDue) event;
            jdbcReadModelUpdater.updatePaymentAsMissing(event.uuid(), itemPaymentTimeout.getWhen());
        } else {
            throw new IllegalArgumentException("Cannot handle event " + event.getClass());

        }
    }

}
