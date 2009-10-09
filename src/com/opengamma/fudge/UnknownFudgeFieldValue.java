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
package com.opengamma.fudge;

import com.opengamma.fudge.types.UnknownFudgeFieldType;

/**
 * Container for a variable-sized field with a type that the current
 * installation of Fudge cannot handle on decoding.
 * In general, while Fudge supports an infinite number of
 * {@link UnknownFudgeFieldType} instances with a particular type ID, it
 * is optimal to use the factory method {@link FudgeTypeDictionary#getUnknownType(int)}
 * to obtain one for a particular context, which is what the Fudge decoding
 * routines will do. 
 *
 * @author kirk
 */
public class UnknownFudgeFieldValue {
  private final byte[] _contents;
  private final UnknownFudgeFieldType _type;
  
  public UnknownFudgeFieldValue(byte[] contents, UnknownFudgeFieldType type) {
    if(contents == null) {
      throw new NullPointerException("Contents must be provided");
    }
    if(type == null) {
      throw new NullPointerException("A valid UnknownFudgeFieldType must be specified");
    }
    _contents = contents;
    _type = type;
  }

  /**
   * @return the contents
   */
  public byte[] getContents() {
    return _contents;
  }

  /**
   * @return the type
   */
  public UnknownFudgeFieldType getType() {
    return _type;
  }

}
