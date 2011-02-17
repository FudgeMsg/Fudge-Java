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

package org.fudgemsg.mapping;

import java.util.Iterator;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Builder wrapper for objects that are already Fudge messages. The FudgeFieldContainer class name is added
 * so that the serialization framework will decode the messages as messages and not as serialized objects.
 * 
 * @author Andrew Griffin
 */
/* package */ class FudgeFieldContainerBuilder implements FudgeBuilder<FudgeFieldContainer> {
  
  /**
   * 
   */
  /* package */ static final FudgeBuilder<FudgeFieldContainer> INSTANCE = new FudgeFieldContainerBuilder (); 
  
  private FudgeFieldContainerBuilder () {
  }

  /**
   *
   */
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, FudgeFieldContainer fields) {
    final MutableFudgeFieldContainer msg = context.newMessage (fields);
    // add the interface name
    msg.add (null, 0, FudgeFieldContainer.class.getName ());
    return msg;
  }
  
  /**
   *
   */
  @Override
  public FudgeFieldContainer buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final MutableFudgeFieldContainer msg = context.getFudgeContext ().newMessage (message);
    // remove the class name(s) if added
    final Short ordinal = 0;
    final Iterator<FudgeField> fields = msg.iterator ();
    while (fields.hasNext ()) {
      final FudgeField field = fields.next ();
      if (ordinal.equals(field.getOrdinal()) && (field.getName() == null)) {
        fields.remove ();
        break;
      }
    }
    return msg;
  }

}