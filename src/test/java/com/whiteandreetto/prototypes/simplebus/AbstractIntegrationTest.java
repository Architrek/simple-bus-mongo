package com.whiteandreetto.prototypes.simplebus;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by AANG on 07/08/14/14:08.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@ContextConfiguration(classes = {AppConfig.class})
public class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);


    @Before
    public void setUp() {

        logger.info("@Before");


    }

    @After
    public void tearDown() {

        logger.info("@After");

    }


}
