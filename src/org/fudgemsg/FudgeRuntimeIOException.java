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

import java.io.IOException;

/**
 * A Fudge-specific IO exception to wrap the checked {@code IOException}.
 * <p>
 * Fudge will never throw a checked exception.
 * Wherever possible, IO exceptions will be wrapped in this exception.
 */
public class FudgeRuntimeIOException extends FudgeRuntimeException {

  /**
   * Creates a wrapper for the checked {@code IOException}.
   * 
   * @param cause  the underlying exception, should not be null
   */
  public FudgeRuntimeIOException(final IOException cause) {
    super(cause.getMessage(), cause);
  }

  /**
   * Creates a wrapper for the checked {@code IOException} with an overridden message.
   * 
   * @param message  the description of the error condition, may be null
   * @param cause  the underlying exception, should not be null
   */
  public FudgeRuntimeIOException(final String message, final IOException cause) {
    super(message, cause);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying {@code IOException} wrapped by this runtime exception.
   * 
   * @return the IO exception, should not be null
   */
  @Override
  public IOException getCause() {
    return (IOException) super.getCause();
  }

}
