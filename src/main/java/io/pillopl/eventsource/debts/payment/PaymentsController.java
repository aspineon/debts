package io.pillopl.eventsource.debts.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class PaymentsController {

    private static final String SELECT_FROM_PAYMENTS = "select uuid, status, when_paid, when_payment_timeout, when_payment_marked_as_missing from payments";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PaymentsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value = "/payments", method = RequestMethod.GET)
    public List<Map<String, Object>> all() {
        return jdbcTemplate.queryForList(SELECT_FROM_PAYMENTS);
    }

}
