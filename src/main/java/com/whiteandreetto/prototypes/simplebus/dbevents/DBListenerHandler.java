package com.whiteandreetto.prototypes.simplebus.dbevents;

import com.whiteandreetto.prototypes.simplebus.gateway.QueueSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by AANG on 07/08/14/10:28.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class DBListenerHandler {

    private static final Logger logger = LoggerFactory.getLogger(DBListenerHandler.class);

    @Autowired
    MongoDbFactory mongoDbFactory;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    QueueSendService queueSendService;


    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;


    final AtomicBoolean readRunning = new AtomicBoolean(true);
    final AtomicLong readCounter = new AtomicLong(0);

    Thread listener;


    public void beginListening() throws Exception {

        logger.info("Begin listening to DB event");

        if (queueSendService != null) {

            listener = new Thread(new DBListener(queueSendService,
                    mongoDbFactory,
                    MONGO_DB_COLLECTION,
                    readRunning,
                    readCounter));

            listener.start();
        }


    }


    public void finishListening() throws Exception {

        logger.info("Stop listening to DB event");

        readRunning.set(false);
        Thread.sleep(5000);

        if (listener != null) {
            listener.interrupt();
        }


    }

}
