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

import java.io.DataInput;
import java.io.IOException;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.TaxonomyResolver;


/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamDecoder {
  
  public static FudgeMsgEnvelope readMsg(DataInput is) throws IOException {
    return readMsg(is, FudgeTypeDictionary.INSTANCE, null);
  }
  
  public static FudgeMsgEnvelope readMsg(DataInput is, FudgeTypeDictionary typeDictionary, TaxonomyResolver taxonomyResolver) throws IOException {
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
    // note that this is size-nRead because the size is for the whole envelope, including the header which we've already read in.
    nRead += readMsgFields(is, size - nRead, typeDictionary, taxonomy, msg); 
    
    if((size > 0) && (nRead != size)) {
      throw new RuntimeException("Expected to read " + size + " but only had " + nRead + " in message.");
    }
    
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg, version);
    return envelope;
  }
  
  public static int readMsgFields(
      DataInput is,
      int size,
      FudgeTypeDictionary typeDictionary,
      FudgeTaxonomy taxonomy,
      FudgeMsg msg)
  throws IOException {
    if(msg == null) {
      throw new NullPointerException("Must specify a message to populate with fields.");
    }
    int nRead = 0;
    while(nRead < size) {
      byte fieldPrefix = is.readByte();
      nRead++;
      int typeId = is.readUnsignedByte();
      nRead++;
      nRead += readField(is, msg, typeDictionary, fieldPrefix, typeId);
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
  public static int readField(
      DataInput is,
      FudgeMsg msg,
      FudgeTypeDictionary typeDictionary,
      byte fieldPrefix,
      int typeId)
  throws IOException {
    checkInputStream(is);
    int nRead = 0;
    
    boolean fixedWidth = FudgeFieldPrefixCodec.isFixedWidth(fieldPrefix);
    boolean hasOrdinal = FudgeFieldPrefixCodec.hasOrdinal(fieldPrefix);
    boolean hasName = FudgeFieldPrefixCodec.hasName(fieldPrefix);
    
    Integer ordinal = null;
    if(hasOrdinal) {
      ordinal = new Integer(is.readShort());
      nRead += 2;
    }
    
    String name = null;
    if(hasName) {
      int nameSize = is.readUnsignedByte();
      nRead++;
      name = ModifiedUTF8Util.readString(is, nameSize);
      nRead += nameSize;
    }
    
    FudgeFieldType<?> type = typeDictionary.getByTypeId(typeId);
    if(type == null) {
      if(fixedWidth) {
        throw new RuntimeException("Unknown fixed width type " + typeId + " for field " + ordinal + ":" + name + " cannot be handled.");
      }
      type = typeDictionary.getUnknownType(typeId);
    }
    int varSize = 0;
    if(!fixedWidth) {
      int varSizeBytes = FudgeFieldPrefixCodec.getFieldWidthByteCount(fieldPrefix);
      switch(varSizeBytes) {
      case 0: varSize = 0; break;
      case 1: varSize = is.readUnsignedByte(); nRead+=1; break;
      case 2: varSize = is.readShort(); nRead += 2; break;
      case 4: varSize = is.readInt();  nRead += 4; break;
      default:
        throw new RuntimeException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
      }
      
    }
    Object fieldValue = readFieldValue(is, type, varSize, typeDictionary);
    if(fixedWidth) {
      nRead += type.getFixedSize();
    } else {
      nRead += varSize;
    }
    
    msg.add(name, ordinal, type, fieldValue);
    
    return nRead;
  }
  
  /**
   * @param is
   * @param type
   * @param varSize 
   * @return
   */
  public static Object readFieldValue(
      DataInput is,
      FudgeFieldType<?> type,
      int varSize,
      FudgeTypeDictionary typeDictionary) throws IOException {
    assert type != null;
    assert is != null;
    assert typeDictionary != null;
    
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
    
    return type.readValue(is, varSize, typeDictionary);
  }

  protected static void checkInputStream(DataInput is) {
    if(is == null) {
      throw new NullPointerException("Must specify a DataInput for processing.");
    }
  }

}
