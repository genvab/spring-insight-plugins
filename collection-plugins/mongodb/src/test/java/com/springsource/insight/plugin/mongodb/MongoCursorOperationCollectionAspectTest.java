/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


/**
 */
public class MongoCursorOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {
    String collectionName = "test";

    DBCursor testCursor() {
        DBCollection collection = mock(DBCollection.class);
        when(collection.getFullName()).thenReturn(collectionName);
        DBObject keysObject = new BasicDBObject("key", "value");
        DBObject queryObject = new BasicDBObject("query", "value");

        DBCursor cursor = new DBCursorDummy(collection, queryObject, keysObject);

        return cursor;
    }
    public void standardAsserts(Operation op) {
        assertEquals(MongoCursorOperationCollectionAspect.TYPE, op.getType());
        assertEquals("{ \"key\" : \"value\"}", op.get("keysWanted"));
        assertEquals("{ \"query\" : \"value\"}", op.get("query"));
    }


    //execution(* DBCursor.next());
    @Test
    public void next() throws Exception {
        testCursor().next();
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.next()", op.getLabel());
        standardAsserts(op);
    }

    //execution(* DBCursor.skip());
    @Test
    public void skip() throws Exception {
        testCursor().skip(34);
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.skip()", op.getLabel());
        assertEquals("34", ((OperationList)op.get("args")).get(0));
        standardAsserts(op);
    }

    //execution(* DBCursor.limit(int));
    @Test
    public void limit() throws Exception {
        testCursor().limit(35);
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.limit()", op.getLabel());
        assertEquals("35", ((OperationList)op.get("args")).get(0));
        standardAsserts(op);
    }

    //execution(* DBCursor.toArray());
    @Test
    public void toArray() throws Exception {
        testCursor().toArray();
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.toArray()", op.getLabel());
        standardAsserts(op);
    }

    @Test
    public void toArrayInt() throws Exception {
        testCursor().toArray(43);
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.toArray()", op.getLabel());
        assertEquals("43", ((OperationList)op.get("args")).get(0));
        standardAsserts(op);
    }

    //execution(* DBCursor.sort(DBSort));
    @Test
    public void sort() throws Exception {
        testCursor().sort(new BasicDBObject("sort", "this"));
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.sort()", op.getLabel());
        assertEquals("{ \"sort\" : \"this\"}", ((OperationList)op.get("args")).get(0));
        standardAsserts(op);
    }

    //execution(* DBCursor.batchSize(int));
    @Test
    public void batchSize() throws Exception {
        testCursor().batchSize(14);
        Operation op = getLastEntered();
        assertNotNull(op);
        assertEquals("MongoDB: DBCursor.batchSize()", op.getLabel());
        assertEquals("14", ((OperationList)op.get("args")).get(0));
        standardAsserts(op);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return MongoCursorOperationCollectionAspect.aspectOf();
    }
}
