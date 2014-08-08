package com.whiteandreetto.prototypes.simplebus.endpoint;

import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by AANG on 07/08/14/12:35.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@Component
public class OutboundMessageBuilder {


    @Transformer(inputChannel = "messageOutboundChannel", outputChannel = "messageExitStoreChannel")
    public Map<String, Object> prepareObjectForExit(Map<String, Object>  msg) {

        Object oldId = msg.get("_id");
        msg.put("old_id", oldId);
        msg.put("_id", System.currentTimeMillis());
        return msg;
    }

}
