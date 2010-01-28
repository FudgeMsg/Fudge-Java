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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * The central point for Fudge message to Java Object serialisation on a given stream.
 * Note that the deserialiser cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned) so this will not send any.
 * 
 * @author Andrew
 */
public class FudgeSerialisationContext implements FudgeMessageFactory {
  
  private final FudgeContext _fudgeContext;
  private final SerialisationBuffer _serialisationBuffer = new SerialisationBuffer ();
  
  public FudgeSerialisationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public void reset () {
    getSerialisationBuffer ().reset ();
  }
  
  @Override
  public MutableFudgeFieldContainer newMessage () {
    return _fudgeContext.newMessage ();
  }
  
  @Override
  public MutableFudgeFieldContainer newMessage (final FudgeFieldContainer fromMessage) {
    return _fudgeContext.newMessage (fromMessage);
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  private SerialisationBuffer getSerialisationBuffer () {
    return _serialisationBuffer;
  }
  
  public void objectToFudgeMsg (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    if (object == null) return;
    final FudgeFieldType<?> fieldType = getFudgeContext ().getTypeDictionary ().getByJavaType (object.getClass ());
    if (fieldType != null) {
      // goes natively into a message
      message.add (name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      message.add (name, ordinal, FudgeMsgFieldType.INSTANCE, objectToFudgeMsg (object));
    }
  }
  
  @SuppressWarnings("unchecked")
  public MutableFudgeFieldContainer objectToFudgeMsg (final Object object) {
    if (object == null) throw new NullPointerException ("object cannot be null");
    getSerialisationBuffer ().beginObject (object);
    try {
      Class<?> clazz = object.getClass ();
      return getFudgeContext ().getObjectDictionary ().getMessageBuilder ((Class<Object>)clazz).buildMessage (this, object);
    } finally {
      getSerialisationBuffer ().endObject (object);
    }
  }
  
  public void addClassHeader (final MutableFudgeFieldContainer message, Class<?> clazz) {
    while ((clazz != null) && (clazz != Object.class)) {
      message.add (null, 0, StringFieldType.INSTANCE, clazz.getName ());
      clazz = clazz.getSuperclass ();
    }
  }
  
}