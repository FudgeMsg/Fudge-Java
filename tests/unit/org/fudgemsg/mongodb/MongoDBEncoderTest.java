/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fudgemsg.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.junit.Test;

import com.mongodb.DBObject;

/**
 * @author kirk
 */
public class MongoDBEncoderTest {
  
  @Test
  public void subMsgEncoding() {
    FudgeMsg msg = new FudgeMsg();
    msg.add("val1", 293836);
    msg.add("val2", "Kirk Wylie");
    FudgeMsg subMsg = new FudgeMsg();
    subMsg.add("val1", "MongoDB");
    msg.add("val3", subMsg);
    
    DBObject dbObject = MongoDBEncoder.encode(msg);
    System.out.println("MongoDBEncoderTest.subMsgEncoding produced " + dbObject);
    assertNotNull(dbObject);
    assertEquals(293836, dbObject.get("val1"));
    assertEquals("Kirk Wylie", dbObject.get("val2"));
    assertTrue(dbObject.get("val3") instanceof DBObject);
    dbObject = (DBObject) dbObject.get("val3");
    assertEquals("MongoDB", dbObject.get("val1"));
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void repeatedValueEncoding() {
    FudgeMsg msg = new FudgeMsg();
    msg.add("val1", 293836);
    msg.add("val1", "Kirk Wylie");
    
    DBObject dbObject = MongoDBEncoder.encode(msg);
    System.out.println("MongoDBEncoderTest.repeatedValueEncoding produced " + dbObject);
    assertNotNull(dbObject);
    assertTrue(dbObject.get("val1") instanceof List);
    List l = (List) dbObject.get("val1");
    assertEquals(2, l.size());
    assertEquals(new Integer(293836), l.get(0));
    assertEquals("Kirk Wylie", l.get(1));
  }

}
