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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * The central point for Fudge message to Java Object serialisation on a given stream.
 * Note that the deserialiser cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned) so this will not send any.
 * 
 * @author Andrew Griffin
 */
public class FudgeSerializationContext implements FudgeMessageFactory {
  
  private final FudgeContext _fudgeContext;
  private final SerializationBuffer _serialisationBuffer = new SerializationBuffer ();
  
  /**
   * Creates a new {@link FudgeSerializationContext} for the given {@link FudgeContext}.
   * 
   * @param fudgeContext the {@code FudgeContext} to use
   */
  public FudgeSerializationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  /**
   * Resets the buffers used for object graph logics. Calling {@code reset()} on this context
   * should match a call to {@link FudgeDeserializationContext#reset()} on the context used by the deserialiser
   * to keep the states of both sender and receiver consistent.
   */
  public void reset () {
    getSerialisationBuffer ().reset ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public MutableFudgeFieldContainer newMessage () {
    return _fudgeContext.newMessage ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public MutableFudgeFieldContainer newMessage (final FudgeFieldContainer fromMessage) {
    return _fudgeContext.newMessage (fromMessage);
  }

  /**
   * Returns the associated {@link FudgeContext}.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  private SerializationBuffer getSerialisationBuffer () {
    return _serialisationBuffer;
  }
  
  /**
   * Add a Java object to a Fudge message ({@link MutableFudgeFieldContainer} instance) either natively if the associated {@link FudgeTypeDictionary}
   * recognises it, or as a sub-message using the serialization framework.
   * 
   * @param message the message to add this object to
   * @param name field name to add with, or {@code null} for none
   * @param ordinal ordinal index to add with, or {@code null} for none
   * @param object value to add
   */
  public void objectToFudgeMsg (final MutableFudgeFieldContainer message, final String name, final Integer ordinal, final Object object) {
    if (object == null) return;
    final FudgeFieldType<?> fieldType = getFudgeContext ().getTypeDictionary ().getByJavaType (object.getClass ());
    if ((fieldType != null) && !FudgeMsgFieldType.INSTANCE.equals (fieldType)) {
      // goes natively into a message
      message.add (name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      message.add (name, ordinal, FudgeMsgFieldType.INSTANCE, objectToFudgeMsg (object));
    }
  }

  /**
   * Add a Java object to a Fudge message ({@link MutableFudgeFieldContainer} instance) either natively if the associated {@link FudgeTypeDictionary}
   * recognises it, or as a sub-message using the serialization framework. If encoded as a sub-message, class header fields are added. This can make
   * deserialization easier but increases the message length.
   * 
   * @param message the message to add this object to
   * @param name field name to add with, or {@code null} for none
   * @param ordinal ordinal index to add with, or {@code null} for none
   * @param object value to add
   */
  public void objectToFudgeMsgWithClassHeaders(final MutableFudgeFieldContainer message, final String name,
      final Integer ordinal, final Object object) {
    objectToFudgeMsgWithClassHeaders(message, name, ordinal, object, Object.class);
  }

  /**
   * Add a Java object to a Fudge message ({@link MutableFudgeFieldContainer} instance) either natively if the associated {@link FudgeTypeDictionary}
   * recognises it, or as a sub-message using the serialization framework. If encoded as a sub-message, class header fields are added. This can make
   * deserialization easier but increases the message length. It is assumed that the deserializer will already know the target class by other means, so
   * the message payload may end up being smaller than with {@link #objectToFudgeMsgWithClassHeaders(MutableFudgeFieldContainer,String,Integer,Object)}.
   * 
   * @param message the message to add this object to
   * @param name field name to add with, or {@code null} for none
   * @param ordinal ordinal index to add with, or {@code null} for none
   * @param object value to add
   * @param receiverTarget the Java class the receiver will expect
   */
  public void objectToFudgeMsgWithClassHeaders(final MutableFudgeFieldContainer message, final String name,
      final Integer ordinal, final Object object, final Class<?> receiverTarget) {
    if (object == null) {
      return;
    }
    final Class<?> clazz = object.getClass();
    final FudgeFieldType<?> fieldType = getFudgeContext().getTypeDictionary().getByJavaType(clazz);
    if ((fieldType != null) && !FudgeMsgFieldType.INSTANCE.equals(fieldType)) {
      // goes natively into a message
      message.add(name, ordinal, fieldType, object);
    } else {
      // look up a custom or default builder and embed as sub-message
      final MutableFudgeFieldContainer submsg = objectToFudgeMsg(object);
      if (!getFudgeContext().getObjectDictionary().isDefaultObject(clazz)) {
        if (submsg.getByOrdinal(0) == null) {
          addClassHeader(submsg, clazz, receiverTarget);
        }
      }
      message.add(name, ordinal, FudgeMsgFieldType.INSTANCE, submsg);
    }
  }

  /**
   * Converts a Java object to a Fudge message {@link MutableFudgeFieldContainer} instance using a {@link FudgeMessageBuilder} registered against the object's class
   * in the current {@link FudgeObjectDictionary}. Note that a mutable container is returned (from the definition of {@code FudgeMessageBuilder} so that the caller is
   * able to append additional data to the message if required, e.g. {@link #addClassHeader(MutableFudgeFieldContainer,Class)}.
   * 
   * @param object the Java object to serialize
   * @return the Fudge message created
   */
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

  /**
   * Adds class names to a message with ordinal 0 for use by a deserializer. The preferred class name is written first, followed by subsequent super-classes that may
   * be acceptable if the deserializer doesn't recognize them.
   * 
   * @param message the message to add the fields to
   * @param clazz the Java class to add type data for
   * @return message the modified message (allows this to be used inline)
   */
  public static MutableFudgeFieldContainer addClassHeader(final MutableFudgeFieldContainer message, Class<?> clazz) {
    while ((clazz != null) && (clazz != Object.class)) {
      message.add (null, 0, StringFieldType.INSTANCE, clazz.getName ());
      clazz = clazz.getSuperclass ();
    }
    return message;
  }

  /**
   * Adds partial class names to a message with ordinal 0 for use by a deserializer. The preferred class name is written first, followed by subsequent super-classes
   * that may be acceptable. It is assumed that the deserializer will already know the target class by other means, so the message payload ends up being smaller
   * than with {@link #addClassHeader(MutableFudgeFieldContainer,Class)}.
   * 
   * @param message the message to add the fields to
   * @param clazz the Java class to add type data for
   * @param receiverTarget the Java class the receiver will expect
   * @return message the modified message (allows this to be used inline)
   */
  public static MutableFudgeFieldContainer addClassHeader(final MutableFudgeFieldContainer message, Class<?> clazz,
      Class<?> receiverTarget) {
    while ((clazz != null) && receiverTarget.isAssignableFrom(clazz) && (receiverTarget != clazz)) {
      message.add(null, 0, StringFieldType.INSTANCE, clazz.getName());
      clazz = clazz.getSuperclass();
    }
    return message;
  }
  
}