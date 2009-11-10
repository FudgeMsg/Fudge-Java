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

package org.fudgemsg;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamParserTest {

  @Test
  public void standardMessageAllNames() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllNames();
    checkResultsMatch(msg);
  }

  @Test
  public void standardMessageAllOrdinals() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllOrdinals();
    checkResultsMatch(msg);
  }
  
  @Test
  public void standardMessageByteArrays() {
    FudgeMsg msg = StandardFudgeMessages.createMessageAllByteArrayLengths();
    checkResultsMatch(msg);
  }
  
  @Test
  public void standardMessageSubMessages() {
    FudgeMsg msg = StandardFudgeMessages.createMessageWithSubMsgs();
    checkResultsMatch(msg);
  }
  
  /**
   * @param msg
   */
  protected void checkResultsMatch(FudgeMsg msg) {
    FudgeMsgEnvelope result = cycleMessage(new FudgeContext(), msg);
    assertNotNull(result);
    assertNotNull(result.getMessage());
    FudgeMsg resultMsg = result.getMessage();
    FudgeUtils.assertAllFieldsMatch(msg, resultMsg);
  }
  
  protected FudgeMsgEnvelope cycleMessage(FudgeContext context, FudgeMsg msg) {
    FudgeStreamParser parser = new FudgeStreamParser(context);
    byte[] msgAsBytes = context.toByteArray(msg);
    return parser.parse(new DataInputStream(new ByteArrayInputStream(msgAsBytes)));
  }
}
