package com.whiteandreetto.prototypes.simplebus;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDbFactory;
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


    @Autowired
    MongoDbFactory factory;



    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;

    @Value("${mongo.db.message.processed}")
    private String PARKING_LOT;

    @Value("${mongo.db.message.collection.max}")
    private Integer COLLECTION_CAP;

    @Value("${mongo.db.message.collection.size}")
    private Integer COLLECTION_SIZE;


    @Before
    public void setUp() {

        logger.info("@Before");


    }

    @After
    public void tearDown() {

        logger.info("@After");

    }



}
