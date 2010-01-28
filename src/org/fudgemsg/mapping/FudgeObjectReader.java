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

import java.io.IOException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgReader;

public class FudgeObjectReader {
  
  private FudgeMsgReader _messageReader;
  
  private FudgeDeserialisationContext _deserialisationContext;
  
  public FudgeObjectReader (final FudgeMsgReader messageReader) {
    if (messageReader == null) throw new NullPointerException ("messageReader cannot be null");
    _messageReader = messageReader;
    _deserialisationContext = new FudgeDeserialisationContext (messageReader.getFudgeContext ());
  }
  
  public void close () {
    if (_messageReader == null) return;
    getFudgeContext ().releaseMessageReader (_messageReader);
    _messageReader = null;
  }
  
  public void reset (final FudgeMsgReader messageReader) {
    close ();
    if (messageReader == null) throw new NullPointerException ("messageReader cannot be null");
    _messageReader = messageReader;
    if (getDeserialisationContext ().getFudgeContext () != messageReader.getFudgeContext ()) {
      _deserialisationContext = new FudgeDeserialisationContext (messageReader.getFudgeContext ());
    }
  }
  
  public FudgeContext getFudgeContext () {
    final FudgeDeserialisationContext context = getDeserialisationContext ();
    if (context == null) return null;
    return context.getFudgeContext ();
  }
  
  public FudgeDeserialisationContext getDeserialisationContext () {
    return _deserialisationContext;
  }
  
  public FudgeMsgReader getMessageReader () {
    return _messageReader;
  }
  
  public boolean hasNext () {
    return getMessageReader ().hasNext ();
  }
  
  public Object read () throws IOException {
    FudgeFieldContainer message = getMessageReader ().nextMessage ();
    getDeserialisationContext ().reset ();
    return getDeserialisationContext ().fudgeMsgToObject (message);
  }
  
  public <T> T read (final Class<T> clazz) throws IOException {
    FudgeFieldContainer message = getMessageReader ().nextMessage ();
    getDeserialisationContext ().reset ();
    return getDeserialisationContext ().fudgeMsgToObject (clazz, message);
  }
  
}