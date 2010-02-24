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

import java.io.IOException;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * <p>Implementation of a {@link FudgeStreamWriter} that writes XML to a text stream. This can be
 * used for XML output, or can be used to assist in developing/debugging a streaming serialiser
 * without having to inspect the binary output from a FudgeDataOutputStreamWriter.</p>
 * 
 * <p>This code is experimental and should not be relied upon at the moment. The XML specification
 * of Fudge messages will be published at <a href="http://wiki.fudgemsg.org/display/FDG/XML+Fudge+Messages">XML Fudge Messages</a>
 * when it is available.</p>
 * 
 * @author Andrew Griffin
 */
public class FudgeXMLStreamWriter implements FudgeStreamWriter {
  
  private static final char[] BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefgihjklmnopqrstuvwxyz0123456789+/".toCharArray ();
  
  private final FudgeContext _fudgeContext;
  private final XMLStreamWriter _writer;
  private FudgeTaxonomy _taxonomy;
  private int _taxonomyId;
  private int _messageSizeToWrite;
  
  /**
   * Creates a new {@link FudgeXMLStreamWriter} for writing to the target XML device.
   * 
   * @param fudgeContext the {@link FudgeContext}
   * @param writer the underlying {@link Writer}
   * @throws XMLStreamException if the XML subsystem can't create a stream wrapper for {@code Writer}
   */
  public FudgeXMLStreamWriter (final FudgeContext fudgeContext, final Writer writer) throws XMLStreamException {
    this (fudgeContext, XMLOutputFactory.newInstance ().createXMLStreamWriter (writer));
  }
  
  /**
   * Creates a new {@link FudgeXMLStreamWriter} for writing a Fudge stream to an {@link XMLStreamWriter}.
   * 
   * @param fudgeContext the {@link FudgeContext}
   * @param writer the underlying {@link Writer}
   */
  public FudgeXMLStreamWriter (final FudgeContext fudgeContext, final XMLStreamWriter writer) {
    _fudgeContext = fudgeContext;
    _writer = writer;
    _taxonomy = null;
    _taxonomyId = 0;
  }
  
  /**
   * Returns the underlying {@link XMLStreamWriter}.
   * 
   * @return the {@code XMLStreamWriter}
   */
  protected XMLStreamWriter getWriter () {
    return _writer;
  }
  
  /**
   * @param operation the operation being attempted when the exception was caught
   * @param e the exception caught
   * @throws IOException if the triggered {@link XMLStreamException} was caused by an {@link IOException}
   */
  protected void wrapException (final String operation, XMLStreamException e) throws IOException {
    if (e.getCause () instanceof IOException) {
      throw (IOException)e.getCause ();
    } else {
      throw new FudgeRuntimeException ("Couldn't " + operation + " XML stream", e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void close () throws IOException {
    try {
      getWriter ().close ();
    } catch (XMLStreamException e) {
      wrapException ("close", e);
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  @Override
  public void flush () throws IOException {
    try {
      getWriter ().flush ();
    } catch (XMLStreamException e) {
      wrapException ("flush", e);
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
  public int writeEnvelopeHeader(
      int processingDirectives,
      int schemaVersion,
      int messageSize) throws IOException {
    try {
      _messageSizeToWrite = messageSize - 8; // the size passed in includes the 8 byte Fudge envelope header
      getWriter ().writeStartDocument ();
      getWriter ().writeStartElement ("fudgeEnvelope");
      if (processingDirectives != 0) {
        getWriter ().writeAttribute ("processingDirectives", Integer.toString (processingDirectives));
      }
      if (schemaVersion != 0) {
        getWriter ().writeAttribute ("schemaVersion", Integer.toString (schemaVersion));
      }
    } catch (XMLStreamException e) {
      wrapException ("write message envelope header to", e);
    }
    return 8;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int writeFields(FudgeFieldContainer msg) throws IOException {
    int nWritten = 0;
    for(FudgeField field : msg.getAllFields()) {
      nWritten += writeField(field);
    }
    return nWritten;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int writeField (FudgeField field) throws IOException {
    if (field == null) {
      throw new NullPointerException ("Cannot write a null field to a Fudge stream");
    }
    return writeField (field.getOrdinal (), field.getName (), field.getType (), field.getValue ());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public int writeField(
      Short ordinal,
      String name,
      FudgeFieldType type,
      Object fieldValue) throws IOException {
    if (_messageSizeToWrite <= 0) throw new FudgeRuntimeException ("too many fields already written");
    final int fudgeMessageBytes = FudgeSize.calculateFieldSize (getCurrentTaxonomy (), ordinal, name, type, fieldValue);
    try {
      String ename = name;
      if (ename == null) {
        ename = "fudgeElement";
        if (ordinal != null) {
          if (getCurrentTaxonomy () != null) {
            final String f = getCurrentTaxonomy ().getFieldName (ordinal);
            if (f != null) ename = f;
          }
        }
      }
      ename = ename.replaceAll ("(^[0-9])|([^a-zA-Z0-9])", "");
      if (ename.length () == 0) ename = "fudgeElement";
      getWriter ().writeStartElement (ename);
      if (ordinal != null) {
        getWriter ().writeAttribute ("ordinal", ordinal.toString ());
      }
      if ((name != null) && !name.equals (ename)) {
        getWriter ().writeAttribute ("name", name);
      }
      getWriter ().writeAttribute ("type", Integer.toString (type.getTypeId ()));
      switch (type.getTypeId ()) {
      case FudgeTypeDictionary.INDICATOR_TYPE_ID :
        // no content
        break;
      case FudgeTypeDictionary.FUDGE_MSG_TYPE_ID :
        // the value returned from writeFields will have already been deducted from messageSizeToWrite so add it back on
        final int n = writeFields ((FudgeFieldContainer)fieldValue);
        _messageSizeToWrite += n;
        break;
      case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
        getWriter ().writeCharacters ((Boolean)fieldValue ? "true" : "false");
        break;
      case FudgeTypeDictionary.BYTE_TYPE_ID:
      case FudgeTypeDictionary.SHORT_TYPE_ID:
      case FudgeTypeDictionary.INT_TYPE_ID:
      case FudgeTypeDictionary.LONG_TYPE_ID:
      case FudgeTypeDictionary.FLOAT_TYPE_ID:
      case FudgeTypeDictionary.DOUBLE_TYPE_ID:
        getWriter ().writeCharacters (fieldValue.toString ());
        break;
      default :
        // default behaviour delegates to the type to get binary data
        final ByteArrayOutputStream baos = new ByteArrayOutputStream (fudgeMessageBytes);
        final DataOutputStream dos = new DataOutputStream (baos);
        type.writeValue (dos, fieldValue);
        dos.close ();
        getWriter ().writeAttribute ("encoding", "base64");
        final byte[] data = baos.toByteArray ();
        final StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < data.length; i += 3) {
          sb.append (BASE64_CHARS[((int)data[i] & 0xFC) >> 2]);
          if (i + 1 < data.length) {
            sb.append (BASE64_CHARS[(((int)data[i] & 0x03) << 4) | (((int)data[i + 1] & 0xF0) >> 4)]);
            if (i + 2 < data.length) {
              sb.append (BASE64_CHARS[(((int)data[i + 1] & 0x0F) << 2) | (((int)data[i + 2] & 0xC0) >> 6)]);
              sb.append (BASE64_CHARS[(int)data[i + 2] & 0x3F]);
            } else {
              sb.append (BASE64_CHARS[((int)data[i + 1] & 0x0F) << 2]);
              sb.append ('=');
            }
          } else {
            sb.append (BASE64_CHARS[((int)data[i] & 0x03) << 4]);
            sb.append ("==");
          }
        }
        getWriter ().writeCharacters (sb.toString ());
        break;
      }
      getWriter ().writeEndElement ();
      _messageSizeToWrite -= fudgeMessageBytes;
      if (_messageSizeToWrite < 0) throw new FudgeRuntimeException ("field data overflow");
      if (_messageSizeToWrite == 0) {
        getWriter ().writeEndElement (); // envelope
        getWriter ().writeEndDocument ();
      }
    } catch (XMLStreamException e) {
      wrapException ("write field to", e);
    }
    return fudgeMessageBytes;
  }
      
}
