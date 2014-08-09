package com.whiteandreetto.prototypes.simplebus.dbevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by AANG on 07/08/14/13:24.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class ContextStartEventHandler implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(ContextStartEventHandler.class);

    private AtomicBoolean aBoolean = new AtomicBoolean(false);

    @Autowired
    private DBListenerHandler dbListenerHandler;

    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;

    @Value("${mongo.db.message.collection.max}")
    private String COLLECTION_CAP;

    @Value("${mongo.db.message.collection.size}")
    private String COLLECTION_SIZE;

    @Value("${mongo.db.message.processed}")
    private String PARKING_LOT;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        if (!aBoolean.get()) {

            logger.info("----------------------------------------");
            logger.info("---------------INITIALIZED--------------");
            logger.info("----------------------------------------");

            try {
                dbListenerHandler.beginListening();
            } catch (Exception e) {
                logger.error("ERROR! {}", e.getMessage());
            }
            aBoolean.set(true);
        }

    }


    @Override
    public void destroy() throws Exception {
        logger.info("Shutdown completed");
    }
}
