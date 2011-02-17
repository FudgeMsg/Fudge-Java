/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.StandardFudgeMessages;
import org.fudgemsg.test.FudgeUtils;
import org.junit.Test;

import com.mongodb.DBObject;

/**
 * Tests bidirectional encoding/decoding of Fudge messages into MongoDB objects.
 *
 * @author Kirk Wylie
 */
public class MongoDBCodecTest {
  
  /**
   * 
   */
  @Test
  public void allNamesCodec() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllNames(FudgeContext.GLOBAL_DEFAULT);
    
    DBObject dbObject = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg(DBObject.class, inputMsg);
    FudgeFieldContainer outputMsg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(dbObject).getMessage();
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * 
   */
  @Test
  public void containsList() {
    MutableFudgeFieldContainer inputMsg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    DBObject dbObject = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg(DBObject.class, inputMsg);
    FudgeFieldContainer outputMsg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(dbObject).getMessage();
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * 
   */
  @Test
  public void byteArrays() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllByteArrayLengths(FudgeContext.GLOBAL_DEFAULT);
    
    DBObject dbObject = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg(DBObject.class, inputMsg);
    FudgeFieldContainer outputMsg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(dbObject).getMessage();
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * 
   */
  @Test
  public void subMsg() {
    MutableFudgeFieldContainer inputMsg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    MutableFudgeFieldContainer subMsg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    subMsg.add("val1", "Kirk Wylie");
    subMsg.add("val1", "Jim Moores");
    subMsg.add("val1", "Yan Tordoff");
    inputMsg.add("val2", subMsg);
    DBObject dbObject = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg(DBObject.class, inputMsg);
    FudgeFieldContainer outputMsg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(dbObject).getMessage();
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
}
