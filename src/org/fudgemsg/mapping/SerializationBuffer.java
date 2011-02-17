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

import java.util.Stack;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A basic buffer for the serialization and deserialization contexts that can detect
 * the cycles they can't deal with. When the method for processing object graphs has
 * been agreed, this will process back and forward references.
 * 
 * @author Andrew Griffin
 */
/* package */ class SerializationBuffer {
  
  private final Stack<Object> _buffer;
  
  /**
   * Creates a new {@link SerializationBuffer}.
   */
  SerializationBuffer () {
    _buffer = new Stack<Object> ();
  }
  
  /**
   * Registers the start of an object being processed. During serialization can detect a loop
   * and raise a {@link FudgeRuntimeException}.
   * 
   * @param object the object currently being processed
   * @throws FudgeRuntimeException if a cyclic reference is detected    
   */
  /* package */ void beginObject (final Object object) {
    if (_buffer.contains (object)) throw new UnsupportedOperationException ("Serialization framework can't support cyclic references");
    _buffer.push (object);
  }
  
  /**
   * Registers the end of an object being processed.
   * 
   * @param object the object being processed
   */
  /* package */ void endObject (final Object object) {
    final Object obj = _buffer.pop ();
    assert obj == object;
  }
  
  /**
   * Resets the state of the buffer.
   */
  /* package */ void reset () {
    _buffer.clear ();
  }
  
}