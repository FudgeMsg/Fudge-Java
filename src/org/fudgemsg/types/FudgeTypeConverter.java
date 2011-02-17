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
package org.fudgemsg.types;

import org.fudgemsg.FudgeTypeDictionary;

/**
 * Definition of an API for conversion of types. This is used for the primary to
 * secondary type conversion within a {@link FudgeTypeDictionary} and also for
 * some other standard Java types.
 * 
 * @param <Primary> type to convert from (or {@link Object} to support any type
 * @param <Secondary> type to convert to
 * @author Andrew Griffin
 */
public interface FudgeTypeConverter<Primary,Secondary> {
  
  /**
   * Returns true if this converter can manipulate the requested class.
   * 
   * @param clazz to query
   * @return {@code true} if a call to {@link #primaryToSecondary} will succeed, {@code false} if it would fail
   */
  public boolean canConvertPrimary (Class<? extends Primary> clazz);
  
  /**
   * Converts an object to the secondary type this converter supports.
   * 
   * @param object to convert
   * @return the converted object
   */
  public Secondary primaryToSecondary (Primary object);
  
}