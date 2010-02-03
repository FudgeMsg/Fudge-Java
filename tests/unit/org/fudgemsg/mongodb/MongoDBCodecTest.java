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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeUtils;
import org.fudgemsg.StandardFudgeMessages;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeObjectMessageFactory;
import org.junit.Test;

import com.mongodb.DBObject;

/**
 * Tests bidirectional encoding/decoding of Fudge messages into MongoDB objects.
 *
 * @author kirk
 */
public class MongoDBCodecTest {
  
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  static {
    MongoDBFudgeBuilder.register (s_fudgeContext);
  }

  @Test
  public void allNamesCodec() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    
    DBObject dbObject = FudgeObjectMessageFactory.deserializeToObject(DBObject.class, inputMsg, s_fudgeContext);
    FudgeFieldContainer outputMsg = FudgeObjectMessageFactory.serializeToMessage(dbObject, s_fudgeContext);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void containsList() {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    DBObject dbObject = FudgeObjectMessageFactory.deserializeToObject(DBObject.class, inputMsg, s_fudgeContext);
    FudgeFieldContainer outputMsg = FudgeObjectMessageFactory.serializeToMessage(dbObject, s_fudgeContext);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void byteArrays() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllByteArrayLengths(s_fudgeContext);
    
    DBObject dbObject = FudgeObjectMessageFactory.deserializeToObject(DBObject.class, inputMsg, s_fudgeContext);
    FudgeFieldContainer outputMsg = FudgeObjectMessageFactory.serializeToMessage(dbObject, s_fudgeContext);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void subMsg() {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    MutableFudgeFieldContainer subMsg = s_fudgeContext.newMessage();
    subMsg.add("val1", "Kirk Wylie");
    subMsg.add("val1", "Jim Moores");
    subMsg.add("val1", "Yan Tordoff");
    inputMsg.add("val2", subMsg);
    DBObject dbObject = FudgeObjectMessageFactory.deserializeToObject(DBObject.class, inputMsg, s_fudgeContext);
    FudgeFieldContainer outputMsg = FudgeObjectMessageFactory.serializeToMessage(dbObject, s_fudgeContext);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

}
