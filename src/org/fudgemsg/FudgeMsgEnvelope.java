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
 * Wraps a {@link FudgeFieldContainer} for the purpose of encoding the envelope header.
 * This is the object which is encoded for a top-level fudge message; sub-messages don't
 * contain a separate envelope.
 *
 * @author kirk
 */
public class FudgeMsgEnvelope implements Serializable {
  private final FudgeFieldContainer _message;
  private final int _processingDirectives;
  private final int _version;
  
  public FudgeMsgEnvelope(FudgeFieldContainer fields) {
    this(fields, 0);
  }
  
  public FudgeMsgEnvelope(FudgeFieldContainer fields, int schemaVersion) {
    this (fields, schemaVersion, 0);
  }
  
  public FudgeMsgEnvelope (FudgeFieldContainer fields, final int schemaVersion, final int processingDirectives) {
    if(fields == null) {
      throw new NullPointerException("Must specify a message to wrap.");
    }
    if ((processingDirectives < 0) || (processingDirectives > 255)) {
      throw new IllegalArgumentException ("Provided processing directives " + processingDirectives + " which doesn't fit within one byte.");
    }
    if((schemaVersion < 0) || (schemaVersion > 255)) {
      throw new IllegalArgumentException("Provided version " + schemaVersion + " which doesn't fit within one byte.");
    }
    _message = fields;
    _version = schemaVersion;
    _processingDirectives = processingDirectives;
  }
  
  /**
   * @return the message
   */
  public FudgeFieldContainer getMessage () {
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
  
  public int computeSize (final FudgeTaxonomy taxonomy) {
    return FudgeSize.calculateMessageEnvelopeSize (taxonomy, this);
  }

}
