package com.whiteandreetto.prototypes.simplebus.cfg;

import com.mongodb.*;
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

/**
 * Created by anda on 06/08/14.
 */
@Configuration
@PropertySource("classpath:META-INF/com/whiteandreetto/prototypes/simplebus/simplebus.properties")
public class MongoDBConfiguration  {


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


    @Value("${mongo.db.message.collection.max}")
    private Long MAXCOUNT;

    @Value("${mongo.db.message.collection.size}")
    private Long SIZE;


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Initializes the factory and collections if required
     * @return
     * @throws Exception
     */
    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {

        final SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(new MongoClient(MONGO_DB_HOST, Integer.parseInt(MONGO_DB_PORT)), MONGO_DB);


        final DB db = simpleMongoDbFactory.getDb();

        //INIT COLLECTIONS IF NOT PRESENT

        if (db.collectionExists(MONGO_DB_COLLECTION)) {
            final DBCollection dbCollection = db.getCollection(MONGO_DB_COLLECTION);
            Assert.isTrue(dbCollection.isCapped(), "Expecting a capped Collection");
        } else {
            final DBObject options = BasicDBObjectBuilder.start()
                    .add("capped", true)
                    .add("size", SIZE)
                    .add("max", MAXCOUNT)
                    .get();

            db.createCollection(MONGO_DB_COLLECTION, options);

            final MongoTemplate template = new MongoTemplate(simpleMongoDbFactory);

            template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
            template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("_id", Sort.Direction.ASC));

        }


        if (!db.collectionExists(PARKING_LOT)) {
            final DBObject options = BasicDBObjectBuilder.start()
                    .add("size", SIZE)
                    .get();
            db.createCollection(PARKING_LOT, options);

            final MongoTemplate template = new MongoTemplate(simpleMongoDbFactory);

            template.indexOps(PARKING_LOT).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
            template.indexOps(PARKING_LOT).ensureIndex(new Index().on("_id", Sort.Direction.ASC));

        }

        return simpleMongoDbFactory;
    }



}
