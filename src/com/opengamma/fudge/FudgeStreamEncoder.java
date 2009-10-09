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
  
  public static void writeMsg(DataOutput os, FudgeMsg msg) throws IOException {
    writeMsg(os, new FudgeMsgEnvelope(msg));
  }
  
  public static void writeMsg(DataOutput os, FudgeMsgEnvelope envelope) throws IOException {
    writeMsg(os, envelope, FudgeTypeDictionary.INSTANCE, null, (short)0);
  }
  
  public static void writeMsg(DataOutput os, FudgeMsgEnvelope envelope, FudgeTypeDictionary typeDictionary, FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    checkOutputStream(os);
    if(envelope == null) {
      throw new NullPointerException("Must provide a message envelope to output.");
    }
    if(typeDictionary == null) {
      throw new NullPointerException("Type dictionary must be provided.");
    }
    int nWritten = 0;
    int msgSize = envelope.getSize(taxonomy);
    FudgeMsg msg = envelope.getMessage();
    nWritten += writeMsgEnvelopeHeader(os, taxonomyId, msgSize, envelope.getVersion());
    nWritten += writeMsgFields(os, msg, taxonomy);
    assert nWritten == msgSize : "Expected to write " + msgSize + " but actually wrote " + nWritten; 
  }
  
  public static int writeMsgFields(DataOutput os, FudgeMsg msg, FudgeTaxonomy taxonomy) throws IOException {
    int nWritten = 0;
    for(FudgeField field : msg.getAllFields()) {
      nWritten += writeField(os, field.getType(), field.getValue(), field.getOrdinal(), field.getName(), taxonomy);
    }
    return nWritten;
  }
  
  public static int writeMsgEnvelopeHeader(DataOutput os, int taxonomy, int messageSize, int version) throws IOException {
    checkOutputStream(os);
    int nWritten = 0;
    os.writeByte(0); // Processing Directives
    nWritten += 1;
    os.writeByte(version);
    nWritten += 1;
    os.writeShort(taxonomy);
    nWritten += 2;
    os.writeInt(messageSize);
    nWritten += 4;
    return nWritten;
  }
  
  public static int writeField(DataOutput os, FudgeFieldType<?> type, Object value, Short ordinal, String name) throws IOException {
    return writeField(os, type, value, ordinal, name, null);
  }
  
  @SuppressWarnings("unchecked")
  public static int writeField(DataOutput os, FudgeFieldType type,
      Object value, Short ordinal, String name,
      FudgeTaxonomy taxonomy) throws IOException {
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
    
    int valueSize = type.isVariableSize() ? type.getVariableSize(value, taxonomy) : type.getFixedSize();
    int nWritten = writeFieldContents(os, value, type, taxonomy, valueSize, type.isVariableSize(), type.getTypeId(), ordinal, name);
    return nWritten;
  }
  
  protected static int writeFieldContents(DataOutput os, Object value,
      FudgeFieldType<?> type, FudgeTaxonomy taxonomy,
      int valueSize, boolean variableSize,
      int typeId, Short ordinal, String name) throws IOException {
    int nWritten = 0;
    
    int fieldPrefix = FudgeFieldPrefixCodec.composeFieldPrefix(!variableSize, valueSize, (ordinal != null), (name != null));
    os.writeByte(fieldPrefix);
    nWritten++;
    os.writeByte(typeId);
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
    if(value != null) {
      assert type != null;
      nWritten += writeFieldValue(os, type, value, valueSize, taxonomy);
    }
    return nWritten;
  }
  
  /**
   * @param os
   * @param type
   * @param value
   */
  @SuppressWarnings("unchecked")
  protected static int writeFieldValue(DataOutput os, FudgeFieldType type, Object value, int valueSize, FudgeTaxonomy taxonomy) throws IOException {
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
      type.writeValue(os, value, taxonomy);
    }
    return nWritten;
  }

  protected static void checkOutputStream(DataOutput os) {
    if(os == null) {
      throw new NullPointerException("Must specify a DataOutput for processing.");
    }
  }
}
