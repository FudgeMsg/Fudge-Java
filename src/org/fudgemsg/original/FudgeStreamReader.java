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

package org.fudgemsg.original;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldPrefixCodec;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.ModifiedUTF8Util;
import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * A Pull-Parser suitable for reading Fudge messages.
 *
 * @author kirk
 */
@Deprecated
public class FudgeStreamReader {
  // Injected Inputs:
  private DataInput _dataInput;
  private final FudgeContext _fudgeContext;
  // Runtime State:
  private final Stack<MessageProcessingState> _processingStack = new Stack<MessageProcessingState>();
  private FudgeStreamElement _currentElement;
  private FudgeTaxonomy _taxonomy;
  // Set for the envelope
  private int _processingDirectives;
  private int _schemaVersion;
  private short _taxonomyId;
  private int _envelopeSize;
  
  // Set for each non-sub-msg field
  private FudgeFieldType<?> _fieldType;
  private Integer _fieldOrdinal;
  private String _fieldName;
  private Object _fieldValue;
  
  public FudgeStreamReader(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Must provide a FudgeContext");
    }
    _fudgeContext = fudgeContext;
  }
  
  public FudgeStreamReader(FudgeContext fudgeContext, DataInput dataInput) {
    this(fudgeContext);
    reset(dataInput);
  }

  /**
   * @return the inputStream
   */
  protected DataInput getDataInput() {
    return _dataInput;
  }

  /**
   * Reset the state of this parser for a new message.
   * This method is primarily designed so that instances can be pooled to minimize
   * object creation in performance sensitive code.
   * 
   * @param dataInput
   */
  public void reset(DataInput dataInput) {
    if(dataInput == null) {
      throw new NullPointerException("Must provide a DataInput to consume data from.");
    }
    _dataInput = dataInput;
    _currentElement = null;
    _processingStack.clear();
    
    _processingDirectives = 0;
    _schemaVersion = 0;
    _taxonomyId = 0;
    _envelopeSize = 0;
    
    _fieldType = null;
    _fieldOrdinal = null;
    _fieldName = null;
    _fieldValue = null;
  }
  
  public void reset(InputStream inputStream) {
    if(inputStream == null) {
      throw new NullPointerException("Must provide an InputStream to consume data from.");
    }
    reset((DataInput) new DataInputStream(inputStream));
  }
  
  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public FudgeStreamElement next() {
    try {
      if(_processingStack.isEmpty()) {
        // Must be an envelope.
        consumeMessageEnvelope();
      } else if(isEndOfSubMessage()) {
        _currentElement = FudgeStreamElement.SUBMESSAGE_FIELD_END;
        _fieldName = null;
        _fieldOrdinal = null;
        _fieldType = null; 
      } else {
        consumeFieldData();
      }
    } catch (IOException ioe) {
      throw new FudgeRuntimeException("Unable to consume data", ioe);
    }
    assert _currentElement != null;
    return _currentElement;
  }
  
  /**
   * @return
   */
  protected boolean isEndOfSubMessage() {
    if(_processingStack.size() == 1) {
      return false;
    }
    MessageProcessingState processingState = _processingStack.peek();
    if(processingState.consumed >= processingState.messageSize) {
      _processingStack.pop();
      _processingStack.peek().consumed += processingState.consumed;
      return true;
    }
    return false;
  }
  
  /**
   * Reads the next field (prefix and value) from the input stream, setting internal state to be returned by getFieldName, getFieldOrdinal,
   * getFieldType, getCurrentElement and getFieldValue. The input stream is left positioned at the start of the next field.
   */
  protected void consumeFieldData() throws IOException {
    byte fieldPrefix = getDataInput().readByte();
    int typeId = getDataInput().readUnsignedByte();
    int nRead = 2;
    boolean fixedWidth = FudgeFieldPrefixCodec.isFixedWidth(fieldPrefix);
    boolean hasOrdinal = FudgeFieldPrefixCodec.hasOrdinal(fieldPrefix);
    boolean hasName = FudgeFieldPrefixCodec.hasName(fieldPrefix);
    
    Integer ordinal = null;
    if(hasOrdinal) {
      ordinal = new Integer(getDataInput().readShort());
      nRead += 2;
    }
    
    String name = null;
    if(hasName) {
      int nameSize = getDataInput().readUnsignedByte();
      nRead++;
      name = ModifiedUTF8Util.readString(getDataInput(), nameSize);
      nRead += nameSize;
    } else if(ordinal != null) {
      if(getTaxonomy() != null) {
        name = getTaxonomy().getFieldName(ordinal.shortValue());
      }
    }
    
    FudgeFieldType<?> type = getFudgeContext().getTypeDictionary().getByTypeId(typeId);
    if(type == null) {
      if(fixedWidth) {
        throw new FudgeRuntimeException("Unknown fixed width type " + typeId + " for field " + ordinal + ":" + name + " cannot be handled.");
      }
      type = getFudgeContext().getTypeDictionary().getUnknownType(typeId);
    }
    
    int varSize = 0;
    if(!fixedWidth) {
      int varSizeBytes = FudgeFieldPrefixCodec.getFieldWidthByteCount(fieldPrefix);
      switch(varSizeBytes) {
      case 0: varSize = 0; break;
      case 1: varSize = getDataInput().readUnsignedByte(); nRead+=1; break;
      case 2: varSize = getDataInput().readShort(); nRead += 2; break;
      case 4: varSize = getDataInput().readInt();  nRead += 4; break;
      default:
        throw new FudgeRuntimeException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
      }
    }
    
    _fieldName = name;
    _fieldOrdinal = ordinal;
    _fieldType = type;
    MessageProcessingState currMsgProcessingState = _processingStack.peek();
    currMsgProcessingState.consumed += nRead;
    if(typeId == FudgeTypeDictionary.FUDGE_MSG_TYPE_ID) {
      _currentElement = FudgeStreamElement.SUBMESSAGE_FIELD_START;
      _fieldValue = null;
      MessageProcessingState subState = new MessageProcessingState();
      subState.messageSize = varSize;
      subState.consumed = 0;
      _processingStack.add(subState);
    } else {
      _currentElement = FudgeStreamElement.SIMPLE_FIELD;
      _fieldValue = readFieldValue(getDataInput(), _fieldType, varSize);
      if(fixedWidth) {
        currMsgProcessingState.consumed += type.getFixedSize();
      } else {
        currMsgProcessingState.consumed += varSize;
      }
    }
  }

  /**
   * Reads a Fudge encoded field value from the input stream.
   * 
   * @param is
   * @param type
   * @param varSize
   * @return 
   * @throws IOException
   */
  public static Object readFieldValue(
      DataInput is,
      FudgeFieldType<?> type,
      int varSize) throws IOException {
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

  /**
   * Reads the next message envelope from the input stream, setting internal state go be returned by getCurrentElement, getProcessingDirectives, getSchemaVersion, getTaxonomyId and getEnvelopeSize.
   */
  protected void consumeMessageEnvelope() throws IOException {
    _currentElement = FudgeStreamElement.MESSAGE_ENVELOPE;
    _processingDirectives = getDataInput().readUnsignedByte();
    _schemaVersion = getDataInput().readUnsignedByte();
    _taxonomyId = getDataInput().readShort();
    _envelopeSize = getDataInput().readInt();
    if(getFudgeContext().getTaxonomyResolver() != null) {
      FudgeTaxonomy taxonomy = getFudgeContext().getTaxonomyResolver().resolveTaxonomy(_taxonomyId);
      _taxonomy = taxonomy;
    }
    MessageProcessingState processingState = new MessageProcessingState();
    processingState.consumed = 8;
    processingState.messageSize = _envelopeSize;
    _processingStack.add(processingState);
  }

  public boolean hasNext() {
    if(_processingStack.size() > 1) {
      // Always have at least one more.
      return true;
    } else if(_processingStack.size() == 1) {
      MessageProcessingState messageProcessingState = _processingStack.peek();
      return messageProcessingState.consumed < messageProcessingState.messageSize;
    } else {
      // Always have the envelope to read.
      return true;
    }
  }
  
  /**
   * @return the currentElement
   */
  public FudgeStreamElement getCurrentElement() {
    return _currentElement;
  }

  /**
   * @return the processingDirectives
   */
  public int getProcessingDirectives() {
    return _processingDirectives;
  }

  /**
   * @return the schemaVersion
   */
  public int getSchemaVersion() {
    return _schemaVersion;
  }

  /**
   * @return the taxonomy
   */
  public short getTaxonomyId() {
    return _taxonomyId;
  }

  /**
   * @return the envelopeSize
   */
  public int getEnvelopeSize() {
    return _envelopeSize;
  }

  /**
   * @return the fieldType
   */
  public FudgeFieldType<?> getFieldType() {
    return _fieldType;
  }

  /**
   * @return the fieldOrdinal
   */
  public Integer getFieldOrdinal() {
    return _fieldOrdinal;
  }

  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return _fieldName;
  }

  /**
   * @return the fieldValue
   */
  public Object getFieldValue() {
    return _fieldValue;
  }

  /**
   * @return the taxonomy
   */
  public FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }

  public enum FudgeStreamElement {
    /**
     * Issued when the envelope header is parsed.
     */
    MESSAGE_ENVELOPE,
    /**
     * Issued when a simple (non-hierarchical) field is encountered.
     */
    SIMPLE_FIELD,
    /**
     * Issued when a sub-Message field is encountered.
     */
    SUBMESSAGE_FIELD_START,
    /**
     * Issued when the end of a sub-Message field is reached
     */
    SUBMESSAGE_FIELD_END
  }
  
  private static class MessageProcessingState {
    public int messageSize;
    public int consumed;
  }
  
}
