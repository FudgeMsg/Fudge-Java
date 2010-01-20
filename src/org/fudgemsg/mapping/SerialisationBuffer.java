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

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A basic buffer for the serialisation and deserialisation contexts that can detect
 * the cycles they can't deal with.
 * 
 * @author Andrew
 */
/* package */ class SerialisationBuffer {
  
  /* package */ static class Entry {
    
    private Object _object;
    private boolean _valid;
    
    private Entry (final Object object, final boolean valid) {
      _object = object;
      _valid = valid;
    }
    
    /* package */ void endObject (final Object object) {
      _object = object;
      _valid = true;
    }
    
  }
  
  private final List<Entry> _buffer;
  
  // TODO 2010-01-19 Andrew -- the buffer cycling when a reference has been used

  // TODO 2010-01-19 Andrew -- looking up a suitable class for back reference

  /* package */ SerialisationBuffer () {
    _buffer = new LinkedList<Entry> ();
  }
  
  /* package */ void reset () {
    _buffer.clear ();
  }
  
  /**
   * Puts a placeholder into the buffer to reserve the spot until the object is validated for use.
   */
  /* package */ Entry beginObject (final Object object) {
    final Entry entry = new Entry (object, false);
    _buffer.add (entry);
    return entry;
  }
  
  /**
   * Puts an object into the buffer for immediate use.
   */
  /* package */ void storeObject (final Object object) {
    _buffer.add (new Entry (object, true));
  }

  private void contextCycleError (final int index, final Object object) {
    final StringBuilder sb = new StringBuilder ("Serialisation framework can't support cyclic reference to");
    if (object != null) sb.append (" object:").append (object);
    sb.append (" index:").append (index).append (" in this version");
    throw new FudgeRuntimeException (sb.toString ());
  }
  
  /**
   * Returns the object at a given index from the buffer head (0 being the current or "this" object).
   */
  /* package */ Object getObject (final int index) {
    final Entry entry = _buffer.get (index);
    if (!entry._valid) contextCycleError (index, entry._object);
    return entry._object;
  }
  
  /**
   * Finds the index of an object in the buffer.
   */
  /* package */ int findObject (final Object object) {
    int index = 0;
    final Iterator<Entry> iter = _buffer.iterator ();
    while (iter.hasNext ()) {
      final Entry entry = iter.next ();
      if (object == entry._object) {
        if (!entry._valid) contextCycleError (index, object);
        return index;
      }
      index++;
    }
    return -1;
  }
  
}