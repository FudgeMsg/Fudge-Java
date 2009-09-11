/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamEncoder {
  // Yes, this is a byte.
  /*package*/ static final int FIELD_PREFIX_FIXED_WIDTH_MASK = 0x80; 
  /*package*/ static final int FIELD_PREFIX_ORDINAL_PROVIDED_MASK = 0x10; 
  /*package*/ static final int FIELD_PREFIX_NAME_PROVIDED_MASK = 0x08;
  
  public static void writeMsg(DataOutput os, FudgeMsg msg) throws IOException {
    writeMsg(os, msg, null, (short)0);
  }
  
  public static void writeMsg(DataOutput os, FudgeMsg msg, FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    checkOutputStream(os);
    if(msg == null) {
      throw new NullPointerException("Must provide a message to output.");
    }
    int nWritten = 0;
    int msgSize = msg.getSize(taxonomy);
    nWritten += writeMsgHeader(os, taxonomyId, msg.getNumFields(), msgSize);
    for(FudgeField field : msg.getAllFields()) {
      nWritten += writeField(os, field.getType(), field.getValue(), field.getOrdinal(), field.getName(), taxonomy, taxonomyId);
    }
    assert nWritten == msgSize : "Expected to write " + msgSize + " but actually wrote " + nWritten; 
  }
  
  public static int writeMsgHeader(DataOutput os, int taxonomy, short nFields, int messageSize) throws IOException {
    checkOutputStream(os);
    int nWritten = 0;
    os.writeShort(taxonomy);
    nWritten += 2;
    os.writeShort(nFields);
    nWritten += 2;
    os.writeInt(messageSize);
    nWritten += 4;
    return nWritten;
  }
  
  public static int writeField(DataOutput os, FudgeFieldType<?> type, Object value, Short ordinal, String name) throws IOException {
    return writeField(os, type, value, ordinal, name, null, (short)0);
  }
  
  @SuppressWarnings("unchecked")
  public static int writeField(DataOutput os, FudgeFieldType type,
      Object value, Short ordinal, String name,
      FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    checkOutputStream(os);
    if(type == null) {
      throw new NullPointerException("Must provide the type of data encoded.");
    }
    if(value == null) {
      throw new NullPointerException("Must provide the value to encode.");
    }
    
    // First, normalize the name/ordinal bit
    if((taxonomy != null) && (name != null)) {
      Short ordinalFromTaxonomy = taxonomy.getFieldOrdinal(name);
      if(ordinalFromTaxonomy != null) {
        if((ordinal != null) && !ObjectUtils.equals(ordinalFromTaxonomy, ordinal)) {
          // In this case, we've been provided an ordinal, but it doesn't match the
          // one from the taxonomy. We have to assume the user meant what they were doing,
          // and not do anything.
        } else {
          ordinal = ordinalFromTaxonomy;
          name = null;
        }
      }
    }
    
    int nWritten = 0;
    int valueSize = type.isVariableSize() ? type.getVariableSize(value, taxonomy) : type.getFixedSize();
    int fieldPrefix = composeFieldPrefix(!type.isVariableSize(), valueSize, (ordinal != null), (name != null));
    os.writeByte(fieldPrefix);
    nWritten++;
    os.writeByte(type.getTypeId());
    nWritten++;
    if(ordinal != null) {
      os.writeShort(ordinal);
      nWritten += 2;
    }
    if(name != null) {
      int utf8size = ModifiedUTF8Util.modifiedUTF8Length(name);
      if(utf8size > 0xFF) {
        throw new IllegalArgumentException("UTF-8 encoded field name cannot exceed 255 characters. Name \"" + name + "\" is " + utf8size + " bytes encoded.");
      }
      os.writeByte(utf8size);
      nWritten++;
      nWritten += ModifiedUTF8Util.writeModifiedUTF8(name, os);
    }
    nWritten += writeFieldValue(os, type, value, valueSize, taxonomy, taxonomyId);
    return nWritten;
  }
  
  /**
   * @param os
   * @param type
   * @param value
   */
  @SuppressWarnings("unchecked")
  protected static int writeFieldValue(DataOutput os, FudgeFieldType type, Object value, int valueSize, FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    // Note that we fast-path types for which at compile time we know how to handle
    // in an optimized way. This is because this particular method is known to
    // be a massive hot-spot for performance.
    int nWritten = 0;
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      os.writeBoolean((Boolean)value);
      nWritten = 1;
      break;
    case FudgeTypeDictionary.BYTE_TYPE_ID:
      os.writeByte((Byte)value);
      nWritten = 1;
      break;
    case FudgeTypeDictionary.SHORT_TYPE_ID:
      os.writeShort((Short)value);
      nWritten = 2;
      break;
    case FudgeTypeDictionary.INT_TYPE_ID:
      os.writeInt((Integer)value);
      nWritten = 4;
      break;
    case FudgeTypeDictionary.LONG_TYPE_ID:
      os.writeLong((Long)value);
      nWritten = 8;
      break;
    case FudgeTypeDictionary.FLOAT_TYPE_ID:
      os.writeFloat((Float)value);
      nWritten = 4;
      break;
    case FudgeTypeDictionary.DOUBLE_TYPE_ID:
      os.writeDouble((Double)value);
      nWritten = 8;
      break;
    }
    if(nWritten == 0) {
      if(type.isVariableSize()) {
        // This is correct. We read this using a .readUnsignedByte(), so we can go to
        // 255 here.
        if(valueSize <= 255) {
          os.writeByte(valueSize);
          nWritten = valueSize + 1;
        } else if(valueSize <= Short.MAX_VALUE) {
          os.writeShort(valueSize);
          nWritten = valueSize + 2;
        } else {
          os.writeInt(valueSize);
          nWritten = valueSize + 4;
        }
      } else {
        nWritten = type.getFixedSize();
      }
      type.writeValue(os, value, taxonomy, taxonomyId);
    }
    return nWritten;
  }

  protected static int composeFieldPrefix(boolean fixedWidth, int varDataSize, boolean hasOrdinal, boolean hasName) {
    int varDataBits = 0;
    if(!fixedWidth) {
      // This is correct. This is an unsigned value for reading. See note in
      // writeFieldValue.
      if(varDataSize <= 255) {
        varDataSize = 1;
      } else if(varDataSize <= Short.MAX_VALUE) {
        varDataSize = 2;
      } else {
        // Yes, this is right. Remember, we only have 2 bits here.
        varDataSize = 3;
      }
      varDataBits = varDataSize << 5;
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
  
  protected static void checkOutputStream(DataOutput os) {
    if(os == null) {
      throw new NullPointerException("Must specify a DataOutput for processing.");
    }
  }
}
