package com.whiteandreetto.prototypes.simplebus.utils;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.util.Set;

/**
 * Created by AANG on 07/08/14/14:32.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
public class DBHelper {

    public static void invoke(MongoDbFactory factory, Integer COLLECTION_SIZE, Integer COLLECTION_CAP, String MONGO_DB_COLLECTION, String PARKING_LOT) {

        final DB db = factory.getDb();


        final Set<String> collectionNames = db.getCollectionNames();

        for (String next : collectionNames) {

            final DBCollection collection = db.getCollection(next);

            if (!next.equals("system.indexes"))
                collection.drop();

        }


        if (!db.collectionExists(MONGO_DB_COLLECTION)) {

            // create a capped collection with max = 1000
            final DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("size", COLLECTION_SIZE).add("max", COLLECTION_CAP).get();

            db.createCollection(MONGO_DB_COLLECTION, options);
        }

        if (!db.collectionExists(PARKING_LOT)) {

            final DBObject options = BasicDBObjectBuilder.start().add("size", COLLECTION_SIZE).get();
            db.createCollection(PARKING_LOT, options);
        }


        MongoTemplate template = new MongoTemplate(factory);

        template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
        template.indexOps(MONGO_DB_COLLECTION).ensureIndex(new Index().on("_id", Sort.Direction.ASC));

        template.indexOps(PARKING_LOT).ensureIndex(new Index().on("timestamp", Sort.Direction.ASC));
        template.indexOps(PARKING_LOT).ensureIndex(new Index().on("_id", Sort.Direction.ASC));
    }


}
