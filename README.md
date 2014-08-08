# simple-bus-mongo
================

Following the [article reposted by Mondora] (https://mondora.com/#!/post/eafc1a14af194ebeb8d0ccc425cbd803) here's a simple implementation.

## Introduction

This implementation of a circular buffer to support a message queue is implemented with Spring and Mongo DB

## Environment

Check [this property file] (src/test/resources/META-INF/com/whiteandreetto/prototypes/simplebus/simplebus.properties) for details about environmental dependencies

## Build

[Gradle] (build.gradle) and [Maven] (pom.xml) build mechanisms are available.

## Spring

### Dependencies

```xml

        <!-- Spring Framework - Core -->
        
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <!-- Spring Framework - Integration -->
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-core</artifactId>
            <version>${spring.integration.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-mongodb</artifactId>
            <version>${spring.integration.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

### XML-less configuration

This prototype uses java-based Spring configuration items.
Configuration entry point is [AppConfig.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/AppConfig.java).

AppConfig.java, imports @Configuration declarations from this [package] (src/main/java/com/whiteandreetto/prototypes/simplebus/cfg) which has two configuration classes, one implementing the general message flow, the other responsible for all things Mongo.

```java

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan({"com.whiteandreetto.prototypes.simplebus", "com.whiteandreetto.prototypes.simplebus.dbevents"})
@Import({MongoDBConfiguration.class, PlumbingConfiguration.class})

```

## Process Flow

[PlumbingConfiguration.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/cfg/PlumbingConfiguration) defines the pipelines for message handling as it follows:

Two gateway beans to simulate inbound and outbound queues

```java

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

```

Two endpoints as message destinations

```java

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


```

the relevant adapters to store messages at the end points:

```java

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

```



 


