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

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;

/**
 * Builder for List objects.
 * 
 * @author Andrew Griffin
 */
/* package */ class ListBuilder implements FudgeBuilder<List<?>> {
  
  /**
   * Singleton instance of the {@link ListBuilder}.
   */
  /* package */ static final FudgeBuilder<List<?>> INSTANCE = new ListBuilder (); 
  
  private ListBuilder () {
  }

  /**
   * Creates a Fudge message representation of a {@link List}.
   * 
   * @param context the serialization context
   * @param list the list to serialize
   * @return the Fudge message
   */
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, List<?> list) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (Object entry : list) {
      if (entry == null) {
        msg.add (null, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, null, entry);
      }
    }
    return msg;
  }
  
  /**
   * Creates a list from a Fudge message.
   * 
   * @param context the deserialization context
   * @param message the Fudge message
   * @return the {@link List}
   */
  @Override
  public List<?> buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final List<Object> list = new ArrayList<Object> ();
    for (FudgeField field : message) {
      if ((field.getOrdinal () != null) && (field.getOrdinal () != 1)) throw new IllegalArgumentException ("Sub-message doesn't contain a list (bad field " + field + ")");
      Object o = context.fieldValueToObject (field);
      list.add ((o instanceof IndicatorType) ? null : o);
    }
    return list;
  }

}