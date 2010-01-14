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

package org.fudgemsg.mapping;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeStreamWriter;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectStreamWriter {
  
  public void write(Object obj, FudgeStreamWriter writer) {
    ObjectDescriptor descriptor = FudgeObjectDescriptors.INSTANCE.getDescriptor(obj.getClass());
    assert descriptor != null;
    // REVIEW kirk 2009-11-12 -- This is the worst implementation evar.
    // However, in the absence of being able to calculate the size of the message without
    // constructing a FudgeMsg, this is how we've got to do it.
    FudgeMsg objectAsMsg = FudgeObjectMessageFactory.serializeToMessage(obj, writer.getFudgeContext());
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(objectAsMsg);
    writer.writeMessageEnvelope(envelope, 0);
  }

}
