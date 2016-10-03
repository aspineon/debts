package io.pillopl.eventsource.debts.payment

import io.pillopl.eventsource.debts.events.PaymentExpected
import io.pillopl.eventsource.debts.events.PaymentReceived
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

import static java.time.Instant.now
import static java.util.UUID.randomUUID

class PaymentsUpdaterSpec extends Specification {

    private static final UUID ANY_UUID = randomUUID()
    private static final Instant ANY_DATE = now()
    private static final Instant ANY_PAYMENT_TIMEOUT = now()

    JdbcUpdater jdbcReadModel = Mock()

    @Subject
    PaymentsUpdater readModelUpdater = new PaymentsUpdater(jdbcReadModel)

    def 'should update or create pending payment when receiving expected payment event'() {
        when:
            readModelUpdater.handle(new PaymentExpected(ANY_UUID, ANY_PAYMENT_TIMEOUT))
        then:
            1 * jdbcReadModel.updateOrCretePendingPayment(ANY_UUID, ANY_PAYMENT_TIMEOUT)
    }

    def 'should update payment when receiving payment received event'() {
        when:
            readModelUpdater.handle(new PaymentReceived(ANY_UUID, ANY_DATE))
        then:
            1 * jdbcReadModel.updatePaymentAsPaid(ANY_UUID, ANY_DATE)
    }

}
