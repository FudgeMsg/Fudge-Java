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

package org.fudgemsg.json;

import java.io.IOException;
import java.io.Writer;

import org.fudgemsg.AlternativeFudgeStreamWriter;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeRuntimeIOException;
import org.fudgemsg.FudgeStreamWriter;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * A {@link FudgeStreamWriter} implementation for generating JSON representations of
 * Fudge messages. Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a> for details on
 * the representation.
 * 
 * @author Andrew Griffin
 */
public class FudgeJSONStreamWriter extends AlternativeFudgeStreamWriter {
  
  private final JSONSettings _settings;
  private final Writer _underlyingWriter;
  private JSONWriter _writer;

  /**
   * Creates a new stream writer for writing Fudge messages in JSON format to a given
   * {@link Writer}.
   * 
   * @param fudgeContext the associated {@link FudgeContext}
   * @param writer the target to write to
   */
  public FudgeJSONStreamWriter(final FudgeContext fudgeContext, final Writer writer) {
    this (fudgeContext, writer, new JSONSettings ());
  }
  
  /**
   * Creates a new stream writer for writing Fudge messages in JSON format to a given
   * {@link Writer}.
   * 
   * @param fudgeContext the associated {@link FudgeContext}
   * @param writer the target to write to
   */
  public FudgeJSONStreamWriter(final FudgeContext fudgeContext, final Writer writer, final JSONSettings settings) {
    super (fudgeContext);
    _settings = settings;
    _underlyingWriter = writer;
  }
  
  /**
   * Returns the settings object 
   */
  public JSONSettings getSettings () {
    return _settings;
  }
  
  /**
   * Returns the JSON writer being used, allocating one if necessary.
   * 
   * @return the writer
   */
  protected JSONWriter getWriter () {
    if (_writer == null) _writer = new JSONWriter (getUnderlying ());
    return _writer;
  }
  
  /**
   * Discards the JSON writer. The implementation only allows a single use so we must drop
   * the instance after each message envelope completes.
   */
  protected void clearWriter () {
    _writer = null;
  }
  
  /**
   * Returns the underlying {@link Writer} that is wrapped by {@link JSONWriter} instances for
   * messages.
   * 
   * @return the writer
   */
  protected Writer getUnderlying () {
    return _underlyingWriter;
  }
  
  /**
   * Wraps a JSON exception (which may in turn wrap {@link IOExceptions} into either a {@link FudgeRuntimeException} or {@link FudgeRuntimeIOException}.
   * 
   * @param message message describing the current operation
   * @param e the originating exception
   */
  protected void wrapException (String message, final JSONException e) {
    message = "Error writing " + message + " to JSON stream";
    if (e.getCause () instanceof IOException) {
      throw new FudgeRuntimeIOException (message, (IOException)e.getCause ());
    } else {
      throw new FudgeRuntimeException (message, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void flush () {
    if (getUnderlying () != null) {
      try {
        getUnderlying ().flush ();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException (e);
      }
    }
  }
  
  /**
   * Begins a JSON object with the processing directives, schema and taxonomy.
   */
  @Override
  protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) {
    try {
      getWriter ().object ();
      if ((processingDirectives != 0) && (getSettings ().getProcessingDirectivesField () != null)) getWriter ().key (getSettings ().getProcessingDirectivesField ()).value (processingDirectives);
      if ((schemaVersion != 0) && (getSettings ().getSchemaVersionField () != null)) getWriter ().key (getSettings ().getSchemaVersionField ()).value (schemaVersion);
      if ((getCurrentTaxonomyId () != 0) && (getSettings ().getTaxonomyField () != null)) getWriter ().key (getSettings ().getTaxonomyField ()).value (getCurrentTaxonomyId ());
    } catch (JSONException e) {
      wrapException ("start of message", e);
    }
  }
  
  /**
   * Ends the JSON object.
   */
  @Override
  protected void fudgeEnvelopeEnd () {
    try {
      getWriter ().endObject ();
      clearWriter ();
    } catch (JSONException e) {
      wrapException ("end of message", e);
    }
  }
  
  /**
   * Writes out the field name to the JSON object.
   */
  @Override
  protected boolean fudgeFieldStart (Short ordinal, String name, FudgeFieldType<?> type) {
    try {
      if (getSettings ().getPreferFieldNames ()) {
        if (name != null) {
          getWriter ().key (name);
        } else if (ordinal != null) {
          getWriter ().key (Integer.toString (ordinal));
        } else {
          getWriter ().key ("");
        }
      } else {
        if (ordinal != null) {
          getWriter ().key (Integer.toString (ordinal));
        } else if (name != null) {
          getWriter ().key (name);
        } else {
          getWriter ().key ("");
        }
      }
    } catch (JSONException e) {
      wrapException ("start of field", e);
    }
    return true;
  }
  
  protected void writeArray (final byte[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final short[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final int[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final long[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final float[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  protected void writeArray (final double[] data) throws JSONException {
    getWriter ().array ();
    for (int i = 0; i < data.length; i++) {
      getWriter ().value (data[i]);
    }
    getWriter ().endArray ();
  }
  
  /**
   * Writes the field value to the JSON object.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void fudgeFieldValue (FudgeFieldType<?> type, Object fieldValue) {
    try {
      if (type instanceof SecondaryFieldTypeBase<?,?,?>) {
        fieldValue = ((SecondaryFieldTypeBase<Object,Object,Object>)type).secondaryToPrimary(fieldValue);
      }
      switch (type.getTypeId ()) {
      case FudgeTypeDictionary.INDICATOR_TYPE_ID :
        getWriter ().value (null);
        break;
      case FudgeTypeDictionary.BYTE_ARRAY_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_4_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_8_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_16_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_20_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_32_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_64_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_128_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_256_TYPE_ID:
      case FudgeTypeDictionary.BYTE_ARR_512_TYPE_ID:
        writeArray ((byte[])fieldValue);
        break;
      case FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID:
        writeArray ((short[])fieldValue);
        break;
      case FudgeTypeDictionary.INT_ARRAY_TYPE_ID:
        writeArray ((int[])fieldValue);
        break;
      case FudgeTypeDictionary.LONG_ARRAY_TYPE_ID:
        writeArray ((long[])fieldValue);
        break;
      case FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID:
        writeArray ((float[])fieldValue);
        break;
      case FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID:
        writeArray ((double[])fieldValue);
        break;
      default :
        getWriter ().value (fieldValue);
        break;
      }
    } catch (JSONException e) {
      wrapException ("field value", e);
    }
  }
  
  /**
   * Starts a sub-object within the JSON object.  
   */
  @Override
  protected void fudgeSubMessageStart () {
    try {
      getWriter ().object ();
    } catch (JSONException e) {
      wrapException ("start of submessage", e);
    }
  }
  
  /**
   * Ends the JSON sub-object.
   */
  @Override
  protected void fudgeSubMessageEnd () {
    try {
      getWriter ().endObject ();
    } catch (JSONException e) {
      wrapException ("end of submessage", e);
    }
  }
  
}