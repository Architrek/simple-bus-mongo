package com.whiteandreetto.prototypes.simplebus.endpoint;

import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AANG on 06/08/14/18:08.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class InboundMessageBuilder {

    @Transformer(inputChannel = "messageInboundChannel", outputChannel = "messageEntryStoreChannel")
    public Map<String, Object> prepareObject(Message<Object> msg) {
        return getDbObject(msg);
    }

    private Map<String, Object> getDbObject(Message<Object> msg) {
        //DBObject dbo = new BasicDBObject();
        IntegrationMessageHeaderAccessor accessor = new IntegrationMessageHeaderAccessor(msg);

        Map<String, Object> dbo = new HashMap<>();
        dbo.put("_id", accessor.getHeader("_id"));
        dbo.put("timestamp", accessor.getHeader("TIMESTAMP"));
        dbo.put("sender", accessor.getHeader("SENDER"));
        dbo.put("uuid", UUID.randomUUID());
        dbo.put("payload", msg.getPayload());
        dbo.put("isReadable",true);

        return dbo;
    }


}
