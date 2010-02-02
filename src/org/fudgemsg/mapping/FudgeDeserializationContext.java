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
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.FudgeRuntimeException;

/**
 * The central point for Fudge message to Java Object deserialisation on a given stream.
 * Note that the deserialiser cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned).
 * 
 * The object builder framework methods all take a deserialisation context so that a
 * deserialiser can refer any sub-messages to this for construction if it does not have
 * sufficient information to process them directly. 
 * 
 * @author Andrew
 */
public class FudgeDeserializationContext {
  
  private final FudgeContext _fudgeContext;
  private final SerialisationBuffer _serialisationBuffer = new SerialisationBuffer ();
  
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
  
  private SerialisationBuffer getSerialisationBuffer () {
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
   * @return the deserialised object
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
   * @return the deserialised object
   */
  @SuppressWarnings("unchecked")
  public <T> T fieldValueToObject (final Class<T> clazz, final FudgeField field) {
    final Object o = field.getValue ();
    if (o instanceof FudgeFieldContainer) {
      return fudgeMsgToObject (clazz, (FudgeFieldContainer)o);
    } else {
      // TODO 2010-01-19 Andrew -- the cast below isn't good; should do more sensible conversion from the standard fudge types or raise an error
      return (T)o;
    }
  }
  
  /**
   * Converts a Fudge message to a best guess Java object. {@link List} and {@link Map} encodings are recognised and inflated. Any other encodings
   * require field ordinal 0 to include possible class names to use.
   * 
   * @param message message to deserialise
   * @return the Java object
   */
  public Object fudgeMsgToObject (final FudgeFieldContainer message) {
    List<FudgeField> types = message.getAllByOrdinal (0);
    if (types.size () == 0) {
      int maxOrdinal = 0;
      for (FudgeField field : message) {
        if (field.getOrdinal () == null) continue;
        if (field.getOrdinal () > maxOrdinal) maxOrdinal = field.getOrdinal ();
      }
      if (maxOrdinal <= 1) {
        return fudgeMsgToObject (List.class, message);
      } else if (maxOrdinal == 2) {
        return fudgeMsgToObject (Map.class, message);
      }
    } else {
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          throw new FudgeRuntimeException ("Serialisation framework doesn't support back/forward references"); 
        } else if (o instanceof String) {
          try {
            final Class<?> clazz = Class.forName ((String)o);
            return fudgeMsgToObject (clazz, message);
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      }
    }
    // can't process - something else will raise an error if we just return the original message
    return message;
  }
  
  /**
   * Converts a Fudge message to a specific Java type. The {@link FudgeObjectDictionary} is used to identify a builder to delegate to. If
   * a builder is not available and the message includes class names in ordinal 0, these will be tested for a valid builder.
   * 
   * @param <T> target Java type to decode to
   * @param clazz class of the target Java type to decode to
   * @param message message to deserialise
   * @return the deserialised Java object
   */
  @SuppressWarnings("unchecked")
  public <T> T fudgeMsgToObject (final Class<T> clazz, final FudgeFieldContainer message) {
    final FudgeObjectBuilder<T> builder = getFudgeContext ().getObjectDictionary ().getObjectBuilder (clazz);
    if (builder == null) {
      // no builder for the requested class, so look to see if there are any embedded class details for a sub-class we know
      List<FudgeField> types = message.getAllByOrdinal (0);
      FudgeRuntimeException fre = null;
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          throw new FudgeRuntimeException ("Serialisation framework doesn't support back/forward references"); 
        } else if (o instanceof String) {
          try {
            final Class<?> possibleClazz = Class.forName ((String)o);
            if (!clazz.equals (possibleClazz) && clazz.isAssignableFrom (possibleClazz)) {
              try {
                return (T)fudgeMsgToObject (possibleClazz, message);
              } catch (FudgeRuntimeException e) {
                fre = e;
              }
            }
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      }
      // nothing matched
      if (fre != null) {
        // propogate one of the inner exceptions
        throw new FudgeRuntimeException ("Don't know how to create " + clazz + " from " + message, fre);
      } else {
        throw new FudgeRuntimeException ("Don't know how to create " + clazz + " from " + message);
      }
    }
    final T object = builder.buildObject (this, message);
    return object;
  }
  
}