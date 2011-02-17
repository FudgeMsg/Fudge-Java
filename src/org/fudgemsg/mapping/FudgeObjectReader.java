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
import org.fudgemsg.FudgeMsgReader;

/**
 * Deserialises Java objects from an underlying stream of Fudge messages.
 * 
 * @author Andrew Griffin
 */
public class FudgeObjectReader {
  
  private final FudgeMsgReader _messageReader;
  
  private FudgeDeserializationContext _deserialisationContext;

  /**
   * Creates a new {@link FudgeObjectReader} around the underlying {@link FudgeMsgReader} stream.
   * 
   * @param messageReader the source of Fudge messages containing serialised objects
   */
  public FudgeObjectReader (final FudgeMsgReader messageReader) {
    if (messageReader == null) throw new NullPointerException ("messageReader cannot be null");
    _messageReader = messageReader;
    _deserialisationContext = new FudgeDeserializationContext (messageReader.getFudgeContext ());
  }
  
  /**
   * Closes the underlying stream.
   */
  public void close () {
    if (_messageReader == null) return;
    _messageReader.close ();
  }
  
  /**
   * Returns the underlying {@link FudgeContext}. This will be the context of the {@link FudgeMsgReader} being used.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext () {
    final FudgeDeserializationContext context = getDeserialisationContext ();
    if (context == null) return null;
    return context.getFudgeContext ();
  }
  
  /**
   * Returns the current {@link FudgeDeserializationContext}. This is associated with the same {@link FudgeContext} as
   * the source message stream.
   * 
   * @return the {@code FudgeDeserialisationContext}
   */
  public FudgeDeserializationContext getDeserialisationContext () {
    return _deserialisationContext;
  }
  
  /**
   * Returns the underlying message source.
   * 
   * @return the {@link FudgeMsgReader}
   */
  public FudgeMsgReader getMessageReader () {
    return _messageReader;
  }
  
  /**
   * Returns {@code true} if the underlying message source has another message and {@link #read()} or {@link #read(Class)} can be
   * called.
   * 
   * @return {@code true} if there are more messages to be deserialized, {@code false} otherwise
   */
  public boolean hasNext () {
    return getMessageReader ().hasNext ();
  }
  
  /**
   * Reads the next message from the underlying source and deserializes it to a Java object.
   * 
   * @return the Java object
   */
  public Object read () {
    FudgeFieldContainer message = getMessageReader ().nextMessage ();
    getDeserialisationContext ().reset ();
    return getDeserialisationContext ().fudgeMsgToObject (message);
  }
  
  /**
   * Reads the next message from the underlying source and deserializes it to the requested Java type.
   * 
   * @param <T> Java type of the requested object
   * @param clazz Java class of the requested object
   * @return the Java object
   */
  public <T> T read (final Class<T> clazz) {
    FudgeFieldContainer message = getMessageReader ().nextMessage ();
    getDeserialisationContext ().reset ();
    return getDeserialisationContext ().fudgeMsgToObject (clazz, message);
  }
  
}