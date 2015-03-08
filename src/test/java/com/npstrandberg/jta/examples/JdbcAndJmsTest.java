package com.npstrandberg.jta.examples;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class JdbcAndJmsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcAndJms service;

    @Before
    public void before() {
        jdbcTemplate.execute("CREATE TABLE PERSON (ID INT, NAME VARCHAR)");
    }

    @After
    public void after() {
        jdbcTemplate.execute("DROP TABLE PERSON");
    }

    @Test
    public void testCommit() throws JMSException {

        // when
        service.registerNewPerson(1, "Niels Peter", false);

        // then
        assertTrue(1 == jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class));
        assertEquals("{id: '1', name: 'Niels Peter'}", service.receivePersonCreatedMessage());
    }

    @Test
    public void testRollback() {

        // when
        try {
            service.registerNewPerson(1, "Niels Peter", true);
        } catch (IllegalStateException e) {
            assertEquals("BOOM", e.getMessage());
        }

        // then
        assertTrue(0 == jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class));
        assertNull(service.receivePersonCreatedMessage());
    }
}
