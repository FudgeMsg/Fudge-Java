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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamWriter {
  private final FudgeContext _fudgeContext;
  private DataOutput _dataOutput;
  private FudgeTaxonomy _taxonomy;
  
  public FudgeStreamWriter(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Must provide a Fudge Context");
    }
    _fudgeContext = fudgeContext;
  }
  
  public void reset(DataOutput dataOutput) {
    if(dataOutput == null) {
      throw new NullPointerException("Must specify a DataOutput for processing.");
    }
    _dataOutput = dataOutput;
    _taxonomy = null;
  }
  
  public void reset(OutputStream outputStream) {
    if(outputStream == null) {
      throw new NullPointerException("Must specify an OutputStream for processing.");
    }
    reset((DataOutput)new DataOutputStream(outputStream));
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  /**
   * @return the dataOutput
   */
  protected DataOutput getDataOutput() {
    return _dataOutput;
  }

  /**
   * @return the taxonomy
   */
  protected FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }

  /**
   * @param taxonomy the taxonomy to set
   */
  protected void setTaxonomy(FudgeTaxonomy taxonomy) {
    _taxonomy = taxonomy;
  }
  
  public int writeMessageEnvelope(FudgeMsgEnvelope envelope, int taxonomyId) {
    if(envelope == null) {
      return 0;
    }
    int nWritten = 0;
    setupTaxonomy(taxonomyId);
    int messageSize = envelope.getSize(getTaxonomy());
    nWritten += writeEnvelopeHeader(0, envelope.getVersion(), taxonomyId, messageSize);
    nWritten += writeMessageFields(envelope.getMessage());
    assert messageSize == nWritten;
    return nWritten;
  }
  
  public int writeEnvelopeHeader(
      int processingDirectives,
      int schemaVersion,
      int taxonomyId,
      int messageSize) {
    try {
      getDataOutput().writeByte(processingDirectives);
      getDataOutput().writeByte(schemaVersion);
      getDataOutput().writeShort(taxonomyId);
      getDataOutput().writeInt(messageSize);
    } catch (IOException ioe) {
      throw new FudgeRuntimeException("Unable to write envelope header", ioe);
    }
    setupTaxonomy(taxonomyId);
    return 8;
  }
  
  protected void setupTaxonomy(int taxonomyId) {
    if(getTaxonomy() != null) {
      return;
    }
    if(getFudgeContext().getTaxonomyResolver() != null) {
      FudgeTaxonomy taxonomy = getFudgeContext().getTaxonomyResolver().resolveTaxonomy((short)taxonomyId);
      _taxonomy = taxonomy;
    }
  }
  
  public int writeMessageFields(FudgeFieldContainer msg) {
    int nWritten = 0;
    for(FudgeField field : msg.getAllFields()) {
      nWritten += writeField(field.getOrdinal(), field.getName(), field.getType(), field.getValue());
    }
    return nWritten;
  }

  /**
   * Writes a field to the stream. If a taxonomy is selected and the field has a name but no ordinal, the ordinal is looked up and written in place of the field name.  
   * 
   * @param ordinal
   * @param name
   * @param type
   * @param fieldValue
   * @return
   */
  @SuppressWarnings("unchecked")
  public int writeField(
      Short ordinal,
      String name,
      FudgeFieldType type,
      Object fieldValue) {
    if(fieldValue == null) {
      throw new NullPointerException("Cannot write a null field value to a Fudge stream.");
    }
    
    //11/12/09 Andrew: If a taxonomy is being used, should we attempt to validate against it (i.e. refuse a mismatching fieldname/ordinal)
    //11/12/09 Andrew: If name, ordinal and taxonomy are supplied, should we not write out the name (this would happen if no ordinal was supplied) 
    
    if((name != null) && (ordinal == null) && (getTaxonomy() != null)) {
      ordinal = getTaxonomy().getFieldOrdinal(name);
      if(ordinal != null) {
        name = null;
      }
    }
    int valueSize = 0;
    int varDataSize = 0;
    if(type.isVariableSize()) {
      valueSize = type.getVariableSize(fieldValue, getTaxonomy());
      varDataSize = valueSize;
    } else {
      valueSize = type.getFixedSize();
      varDataSize = 0;
    }
    int fieldPrefix = FudgeFieldPrefixCodec.composeFieldPrefix(!type.isVariableSize(), varDataSize, (ordinal != null), (name != null));
    
    // Start writing.
    int nWritten = 0;
    try {
      getDataOutput().writeByte(fieldPrefix);
      getDataOutput().writeByte(type.getTypeId());
      nWritten = 2;
      if(ordinal != null) {
        getDataOutput().writeShort(ordinal.intValue());
        nWritten += 2;
      }
      if(name != null) {
        int utf8size = ModifiedUTF8Util.modifiedUTF8Length(name);
        if(utf8size > 0xFF) {
          throw new IllegalArgumentException("UTF-8 encoded field name cannot exceed 255 characters. Name \"" + name + "\" is " + utf8size + " bytes encoded.");
        }
        getDataOutput().writeByte(utf8size);
        nWritten++;
        nWritten += ModifiedUTF8Util.writeModifiedUTF8(name, getDataOutput());
      }
      
      nWritten += writeFieldValue(type, fieldValue, valueSize);
    } catch (IOException ioe) {
      throw new FudgeRuntimeException("Unable to write field to DataOutput", ioe);
    }
    
    return nWritten;
  }
      

  /**
   * @param type
   * @param fieldValue
   * @return
   */
  @SuppressWarnings("unchecked")
  protected int writeFieldValue(FudgeFieldType type, Object value, int valueSize) throws IOException {
    // Note that we fast-path types for which at compile time we know how to handle
    // in an optimized way. This is because this particular method is known to
    // be a massive hot-spot for performance.
    int nWritten = 0;
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      getDataOutput().writeBoolean((Boolean)value);
      nWritten = 1;
      break;
    case FudgeTypeDictionary.BYTE_TYPE_ID:
      getDataOutput().writeByte((Byte)value);
      nWritten = 1;
      break;
    case FudgeTypeDictionary.SHORT_TYPE_ID:
      getDataOutput().writeShort((Short)value);
      nWritten = 2;
      break;
    case FudgeTypeDictionary.INT_TYPE_ID:
      getDataOutput().writeInt((Integer)value);
      nWritten = 4;
      break;
    case FudgeTypeDictionary.LONG_TYPE_ID:
      getDataOutput().writeLong((Long)value);
      nWritten = 8;
      break;
    case FudgeTypeDictionary.FLOAT_TYPE_ID:
      getDataOutput().writeFloat((Float)value);
      nWritten = 4;
      break;
    case FudgeTypeDictionary.DOUBLE_TYPE_ID:
      getDataOutput().writeDouble((Double)value);
      nWritten = 8;
      break;
    }
    if(nWritten == 0) {
      if(type.isVariableSize()) {
        // This is correct. We read this using a .readUnsignedByte(), so we can go to
        // 255 here.
        if(valueSize <= 255) {
          getDataOutput().writeByte(valueSize);
          nWritten = valueSize + 1;
        } else if(valueSize <= Short.MAX_VALUE) {
          getDataOutput().writeShort(valueSize);
          nWritten = valueSize + 2;
        } else {
          getDataOutput().writeInt(valueSize);
          nWritten = valueSize + 4;
        }
      } else {
        nWritten = type.getFixedSize();
      }
      if(value instanceof FudgeFieldContainer) {
        FudgeFieldContainer subMsg = (FudgeFieldContainer) value;
        writeMessageFields(subMsg);
      } else {
        type.writeValue(getDataOutput(), value);
      }
    }
    return nWritten;
  }

  protected static void checkDataOutput(DataOutput dataOutput) {
    if(dataOutput == null) {
      throw new NullPointerException("Must specify a DataOutput for processing.");
    }
  }
}
