package com.whiteandreetto.prototypes.simplebus.gateway;

import org.springframework.integration.annotation.MessagingGateway;

/**
 * Created by AANG on 06/08/14/16:52.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@MessagingGateway(name = "messageEntryGateway", defaultRequestChannel = "messageInboundChannel")
public interface MessageService {

    public void process(Object message);

}
