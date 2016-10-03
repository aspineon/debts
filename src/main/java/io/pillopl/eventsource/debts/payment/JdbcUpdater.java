package io.pillopl.eventsource.debts.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static java.sql.Timestamp.from;

@Component
class JdbcUpdater {

    private static final String UPDATE_PENDING_PAYMENT
            = "UPDATE payments SET when_payment_timeout = ?, status = 'PENDING' WHERE uuid = ?";

    private static final String INSERT_PENDING_PAYMENT =
            "INSERT INTO payments " +
                    "(id, uuid, status, when_paid, when_payment_timeout, when_payment_marked_as_missing)" +
                    " VALUES (payments_seq.nextval, ?, 'PENDING', NULL, ?, NULL)";

    private static final String UPDATE_PAYMENT_DONE
            = "UPDATE payments SET when_paid = ?, status = 'PAID' WHERE when_paid IS NULL AND uuid = ?";

    private static final String UPDATE_PAYMENT_MISSING_SQL
            = "UPDATE payments SET when_payment_marked_as_missing = ?, status = 'MISSING' WHERE when_payment_marked_as_missing IS NULL AND uuid = ?";

    private static final String QUERY_FOR_PAYMENT_SQL =
            "SELECT * FROM payments WHERE uuid = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void updateOrCretePendingPayment(UUID uuid, Instant paymentTimeoutDate) {
        final int affectedRows = jdbcTemplate.update(UPDATE_PENDING_PAYMENT, from(paymentTimeoutDate), uuid);
        if (affectedRows == 0) {
            jdbcTemplate.update(INSERT_PENDING_PAYMENT, uuid, from(paymentTimeoutDate));
        }
    }

    void updatePaymentAsPaid(UUID uuid, Instant when) {
        jdbcTemplate.update(UPDATE_PAYMENT_DONE, from(when), uuid);
    }

    void updatePaymentAsMissing(UUID uuid, Instant when) {
        jdbcTemplate.update(UPDATE_PAYMENT_MISSING_SQL, from(when), uuid);
    }

    Payment getPaymentBy(UUID uuid) {
        return jdbcTemplate.queryForObject(QUERY_FOR_PAYMENT_SQL, new Object[]{uuid}, new BeanPropertyRowMapper<>(Payment.class));
    }
}
