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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeUtils;
import org.fudgemsg.StandardFudgeMessages;
import org.junit.Test;

import com.mongodb.DBObject;

/**
 * Tests bidirectional encoding/decoding of Fudge messages into MongoDB objects.
 *
 * @author kirk
 */
public class MongoDBCodecTest {
  private static final FudgeContext s_fudgeContext = new FudgeContext();

  @Test
  public void allNamesCodec() {
    FudgeMsg inputMsg = StandardFudgeMessages.createMessageAllNames();
    
    DBObject dbObject = MongoDBEncoder.encode(inputMsg);
    FudgeMsg outputMsg = MongoDBDecoder.decode(dbObject);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void containsList() {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    DBObject dbObject = MongoDBEncoder.encode(inputMsg);
    FudgeMsg outputMsg = MongoDBDecoder.decode(dbObject);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void byteArrays() {
    FudgeMsg inputMsg = StandardFudgeMessages.createMessageAllByteArrayLengths();
    
    DBObject dbObject = MongoDBEncoder.encode(inputMsg);
    FudgeMsg outputMsg = MongoDBDecoder.decode(dbObject);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  @Test
  public void subMsg() {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("val1", "Kirk Wylie");
    inputMsg.add("val1", "Jim Moores");
    inputMsg.add("val1", "Yan Tordoff");
    FudgeMsg subMsg = s_fudgeContext.newMessage();
    subMsg.add("val1", "Kirk Wylie");
    subMsg.add("val1", "Jim Moores");
    subMsg.add("val1", "Yan Tordoff");
    inputMsg.add("val2", subMsg);
    DBObject dbObject = MongoDBEncoder.encode(inputMsg);
    FudgeMsg outputMsg = MongoDBDecoder.decode(dbObject);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

}
