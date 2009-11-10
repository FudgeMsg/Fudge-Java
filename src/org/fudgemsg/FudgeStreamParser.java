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

import java.io.DataInput;

import org.fudgemsg.FudgeStreamReader.FudgeStreamElement;

/**
 * A parser for {@link FudgeMsg} instances which uses a {@link FudgeStreamReader}.
 *
 * @author kirk
 */
public class FudgeStreamParser {
  private final FudgeContext _fudgeContext;
  
  public FudgeStreamParser(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Must provide a fudge context.");
    }
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public FudgeMsgEnvelope parse(DataInput dataInput) {
    // TODO kirk 2009-11-10 -- This should actually get one from the context,
    // which can then pool them.
    FudgeStreamReader reader = new FudgeStreamReader(getFudgeContext(), dataInput);
    FudgeStreamElement element = reader.next();
    if(element == null) {
      return null;
    }
    if(element != FudgeStreamElement.MESSAGE_ENVELOPE) {
      throw new IllegalArgumentException("First element in encoding stream wasn't a message element.");
    }
    int version = reader.getSchemaVersion();
    FudgeMsg msg = getFudgeContext().newMessage();
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg, version);
    processFields(reader, msg);
    return envelope;
  }

  /**
   * @param reader
   * @param msg
   */
  protected void processFields(FudgeStreamReader reader, FudgeMsg msg) {
    while(reader.hasNext()) {
      FudgeStreamElement element = reader.next();
      switch(element) {
      case SIMPLE_FIELD:
        msg.add(reader.getFieldName(), reader.getFieldOrdinal(), reader.getFieldType(), reader.getFieldValue());
        break;
      case SUBMESSAGE_FIELD_START:
        FudgeMsg subMsg = getFudgeContext().newMessage();
        msg.add(reader.getFieldName(), reader.getFieldOrdinal(), subMsg);
        processFields(reader, subMsg);
        break;
      case SUBMESSAGE_FIELD_END:
        return;
      }
    }
  }

}
