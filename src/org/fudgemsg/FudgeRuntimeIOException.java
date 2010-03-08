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

import java.io.IOException;

/**
 * A subclass of {@link FudgeRuntimeException} for wrapping {@link IOException}s raised by
 * underlying libraries. The Fudge system never throws a checked exception - this subclass
 * allows IOExceptions to be easily detected and handled.
 *
 * @author Andrew Griffin
 */
public class FudgeRuntimeIOException extends FudgeRuntimeException {
  
  /**
   * Creates a new {@link FudgeRuntimeIOException} to wrap an {@link IOException}.
   * 
   * @param cause the checked exception raised by a library
   */
  public FudgeRuntimeIOException(final IOException cause) {
    super(cause.getMessage (), cause);
  }
  
}
