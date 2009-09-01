/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.DataInput;
import java.io.IOException;

import com.opengamma.fudge.taxon.FudgeTaxonomy;
import com.opengamma.fudge.taxon.TaxonomyResolver;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamDecoder {
  
  public static FudgeMsg readMsg(DataInput is) throws IOException {
    return readMsg(is, null);
  }
  
  public static FudgeMsg readMsg(DataInput is, TaxonomyResolver taxonomyResolver) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    short taxonomyId = is.readShort();
    nRead += 2;
    short nFields = is.readShort();
    nRead += 2;
    int size = is.readInt();
    nRead += 4;
    
    FudgeMsg msg = new FudgeMsg();
    for(int i = 0; i < nFields; i++) {
      nRead += readField(is, msg);
    }
    
    if((size > 0) && (nRead != size)) {
      throw new RuntimeException("Expected to read " + size + " but only had " + nRead + " in message.");
    }
    
    if(taxonomyResolver != null) {
      FudgeTaxonomy taxonomy = taxonomyResolver.resolveTaxonomy(taxonomyId);
      if(taxonomy != null) {
        msg.setNamesFromTaxonomy(taxonomy);
      }
    }
    
    return msg;
  }

  /**
   * Reads data about a field, and adds it to the message as a new field.
   * 
   * @param is
   * @param msg
   * @return The number of bytes read.
   */
  public static int readField(DataInput is, FudgeMsg msg) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    
    byte fieldPrefix = is.readByte();
    nRead++;
    boolean fixedWidth = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_FIXED_WIDTH_MASK) != 0;
    boolean hasOrdinal = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_ORDINAL_PROVIDED_MASK) != 0;
    boolean hasName = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_NAME_PROVIDED_MASK) != 0;
    int varSizeBytes = 0;
    if(!fixedWidth) {
      varSizeBytes = (fieldPrefix << 1) >> 6;
    }
    
    int typeId = is.readUnsignedByte();
    nRead++;
    
    Short ordinal = null;
    if(hasOrdinal) {
      ordinal = is.readShort();
      nRead += 2;
    }
    
    String name = null;
    if(hasName) {
      int nameSize = is.readUnsignedByte();
      nRead++;
      name = ModifiedUTF8Util.readString(is, nameSize);
      nRead += nameSize;
    }
    
    FudgeFieldType<?> type = FudgeTypeDictionary.INSTANCE.getByTypeId(typeId);
    if(type == null) {
      // REVIEW kirk 2009-08-18 -- Is this the right behavior?
      throw new RuntimeException("Unable to locate a FudgeFieldType for type id " + typeId + " for field " + ordinal + ":" + name);
    }
    int varSize = 0;
    if(!fixedWidth) {
      switch(varSizeBytes) {
      case 0: varSize = 0; break;
      case 1: varSize = is.readUnsignedByte(); nRead+=1; break;
      case 2: varSize = is.readShort(); nRead += 2; break;
      // Yes, this is right. We only have 2 bits here.
      case 3: varSize = is.readInt();  nRead += 4; break;
      default:
        throw new RuntimeException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
      }
      
    }
    Object fieldValue = readFieldValue(is, type, varSize);
    if(fixedWidth) {
      nRead += type.getFixedSize();
    } else {
      nRead += varSize;
    }
    
    msg.add(type, fieldValue, name, ordinal);
    
    return nRead;
  }
  
  /**
   * @param is
   * @param type
   * @param varSize 
   * @return
   */
  public static Object readFieldValue(DataInput is, FudgeFieldType<?> type, int varSize) throws IOException {
    assert type != null;
    assert is != null;
    
    // Special fast-pass for known field types
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      return is.readBoolean();
    case FudgeTypeDictionary.BYTE_TYPE_ID:
      return is.readByte();
    case FudgeTypeDictionary.SHORT_TYPE_ID:
      return is.readShort();
    case FudgeTypeDictionary.INT_TYPE_ID:
      return is.readInt();
    case FudgeTypeDictionary.LONG_TYPE_ID:
      return is.readLong();
    case FudgeTypeDictionary.FLOAT_TYPE_ID:
      return is.readFloat();
    case FudgeTypeDictionary.DOUBLE_TYPE_ID:
      return is.readDouble();
    }
    
    return type.readValue(is, varSize);
  }

  protected static void checkInputStream(DataInput is) {
    if(is == null) {
      throw new NullPointerException("Must specify a DataInput for processing.");
    }
  }

}
