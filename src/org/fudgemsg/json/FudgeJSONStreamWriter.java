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

package org.fudgemsg.json;

import java.io.IOException;
import java.io.Writer;

import org.fudgemsg.AlternativeFudgeStreamWriter;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeStreamWriter;
import org.fudgemsg.FudgeTypeDictionary;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * A {@link FudgeStreamWriter} implementation for generating JSON representations of
 * Fudge messages. This is experimental code; a page needs to be created on the Wiki
 * describing the Fudge/JSON conversion rules that this must adhere to.
 * 
 * @author Andrew Griffin
 */
public class FudgeJSONStreamWriter extends AlternativeFudgeStreamWriter {
  
  private final Writer _underlyingWriter;
  private final JSONWriter _writer;

  /**
   * Creates a new stream writer for writing Fudge messages in JSON format to a given
   * {@link Writer}.
   * 
   * @param fudgeContext the associated {@link FudgeContext}
   */
  public FudgeJSONStreamWriter(final FudgeContext fudgeContext, final Writer writer) {
    super (fudgeContext);
    _writer = new JSONWriter (writer);
    _underlyingWriter = writer;
  }
  
  public FudgeJSONStreamWriter (final FudgeContext fudgeContext, final JSONWriter writer) {
    super (fudgeContext);
    _writer = writer;
    _underlyingWriter = null;
  }
  
  protected JSONWriter getWriter () {
    return _writer;
  }
  
  protected Writer getUnderlying () {
    return _underlyingWriter;
  }
  
  protected void wrapException (final String message, final JSONException e) throws IOException {
    if (e.getCause () instanceof IOException) {
      throw (IOException)e.getCause ();
    } else {
      throw new FudgeRuntimeException ("Error writing " + message + " to JSON stream", e);
    }
  }
  
  @Override
  public void flush () throws IOException {
    if (getUnderlying () != null) {
      getUnderlying ().flush ();
    }
  }
  
  @Override
  protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) throws IOException {
    try {
      getWriter ().object ();
      if (processingDirectives != 0) getWriter ().key ("processingDirectives").value (processingDirectives);
      if (schemaVersion != 0) getWriter ().key ("schemaVersion").value (schemaVersion);
    } catch (JSONException e) {
      wrapException ("start of message", e);
    }
  }
  
  @Override
  protected void fudgeEnvelopeEnd () throws IOException {
    try {
      getWriter ().endObject ();
    } catch (JSONException e) {
      wrapException ("end of message", e);
    }
  }
  
  @Override
  protected boolean fudgeFieldStart (Short ordinal, String name, FudgeFieldType<?> type) throws IOException {
    try {
      if (name != null) {
        getWriter ().key (name);
      } else if (ordinal != null) {
        getWriter ().key (Integer.toString (ordinal));
      } else {
        getWriter ().key ("");
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
  
  @Override
  protected void fudgeFieldValue (FudgeFieldType<?> type, Object fieldValue) throws IOException {
    try {
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
  
  @Override
  protected void fudgeSubMessageStart () throws IOException {
    try {
      getWriter ().object ();
    } catch (JSONException e) {
      wrapException ("start of submessage", e);
    }
  }
  
  @Override
  protected void fudgeSubMessageEnd () throws IOException {
    try {
      getWriter ().endObject ();
    } catch (JSONException e) {
      wrapException ("end of submessage", e);
    }
  }
  
}