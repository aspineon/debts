package io.pillopl.eventsource.debts.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.sql.Timestamp.from;
import static java.util.UUID.fromString;

@Component
@Slf4j
public class PaymentsTimeoutChecker {

    private static final String MISSING_PAYMENTS_SQL_QUERY = "SELECT item_uuid FROM payments WHERE deadline <= ? AND status = 'PENDING'";

    private final PublishMissingPaymentChannel channel;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    PaymentsTimeoutChecker(JdbcTemplate jdbcTemplate, PublishMissingPaymentChannel channel) {
        this.channel = channel;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = 10000)
    public void run() {
        final Instant now = Instant.now();
        final List<String> missingPayments = jdbcTemplate.queryForList(
                MISSING_PAYMENTS_SQL_QUERY,
                String.class,
                from(now));
        log.info("Marking {} payments as missing at {}", missingPayments.size(), now);
        missingPayments
                .forEach(uuid -> publishMarkPaymentTimeoutCommand(uuid, now));
    }

    private void publishMarkPaymentTimeoutCommand(String itemUuid, Instant when) {
        final UUID uuid = fromString(itemUuid);
        final MarkPaymentTimeout command = new MarkPaymentTimeout(uuid, when);
        log.info("About to send: {} ", command);
        channel.send(command, uuid);
        log.info("Sent: {} ", command);
    }
}
