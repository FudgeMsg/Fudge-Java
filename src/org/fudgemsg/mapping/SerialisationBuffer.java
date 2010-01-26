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

import java.util.Stack;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A basic buffer for the serialisation and deserialisation contexts that can detect
 * the cycles they can't deal with. When the method for processing object graphs has
 * been agreed, this will process back and forward references.
 * 
 * @author Andrew
 */
/* package */ class SerialisationBuffer {
  
  private final Stack<Object> _buffer;
  
  /* package */ SerialisationBuffer () {
    _buffer = new Stack<Object> ();
  }
  
  /* package */ void beginObject (final Object object) {
    if (_buffer.contains (object)) throw new FudgeRuntimeException ("Serialisation framework can't support cyclic references");
    _buffer.push (object);
  }
  
  /* package */ void endObject (final Object object) {
    final Object obj = _buffer.pop ();
    assert obj == object;
  }
  
  /* package */ void reset () {
    _buffer.clear ();
  }
  
}