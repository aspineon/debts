package io.pillopl.eventsource.debts.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static java.sql.Timestamp.from;

@Component
class JdbcUpdater {

    private static final String UPDATE_PENDING_PAYMENT
            = "UPDATE payments SET deadline = ?, status = 'PENDING' WHERE item_uuid = ?";

    private static final String INSERT_PENDING_PAYMENT =
            "INSERT INTO payments " +
                    "(id, item_uuid, amount, status, arrived, deadline, when_payment_marked_as_missing)" +
                    " VALUES (payments_seq.nextval, ?, ?, 'PENDING', NULL, ?, NULL)";

    private static final String UPDATE_PAYMENT_DONE
            = "UPDATE payments SET arrived = ?, status = 'PAID' WHERE arrived IS NULL AND item_uuid = ?";

    private static final String UPDATE_PAYMENT_MISSING_SQL
            = "UPDATE payments SET when_payment_marked_as_missing = ?, status = 'MISSING' WHERE when_payment_marked_as_missing IS NULL AND item_uuid = ?";

    private static final String QUERY_FOR_PAYMENT_SQL =
            "SELECT * FROM payments WHERE item_uuid = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void updateOrCretePendingPayment(UUID itemUuid, Instant paymentTimeoutDate, BigDecimal amount) {
        final int affectedRows = jdbcTemplate.update(UPDATE_PENDING_PAYMENT, from(paymentTimeoutDate), itemUuid);
        if (affectedRows == 0) {
            jdbcTemplate.update(INSERT_PENDING_PAYMENT, itemUuid, amount, from(paymentTimeoutDate));
        }
    }

    void updatePaymentAsPaid(UUID itemUuid, Instant when) {
        jdbcTemplate.update(UPDATE_PAYMENT_DONE, from(when), itemUuid);
    }

    void updatePaymentAsMissing(UUID itemUuid, Instant when) {
        jdbcTemplate.update(UPDATE_PAYMENT_MISSING_SQL, from(when), itemUuid);
    }

    Payment getPaymentBy(UUID itemUuid) {
        return jdbcTemplate.queryForObject(QUERY_FOR_PAYMENT_SQL, new Object[]{itemUuid}, new BeanPropertyRowMapper<>(Payment.class));
    }
}
