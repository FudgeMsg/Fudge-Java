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

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;

/**
 * The central point for Fudge message to Java Object serialisation on a given stream.
 * Note that the deserialiser cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned) so this will not send any.
 * 
 * @author Andrew
 */
public class FudgeSerialisationContext {
  
  private final FudgeContext _fudgeContext;
  private final SerialisationBuffer _buffer = new SerialisationBuffer ();
  
  public FudgeSerialisationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public void reset () {
    _buffer.reset ();
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  public FudgeMsg newMessage () {
    return _fudgeContext.newMessage ();
  }
  
  private boolean nullOrPreviousObject (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    if (object == null) return true;
    final int index = _buffer.findObject (object);
    if (index >= 0) {
      final FudgeMsg msg = newMessage ();
      msg.add (null, null, PrimitiveFieldTypes.INT_TYPE, index);
      message.add (name, ordinal, msg);
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public void objectToFudgeMsg (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    if (nullOrPreviousObject (message, name, ordinal, object)) return;
    final FudgeFieldType fieldType = getFudgeContext ().getTypeDictionary ().getByJavaType (object.getClass ());
    if (fieldType != null) {
      // goes natively into a message
      message.add (name, ordinal, fieldType, object);
    } else if (object instanceof Map<?,?>) {
      // goes in with Map logic
      mapToFudgeMsg (message, name, ordinal, (Map<?,?>)object);
    } else if (object instanceof List<?>) {
      // goes in with List logic
      listToFudgeMsg (message, name, ordinal, (List<?>)object);
    } else {
      // look up a custom or default builder and embed as sub-message
      final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (object);
      try {
        final FudgeMsg msg = getFudgeContext ().getObjectDictionary ().getMessageBuilder ((Class<Object>)object.getClass ()).buildMessage (this, object);
        // TODO 2010-01-19 Andrew -- add the class hierarchy information
        message.add (name, ordinal, FudgeMsgFieldType.INSTANCE, msg);
      } finally {
        bufferEntry.endObject (object);
      }
    }
  }
  
  public <K,V> void mapToFudgeMsg (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Map<K,V> map) {
    if (nullOrPreviousObject (message, name, ordinal, map)) return;
    final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (map);
    try {
      final FudgeMsg msg = newMessage ();
      for (Map.Entry<K,V> entry : map.entrySet ()) {
        objectToFudgeMsg (msg, null, 1, entry.getKey ());
        objectToFudgeMsg (msg, null, 2, entry.getValue ());
      }
      message.add (name, ordinal, FudgeMsgFieldType.INSTANCE, msg);
    } finally {
      bufferEntry.endObject (map);
    }
  }
  
  public <E> void listToFudgeMsg (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final List<E> list) {
    if (nullOrPreviousObject (message, name, ordinal, list)) return;
    final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (list);
    try {
      final FudgeMsg msg = newMessage ();
      for (E entry : list) {
        objectToFudgeMsg (msg, null, null, entry);
      }
      message.add (name, ordinal, FudgeMsgFieldType.INSTANCE, msg);
    } finally {
      bufferEntry.endObject (list);
    }
  }
  
}