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

import org.fudgemsg.test.FudgeUtils;
import org.junit.Test;

/**
 * 
 *
 * @author Kirk Wylie
 */
public class FudgeStreamParserTest {
  private static final FudgeContext s_fudgeContext = new FudgeContext();

  /**
   * 
   */
  @Test
  public void standardMessageAllNames() {
    FudgeFieldContainer msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    checkResultsMatch(msg);
  }

  /**
   * 
   */
  @Test
  public void standardMessageAllOrdinals() {
    FudgeFieldContainer msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    checkResultsMatch(msg);
  }
  
  /**
   * 
   */
  @Test
  public void standardMessageByteArrays() {
    FudgeFieldContainer msg = StandardFudgeMessages.createMessageAllByteArrayLengths(s_fudgeContext);
    checkResultsMatch(msg);
  }
  
  /**
   * 
   */
  @Test
  public void standardMessageSubMessages() {
    FudgeFieldContainer msg = StandardFudgeMessages.createMessageWithSubMsgs(s_fudgeContext);
    checkResultsMatch(msg);
  }
  
  /**
   * 
   */
  @Test
  public void allMessagesSameContext() {
    FudgeContext fudgeContext = new FudgeContext();
    FudgeFieldContainer msg = null;
    msg = StandardFudgeMessages.createMessageAllNames(fudgeContext);
    checkResultsMatch(msg, fudgeContext);
    msg = StandardFudgeMessages.createMessageAllOrdinals(fudgeContext);
    checkResultsMatch(msg, fudgeContext);
    msg = StandardFudgeMessages.createMessageAllByteArrayLengths(fudgeContext);
    checkResultsMatch(msg, fudgeContext);
    msg = StandardFudgeMessages.createMessageWithSubMsgs(fudgeContext);
    checkResultsMatch(msg, fudgeContext);
  }
  
  /**
   * @param msg [documentation not available]
   */
  protected void checkResultsMatch(FudgeFieldContainer msg) {
    checkResultsMatch(msg, new FudgeContext());
  }
  
  /**
   * @param msg [documentation not available]
   * @param fudgeContext [documentation not available]
   */
  protected void checkResultsMatch(FudgeFieldContainer msg, FudgeContext fudgeContext) {
    FudgeMsgEnvelope result = cycleMessage(fudgeContext, msg);
    assertNotNull(result);
    assertNotNull(result.getMessage ());
    FudgeFieldContainer resultMsg = result.getMessage ();
    FudgeUtils.assertAllFieldsMatch(msg, resultMsg);
  }
  
  /**
   * @param context [documentation not available]
   * @param msg [documentation not available]
   * @return [documentation not available]
   */
  protected FudgeMsgEnvelope cycleMessage(FudgeContext context, FudgeFieldContainer msg) {
    byte[] msgAsBytes = context.toByteArray(msg);
    final FudgeMsgReader reader = context.createMessageReader (new ByteArrayInputStream(msgAsBytes));
    return reader.nextMessageEnvelope ();
  }
}
