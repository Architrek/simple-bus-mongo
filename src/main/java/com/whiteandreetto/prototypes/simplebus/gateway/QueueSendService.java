package com.whiteandreetto.prototypes.simplebus.gateway;

import org.springframework.integration.annotation.MessagingGateway;

/**
 * Created by AANG on 07/08/14/03:43.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
@MessagingGateway(name = "messageExitGateway", defaultRequestChannel = "messageOutboundChannel")
public interface QueueSendService {

    public void process(Object message);

}
