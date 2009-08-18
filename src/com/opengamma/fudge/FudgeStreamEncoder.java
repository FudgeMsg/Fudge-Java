/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamEncoder {
  // Yes, this is a byte.
  /*package*/ static final int MESSAGE_START_MAGIC_BYTE = 0x65;
  /*package*/ static final int FIELD_PREFIX_FIXED_WIDTH_MASK = 0x80; 
  /*package*/ static final int FIELD_PREFIX_ORDINAL_PROVIDED_MASK = 0x10; 
  /*package*/ static final int FIELD_PREFIX_NAME_PROVIDED_MASK = 0x08;
  
  public static void writeMsg(DataOutput os, FudgeMsg msg) throws IOException {
    checkOutputStream(os);
    if(msg == null) {
      throw new NullPointerException("Must provide a message to output.");
    }
    int nWritten = 0;
    int msgSize = msg.getSize();
    nWritten += writeMsgHeader(os, 0, msg.getNumFields(), msgSize);
    for(FudgeField field : msg.getAllFields()) {
      nWritten += writeField(os, field.getType(), field.getValue(), field.getOrdinal(), field.getName());
    }
    assert nWritten == msgSize : "Expected to write " + msgSize + " but actually wrote " + nWritten; 
  }
  
  public static int writeMsgHeader(DataOutput os, int taxonomy, short nFields, int messageSize) throws IOException {
    checkOutputStream(os);
    int nWritten = 0;
    os.writeByte(MESSAGE_START_MAGIC_BYTE);
    nWritten++;
    os.writeByte(taxonomy);
    nWritten++;
    os.writeShort(nFields);
    nWritten += 2;
    os.writeInt(messageSize);
    nWritten += 4;
    return nWritten;
  }
  
  public static int writeField(DataOutput os, FudgeFieldType type, Object value, Short ordinal, String name) throws IOException {
    checkOutputStream(os);
    if(type == null) {
      throw new NullPointerException("Must provide the type of data encoded.");
    }
    if(value == null) {
      throw new NullPointerException("Must provide the value to encode.");
    }
    int nWritten = 0;
    int fieldPrefix = composeFieldPrefix(!type.isVariableSize(), 0, (ordinal != null), (name != null));
    os.writeByte(fieldPrefix);
    nWritten++;
    os.writeByte(type.getTypeId());
    nWritten++;
    if(ordinal != null) {
      os.writeShort(ordinal);
      nWritten += 2;
    }
    if(name != null) {
      int utf8size = modifiedUTF8Length(name);
      if(utf8size > 0xFF) {
        throw new IllegalArgumentException("UTF-8 encoded field name cannot exceed 255 characters. Name \"" + name + "\" is " + utf8size + " bytes encoded.");
      }
      os.writeByte(utf8size);
      nWritten++;
      nWritten += writeModifiedUTF8(name, os);
    }
    nWritten += writeFieldValue(os, type, value);
    return nWritten;
  }
  
  /**
   * @param os
   * @param type
   * @param value
   */
  protected static int writeFieldValue(DataOutput os, FudgeFieldType type, Object value) throws IOException {
    // Note that we fast-path types for which at compile time we know how to handle
    // in an optimized way. This is because this particular method is known to
    // be a massive hot-spot for performance.
    int nWritten = 0;
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      os.writeBoolean((Boolean)value);
      nWritten = 1;
      break;
    }
    if(nWritten == 0) {
      throw new UnsupportedOperationException("Cannot handle field value of type " + type);
    }
    return nWritten;
  }

  protected static int composeFieldPrefix(boolean fixedWidth, int varDataSize, boolean hasOrdinal, boolean hasName) {
    int varDataBits = 0;
    if(varDataSize > 0) {
      if(varDataSize <= Byte.MAX_VALUE) {
        varDataSize = 1;
      } else if(varDataSize <= Short.MAX_VALUE) {
        varDataSize = 2;
      } else {
        varDataSize = 4;
      }
      varDataBits = varDataSize << 4;
    }
    int fieldPrefix = varDataBits;
    if(fixedWidth) {
      fieldPrefix |= FIELD_PREFIX_FIXED_WIDTH_MASK;
    }
    if(hasOrdinal) {
      fieldPrefix |= FIELD_PREFIX_ORDINAL_PROVIDED_MASK;
    }
    if(hasName) {
      fieldPrefix |= FIELD_PREFIX_NAME_PROVIDED_MASK;
    }
    return fieldPrefix;
  }
  
  protected static int modifiedUTF8Length(String str) {
    // REVIEW wyliekir 2009-08-17 -- This was taken almost verbatim from
    // DataOutputStream.
    int strlen = str.length();
    int utflen = 0;
    int c = 0;

    /* use charAt instead of copying String to char array */
    for (int i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }

  protected static int writeModifiedUTF8(String str, DataOutput os)
      throws IOException {
    // REVIEW wyliekir 2009-08-17 -- This was taken almost verbatim from
    // DataOutputStream.
    int strlen = str.length();
    int utflen = 0;
    int c, count = 0;

    /* use charAt instead of copying String to char array */
    for (int i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    if (utflen > 65535)
      throw new UTFDataFormatException("encoded string too long: " + utflen
          + " bytes");

    byte[] bytearr = new byte[utflen];

    int i = 0;
    for (i = 0; i < strlen; i++) {
      c = str.charAt(i);
      if (!((c >= 0x0001) && (c <= 0x007F)))
        break;
      bytearr[count++] = (byte) c;
    }

    for (; i < strlen; i++) {
      c = str.charAt(i);
      if ((c >= 0x0001) && (c <= 0x007F)) {
        bytearr[count++] = (byte) c;

      } else if (c > 0x07FF) {
        bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      } else {
        bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
      }
    }
    assert count == utflen;
    os.write(bytearr);
    return utflen;
  }

  protected static void checkOutputStream(DataOutput os) {
    if(os == null) {
      throw new NullPointerException("Must specify a DataOutput for processing.");
    }
  }
}
