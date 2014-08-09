package com.whiteandreetto.prototypes.simplebus;

import com.mongodb.DBCollection;
import com.whiteandreetto.prototypes.simplebus.gateway.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * Created by AANG on 07/08/14/14:11.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MessageFlowIntegrationTest extends AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MessageFlowIntegrationTest.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    MessageService messageService;

    @Autowired
    MongoDbFactory factory;


    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;

    @Value("${mongo.db.message.processed}")
    private String PARKING_LOT;

    @Value("${mongo.db.message.collection.max}")
    private Integer COLLECTION_CAP;


    @Test
    public void testSomething() throws Exception {


        Thread.sleep(1000);


        for (int idx = 0; idx < 100; idx++) {

            // SOME RANDOM INTERVAL BETWEEN MESSAGES
            try {
                //Thread.sleep(new Random(System.nanoTime()).nextInt(100));
                Thread.sleep(10);
            } catch (final InterruptedException ie) {
                logger.error(ie.getMessage());
            }

            // BUILD AND SEND A MESSAGE
            final Message<String> oneMessage = MessageBuilder.withPayload("SAMPLE PAYLOAD FOR A SAMPLE MESSAGE")
                    .setHeader("_id", idx)
                    .setHeader("sender", "TEST-CASE")
                    .setHeader("TIMESTAMP", System.currentTimeMillis())
                    .build();

            messageService.process(oneMessage);

        }


        Thread.sleep(1000);

        DBCollection col = factory.getDb().getCollection(MONGO_DB_COLLECTION);

        Assert.isTrue(col.count()==COLLECTION_CAP);

        col = factory.getDb().getCollection(PARKING_LOT);

        Assert.isTrue(col.count()>=COLLECTION_CAP);



    }


}
