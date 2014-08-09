![alt text](http://whiteandreetto.com/wp-content/uploads/2014/08/logo.full_.black_.png "White Andreetto Consulting")



# A Simple Service Bus Prototype
## Implementing a Persistent Message Queue with MongoDB & Spring
================

Following this [article reposted by Mondora] (https://mondora.com/#!/post/eafc1a14af194ebeb8d0ccc425cbd803) and the notable work [MongoDBx::Queue](https://metacpan.org/pod/MongoDBx::Queue) from [David Golden] (http://www.dagolden.com), here's a simple implementation of a circular buffer and relevant read components.
The implementation relies on MongoDB Capped Collections and Tailable Cursor.

### Capped Collections

1. Capped collections guarantee preservation of the insertion order. As a result, queries do not need an index to return documents in insertion order. Without this indexing overhead, they can support higher insertion throughput.
2. Capped collections guarantee that insertion order is identical to the order on disk (natural order) and do so by prohibiting updates that increase document size. Capped collections only allow updates that fit the original document size, which ensures a document does not change its location on disk.
3. Capped collections automatically remove the oldest documents in the collection without requiring scripts or explicit remove operations.

### Tailable Cursors

1. Tailable cursors do not use indexes and return documents in natural order.
Because tailable cursors do not use indexes, the initial scan for the query may be expensive; but, after initially exhausting the cursor, subsequent retrievals of the newly added documents are inexpensive.
2. Tailable cursors may become dead, or invalid, if either:
..* the query returns no match.
..* the cursor returns the document at the “end” of the collection and then the application deletes those document.

### Considerations
A solution of this caliber is ideally fit for entering the Integration Architecture in order to plug together fast write operations like log, montitoring, audit, etc..
 
 
This prototype has been stress tested with the following benchmark on a circular buffer of 100 elements:
 
 up to 500 msg/sec 100% 
 500 - 700 msg/sec  95%
 700 - 1000 msg7sec 80%

Message loss is due to the fastest write cycle which fills the buffer before it could be read.


### Next steps
A simple filtering mechanism is about to be implemented in order to allow concurrent queue consumption and therefore maximize throughput. Stay tuned.

## Prototype Implementation

This is the implementation of a circular buffer to support a message queue implemented with Spring and Mongo DB

## Environment

Check [this property file] (src/test/resources/META-INF/com/whiteandreetto/prototypes/simplebus/simplebus.properties) for details about environmental dependencies

```code

## MONGO DB 
mongo.db.host=localhost
mongo.db.port=27017
mongo.db=simple-bus-mongo
## THE CAPPED COLLECTION
mongo.db.message.collection=CIRCULAR_BUFFER
mongo.db.message.collection.max=10
## THE DESTINATION QUEUE
mongo.db.message.processed=PARKING_LOT
mongo.db.message.collection.size=1000000000
## FLAG TO CLEAN UP THE DB AT STARTUP 
mongo.db.clean.at.startup=false


```

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
## Mongo DB configuration
[MongoDBConfiguration.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/cfg/MongoDBConfiguration.java) defines a [MongoDbFactry](http://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/MongoDbFactory.html) to allow access to the Mongo DB server.

```java


```



## Process Flow

[PlumbingConfiguration.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/cfg/PlumbingConfiguration.java) defines the pipelines for message handling as it follows:

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

## Tailable cursor

The read operations are carried on by the class [DBListener.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/dbevents/DBListener.java) which extendsThread.

```java

public class DBListener extends Thread {...}

```

The key of the read operations is the tailable cursor created on the capped collection.

```java

    public class DBListener extends Thread {
    
        ...
    
        private DBCursor createCursor(final long pLast) {
    
            try {
                final DBCollection col = mongoDbFactory.getDb().getCollection(MONGO_DB_COLLECTION);
                final ArrayList<BasicDBObject> and = new ArrayList<>();
    
                if (pLast != 0) {
                    and.add(new BasicDBObject("timestamp", new BasicDBObject("$gt", pLast)));
                }
    
                final BasicDBObject query = new BasicDBObject("$and", and);
    
                return col.find(query)
                        .sort(new BasicDBObject("$natural", 1))
                        .addOption(Bytes.QUERYOPTION_TAILABLE)
                        .addOption(Bytes.QUERYOPTION_AWAITDATA);
    
            } catch (DataAccessException e) {
                logger.error("ERROR! {}", e.getMessage());
            }
            return null;
        }

    }

```



[DBListener.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/dbevents/DBListener.java) is managed by [DBListenerHandler.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/dbevents/DBListenerHandler.java) which itself if a Spring bean and it's operated from [ContextStartEventHandler.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/dbevents/ContextStartEventHandler.java) and [ContextStopEventHandler.java] (src/main/java/com/whiteandreetto/prototypes/simplebus/dbevents/ContextStopEventHandler.java)

Basically at Spring Context Refresh, the ContextStartEventHandler.java initiates read operations

```java
    
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
    
```

Similarly class ContextStopEventHandler.java interrupts read operations on a ContextClosedEvent

```java


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


```






 


