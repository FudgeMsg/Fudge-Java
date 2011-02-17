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
import org.fudgemsg.FudgeMsgWriter;

/**
 * Serialises Java objects to a target Fudge message stream.
 * 
 * @author Andrew Griffin
 */
public class FudgeObjectWriter {
  
  private final FudgeMsgWriter _messageWriter;
  
  private FudgeSerializationContext _serialisationContext;
  
  /**
   * Creates a new {@link FudgeObjectWriter} around a {@link FudgeMsgWriter}.
   * 
   * @param messageWriter the target for Fudge messages
   */
  public FudgeObjectWriter (final FudgeMsgWriter messageWriter) {
    if (messageWriter == null) throw new NullPointerException ("messageWriter cannot be null");
    _messageWriter = messageWriter;
    _serialisationContext = new FudgeSerializationContext (messageWriter.getFudgeContext ());
  }
  
  /**
   * Closes the underlying target stream.
   */
  public void close () {
    if (_messageWriter == null) return;
    _messageWriter.close ();
  }
  
  /**
   * Returns the underlying {@link FudgeContext}. This will be the context of the {@link FudgeMsgWriter} being used.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext () {
    final FudgeSerializationContext context = getSerialisationContext ();
    if (context == null) return null;
    return context.getFudgeContext ();
  }
  
  /**
   * Returns the current {@link FudgeSerializationContext}. This is associated with the same {@link FudgeContext} as
   * the target message stream.
   * 
   * @return the {@code FudgeSerialisationContext}
   */
  public FudgeSerializationContext getSerialisationContext () {
    return _serialisationContext;
  }
  
  /**
   * Returns the underlying message target.
   * 
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter getMessageWriter () {
    return _messageWriter;
  }
  
  /**
   * Serialises a Java object to a Fudge message and writes it to the target stream.
   * 
   * @param <T> type of the Java object
   * @param obj the object to write
   */
  public <T> void write (final T obj) {
    getSerialisationContext ().reset ();
    FudgeFieldContainer message;
    if (obj == null) {
      // write an empty message
      message = getSerialisationContext ().newMessage ();
    } else {
      // delegate to a message builder
      message = getSerialisationContext ().objectToFudgeMsg (obj);
    }
    getMessageWriter ().writeMessage (message, 0);
  }
  
}