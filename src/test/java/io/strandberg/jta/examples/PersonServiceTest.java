package io.strandberg.jta.examples;

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
public class PersonServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PersonService personService;

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
        personService.registerNewPerson(1, "Niels Peter", false);

        // then
        assertTrue(1 == jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class));
        assertEquals("{id: '1', name: 'Niels Peter'}", personService.receivePersonCreatedMessage());
    }

    @Test
    public void testRollback() {

        // when
        try {
            personService.registerNewPerson(1, "Niels Peter", true);
        } catch (IllegalStateException e) {
            assertEquals("BOOM", e.getMessage());
        }

        // then
        assertTrue(0 == jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class));
        assertNull(personService.receivePersonCreatedMessage());
    }
}
