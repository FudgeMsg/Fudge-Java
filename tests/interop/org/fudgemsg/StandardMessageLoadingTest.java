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

import java.io.IOException;
import java.io.InputStream;

import org.fudgemsg.test.FudgeUtils;
import org.junit.Test;

/**
 * Checks that we can load all the files that correspond to standard messages
 * and that they match up.
 *
 * @author Kirk Wylie
 */
public class StandardMessageLoadingTest {
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  /**
   * 
   */
  @Test
  public void allNames() {
    testFile(StandardFudgeMessages.createMessageAllNames(s_fudgeContext), "allNames.dat");
  }
  
  /**
   * 
   */
  @Test
  public void allOrdinals() {
    testFile(StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext), "allOrdinals.dat");
  }
  
  /**
   * 
   */
  @Test
  public void subMsg() {
    testFile(StandardFudgeMessages.createMessageWithSubMsgs(s_fudgeContext), "subMsg.dat");
  }
  
  /**
   * 
   */
  @Test
  public void fixedWidthByteArrays() {
    testFile(FudgeInteropTest.createFixedWidthByteArrayMsg(s_fudgeContext), "fixedWidthByteArrays.dat");
  }
  
  /**
   * 
   */
  @Test
  public void variableWidthColumnSizes() {
    testFile(FudgeInteropTest.createVariableWidthColumnSizes(s_fudgeContext), "variableWidthColumnSizes.dat");
  }

  /**
   * 
   */
  @Test
  public void unknown() {
    testFile(FudgeInteropTest.createUnknown(s_fudgeContext), "unknown.dat");
  }
  
  /**
   * 
   */
  @Test
  public void dateTimes () {
    testFile (FudgeInteropTest.createDateTimes (s_fudgeContext), "dateTimes.dat");
  }
  
  /**
   * @param expected [documentation not available]
   * @param fileName [documentation not available]
   */
  protected static void testFile(FudgeFieldContainer expected, String fileName) {
    FudgeMsgEnvelope envelope = loadMessage(s_fudgeContext, fileName);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage ());
    FudgeFieldContainer actual = envelope.getMessage ();
    FudgeUtils.assertAllFieldsMatch(expected, actual);
  }
  
  /**
   * @param context [documentation not available]
   * @param fileName [documentation not available]
   * @return [documentation not available]
   */
  protected static FudgeMsgEnvelope loadMessage(FudgeContext context, String fileName) {
    InputStream is = StandardMessageLoadingTest.class.getResourceAsStream(fileName);
    FudgeMsgReader reader = context.createMessageReader (is);
    FudgeMsgEnvelope envelope = reader.nextMessageEnvelope ();
    reader.close ();
    return envelope;
  }

}
