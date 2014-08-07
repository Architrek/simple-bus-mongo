package com.whiteandreetto.prototypes.simplebus.dbevents;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.whiteandreetto.prototypes.simplebus.gateway.QueueSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;

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


    private static Lock lock = new ReentrantLock();

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

        if (DBListener.lock.tryLock()) {

            try {
                final HashSet<Integer> processedMessageIds = new HashSet<>();
                long lastTimestamp = 0;
                while (running.get()) {
                    try {
                        mongoDbFactory.getDb().requestStart();
                        DBCursor cur = createCursor(lastTimestamp);
                        try {
                            while (cur.hasNext() && running.get()) {
                                final BasicDBObject doc = (BasicDBObject) cur.next();

                                try {
                                    final int id = doc.getInt("_id");

                                    logger.info("Cursor @{}", id);

                                    lastTimestamp = doc.getLong("timestamp");

                                    if (processedMessageIds.contains(id)) {
                                        logger.warn("duplicate id found: " + id);
                                    }

                                    processedMessageIds.add(id);

                                    queueSendService.process(doc);
                                    counter.incrementAndGet();

                                } catch (NullPointerException e) {

                                    logger.warn("Circular buffer restart");
                                    lastTimestamp = System.currentTimeMillis();
                                }

                                try {
                                    cur.hasNext();
                                } catch (Exception e) {
                                    // reached teh end of the circular buffer
                                    cur = createCursor(lastTimestamp);
                                }

                            }
                        } finally {
                            try {
                                if (cur != null) cur.close();
                            } catch (final Throwable t) { /* nada */ }
                            mongoDbFactory.getDb().requestDone();
                        }

                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException ie) {
                            break;
                        }
                    } catch (final Throwable t) {
                        t.printStackTrace();
                    }
                }
            } finally {

                DBListener.lock.unlock();

            }
        }

    }

    private DBCursor createCursor(final long pLast) {

        final DBCollection col = mongoDbFactory.getDb().getCollection(MONGO_DB_COLLECTION);

        if (pLast == 0) {
            return col.find().sort(new BasicDBObject("$natural", 1)).addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA);
        }

        final BasicDBObject query = new BasicDBObject("timestamp", new BasicDBObject("$gt", pLast));
        return col.find(query).sort(new BasicDBObject("$natural", 1)).addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA);
    }


}
