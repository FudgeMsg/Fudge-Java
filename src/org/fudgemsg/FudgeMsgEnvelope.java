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
package org.fudgemsg;

import java.io.Serializable;

/**
 * An envelope enclosing the top message in the Fudge system.
 * <p>
 * When Fudge communicates it typically uses an envelope to wrap the top message.
 * The envelope is only used at the top level and cannot be embedded in a message.
 * The additional envelope data includes a version number and processing directives.
 * <p>
 * This class makes no guarantees about the immutability or thread-safety of its
 * content, although it holds the references in an immutable and thread-safe way.
 */
public class FudgeMsgEnvelope implements Serializable {

  /**
   * The message this envelope wraps.
   */
  private final FudgeFieldContainer _message;
  /**
   * The processing direcives.
   */
  private final int _processingDirectives;
  /**
   * The version.
   */
  private final int _version;

  /**
   * The processing direcives.
   */

  /**
   * Creates an envelope wrapping the given message.
   * No version or processing directives are used.
   * 
   * @param message  the message to wrap, not null
   */
  public FudgeMsgEnvelope(FudgeFieldContainer message) {
    this(message, 0);
  }

  /**
   * Creates an envelope wrapping the given message with a version.
   * No processing directives are used.
   * 
   * @param message  the message to wrap, not null
   * @param version  the version, from 0 to 255
   */
  public FudgeMsgEnvelope(FudgeFieldContainer message, int version) {
    this(message, version, 0);
  }

  /**
   * Creates an envelope wrapping the given message with a version and processing directive flags.
   * 
   * @param message  the message to wrap, not null
   * @param version  the version, from 0 to 255
   * @param processingDirectives  the processing directive flags, from 0 to 255
   */
  public FudgeMsgEnvelope(FudgeFieldContainer message, final int version, final int processingDirectives) {
    if (message == null) {
      throw new NullPointerException("Message must not be null");
    }
    if (processingDirectives < 0 || processingDirectives > 255) {
      throw new IllegalArgumentException("Processing directives " + processingDirectives + " must fit in one byte");
    }
    if (version < 0 || version > 255) {
      throw new IllegalArgumentException("Version " + version + " must fit in one byte");
    }
    _message = message;
    _version = version;
    _processingDirectives = processingDirectives;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying message.
   * 
   * @return the message, not null
   */
  public FudgeFieldContainer getMessage() {
    return _message;
  }

  /**
   * Gets the version number.
   * 
   * @return the version
   */
  public int getVersion() {
    return _version;
  }

  /**
   * Gets the processing directive flags.
   * 
   * @return processing directive flags
   */
  public int getProcessingDirectives() {
    return _processingDirectives;
  }

}
