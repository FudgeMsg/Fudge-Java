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

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;

/**
 * Builder for Set objects.
 * 
 * @author Andrew Griffin
 */
/* package */ class SetBuilder implements FudgeBuilder<Set<?>> {
  
  /**
   * Singleton instance of the {@link SetBuilder}.
   */
  /* package */ static final FudgeBuilder<Set<?>> INSTANCE = new SetBuilder (); 
  
  private SetBuilder () {
  }

  /**
   * Creates a Fudge message representation of a {@link Set}.
   * 
   * @param context the serialization context
   * @param set the set to serialize
   * @return the Fudge message
   */
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, Set<?> set) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (Object entry : set) {
      if (entry == null) {
        msg.add (null, 1, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, 1, entry);
      }
    }
    return msg;
  }
  
  /**
   * Creates a {@link Set} from a Fudge message.
   * 
   * @param context the deserialization context
   * @param message the Fudge message
   * @return the {@code Set} 
   */
  @Override
  public Set<?> buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final Set<Object> set = new HashSet<Object> ();
    for (FudgeField field : message) {
      Object fieldValue = context.fieldValueToObject (field);
      if (fieldValue instanceof IndicatorType) fieldValue = null;
      if (field.getOrdinal () == 1) {
        set.add (fieldValue);
      } else {
        throw new IllegalArgumentException ("Sub-message doesn't contain a set (bad field " + field + ")");
      }
    }
    return set;
  }

}