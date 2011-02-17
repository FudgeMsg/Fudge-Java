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

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * <p>The central point for Fudge message to Java Object deserialization on a given stream.
 * Note that the deserializer cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned).</p>
 * 
 * <p>The object builder framework methods all take a deserialization context so that a
 * deserializer can refer any sub-messages to this for construction if it does not have
 * sufficient information to process them directly.</p> 
 * 
 * @author Andrew Griffin
 */
public class FudgeDeserializationContext {
  
  private final FudgeContext _fudgeContext;
  private final SerializationBuffer _serialisationBuffer = new SerializationBuffer ();
  
  /**
   * Creates a new {@link FudgeDeserializationContext} for the given {@link FudgeContext}.
   * 
   * @param fudgeContext the {@code FudgeContext} to use
   */
  public FudgeDeserializationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  /**
   * Resets the buffers used for object graph logics. Calling {@code reset()} on this context
   * should match a call to {@link FudgeSerializationContext#reset()} on the context used by the serialiser
   * to keep the states of both sender and receiver consistent.
   */
  public void reset () {
    getSerialisationBuffer ().reset ();
  }
  
  private SerializationBuffer getSerialisationBuffer () {
    return _serialisationBuffer;
  }
  
  /**
   * Returns the associated {@link FudgeContext}.
   * 
   * @return the {@code FudgeContext}.
   */
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  /**
   * Converts a field value to a Java object. This may be a base Java type supported by the current {@link FudgeTypeDictionary}
   * or if it is a sub-message will be expanded through {@link #fudgeMsgToObject(FudgeFieldContainer)}.
   * 
   * @param field field to convert
   * @return the deserialized object
   */
  public Object fieldValueToObject (final FudgeField field) {
    final Object o = field.getValue ();
    if (o instanceof FudgeFieldContainer) {
      return fudgeMsgToObject ((FudgeFieldContainer)o);
    } else {
      return o;
    }
  }
  
  /**
   * Converts a field value to a Java object with a specific type. This may be a base Java type supported by the current 
   * {@link FudgeTypeDictionary} or if it is a sub-message will be expanded through {@link #fudgeMsgToObject(Class,FudgeFieldContainer)}.
   * 
   * @param <T> target Java type to decode to
   * @param clazz class of the target Java type to decode to
   * @param field value to decode
   * @return the deserialized object
   */
  public <T> T fieldValueToObject (final Class<T> clazz, final FudgeField field) {
    final Object o = field.getValue ();
    if (o instanceof FudgeFieldContainer) {
      return fudgeMsgToObject (clazz, (FudgeFieldContainer)o);
    } else {
      return getFudgeContext ().getFieldValue (clazz, field);
    }
  }
  
  /**
   * Converts a Fudge message to a best guess Java object. {@link List} and {@link Map} encodings are recognized and inflated. Any other encodings
   * require field ordinal 0 to include possible class names to use.
   * 
   * @param message message to deserialize
   * @return the Java object
   */
  public Object fudgeMsgToObject (final FudgeFieldContainer message) {
    List<FudgeField> types = message.getAllByOrdinal (0);
    if (types.size () == 0) {
      int maxOrdinal = 0;
      for (FudgeField field : message) {
        if (field.getOrdinal () == null) continue;
        if (field.getOrdinal () < 0) {
          // not a list/set/map
          return message;
        }
        if (field.getOrdinal () > maxOrdinal) maxOrdinal = field.getOrdinal ();
      }
      final Class<?> defaultClass = getFudgeContext().getObjectDictionary().getDefaultObjectClass(maxOrdinal);
      if (defaultClass != null) {
        return fudgeMsgToObject(defaultClass, message);
      }
    } else {
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          throw new UnsupportedOperationException ("Serialisation framework doesn't support back/forward references"); 
        } else if (o instanceof String) {
          try {
            FudgeObjectBuilder<?> builder = getFudgeContext ().getObjectDictionary ().getObjectBuilder (Class.forName ((String)o));
            if (builder != null) return builder.buildObject (this, message);
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      }
    }
    // couldn't process - return the raw message
    return message;
  }
  
  /**
   * Converts a Fudge message to a specific Java type. The {@link FudgeObjectDictionary} is used to identify a builder to delegate to. If
   * the message includes class names in ordinal 0, these will be tested for a valid builder and used if they will provide a subclass of
   * the requested class.
   * 
   * @param <T> target Java type to decode to
   * @param clazz class of the target Java type to decode to
   * @param message message to deserialise
   * @return the deserialised Java object
   */
  @SuppressWarnings("unchecked")
  public <T> T fudgeMsgToObject (final Class<T> clazz, final FudgeFieldContainer message) {
    FudgeObjectBuilder<T> builder;
    Exception lastError = null;
    /*if (clazz == Object.class) {
      System.out.println(message);
    }*/
    List<FudgeField> types = message.getAllByOrdinal (0);
    if (types.size () != 0) {
      // message contains type information - use it if we can
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          throw new UnsupportedOperationException ("Serialisation framework doesn't support back/forward references"); 
        } else if (o instanceof String) {
          try {
            final Class<?> possibleClazz = Class.forName ((String)o);
            // System.out.println("Trying " + possibleClazz);
            if (clazz.isAssignableFrom (possibleClazz)) {
              builder = (FudgeObjectBuilder<T>)getFudgeContext ().getObjectDictionary ().getObjectBuilder (possibleClazz);
              // System.out.println("Builder " + builder);
              if (builder != null) return builder.buildObject (this, message);
            }
          } catch (ClassNotFoundException e) {
            // ignore
          } catch (Exception e) {
            //e.printStackTrace();
            lastError = e;
          }
        }
      }
    }
    // try the requested type
    //System.out.println ("fallback to " + clazz);
    builder = getFudgeContext ().getObjectDictionary ().getObjectBuilder (clazz);
    if (builder != null) {
      try {
        return builder.buildObject (this, message);
      } catch (Exception e) {
        lastError = e;
      }
    }
    // nothing matched
    if (lastError != null) {
      throw new FudgeRuntimeException ("Don't know how to create " + clazz + " from " + message, lastError);
    } else {
      throw new IllegalArgumentException ("Don't know how to create " + clazz + " from " + message);
    }
  }
  
}