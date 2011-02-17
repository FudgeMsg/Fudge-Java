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

package org.fudgemsg.xml;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.fudgemsg.AlternativeFudgeStreamWriter;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeRuntimeIOException;
import org.fudgemsg.FudgeStreamWriter;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.SecondaryFieldTypeBase;

/**
 * <p>Implementation of a {@link FudgeStreamWriter} that writes XML to a text stream. This can be
 * used for XML output, or can be used to assist in developing/debugging a streaming serializer
 * without having to inspect the binary output from a FudgeDataOutputStreamWriter.</p>
 * 
 * <p>This code should adhere to the <a href="http://wiki.fudgemsg.org/display/FDG/XML+Fudge+Messages">XML Fudge Message specification</a>.</p>
 * 
 * <p>Note that no pretty printing options are available here. This implementation uses the systems default {@link XMLOutputFactory} if only passed
 * a {@link Writer} object at construction. If you require control over the output, you will need to use a suitable {@link XMLStreamWriter}
 * implementation that allows it. For example <a href="http://www.java2s.com/Open-Source/Java-Document/XML/stax-utils/javanet.staxutils.htm">javanet.staxutils</a>.</p>
 * 
 * @author Andrew Griffin
 */
public class FudgeXMLStreamWriter extends FudgeXMLSettings implements FudgeStreamWriter {
  
  private class DelegateWriter extends AlternativeFudgeStreamWriter {

    protected DelegateWriter(FudgeContext fudgeContext) {
      super(fudgeContext);
    }
    
    @Override
    protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) {
      try {
        FudgeXMLStreamWriter.this.fudgeEnvelopeStart (processingDirectives, schemaVersion);
      } catch (XMLStreamException e) {
        throw wrapException ("write envelope header to", e);
      }
    }
    
    @Override
    protected void fudgeEnvelopeEnd () {
      try {
        FudgeXMLStreamWriter.this.fudgeEnvelopeEnd ();
      } catch (XMLStreamException e) {
        throw wrapException ("write envelope end to", e);
      }
    }
    
    @Override
    protected boolean fudgeFieldStart (final Short ordinal, final String name, final FudgeFieldType<?> type) {
      try {
        return FudgeXMLStreamWriter.this.fudgeFieldStart (ordinal, name, type);
      } catch (XMLStreamException e) {
        throw wrapException ("write field start to", e);
      }
    }
    
    @Override
    protected void fudgeFieldValue (final FudgeFieldType<?> type, final Object fieldValue) {
      try {
        FudgeXMLStreamWriter.this.fudgeFieldValue (type, fieldValue);
      } catch (XMLStreamException e) {
        throw wrapException ("write field value to", e);
      }
    }
    
    @Override
    protected void fudgeFieldEnd () {
      try {
        FudgeXMLStreamWriter.this.fudgeFieldEnd ();
      } catch (XMLStreamException e) {
        throw wrapException ("write field end to", e);
      }
    }
    
  }
  
  private final FudgeStreamWriter _delegate;
  private final XMLStreamWriter _writer;
  
  private static XMLStreamWriter createXMLStreamWriter (final Writer writer) {
    try {
      return XMLOutputFactory.newInstance ().createXMLStreamWriter (writer);
    } catch (XMLStreamException e) {
      throw wrapException ("create", e);
    }
  }
  
  /**
   * Creates a new {@link FudgeXMLStreamWriter} for writing to the target XML device.
   * 
   * @param fudgeContext the {@link FudgeContext}
   * @param writer the underlying {@link Writer}
   */
  public FudgeXMLStreamWriter (final FudgeContext fudgeContext, final Writer writer) {
    this (fudgeContext, createXMLStreamWriter (writer));
  }
  
  public FudgeXMLStreamWriter (final FudgeXMLSettings settings, final FudgeContext fudgeContext, final Writer writer) {
    this (settings, fudgeContext, createXMLStreamWriter (writer));
  }
  
  /**
   * Creates a new {@link FudgeXMLStreamWriter} for writing a Fudge stream to an {@link XMLStreamWriter}.
   * 
   * @param fudgeContext the {@link FudgeContext}
   * @param writer the underlying {@link Writer}
   */
  public FudgeXMLStreamWriter (final FudgeContext fudgeContext, final XMLStreamWriter writer) {
    _delegate = new DelegateWriter (fudgeContext);
    _writer = writer;
  }
  
  public FudgeXMLStreamWriter (final FudgeXMLSettings settings, final FudgeContext fudgeContext, final XMLStreamWriter writer) {
    super (settings);
    _delegate = new DelegateWriter (fudgeContext);
    _writer = writer;
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
   */
  protected static FudgeRuntimeException wrapException (final String operation, XMLStreamException e) {
    if (e.getCause () instanceof IOException) {
      return new FudgeRuntimeIOException ((IOException)e.getCause ());
    } else {
      return new FudgeRuntimeException ("Couldn't " + operation + " XML stream", e);
    }
  }

  protected FudgeStreamWriter getDelegate () {
    return _delegate;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void close () {
    getDelegate ().close ();
    try {
      getWriter ().close ();
    } catch (XMLStreamException e) {
      throw wrapException ("close", e);
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  @Override
  public void flush () {
    getDelegate ().flush ();
    try {
      getWriter ().flush ();
    } catch (XMLStreamException e) {
      throw wrapException ("flush", e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeContext getFudgeContext() {
    return getDelegate ().getFudgeContext ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTaxonomy getCurrentTaxonomy() {
    return getDelegate ().getCurrentTaxonomy ();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCurrentTaxonomyId (final int taxonomyId) {
    getDelegate ().setCurrentTaxonomyId (taxonomyId);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getCurrentTaxonomyId () {
    return getDelegate ().getCurrentTaxonomyId ();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEnvelopeHeader(
      int processingDirectives,
      int schemaVersion,
      int messageSize) {
    getDelegate ().writeEnvelopeHeader (processingDirectives, schemaVersion, messageSize);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void envelopeComplete () {
    getDelegate ().envelopeComplete ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeFields(FudgeFieldContainer msg) {
    getDelegate ().writeFields (msg);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeField (FudgeField field) {
    getDelegate ().writeField (field);
  }

  @Override
  public void writeField(Short ordinal, String name, FudgeFieldType<?> type,
      Object fieldValue) {
    getDelegate ().writeField (ordinal, name, type, fieldValue);
  }
      
  protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) throws XMLStreamException {
    getWriter ().writeStartDocument ();
    if (getEnvelopeElementName () != null) {
      getWriter ().writeStartElement (getEnvelopeElementName ());
      if ((processingDirectives != 0) && (getEnvelopeAttributeProcessingDirectives () != null)) {
        getWriter ().writeAttribute (getEnvelopeAttributeProcessingDirectives (), Integer.toString (processingDirectives));
      }
      if ((schemaVersion != 0) && (getEnvelopeAttributeSchemaVersion () != null)) {
        getWriter ().writeAttribute (getEnvelopeAttributeSchemaVersion (), Integer.toString (schemaVersion));
      }
    }
  }
  
  /**
   * Remove any invalid characters to leave an XML element name.
   */
  protected String convertFieldName (String str) {
    /*
     * nameStartChar :=  ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF]
     *                | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF]
     *                | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * nameChar := nameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     */
    if (str == null) return null;
    final StringBuilder sb = new StringBuilder ();
    boolean firstChar = true;
    for (int i = 0; i < str.length (); i++) {
      final char c = str.charAt (i);
      if ((c == ':')
       || ((c >= 'A') && (c <= 'Z'))
       || (c == '_')
       || ((c >= 'a') && (c <= 'z'))
       || ((c >= 0xC0) && (c <= 0xD6))
       || ((c >= 0xD8) && (c <= 0xF6))
       || ((c >= 0xF8) && (c <= 0x2FF))
       || ((c >= 0x370) && (c <= 0x37D))
       || ((c >= 0x37F) && (c <= 0x1FFF))
       || ((c >= 0x200C) && (c <= 0x200D))
       || ((c >= 0x2070) && (c <= 0x2FEF))
       || ((c >= 0x3001) && (c <= 0xD7FF))
       || ((c >= 0xF900) && (c <= 0xFDCF))
       || ((c >= 0xFDF0) && (c <= 0xFFFD))
       || ((c >= 0x10000) && (c <= 0xEFFFF))) {
        firstChar = false;
        sb.append (c);
      } else if (!firstChar) {
        if ((c == '-')
         || (c == '.')
         || ((c >= '0') && (c <= '9'))
         || (c == 0xB7)
         || ((c >= 0x300) && (c <= 0x36F))
         || ((c >= 0x203F) && (c <= 0x2040))) {
          sb.append (c);
        }
      }
    }
    return (sb.length () > 0) ? sb.toString () : null;
  }
  
  protected void writeArray (final byte[] array) throws XMLStreamException {
    boolean first = true;
    for (byte value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Byte.toString (value));
    }
  }
  
  protected void writeArray (final short[] array) throws XMLStreamException {
    boolean first = true;
    for (short value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Short.toString (value));
    }
  }
  
  protected void writeArray (final int[] array) throws XMLStreamException {
    boolean first = true;
    for (int value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Integer.toString (value));
    }
  }
  
  protected void writeArray (final long[] array) throws XMLStreamException {
    boolean first = true;
    for (long value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Long.toString (value));
    }
  }
  
  protected void writeArray (final float[] array) throws XMLStreamException {
    boolean first = true;
    for (float value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Float.toString (value));
    }
  }
  
  protected void writeArray (final double[] array) throws XMLStreamException {
    boolean first = true;
    for (double value : array) {
      if (first) first = false; else getWriter ().writeCharacters (",");
      getWriter ().writeCharacters (Double.toString (value));
    }
  }
  
  protected boolean fudgeFieldStart (final Short ordinal, final String name, final FudgeFieldType type) throws XMLStreamException {
    String ename = null;
    if (getPreserveFieldNames ()) {
      ename = convertFieldName (name);
    }
    if (ename == null) {
      if (ordinal != null) {
        if (getCurrentTaxonomy () != null) {
          ename = convertFieldName (getCurrentTaxonomy ().getFieldName (ordinal));
        }
      }
      if (ename == null) {
        ename = getFieldElementName ();
        if ((ename != null) && (ordinal != null) && getAppendFieldOrdinal ()) {
          ename = ename + ordinal;
        }
      }
    }
    if (ename == null) return false;
    getWriter ().writeStartElement (ename);
    if ((ordinal != null) && (getFieldAttributeOrdinal () != null)) {
      getWriter ().writeAttribute (getFieldAttributeOrdinal (), ordinal.toString ());
    }
    if ((name != null) && !name.equals (ename) && (getFieldAttributeName () != null)) {
      getWriter ().writeAttribute (getFieldAttributeName (), name);
    }
    if (getFieldAttributeType () != null) {
      final String typeString = fudgeTypeIdToString (type.getTypeId ());
      if (typeString != null) {
        getWriter ().writeAttribute (getFieldAttributeType (), typeString);
      }
    }
    return true;
  }
  
  @SuppressWarnings("unchecked")
  protected void fudgeFieldValue (final FudgeFieldType type, Object fieldValue) throws XMLStreamException {
    if (type instanceof SecondaryFieldTypeBase<?,?,?>) {
      fieldValue = ((SecondaryFieldTypeBase<Object,Object,Object>)type).secondaryToPrimary(fieldValue);
    }
    switch (type.getTypeId ()) {
    case FudgeTypeDictionary.INDICATOR_TYPE_ID :
      // no content
      break;
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      getWriter ().writeCharacters ((Boolean)fieldValue ? getBooleanTrue () : getBooleanFalse ());
      break;
    case FudgeTypeDictionary.BYTE_TYPE_ID:
    case FudgeTypeDictionary.SHORT_TYPE_ID:
    case FudgeTypeDictionary.INT_TYPE_ID:
    case FudgeTypeDictionary.LONG_TYPE_ID:
    case FudgeTypeDictionary.FLOAT_TYPE_ID:
    case FudgeTypeDictionary.DOUBLE_TYPE_ID:
    case FudgeTypeDictionary.STRING_TYPE_ID:
    case FudgeTypeDictionary.DATE_TYPE_ID:
    case FudgeTypeDictionary.TIME_TYPE_ID:
    case FudgeTypeDictionary.DATETIME_TYPE_ID:
      getWriter ().writeCharacters (fieldValue.toString ());
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
      if (getBase64UnknownTypes ()) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        final DataOutputStream dos = new DataOutputStream (new Base64OutputStream (baos));
        try {
          type.writeValue (dos, fieldValue);
          dos.close ();
        } catch (IOException e) {
          throw new FudgeRuntimeIOException (e);
        }
        if (getFieldAttributeEncoding () != null) {
          getWriter ().writeAttribute (getFieldAttributeEncoding (), getBase64EncodingName ());
        }
        getWriter ().writeCharacters (new String (baos.toByteArray ()));
      } else {
        getWriter ().writeCharacters (fieldValue.toString ());
      }
      break;
    }
  }
  
  protected void fudgeFieldEnd () throws XMLStreamException {
    getWriter ().writeEndElement ();
  }
  
  protected void fudgeEnvelopeEnd () throws XMLStreamException {
    if (getEnvelopeElementName () != null) {
      getWriter ().writeEndElement (); // envelope
    }
    getWriter ().writeEndDocument ();
  }

}
