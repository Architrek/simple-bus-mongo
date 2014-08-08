package com.whiteandreetto.prototypes.simplebus.interceptor;

import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

/**
 * Created by AANG on 07/08/14/01:35.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class OutboundMessageInterceptor extends ChannelInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OutboundMessageInterceptor.class);

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

        logger.info("Sent: {}", sent);

        BasicDBObject inMsg = (BasicDBObject) message.getPayload();

        if (sent) {
            logger.info("Saved message #{} on channel: {}", inMsg.get("_id"), channel.toString());
        }
    }

}
