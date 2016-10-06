package io.pillopl.eventsource.debts.integration.payment

import io.pillopl.eventsource.debts.integration.IntegrationSpec
import io.pillopl.eventsource.debts.payment.PaymentsTimeoutChecker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.messaging.Source
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

import java.time.Instant
import java.util.concurrent.BlockingQueue

import static java.math.BigDecimal.TEN
import static java.time.Instant.parse

class E2ESpec extends IntegrationSpec {

    private static final Instant TIMEOUT_FAR_IN_THE_PAST = parse("1995-10-23T10:12:35Z")
    private static final Instant TIMEOUT_FAR_IN_THE_FUTURE = parse("2055-10-23T10:12:35Z")
    private static final Instant ANY_TIME = parse("1995-10-23T10:12:35Z")

    @Subject @Autowired PaymentsTimeoutChecker paymentsTimeoutChecker

    @Autowired Sink sink
    @Autowired MessageCollector messageCollector
    @Autowired Source source

    BlockingQueue<Message<?>> channel
    PollingConditions pollingConditions

    def setup() {
        channel = messageCollector.forChannel(source.output())
        pollingConditions = new PollingConditions(timeout: 12, initialDelay: 0, factor: 1)
    }

    def 'when deadline reached should send command about item payment timeout to output channel'() {
        given:
            UUID itemUUID = UUID.randomUUID()
            paymentExpected(itemUUID, TIMEOUT_FAR_IN_THE_PAST)
        when:
            paymentsTimeoutChecker.run()
        then:
            pollingConditions.eventually {
                Message<String> received = channel.poll()
                received != null
                received.payload.contains("item.markPaymentTimeout")
            }
    }

    def 'should not send information when deadline not reached'() {
        given:
            UUID itemUUID = UUID.randomUUID()
            paymentExpected(itemUUID, TIMEOUT_FAR_IN_THE_FUTURE)
        when:
            paymentsTimeoutChecker.run()
        then:
            assertNextMessageDoesNotContain(itemUUID.toString())
    }

    def 'should not send information when payment already marked as due'() {
        given:
            UUID itemUUID = UUID.randomUUID()
            paymentExpected(itemUUID, TIMEOUT_FAR_IN_THE_PAST)
        and:
            paymentIsDue(itemUUID, ANY_TIME)
        when:
            paymentsTimeoutChecker.run()
        then:
            assertNextMessageDoesNotContain(itemUUID.toString())
    }

    void assertNextMessageDoesNotContain(String text) {
        Message<String> message = channel.poll()
        assert message == null || !message.getPayload().contains(text)
    }


    void paymentExpected(UUID uuid, Instant paymentTimeout = ANY_TIME, BigDecimal price =  TEN) {
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
