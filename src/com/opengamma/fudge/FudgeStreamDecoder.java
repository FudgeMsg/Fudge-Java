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
  
  public static FudgeMsgEnvelope readMsg(DataInput is) throws IOException {
    return readMsg(is, null);
  }
  
  public static FudgeMsgEnvelope readMsg(DataInput is, TaxonomyResolver taxonomyResolver) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    /*int processingDirectives = */is.readUnsignedByte();
    nRead += 1;
    int version = is.readUnsignedByte();
    nRead += 1;
    short taxonomyId = is.readShort();
    nRead += 2;
    int size = is.readInt();
    nRead += 4;
    
    FudgeTaxonomy taxonomy = null;
    if(taxonomyResolver != null) {
      taxonomy = taxonomyResolver.resolveTaxonomy(taxonomyId);
    }
    
    FudgeMsg msg = new FudgeMsg();
    nRead += readMsgFields(is, taxonomy, msg);
    
    if((size > 0) && (nRead != size)) {
      throw new RuntimeException("Expected to read " + size + " but only had " + nRead + " in message.");
    }
    
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg, version);
    return envelope;
  }
  
  public static int readMsgFields(DataInput is, FudgeTaxonomy taxonomy, FudgeMsg msg) throws IOException {
    if(msg == null) {
      throw new NullPointerException("Must specify a message to populate with fields.");
    }
    int nRead = 0;
    while(true) {
      byte fieldPrefix = is.readByte();
      nRead++;
      int typeId = is.readUnsignedByte();
      nRead++;
      if(typeId == FudgeTypeDictionary.END_FUDGE_MSG_TYPE_ID) {
        break;
      }
      nRead += readField(is, msg, fieldPrefix, typeId);
    }
    if(taxonomy != null) {
      msg.setNamesFromTaxonomy(taxonomy);
    }
    return nRead;
  }

  /**
   * Reads data about a field, and adds it to the message as a new field.
   * 
   * @param is
   * @param msg
   * @return The number of bytes read.
   */
  public static int readField(DataInput is, FudgeMsg msg, byte fieldPrefix, int typeId) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    
    boolean fixedWidth = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_FIXED_WIDTH_MASK) != 0;
    boolean hasOrdinal = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_ORDINAL_PROVIDED_MASK) != 0;
    boolean hasName = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_NAME_PROVIDED_MASK) != 0;
    int varSizeBytes = 0;
    if(!fixedWidth) {
      varSizeBytes = (fieldPrefix << 1) >> 6;
    }
    
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
      if(fixedWidth) {
        throw new RuntimeException("Unknown fixed width type " + typeId + " for field " + ordinal + ":" + name + " cannot be handled.");
      }
      type = FudgeTypeDictionary.INSTANCE.getUnknownType(typeId);
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
