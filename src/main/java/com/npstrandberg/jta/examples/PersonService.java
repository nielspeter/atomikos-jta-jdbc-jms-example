package com.npstrandberg.jta.examples;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void registerNewPerson(int id, String name, boolean fail) {
        jdbcTemplate.execute("insert into person (id, name) values (" + id + ", '" + name + "')");
        jmsTemplate.convertAndSend("PERSON_CREATED", "{id: '" + id + "', name: '" + name + "'}");
        if (fail) throw new IllegalStateException("BOOM");
    }

    @Transactional
    public String receivePersonCreatedMessage() {
        return (String) jmsTemplate.receiveAndConvert("PERSON_CREATED");
    }
}

