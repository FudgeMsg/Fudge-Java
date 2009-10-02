/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

/**
 * A container for all the utilities for working with fudge field prefixes.
 *
 * @author kirk
 */
public final class FudgeFieldPrefixCodec {
  // Yes, these are actually bytes.
  /*package*/ static final int FIELD_PREFIX_FIXED_WIDTH_MASK = 0x80;
  /*package*/ static final int FIELD_PREFIX_ORDINAL_PROVIDED_MASK = 0x10;
  /*package*/ static final int FIELD_PREFIX_NAME_PROVIDED_MASK = 0x08;

  
  private FudgeFieldPrefixCodec() {
  }
  
  public static boolean isFixedWidth(byte fieldPrefix) {
    return (fieldPrefix & FIELD_PREFIX_FIXED_WIDTH_MASK) != 0;    
  }

  public static boolean hasOrdinal(byte fieldPrefix) {
    return (fieldPrefix & FIELD_PREFIX_ORDINAL_PROVIDED_MASK) != 0;
  }
  
  public static boolean hasName(byte fieldPrefix) {
    return (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_NAME_PROVIDED_MASK) != 0;
  }
  
  public static int getFieldWidthByteCount(byte fieldPrefix) {
    int count = (fieldPrefix << 1) >> 6;
    if(count == 3) {
      // We do this because we only have two bits to encode data in.
      // Therefore, we use binary 11 to indicate 4 bytes.
      count = 4;
    }
    return count;
  }
  
}
