package com.whiteandreetto.prototypes.simplebus.dbevents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by AANG on 07/08/14/13:24.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class ContextStopEventHandler implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ContextStopEventHandler.class);

    @Autowired
    DBListenerHandler dbListenerHandler;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {

        logger.info("----------------------------------------");
        logger.info("----------------DISPOSED----------------");
        logger.info("----------------------------------------");

        try {
            dbListenerHandler.finishListening();
        } catch (Exception e) {
            logger.error("ERROR! {}", e.getMessage());
        }

    }


}
