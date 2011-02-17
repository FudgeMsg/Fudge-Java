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

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * An implementation of {@link FudgeStreamReader} for consuming data from a {@link DataInput}.
 */
public class FudgeDataInputStreamReader implements FudgeStreamReader {
  
  private static class MessageProcessingState {
    public int messageSize;
    public int consumed;
  }
  
  // Injected Inputs:
  private final DataInput _dataInput;
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
  
  private static DataInput convertInputStream (final InputStream inputStream) {
    //System.out.println ("FudgeDataInputStreamReader::convertInputStream(" + inputStream + ")");
    if (inputStream == null) {
      throw new NullPointerException ("Must specify an InputStream for processing");
    }
    if (inputStream instanceof DataInput) {
      return (DataInput)inputStream;
    } else {
      return new DataInputStream (inputStream);
    }
  }
  
  /**
   * Creates a new {@link FudgeDataInputStreamReader} associated with the given {@link FudgeContext} and {@link DataInput} data source.
   * The Fudge context is used to hold all decoding parameters such as taxonomy and type resolution.
   * 
   * @param fudgeContext the {@code FudgeContext} to associate with
   * @param dataInput the source of data to read Fudge elements from
   */
  public FudgeDataInputStreamReader (final FudgeContext fudgeContext, final DataInput dataInput) {
    //System.out.println ("FudgeDataInputStreamReader::FudgeDataInputStreamReader(" + fudgeContext + ", " + dataInput + ")");
    if(fudgeContext == null) {
      throw new NullPointerException("Must provide a FudgeContext");
    }
    if (dataInput == null) {
      throw new NullPointerException ("Must provide a DataInput");
    }
    _fudgeContext = fudgeContext;
    _dataInput = dataInput;
  }
  
  /**
   * Creates a new {@link FudgeDataInputStreamReader} by wrapping a {@link InputStream} with a {@link DataInput}.
   * 
   * @param fudgeContext the {@link FudgeContext} to associate with
   * @param inputStream the source of data to read Fudge elements from
   */
  public FudgeDataInputStreamReader (final FudgeContext fudgeContext, final InputStream inputStream) {
    this (fudgeContext, convertInputStream (inputStream));
  }
  
  /**
   * Closes this reader. If the underlying data source implements the {@link Closeable} interface, {@link Closeable#close()} will be called on it.
   */
  @Override
  public void close () {
    //System.out.println ("FudgeDataInputStreamReader::close()");
    if (_dataInput == null) return;
    if (_dataInput instanceof Closeable) {
      try {
        ((Closeable)_dataInput).close ();
      } catch (IOException ioe) {
        // ignore
      }
    }
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

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeStreamElement getCurrentElement() {
    return _currentElement;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFieldName() {
    return _fieldName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getFieldOrdinal() {
    return _fieldOrdinal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldType<?> getFieldType() {
    return _fieldType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getFieldValue() {
    return _fieldValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getProcessingDirectives() {
    return _processingDirectives;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSchemaVersion() {
    return _schemaVersion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public short getTaxonomyId() {
    return _taxonomyId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    if(_processingStack.size() > 1) {
      // Always have at least one more.
      return true;
    } else if(_processingStack.size() == 1) {
      MessageProcessingState messageProcessingState = _processingStack.peek();
      if (messageProcessingState.consumed < messageProcessingState.messageSize) {
        // More to read
        return true;
      } else {
        // End of the outermost envelope, so clear the stack and return a temporary false
        _processingStack.pop ();
        return false;
      }
    } else {
      // Might have another envelope to read
      return true;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeStreamElement next() {
    //System.out.println ("FudgeDataInputStreamReader::next()");
    try {
      if(_processingStack.isEmpty()) {
        // Must be an envelope (or an EOF)
        if (!consumeMessageEnvelope()) {
          return null;
        }
      } else if(isEndOfSubMessage()) {
        _currentElement = FudgeStreamElement.SUBMESSAGE_FIELD_END;
        _fieldName = null;
        _fieldOrdinal = null;
        _fieldType = null; 
      } else {
        consumeFieldData();
      }
      assert _currentElement != null;
      return _currentElement;
    } catch (IOException e) {
      throw new FudgeRuntimeIOException (e);
    }
  }

  /**
   * Detects the end of a sub-message field; i.e. the last field within the sub-message has been fully consumed.
   * After the end has been reached, further calls to {@link #next()} will resume consuming fields from the containing
   * message again.
   * 
   * @return {@code true} if the end of the sub-message has been reached, {@code false} otherwise. 
   */
  protected boolean isEndOfSubMessage() {
    //System.out.println ("FudgeDataInputStreamReader::isEndOfSubMessage()");
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
   * Returns the underlying {@link DataInput}.
   * 
   * @return the {@code DataInput}
   */
  protected DataInput getDataInput () {
    return _dataInput;
  }

  /**
   * Reads the next field (prefix and value) from the input stream, setting internal state to be returned by getFieldName, getFieldOrdinal,
   * getFieldType, getCurrentElement and getFieldValue. The input stream is left positioned at the start of the next field.
   * 
   * @throws IOException if the underlying stream raises one
   */
  protected void consumeFieldData() throws IOException {
    //System.out.println ("FudgeDataInputStreamReader::consumeFieldData()");
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
      name = UTF8.readString(getDataInput(), nameSize);
      nRead += nameSize;
    } else if(ordinal != null) {
      if(getTaxonomy() != null) {
        name = getTaxonomy().getFieldName(ordinal.shortValue());
      }
    }
    
    FudgeFieldType<?> type = getFudgeContext().getTypeDictionary().getByTypeId(typeId);
    if(type == null) {
      if(fixedWidth) {
        throw new IOException("Unknown fixed width type " + typeId + " for field " + ordinal + ":" + name + " cannot be handled.");
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
        throw new IOException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
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
   * Reads a Fudge encoded field value from an input stream.
   * 
   * @param is the {@link DataInput} wrapped input steram
   * @param type the {@link FudgeFieldType} of the data to read
   * @param varSize number of bytes in a variable width field payload
   * @return the field value
   */
  public static Object readFieldValue(
      DataInput is,
      FudgeFieldType<?> type,
      int varSize) {
    //System.out.println ("FudgeDataInputStreamReader::readFieldValue(" + is + ", " + type + ", " + varSize + ")");
    assert type != null;
    assert is != null;
    try {
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
    } catch (IOException e) {
      throw new FudgeRuntimeIOException (e);
    }
  }

  /**
   * Reads the next message envelope from the input stream, setting internal state go be returned by getCurrentElement, getProcessingDirectives, getSchemaVersion, getTaxonomyId and getEnvelopeSize.
   * 
   * @throws IOException if the underlying data source raises an {@link IOException} other than an {@link EOFException} on the first byte of the envelope
   * @return {@code true} if there was an envelope to consume, {@code false} if an EOF was found on reading the first byte
   */
  protected boolean consumeMessageEnvelope() throws IOException {
    //System.out.println ("FudgeDataInputStreamReader::consumeMessageEnvelope()");
    try {
      _processingDirectives = getDataInput().readUnsignedByte();
    } catch (EOFException e) {
      _currentElement = null;
      return false;
    }
    _currentElement = FudgeStreamElement.MESSAGE_ENVELOPE;
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
    return true;
  }
  
}