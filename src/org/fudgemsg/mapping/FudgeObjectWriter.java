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
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.FudgeFieldContainer;

/**
 * Serialises Java objects to a target Fudge message stream.
 * 
 * @author Andrew
 */
public class FudgeObjectWriter {

  private FudgeMsgWriter _messageWriter;
  
  private FudgeSerialisationContext _serialisationContext;
  
  /**
   * Creates a new {@link FudgeObjectWriter} around a {@link FudgeMsgWriter}.
   * 
   * @param messageWriter the target for Fudge messages
   */
  public FudgeObjectWriter (final FudgeMsgWriter messageWriter) {
    if (messageWriter == null) throw new NullPointerException ("messageWriter cannot be null");
    _messageWriter = messageWriter;
    _serialisationContext = new FudgeSerialisationContext (messageWriter.getFudgeContext ());
  }
  
  /**
   * Closes the underlying target stream.
   */
  public void close () {
    if (_messageWriter == null) return;
    getFudgeContext ().releaseMessageWriter (_messageWriter);
    _messageWriter = null;
  }
  
  /**
   * Resets the writer to use a different Fudge message target.
   * 
   * @param messageWriter the new target.
   */
  public void reset (final FudgeMsgWriter messageWriter) {
    close ();
    if (messageWriter == null) throw new NullPointerException ("messageReader cannot be null");
    _messageWriter = messageWriter;
    if (getSerialisationContext ().getFudgeContext () != messageWriter.getFudgeContext ()) {
      // only reallocate the S-context if the new stream has a different F-context
      _serialisationContext = new FudgeSerialisationContext (messageWriter.getFudgeContext ());
    }
  }
  
  /**
   * Returns the underlying {@link FudgeContext}. This will be the context of the {@link FudgeMsgWriter} being used.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext () {
    final FudgeSerialisationContext context = getSerialisationContext ();
    if (context == null) return null;
    return context.getFudgeContext ();
  }
  
  /**
   * Returns the current {@link FudgeSerialisationContext}. This is associated with the same {@link FudgeContext} as
   * the target message stream.
   * 
   * @return the {@code FudgeSerialisationContext}
   */
  public FudgeSerialisationContext getSerialisationContext () {
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
   * @return number of bytes written
   * @throws IOException if the message cannot be written
   */
  public <T> int write (final T obj) throws IOException {
    getSerialisationContext ().reset ();
    FudgeFieldContainer message;
    if (obj == null) {
      // write an empty message
      message = getSerialisationContext ().newMessage ();
    } else {
      // delegate to a message builder
      message = getSerialisationContext ().objectToFudgeMsg (obj);
    }
    return getMessageWriter ().writeMessage (message, 0);
  }
  
}