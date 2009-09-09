/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
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
