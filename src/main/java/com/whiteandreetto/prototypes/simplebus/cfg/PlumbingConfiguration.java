package com.whiteandreetto.prototypes.simplebus.cfg;

import com.whiteandreetto.prototypes.simplebus.interceptor.InboundMessageInterceptor;
import com.whiteandreetto.prototypes.simplebus.interceptor.OutboundMessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mongodb.outbound.MongoDbStoringMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Created by anda on 06/08/14.
 */
@Configuration
@IntegrationComponentScan({"com.whiteandreetto.prototypes.simplebus.endpoint", "com.whiteandreetto.prototypes.simplebus.gateway", "com.whiteandreetto.prototypes.simplebus.interceptor"})
public class PlumbingConfiguration {

    @Autowired
    MongoDbFactory mongoDbFactory;
    @Autowired
    InboundMessageInterceptor inboundMessageInterceptor;
    @Autowired
    OutboundMessageInterceptor outboundMessageInterceptor;
    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;
    @Value("${mongo.db.message.processed}")
    private String PARKING_LOT;

    @Bean
    @Description("The gateway service to entry the messaging system.")
    public MessageChannel messageInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @Description("The gateway service to exit the messaging system.")
    public MessageChannel messageOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @Description("Persistent Message Store channel for the inbound requests.")
    public MessageChannel messageEntryStoreChannel() {
        final DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(inboundMessageInterceptor);
        return directChannel;
    }

    @Bean
    @Description("Persistent Message Store channel for the outbound requests.")
    public MessageChannel messageExitStoreChannel() {
        final DirectChannel directChannel = new DirectChannel();
        directChannel.addInterceptor(outboundMessageInterceptor);
        return directChannel;
    }

    @Bean
    @ServiceActivator(inputChannel = "messageEntryStoreChannel")
    @Description("Mongo DB Adapter for storing inbound requests on the circular buffer.")
    public MessageHandler mongodbAdapterEntry() throws Exception {
        final MongoDbStoringMessageHandler adapter = new MongoDbStoringMessageHandler(mongoDbFactory);
        adapter.setCollectionNameExpression(new LiteralExpression(MONGO_DB_COLLECTION));
        return adapter;
    }


    @Bean
    @ServiceActivator(inputChannel = "messageExitStoreChannel")
    @Description("Endpoint to store outbound messages.")
    public MessageHandler mongodbAdapterEnd() throws Exception {
        final MongoDbStoringMessageHandler adapter = new MongoDbStoringMessageHandler(mongoDbFactory);
        adapter.setCollectionNameExpression(new LiteralExpression(PARKING_LOT));
        return adapter;
    }


}
