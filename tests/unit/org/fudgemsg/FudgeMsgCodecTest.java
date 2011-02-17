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
package org.fudgemsg;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Random;

import org.fudgemsg.test.FudgeUtils;
import org.junit.Test;

/**
 * A test class that will encode and decode a number of different Fudge messages
 * to test that encoding and decoding works properly.
 *
 * @author Kirk Wylie
 */
public class FudgeMsgCodecTest {
  private final Random _random = new Random();
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  /**
   * 
   */
  @Test
  public void allNames() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg);
    assertNotNull(outputMsg);
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * 
   */
  @Test
  public void variableWidthColumnSizes() {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("100", new byte[100]);
    inputMsg.add("1000", new byte[1000]);
    inputMsg.add("10000", new byte[100000]);
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg);
    assertNotNull(outputMsg);
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * 
   */
  @Test
  public void subMsg() {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageWithSubMsgs(s_fudgeContext);
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg);
    assertNotNull(outputMsg);
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   *
   */
  @Test
  public void unknown() {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("unknown", new UnknownFudgeFieldValue(new byte[10], s_fudgeContext.getTypeDictionary ().getUnknownType(200)));
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg);
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * @param length [documentation not available]
   * @return [documentation not available]
   */
  protected byte[] createRandomArray(int length) {
    byte[] bytes = new byte[length];
    _random.nextBytes(bytes);
    return bytes;
  }

  /**
   * 
   */
  @Test
  public void fixedWidthByteArrays() {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("byte[4]", createRandomArray(4));
    inputMsg.add("byte[8]", createRandomArray(8));
    inputMsg.add("byte[16]", createRandomArray(16));
    inputMsg.add("byte[20]", createRandomArray(20));
    inputMsg.add("byte[32]", createRandomArray(32));
    inputMsg.add("byte[64]", createRandomArray(64));
    inputMsg.add("byte[128]", createRandomArray(128));
    inputMsg.add("byte[256]", createRandomArray(256));
    inputMsg.add("byte[512]", createRandomArray(512));
    
    inputMsg.add("byte[28]", createRandomArray(28));
    
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg);
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * @param msg [documentation not available]
   * @return [documentation not available]
   */
  protected FudgeFieldContainer cycleMessage(FudgeFieldContainer msg) {
    byte[] content = s_fudgeContext.toByteArray(msg);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(content);
    DataInputStream dis = new DataInputStream(bais);
    FudgeMsgEnvelope outputMsgEnvelope = s_fudgeContext.deserialize(dis);
    assertNotNull(outputMsgEnvelope);
    assertNotNull(outputMsgEnvelope.getMessage ());
    return outputMsgEnvelope.getMessage ();
  }

}
