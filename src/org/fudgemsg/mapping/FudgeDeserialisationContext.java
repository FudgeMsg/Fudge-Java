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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
  private final SerialisationBuffer _buffer = new SerialisationBuffer ();
  
  public FudgeDeserialisationContext (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public void reset () {
    _buffer.reset ();
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
        return fudgeMsgToList (message);
      } else if (maxOrdinal == 2) {
        return fudgeMsgToMap (message);
      }
    } else {
      // look up the classes
      for (FudgeField type : types) {
        final Object o = type.getValue ();
        if (o instanceof Number) {
          final int backRef = ((Number)o).intValue ();
          if (backRef >= 0) {
            return _buffer.getObject (backRef);
          } else {
            // TODO 2010-01-19 Andrew -- use the buffer of previously decoded objects to get the class to inflate
          }
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
    // don't know how to inflate the message - leave it as is (something else will probably error soon)
    _buffer.storeObject (message);
    return message;
  }
  
  @SuppressWarnings("unchecked")
  private <T> T checkPreviousObject (final FudgeFieldContainer message) {
    final FudgeField field = message.getByOrdinal (0);
    if (field != null) {
      if (field.getValue () instanceof Number) {
        final int backRef = ((Number)field.getValue ()).intValue ();
        if (backRef >= 0) {
          return (T)_buffer.getObject (backRef);
        }
      }
    }
    return null;
  }
  
  /**
   * Reads an object with a specific type.
   */
  @SuppressWarnings("unchecked")
  public <T> T fudgeMsgToObject (final Class<T> clazz, final FudgeFieldContainer message) {
    final T previous = (T)checkPreviousObject (message);
    if (previous != null) return previous;
    final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (null);
    final FudgeObjectBuilder<T> builder = _fudgeContext.getObjectDictionary ().getObjectBuilder (clazz);
    if (builder == null) throw new FudgeRuntimeException ("Don't know how to create " + clazz + " from " + message);
    final T object = builder.buildObject (this, message);
    bufferEntry.endObject (object);
    return object;
  }
  
  /**
   * Reads a map.
   */
  public Map<?,?> fudgeMsgToMap (final FudgeFieldContainer message) {
    return fudgeMsgToMap (Object.class, Object.class, message);
  }
  
  /**
   * Reads a map with a specific type.
   */
  @SuppressWarnings("unchecked")
  public <K,V> Map<K,V> fudgeMsgToMap (final Class<K> clazzK, final Class<V> clazzV, final FudgeFieldContainer message) {
    final Map<K,V> previous = (Map<K,V>)checkPreviousObject (message);
    if (previous != null) return previous;
    final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (null);
    final Map<K, V> map = new HashMap<K, V> ();
    final Queue<K> keys = new LinkedList<K> ();
    final Queue<V> values = new LinkedList<V> ();
    for (FudgeField field : message) {
      if (field.getOrdinal () == 1) {
        final K fieldValue = fieldValueToObject (clazzK, field);
        if (values.isEmpty ()) {
          // no values ready, so store the key till next time
          keys.add (fieldValue);
        } else {
          // store key along with next value
          map.put (fieldValue, values.remove ());
        }
      } else if (field.getOrdinal () == 2) {
        final V fieldValue = fieldValueToObject (clazzV, field);
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
    bufferEntry.endObject (map);
    return map;
  }
  
  /**
   * Reads a list or array
   */
  public List<?> fudgeMsgToList (final FudgeFieldContainer message) {
    return fudgeMsgToList (Object.class, message);
  }
  
  /**
   * Reads a list or array
   */
  public <E> List<E> fudgeMsgToList (final Class<E> clazz, final FudgeFieldContainer message) {
    final List<E> previous = checkPreviousObject (message);
    if (previous != null) return previous;
    final SerialisationBuffer.Entry bufferEntry = _buffer.beginObject (null);
    final List<E> list = new ArrayList<E> ();
    for (FudgeField field : message) {
      if ((field.getOrdinal () != null) && (field.getOrdinal () != 1)) throw new FudgeRuntimeException ("Sub-message doesn't contain a list (bad field " + field + ")");
      list.add (fieldValueToObject (clazz, field));
    }
    bufferEntry.endObject (list);
    return list;
  }
  
}