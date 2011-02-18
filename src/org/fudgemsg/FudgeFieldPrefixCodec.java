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

/**
 * Utility to manage the one byte field prefix.
 * <p>
 * This class is a static utility with no shared state.
 */
public final class FudgeFieldPrefixCodec {

  // Yes, these are actually bytes.
  private static final int FIELD_PREFIX_FIXED_WIDTH_MASK = 0x80;
  private static final int FIELD_PREFIX_ORDINAL_PROVIDED_MASK = 0x10;
  private static final int FIELD_PREFIX_NAME_PROVIDED_MASK = 0x08;

  /**
   * Restricted constructor.
   */
  private FudgeFieldPrefixCodec() {
  }

  //-------------------------------------------------------------------------
  /**
   * Tests if the fixed width flag is set.
   * 
   * @param fieldPrefix  the field prefix byte from the field header
   * @return {@code true} if the fixed width flag is set, {@code false} otherwise
   */
  public static boolean isFixedWidth(int fieldPrefix) {
    return (fieldPrefix & FIELD_PREFIX_FIXED_WIDTH_MASK) != 0;    
  }

  /**
   * Tests if the ordinal value present flag is set.
   * 
   * @param fieldPrefix  the field prefix byte from the field header
   * @return {@code true} if the ordinal present flag is set, {@code false} otherwise
   */
  public static boolean hasOrdinal(int fieldPrefix) {
    return (fieldPrefix & FIELD_PREFIX_ORDINAL_PROVIDED_MASK) != 0;
  }

  /**
   * Tests if the field name present flag is set.
   * 
   * @param fieldPrefix  the field prefix byte from the field header
   * @return {@code true} if the name present flag is set, {@code false} otherwise
   */
  public static boolean hasName(int fieldPrefix) {
    return (fieldPrefix & FIELD_PREFIX_NAME_PROVIDED_MASK) != 0;
  }

  /**
   * Returns the length of the field width indicator.
   * If the field is fixed width, a valid Fudge field header will have a field width of zero.
   * 
   * @param fieldPrefix  the field prefix byte from the field header
   * @return the number of bytes used for the variable field width
   */
  public static int getFieldWidthByteCount(int fieldPrefix) {
    fieldPrefix &= 0x60;
    int count = fieldPrefix >> 5;
    if (count == 3) {
      // We do this because we only have two bits to encode data in.
      // Therefore, we use binary 11 to indicate 4 bytes.
      count = 4;
    }
    return count;
  }

  /**
   * Creates a Fudge field prefix byte.
   * 
   * @param fixedWidth  {@code true} if the field type defines the width of the data to follow, {@code false} for variable width
   * @param varDataSize  the number of bytes of field data - ignored if {@code fixedWidth} is true
   * @param hasOrdinal  {@code true} if the field header will include an ordinal, {@code false} otherwise
   * @param hasName  {@code true} if the field header will include a field name, {@code false} otherwise
   * @return the field prefix byte
   */
  public static int composeFieldPrefix(boolean fixedWidth, int varDataSize, boolean hasOrdinal, boolean hasName) {
    int varDataBits = 0;
    if (!fixedWidth) {
      // This is correct. This is an unsigned value for reading. See note in
      // writeFieldValue.
      if (varDataSize <= 255) {
        varDataSize = 1;
      } else if (varDataSize <= Short.MAX_VALUE) {
        varDataSize = 2;
      } else {
        // Yes, this is right. Remember, we only have 2 bits here.
        varDataSize = 3;
      }
      varDataBits = varDataSize << 5;
    }
    int fieldPrefix = varDataBits;
    if (fixedWidth) {
      fieldPrefix |= FIELD_PREFIX_FIXED_WIDTH_MASK;
    }
    if (hasOrdinal) {
      fieldPrefix |= FIELD_PREFIX_ORDINAL_PROVIDED_MASK;
    }
    if (hasName) {
      fieldPrefix |= FIELD_PREFIX_NAME_PROVIDED_MASK;
    }
    return fieldPrefix;
  }

}
