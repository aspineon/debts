package io.pillopl.eventsource.debts.integration.payment

import io.pillopl.eventsource.debts.integration.IntegrationSpec
import io.pillopl.eventsource.debts.payment.JdbcUpdater
import io.pillopl.eventsource.debts.payment.Payment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.messaging.support.GenericMessage
import spock.lang.Subject

import java.time.Instant

import static java.math.BigDecimal.TEN
import static java.time.Instant.parse

class PaymentsUpdaterIntegrationSpec extends IntegrationSpec {

    private static final Instant PAYMENT_TIMEOUT = parse("1995-10-23T10:12:35Z")
    private static final Instant ANY_TIME_LATER = parse("1995-10-23T10:12:35Z").plusSeconds(3600)

    private static final Instant ANY_OTHER_TIME = PAYMENT_TIMEOUT.plusSeconds(100)
    private static final Instant YET_ANOTHER_TIME = ANY_OTHER_TIME.plusSeconds(100)

    @Subject @Autowired JdbcUpdater readModel

    @Autowired Sink sink

    def 'should store expected payment'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT, TEN)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.item_uuid == itemUUID.toString()
            payment.status == 'PENDING'
            payment.arrived == null
            payment.deadline.toInstant() == PAYMENT_TIMEOUT
            payment.when_payment_marked_as_missing == null
    }

    def 'creating a payment should be idempotent'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        and:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.deadline.toInstant() == PAYMENT_TIMEOUT
    }

    def 'should update payment as arrived'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        and:
            paymentReceived(itemUUID, ANY_OTHER_TIME)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.arrived.toInstant() == ANY_OTHER_TIME
    }

    def 'updating as arrived should be idempotent'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        and:
            paymentReceived(itemUUID, ANY_OTHER_TIME)
        and:
            paymentReceived(itemUUID, YET_ANOTHER_TIME)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.arrived.toInstant() == ANY_OTHER_TIME
    }

    def 'should update payment as due'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        and:
            paymentIsDue(itemUUID, ANY_OTHER_TIME)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.status == "MISSING"
            payment.when_payment_marked_as_missing.toInstant() == ANY_OTHER_TIME
    }

    def 'updating payment as due should be idempotent'() {
        given:
            UUID itemUUID = UUID.randomUUID()
        when:
            paymentExpected(itemUUID, PAYMENT_TIMEOUT)
        and:
            paymentIsDue(itemUUID, ANY_OTHER_TIME)
        and:
            paymentIsDue(itemUUID, YET_ANOTHER_TIME)
        then:
            Payment payment = readModel.getPaymentBy(itemUUID)
            payment.when_payment_marked_as_missing.toInstant() == ANY_OTHER_TIME
    }

    void paymentExpected(UUID uuid, Instant paymentTimeout = ANY_TIME_LATER, BigDecimal price =  TEN) {
        sink.input().send(new GenericMessage<>(itemOrderedInJson(uuid, paymentTimeout, price)))
    }

    void paymentReceived(UUID uuid, Instant when) {
        sink.input().send(new GenericMessage<>(itemPaidInJson(uuid, when)))
    }

    void paymentIsDue(UUID uuid, Instant when) {
        sink.input().send(new GenericMessage<>(itemTimeoutInJson(uuid, when)))
    }

    private static String itemOrderedInJson(UUID uuid, Instant paymentTimeout, BigDecimal price) {
        return "{\"type\":\"item.ordered\",\"uuid\":\"$uuid\",\"paymentTimeoutDate\":\"$paymentTimeout\",\"price\":$price}"
    }

    private static String itemPaidInJson(UUID uuid, Instant when) {
        return "{\"type\":\"item.paid\",\"uuid\":\"$uuid\",\"when\":\"$when\"}"
    }

    private static String itemTimeoutInJson(UUID uuid, Instant when) {
        return "{\"type\":\"item.payment.timeout\",\"uuid\":\"$uuid\",\"when\":\"$when\"}"
    }

}
