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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Builder for Map objects.
 * 
 * @author Andrew
 */
/* package */ class MapBuilder implements FudgeBuilder<Map<?,?>> {
  
  /* package */ static final MapBuilder INSTANCE = new MapBuilder (); 
  
  private MapBuilder () {
  }

  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerialisationContext context, Map<?,?> map) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (Map.Entry<?,?> entry : map.entrySet ()) {
      context.objectToFudgeMsg (msg, null, 1, entry.getKey ());
      context.objectToFudgeMsg (msg, null, 2, entry.getValue ());
    }
    return msg;
  }
  
  @Override
  public Map<?,?> buildObject (FudgeDeserialisationContext context, FudgeFieldContainer message) {
    final Map<Object, Object> map = new HashMap<Object, Object> ();
    final Queue<Object> keys = new LinkedList<Object> ();
    final Queue<Object> values = new LinkedList<Object> ();
    for (FudgeField field : message) {
      if (field.getOrdinal () == 1) {
        final Object fieldValue = context.fieldValueToObject (field);
        if (values.isEmpty ()) {
          // no values ready, so store the key till next time
          keys.add (fieldValue);
        } else {
          // store key along with next value
          map.put (fieldValue, values.remove ());
        }
      } else if (field.getOrdinal () == 2) {
        final Object fieldValue = context.fieldValueToObject (field);
        if (keys.isEmpty ()) {
          // no keys ready, so store the value till next time
          values.add (fieldValue);
        } else {
          // store value along with next key
          map.put (keys.remove (), fieldValue);
        }
      } else {
        throw new FudgeRuntimeException ("Sub-message doesn't contain a map (bad field " + field + ")");
      }
    }
    return map;
  }

}