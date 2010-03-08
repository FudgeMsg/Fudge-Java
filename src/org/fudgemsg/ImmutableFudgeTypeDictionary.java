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

import org.fudgemsg.types.FudgeTypeConverter;

/**
 * An immutable version of a {@link FudgeTypeDictionary}.
 *
 * @author Andrew Griffin
 */
/* package */ class ImmutableFudgeTypeDictionary extends FudgeTypeDictionary {
  
  /**
   * Creates a new {@link FudgeTypeDictionary} as an immutable clone of an existing one.
   * 
   * @param dictionary the {@code FudgeTypeDictionary} to wrap
   */
  /* package */ ImmutableFudgeTypeDictionary(final FudgeTypeDictionary dictionary) {
    super (dictionary);
  }
  
  /**
   * Always throws an exception - this is an immutable dictionary.
   * 
   * @param converter the converter
   * @param type the type to register against
   */
  @Override
  public void addTypeConverter (FudgeTypeConverter<?,?> converter, Class<?> ... types) {
    throw new UnsupportedOperationException ("addTypeConverter called on an immutable Fudge type dictionary");
  }
  
  /**
   * Always throws an exception - this is an immutable dictionary.
   * 
   * @param type the {@link FudgeFieldType} definition of the type
   * @param alternativeTypes any additional Java classes that are synonymous with this type.
   */
  @Override
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    throw new UnsupportedOperationException ("addType called on an immutable Fudge type dictionary");
  }
  
}