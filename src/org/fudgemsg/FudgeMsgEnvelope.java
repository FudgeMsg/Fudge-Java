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
package org.fudgemsg;

import java.io.Serializable;

import org.fudgemsg.taxon.FudgeTaxonomy;


/**
 * Wraps a {@link FudgeMsg} for the purpose of encoding the envelope header.
 * This is the object which is encoded for a top-level fudge message; sub-messages don't
 * contain a separate envelope.
 *
 * @author kirk
 */
public class FudgeMsgEnvelope extends FudgeEncodingObject implements Serializable {
  private final FudgeMsg _message;
  private final int _processingDirectives;
  private final int _version;
  
  public FudgeMsgEnvelope(FudgeContext fudgeContext) {
    this(fudgeContext.newMessage());
  }
  
  public FudgeMsgEnvelope(FudgeMsg msg) {
    this(msg, 0);
  }
  
  public FudgeMsgEnvelope(FudgeMsg message, int schemaVersion) {
    this (message, schemaVersion, 0);
  }
  
  public FudgeMsgEnvelope (FudgeMsg message, final int schemaVersion, final int processingDirectives) {
    if(message == null) {
      throw new NullPointerException("Must specify a message to wrap.");
    }
    if ((processingDirectives < 0) || (processingDirectives > 255)) {
      throw new IllegalArgumentException ("Provided processing directives " + processingDirectives + " which doesn't fit within one byte.");
    }
    if((schemaVersion < 0) || (schemaVersion > 255)) {
      throw new IllegalArgumentException("Provided version " + schemaVersion + " which doesn't fit within one byte.");
    }
    _message = message;
    _version = schemaVersion;
    _processingDirectives = processingDirectives;
  }
  
  /**
   * @return the message
   */
  public FudgeMsg getMessage() {
    return _message;
  }
  /**
   * @return the version
   */
  public int getVersion() {
    return _version;
  }
  
  public int getProcessingDirectives () {
    return _processingDirectives;
  }

  @Override
  int computeSize(FudgeTaxonomy taxonomy) {
    int size = 0;
    // Message envelope header
    size += 8;
    size += getMessage().getSize(taxonomy);
    return size;
  }

}
