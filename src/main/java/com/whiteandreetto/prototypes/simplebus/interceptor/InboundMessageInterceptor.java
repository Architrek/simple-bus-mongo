package com.whiteandreetto.prototypes.simplebus.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
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
public class InboundMessageInterceptor extends ChannelInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InboundMessageInterceptor.class);

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        IntegrationMessageHeaderAccessor accessor = new IntegrationMessageHeaderAccessor(message);
        if (sent) {
            logger.info("Saved message #{} on channel: {}", accessor.getHeader("_id"), channel.toString());
        }
    }

}
