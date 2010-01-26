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
import org.fudgemsg.FudgeRuntimeException;

/**
 * The central point for Fudge message to Java Object deserialisation on a given stream.
 * Note that the deserialiser cannot process cyclic object graphs at the moment because
 * of the way the builder interfaces are structured (i.e. we don't have access to an
 * outer object until it's builder returned).
 * 
 * @author Andrew
 */
public class FudgeDeserialisationContext {
  
  private final FudgeContext _fudgeContext;
  private final SerialisationBuffer _serialisationBuffer = new SerialisationBuffer ();
  
  public FudgeDeserialisationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public void reset () {
    getSerialisationBuffer ().reset ();
  }
  
  private SerialisationBuffer getSerialisationBuffer () {
    return _serialisationBuffer;
  }
  
  public FudgeContext getFudgeContext () {
    return _fudgeContext;
  }
  
  public Object fieldValueToObject (final FudgeField field) {
    final Object o = field.getValue ();
    if (o instanceof FudgeFieldContainer) {
      return fudgeMsgToObject ((FudgeFieldContainer)o);
    } else {
      return o;
    }
  }
  
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
      // look up the classes
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          throw new FudgeRuntimeException ("Serialisation framework doesn't support back/forward references"); 
        } else if (o instanceof String) {
          //System.out.println ("inflate type " + o);
          try {
            final Class<?> clazz = Class.forName ((String)o);
            return fudgeMsgToObject (clazz, message);
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      }
    }
    // don't know how to inflate the message - leave it as is (something else will probably error soon)
    return message;
  }
  
  /**
   * Reads an object with a specific type.
   */
  public <T> T fudgeMsgToObject (final Class<T> clazz, final FudgeFieldContainer message) {
    final FudgeObjectBuilder<T> builder = _fudgeContext.getObjectDictionary ().getObjectBuilder (clazz);
    if (builder == null) throw new FudgeRuntimeException ("Don't know how to create " + clazz + " from " + message);
    final T object = builder.buildObject (this, message);
    return object;
  }
  
}