package com.whiteandreetto.prototypes.simplebus.cfg;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * Created by anda on 06/08/14.
 */
@Configuration
@PropertySource("classpath:META-INF/com/whiteandreetto/prototypes/simplebus/simplebus.properties")
public class MongoDBConfiguration {

    @Value("${mongo.db}")
    private String MONGO_DB;

    @Value("${mongo.db.host}")
    private String MONGO_DB_HOST;

    @Value("${mongo.db.port}")
    private String MONGO_DB_PORT;

    @Value("${mongo.db.message.collection}")
    private String MONGO_DB_COLLECTION;

    @Value("${mongo.db.message.processed}")
    private String PARKING_LOT;

    @Value("${mongo.db.clean.at.startup}")
    private boolean CLEAN_AT_STARTUP;

    @Value("${mongo.db.message.collection.max}")
    private Long MAXCOUNT;

    @Value("${mongo.db.message.collection.size}")
    private Long SIZE;

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfiguration.class);


    /**
     * Initializes the factory and collections if required
     *
     * @return
     * @throws Exception
     */
    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {

        final SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(new MongoClient(MONGO_DB_HOST, Integer.parseInt(MONGO_DB_PORT)), MONGO_DB);


        final DB db = simpleMongoDbFactory.getDb();

        //INIT COLLECTIONS IF NOT PRESENT

        DBCollection dbCollection;


        if (CLEAN_AT_STARTUP) {

            final Set<String> collectionNames = db.getCollectionNames();

            for (String next : collectionNames) {

                final DBCollection collection = db.getCollection(next);
                if (!collection.getName().equals("system.indexes")) {

                    logger.info("DROPPING COLLECTION {}", next);

                    collection.drop();
                }
            }

        }


        if (db.collectionExists(MONGO_DB_COLLECTION)) {

            dbCollection = db.getCollection(MONGO_DB_COLLECTION);
            Assert.isTrue(dbCollection.isCapped(), "Expecting a capped Collection");

        } else {
            final DBObject options = BasicDBObjectBuilder.start()
                    .add("capped", true)
                    .add("size", SIZE)
                    .add("max", MAXCOUNT)
                    .get();

            dbCollection = db.createCollection(MONGO_DB_COLLECTION, options);

            final MongoTemplate template = new MongoTemplate(simpleMongoDbFactory);

            template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
            template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("_id", Sort.Direction.ASC));

            Assert.isTrue(dbCollection.isCapped(), "Expecting a capped Collection");

        }

        DBCollection parkingLotDbCollection;


        if (db.collectionExists(PARKING_LOT)) {
            parkingLotDbCollection = db.getCollection(PARKING_LOT);
            Assert.isTrue(!parkingLotDbCollection.isCapped(), "Not expecting a capped Collection");

        } else {
            final DBObject options = BasicDBObjectBuilder.start()
                    .add("size", SIZE)
                    .get();
            parkingLotDbCollection = db.createCollection(PARKING_LOT, options);

            final MongoTemplate template = new MongoTemplate(simpleMongoDbFactory);

            template.indexOps(PARKING_LOT).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
            template.indexOps(PARKING_LOT).ensureIndex(new Index().on("_id", Sort.Direction.ASC));

            Assert.isTrue(!parkingLotDbCollection.isCapped(), "Not expecting a capped Collection");

        }

        return simpleMongoDbFactory;
    }


}
