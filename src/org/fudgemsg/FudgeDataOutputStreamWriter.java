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
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Implementation of a {@link FudgeStreamWriter} that writes to a {@link DataOutput}.
 */
public class FudgeDataOutputStreamWriter implements FudgeStreamWriter {
  
  private final FudgeContext _fudgeContext;
  private final DataOutput _dataOutput;
  private FudgeTaxonomy _taxonomy = null;
  private int _taxonomyId = 0;
  private boolean _automaticFlush = true;
  
  private static DataOutput convertOutputStream (final OutputStream outputStream) {
    if (outputStream instanceof DataOutput) {
      return (DataOutput)outputStream;
    } else {
      return new DataOutputStream (outputStream);
    }
  }
  
  /**
   * Creates a new {@link FudgeDataOutputStreamWriter} associated with the given {@link FudgeContext} and {@link DataOutput} target.
   * The Fudge context is used to hold all encoding parameters such as taxonomy and type resolution.
   * 
   * @param fudgeContext the {@code FudgeContext} to associate with
   * @param dataOutput the target to write Fudge elements to
   */
  public FudgeDataOutputStreamWriter(FudgeContext fudgeContext, final DataOutput dataOutput) {
    if(fudgeContext == null) {
      throw new NullPointerException("Must provide a Fudge Context");
    }
    if (dataOutput == null) {
      throw new NullPointerException("Must provide an output target");
    }
    _fudgeContext = fudgeContext;
    _dataOutput = dataOutput;
  }
  
  /**
   * Creates a new {@link FudgeDataOutputStreamWriter} by wrapping a {@link OutputStream} with a {@link DataOutput}.
   * 
   * @param fudgeContext the {@link FudgeContext} to associate with
   * @param outputStream the target to write Fudge elements to
   */
  public FudgeDataOutputStreamWriter (FudgeContext fudgeContext, final OutputStream outputStream) {
    this (fudgeContext, convertOutputStream (outputStream));
  }
  
  /**
   * Flushes and closes this writer and the underlying target.
   */
  @Override
  public void close () {
    if (_dataOutput == null) return;
    flush ();
    if (_dataOutput instanceof Closeable) {
      try {
        ((Closeable)_dataOutput).close ();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException (e);
      }
    }
    _taxonomy = null;
    _taxonomyId = 0;
  }
  
  /**
   * {@inheritDoc} 
   */
  @Override
  public void flush () {
    final Object out = getDataOutput ();
    if (out instanceof Flushable) {
      try {
        ((Flushable)out).flush ();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException (e);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
   * {@inheritDoc}
   */
  @Override
  public FudgeTaxonomy getCurrentTaxonomy() {
    return _taxonomy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCurrentTaxonomyId (final int taxonomyId) {
    _taxonomyId = taxonomyId;
    if(getFudgeContext().getTaxonomyResolver() != null) {
      FudgeTaxonomy taxonomy = getFudgeContext().getTaxonomyResolver().resolveTaxonomy((short)taxonomyId);
      _taxonomy = taxonomy;
    } else {
      _taxonomy = null;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getCurrentTaxonomyId () {
    return _taxonomyId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEnvelopeHeader(
      int processingDirectives,
      int schemaVersion,
      int messageSize) {
    try {
      getDataOutput().writeByte(processingDirectives);
      getDataOutput().writeByte(schemaVersion);
      getDataOutput().writeShort(getCurrentTaxonomyId ());
      getDataOutput().writeInt(messageSize);
    } catch (IOException e) {
      throw new FudgeRuntimeIOException (e);
    }
  }
  
  /**
   * No data is written - the end of the envelope is implied by the size from the header. If the writer is set
   * to automatically flush on message completion (the default) then {@link #flush()} will be called to flush
   * the underlying stream if possible.
   */
  @Override
  public void envelopeComplete () {
    if (isFlushOnEnvelopeComplete ()) {
      flush ();
    }
  }
  
  /**
   * Indicates if {@link #flush} is to be called on envelope completion.
   * 
   * @return {@code true} if {@code flush} is to be called, {@code false} otherwise
   */
  public boolean isFlushOnEnvelopeComplete () {
    return _automaticFlush;
  }
  
  /**
   * Set whether to call {@link #flush} on envelope completion. The default behavior is to do so.
   * 
   * @param automaticFlush {@code true} to call {@code flush} on envelope completion, {@code false} otherwise
   */
  public void setFlushOnEnvelopeComplete (final boolean automaticFlush) {
    _automaticFlush = automaticFlush;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeFields(FudgeFieldContainer msg) {
    for(FudgeField field : msg.getAllFields()) {
      writeField(field);
    }
  }
  
/**
 * {@inheritDoc}
 */
  @Override
  public void writeField (FudgeField field) {
    if (field == null) {
      throw new NullPointerException ("Cannot write a null field to a Fudge stream");
    }
    writeField (field.getOrdinal (), field.getName (), field.getType (), field.getValue ());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void writeField(
      Short ordinal,
      String name,
      FudgeFieldType type,
      Object fieldValue) {
    if(fieldValue == null) {
      throw new NullPointerException("Cannot write a null field value to a Fudge stream.");
    }
    
    //11/12/09 Andrew: If a taxonomy is being used, should we attempt to validate against it (i.e. refuse a mismatching fieldname/ordinal)
    //11/12/09 Andrew: If name, ordinal and taxonomy are supplied, should we not write out the name (this would happen if no ordinal was supplied) 
    
    if((name != null) && (ordinal == null) && (getCurrentTaxonomy() != null)) {
      ordinal = getCurrentTaxonomy().getFieldOrdinal(name);
      if(ordinal != null) {
        name = null;
      }
    }
    int valueSize = 0;
    int varDataSize = 0;
    if(type.isVariableSize()) {
      valueSize = type.getVariableSize(fieldValue, getCurrentTaxonomy());
      varDataSize = valueSize;
    } else {
      valueSize = type.getFixedSize();
      varDataSize = 0;
    }
    int fieldPrefix = FudgeFieldPrefixCodec.composeFieldPrefix(!type.isVariableSize(), varDataSize, (ordinal != null), (name != null));
    
    // Start writing.
    try {
      getDataOutput().writeByte(fieldPrefix);
      getDataOutput().writeByte(type.getTypeId());
      if(ordinal != null) {
        getDataOutput().writeShort(ordinal.intValue());
      }
      if(name != null) {
        int utf8size = UTF8.getLengthBytes(name);
        if(utf8size > 0xFF) {
          throw new IllegalArgumentException("UTF-8 encoded field name cannot exceed 255 characters. Name \"" + name + "\" is " + utf8size + " bytes encoded.");
        }
        getDataOutput().writeByte(utf8size);
        UTF8.writeString(getDataOutput(), name);
      }
    } catch (IOException e) {
      throw new FudgeRuntimeIOException (e);
    }
    
    writeFieldValue(type, fieldValue, valueSize);
  }
      

  /**
   * @param type the {@link FudgeFieldType} defining how to write this
   * @param value the value to write
   * @param valueSize the size of the value
   * @returns number of bytes written
   */
  @SuppressWarnings("unchecked")
  protected void writeFieldValue(FudgeFieldType type, Object value, int valueSize) {
    // Note that we fast-path types for which at compile time we know how to handle
    // in an optimized way. This is because this particular method is known to
    // be a massive hot-spot for performance.
    try {
      switch(type.getTypeId()) {
      case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
        getDataOutput().writeBoolean((Boolean)value);
        break;
      case FudgeTypeDictionary.BYTE_TYPE_ID:
        getDataOutput().writeByte((Byte)value);
        break;
      case FudgeTypeDictionary.SHORT_TYPE_ID:
        getDataOutput().writeShort((Short)value);
        break;
      case FudgeTypeDictionary.INT_TYPE_ID:
        getDataOutput().writeInt((Integer)value);
        break;
      case FudgeTypeDictionary.LONG_TYPE_ID:
        getDataOutput().writeLong((Long)value);
        break;
      case FudgeTypeDictionary.FLOAT_TYPE_ID:
        getDataOutput().writeFloat((Float)value);
        break;
      case FudgeTypeDictionary.DOUBLE_TYPE_ID:
        getDataOutput().writeDouble((Double)value);
      default :
        if(type.isVariableSize()) {
          // This is correct. We read this using a .readUnsignedByte(), so we can go to
          // 255 here.
          if(valueSize <= 255) {
            getDataOutput().writeByte(valueSize);
          } else if(valueSize <= Short.MAX_VALUE) {
            getDataOutput().writeShort(valueSize);
          } else {
            getDataOutput().writeInt(valueSize);
          }
        }
        if(value instanceof FudgeFieldContainer) {
          FudgeFieldContainer subMsg = (FudgeFieldContainer) value;
          writeFields(subMsg);
        } else {
          type.writeValue(getDataOutput(), value);
        }
      }
    } catch (IOException e) {
      throw new FudgeRuntimeIOException (e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder ("FudgeDataOutputStreamWriter{");
    if (getDataOutput () != null) sb.append (getDataOutput ());
    return sb.append ('}').toString ();
  }
  
}
