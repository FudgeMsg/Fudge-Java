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
  
  /**
   * Creates a new {@link FudgeMsgEnvelope} around the given set of fields with no schema or processing directives.
   * 
   * @param fields the {@link FudgeFieldContainer} containing the message fields.
   */
  public FudgeMsgEnvelope(FudgeFieldContainer fields) {
    this(fields, 0);
  }
  
  /**
   * Creates a new {@link FudgeMsgEnvelope} around the given set of fields with a given schema version and no processing directives.
   * 
   * @param fields the {@link FudgeFieldContainer} containing the message fields.
   * @param schemaVersion the schema version
   */
  public FudgeMsgEnvelope(FudgeFieldContainer fields, int schemaVersion) {
    this (fields, schemaVersion, 0);
  }
  
  /**
   * Creates a new {@link FudgeMsgEnvelope} around the given set of fields with a given schema version and set of processing directives.
   * 
   * @param fields the {@link FudgeFieldContainer} containing the message fields
   * @param schemaVersion the schema version
   * @param processingDirectives the processing directive flags
   */
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
   * Returns the underlying message.
   * 
   * @return the message
   */
  public FudgeFieldContainer getMessage () {
    return _message;
  }
  /**
   * Returns the schema version.
   * 
   * @return the version
   */
  public int getVersion() {
    return _version;
  }
  
  /**
   * Returns the processing directive flags.
   * 
   * @return processing directive flags
   */
  public int getProcessingDirectives () {
    return _processingDirectives;
  }
  
}
