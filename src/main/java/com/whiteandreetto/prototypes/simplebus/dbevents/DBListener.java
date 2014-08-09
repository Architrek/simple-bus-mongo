package com.whiteandreetto.prototypes.simplebus.dbevents;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.whiteandreetto.prototypes.simplebus.gateway.QueueSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoDbFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by AANG on 07/08/14/10:49.
 * <p/>
 * Perfection is unachievable, I'm all right with Excellence
 * <p/>
 * (c) White Andreetto Consulting 2014 All rights reserved
 */
public class DBListener extends Thread {


    private static final Lock REENTRANT_LOCK = new ReentrantLock();

    private static final Logger logger = LoggerFactory.getLogger(DBListener.class);

    private AtomicBoolean running;
    private AtomicLong counter;
    private QueueSendService queueSendService;


    private String MONGO_DB_COLLECTION;


    private MongoDbFactory mongoDbFactory;

    public DBListener(QueueSendService queueSendService, MongoDbFactory mongoDbFactory, String collection, AtomicBoolean running, AtomicLong counter) {

        this.queueSendService = queueSendService;
        this.mongoDbFactory = mongoDbFactory;
        this.MONGO_DB_COLLECTION = collection;
        this.running = running;
        this.counter = counter;
    }


    @Override
    public void run() {

        logger.info("Reentrant Lock {}", DBListener.REENTRANT_LOCK.tryLock());

        if (DBListener.REENTRANT_LOCK.tryLock()) {

            try {
                final HashSet<Integer> processedMessageIds = new HashSet<>();
                long lastTimestamp = 0;
                while (running.get()) {
                    try {
                        mongoDbFactory.getDb().requestStart();
                        DBCursor cursor = createCursor(lastTimestamp);
                        if (cursor != null)
                            try {
                                // keep reading the cursor, it's live
                                while (cursor.hasNext() && running.get()) {

                                    final BasicDBObject messageFromBuffer = (BasicDBObject) cursor.next();

                                    try {
                                        final int id = messageFromBuffer.getInt("_id");

                                        logger.info("Cursor @{}", id);

                                        lastTimestamp = messageFromBuffer.getLong("timestamp");


                                        if (processedMessageIds.contains(id)) {
                                            logger.warn("Duplicate id found: " + id);
                                        }

                                        processedMessageIds.add(id);
                                        queueSendService.process(messageFromBuffer);
                                        counter.incrementAndGet();

                                    } catch (NullPointerException e) {
                                        logger.warn("Circular buffer restart");
                                        // shit happens, start reading message more recent than now
                                        lastTimestamp = System.currentTimeMillis();
                                    }


                                }
                            } finally {
                                try {
                                    cursor.close();
                                    // NEED DB NO MORE
                                    mongoDbFactory.getDb().requestDone();
                                } catch (final Throwable t) {
                                    logger.error("ERROR! {}", t.getMessage());
                                }
                            }
                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException ie) {
                            logger.error("ERROR! {}", ie.getMessage());
                            break;
                        }
                    } catch (final Throwable t) {
                        logger.error("ERROR! {}", t.getMessage());
                    }
                }
            } finally {
                // NEED SERVICE NO MORE, RELEASE LOCK
                queueSendService = null;
                DBListener.REENTRANT_LOCK.unlock();
            }
        }
        logger.info("Run, completed");
    }

    private DBCursor createCursor(final long pLast) {

        try {
            final DBCollection col = mongoDbFactory.getDb().getCollection(MONGO_DB_COLLECTION);
            final ArrayList<BasicDBObject> and = new ArrayList<>();

            and.add(new BasicDBObject(Bytes.QUERYOPTION_TAILABLE));
            and.add(new BasicDBObject(Bytes.QUERYOPTION_AWAITDATA));

            if (pLast != 0) {
                and.add(new BasicDBObject("timestamp", new BasicDBObject("$gt", pLast)));
            }

            final BasicDBObject query = new BasicDBObject("$and", and);

            return col.find(query)
                    .sort(new BasicDBObject("$natural", 1));

        } catch (DataAccessException e) {
            logger.error("ERROR! {}", e.getMessage());
        }
        return null;
    }


}
