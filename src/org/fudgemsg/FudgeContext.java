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
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.TaxonomyResolver;

/**
 * <p>The primary entry-point for code to interact with the rest of the Fudge system.
 * For performance reasons, there are many options that are passed around as parameters
 * inside static methods for encoding and decoding, and many lightweight objects that
 * ideally don't know of their configuration context. However, in a large application,
 * it is often desirable to collect all configuration parameters in one location and
 * inject options into it.</p>
 * 
 * <p>{@code FudgeContext} allows application developers to have a single location
 * to inject dependent parameters and instances, and make them available through
 * simple method invocations. In addition, because it wraps all checked exceptions
 * into instances of {@link FudgeRuntimeException}, it is the ideal way to use
 * the Fudge encoding system from within Spring applications.</p>
 * 
 * <p>While most applications will have a single instance of {@code FudgeContext},
 * some applications will have one instance per unit of encoding/decoding parameters.
 * For example, if an application is consuming data from two messaging feeds, each
 * of which reuses the same taxonomy ID to represent a different
 * {@link FudgeTaxonomy}, it would configure two different instances of
 * {@code FudgeContext}, one per feed.</p>
 *
 * @author kirk
 */
public class FudgeContext implements FudgeMessageFactory {
  private FudgeTypeDictionary _typeDictionary = new FudgeTypeDictionary();
  private FudgeObjectDictionary _objectDictionary = new FudgeObjectDictionary ();
  private TaxonomyResolver _taxonomyResolver;
  
  /**
   * Returns the current {@link TaxonomyResolver} used by this context. A new {@code FudgeContext} starts with its own, default,
   * taxonomy resolver. Any custom taxonomies must be registered with a resolver before they can be used.
   * 
   * @return the taxonomy resolver
   */
  public TaxonomyResolver getTaxonomyResolver() {
    return _taxonomyResolver;
  }

  /**
   * Sets the {@link TaxonomyResolver} to be used by this context when expanding field names for incoming Fudge messages.
   * 
   * @param taxonomyResolver the {@link TaxonomyResolver} to set
   */
  public void setTaxonomyResolver(TaxonomyResolver taxonomyResolver) {
    _taxonomyResolver = taxonomyResolver;
  }
  
  /**
   * Returns the current {@link FudgeTypeDictionary} used by this context and any messages created or decoded through it. A new
   * {@code FudgeContext} starts with its own, default, type dictionary. Any custom types must be registered with the dictionary
   * before they can be used.
   * 
   * @return the current {@code FudgeTypeDictionary}
   */ 
  public FudgeTypeDictionary getTypeDictionary() {
    return _typeDictionary;
  }

  /**
   * Sets the current {@link FudgeTypeDictionary} to be used by the context and any messages created or decoded through it.
   * 
   * @param typeDictionary the new {@code FudgeTypeDictionary}
   */
  public void setTypeDictionary(FudgeTypeDictionary typeDictionary) {
    if(typeDictionary == null) {
      throw new NullPointerException("Every fudge context must have a type dictionary.");
    }
    _typeDictionary = typeDictionary;
  }
  
  /**
   * Returns the current {@link FudgeObjectDictionary} used by the context for object/Fudge message serialisation and deserialisation.
   * A new {@code FudgeContext} starts with its own, default, object dictionary. Any custom object or message builders must be
   * registered with the dictionary before they can be used.
   * 
   * @return the current {@code FudgeObjectDictionary}
   */
  public FudgeObjectDictionary getObjectDictionary() {
    return _objectDictionary;
  }

  /**
   * Sets the current {@link FudgeObjectDictionary} to be used for object/Fudge message serialisation and deserialisation.
   * 
   * @param objectDictionary the new {@code FudgeObjectDictionary}
   */
  public void setObjectDictionary(FudgeObjectDictionary objectDictionary) {
    if(objectDictionary == null) {
      throw new NullPointerException("Every fudge context must have an object dictionary.");
    }
    _objectDictionary = objectDictionary;
  }
  
  /**
   * {@inheritDoc}
   */ 
  @Override
  public MutableFudgeFieldContainer newMessage() {
    return new FudgeMsg(this);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public MutableFudgeFieldContainer newMessage (final FudgeFieldContainer fromMessage) {
    return new FudgeMsg (fromMessage, this);
  }
  
  /**
   * Encodes a Fudge message object to an {@link OutputStream} without any
   * taxonomy reference.
   * 
   * @param msg
   *          the {@code FudgeFieldContainer} to write
   * @param os
   *          the {@code OutputStream} to write to
   * @throws IOException if the target {@code OutputStream} errors
   */
  public void serialize(FudgeFieldContainer msg, OutputStream os) throws IOException {
    serialize(msg, null, os);
  }

  /**
   * Encodes a Fudge message object to an {@link OutputStream} with an
   * optional taxonomy reference. If a taxonomy is supplied it may be used to
   * optimize the output by writing ordinals instead of field names.
   * 
   * @param msg
   *          the {@code FudgeMsg} to write
   * @param taxonomyId
   *          the identifier of the taxonomy to use. Specify {@code null} for no
   *          taxonomy.
   * @param os
   *          the {@code OutputStream} to write to
   * @throws IOException if the target {@code OutputStream} errors
   */
  public void serialize(FudgeFieldContainer msg, Short taxonomyId, OutputStream os) throws IOException {
    int realTaxonomyId = (taxonomyId == null) ? 0 : taxonomyId.intValue();
    FudgeMsgWriter writer = createMessageWriter (os);
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    writer.writeMessageEnvelope(envelope, realTaxonomyId);
  }
  
  /**
   * Returns the Fudge encoded form of a {@link FudgeFieldContainer} as a {@code byte} array
   * with a taxonomy reference. The encoding includes an envelope header.
   * 
   * @param msg the {@code FudgeFieldContainer} to encode
   * @param taxonomyId the identifier of the taxonomy to use. Specify {@code null} or {@code 0} for no taxonomy
   * @return an array containing the encoded message
   */
  public byte[] toByteArray(FudgeFieldContainer msg, Short taxonomyId) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serialize(msg, taxonomyId, baos);
      return baos.toByteArray();
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("Couldn't serialize message", ioe);
    }
  }
  
  /**
   * Returns the Fudge encoded form of a {@link FudgeFieldContainer} as a {@code byte} array without a taxonomy reference.
   * The encoding includes an envelope header.
   * 
   * @param msg the {@code FudgeFieldContainer} to encode
   * @return an array containing the encoded message
   */
  public byte[] toByteArray (FudgeFieldContainer msg) {
    return toByteArray (msg, null);
  }
  
  /**
   * Decodes a Fudge message from an {@link InputStream}.
   * 
   *  @param is the {@code InputStream} to read encoded data from
   *  @return the next {@link FudgeMsgEnvelope} encoded on the stream
   *  @throws IOException if the target {@code InputStream} errors
   */
  public FudgeMsgEnvelope deserialize(InputStream is) throws IOException {
    FudgeMsgReader reader = createMessageReader (is);
    FudgeMsgEnvelope envelope = reader.nextMessageEnvelope ();
    return envelope;
  }

  /**
   * Decodes a Fudge message from a {@code byte} array. If the array is
   * larger than the Fudge envelope, any additional data is ignored.
   * 
   * @param bytes
   *          an array containing the encoded Fudge message including its envelope
   * @return the decoded {@link FudgeMsgEnvelope}
   */
  public FudgeMsgEnvelope deserialize(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    try {
      return deserialize(bais);
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("Couldn't deserialize array", ioe);
    }
  }
  
  /**
   * Creates a new reader for extracting Fudge stream elements from an {@link InputStream}.
   * 
   * @param is the {@code InputStream} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader createReader (final InputStream is) {
    return new FudgeDataInputStreamReader (this, is);
  }
  
  /**
   * Creates a new reader for extracting Fudge stream elements from a {@link DataInput}.
   * 
   * @param di the {@code DataInput} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader createReader (final DataInput di) {
    return new FudgeDataInputStreamReader (this, di);
  }
  
  /**
   * Creates a new writer for encoding Fudge stream elements to a {@link OutputStream}.
   * 
   * @param outputStream the {@code OutputStream} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter createWriter (final OutputStream outputStream) {
    return new FudgeDataOutputStreamWriter(this, outputStream);
  }
  
  /**
   * Creates a new writer for encoding Fudge stream elements to a {@link DataOutput}.
   * 
   * @param dataOutput the {@code DataOutput} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter createWriter (final DataOutput dataOutput) {
    return new FudgeDataOutputStreamWriter (this, dataOutput);
  }
  
  /**
   * Creates a new reader for extracting whole Fudge messages from a {@link DataInput} source.
   * 
   * @param dataInput the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader createMessageReader (final DataInput dataInput) {
    return new FudgeMsgReader (createReader (dataInput));
  }
  
  /**
   * Creates a new reader for extracting whole Fudge messages from a {@link InputStream} source.
   * 
   * @param inputStream the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader createMessageReader (final InputStream inputStream) {
    return new FudgeMsgReader (createReader (inputStream));
  }
  
  /**
   * Creates a new writer for sending whole Fudge messages to a {@link DataOutput} target.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter createMessageWriter (final DataOutput dataOutput) {
    return new FudgeMsgWriter (createWriter (dataOutput));
  }
  
  /**
   * Creates a new writer for sending whole Fudge messages to a {@link OutputStream} target.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter createMessageWriter (final OutputStream outputStream) {
    return new FudgeMsgWriter (createWriter (outputStream));
  }
  
  /**
   * Creates a new reader for deserialising Java objects from a Fudge data source.
   * 
   * @param dataInput the {@code DataInput} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader createObjectReader (final DataInput dataInput) {
    return new FudgeObjectReader (createMessageReader (dataInput));
  }
  
  /**
   * Creates a new reader for deserialising Java objects from a Fudge data source.
   * 
   * @param inputStream the {@code InputStream} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader createObjectReader (final InputStream inputStream) {
    return new FudgeObjectReader (createMessageReader (inputStream));
  }
  
  /**
   * Creates a new writer for serialising Java objects to a Fudge stream.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter createObjectWriter (final DataOutput dataOutput) {
    return new FudgeObjectWriter (createMessageWriter (dataOutput));
  }
  
  /**
   * Creates a new writer for serialising Java objects to a Fudge stream.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter createObjectWriter (final OutputStream outputStream) {
    return new FudgeObjectWriter (createMessageWriter (outputStream));
  }
  
  /**
   * Writes a Java object to an {@link OutputStream} using the Fudge serialisation framework. The
   * current {@link FudgeObjectDictionary} will be used to identify any custom message builders or apply
   * default serialisation behaviour.
   * 
   * @param object the {@link Object} to write
   * @param outputStream the {@code OutputStream} to write the Fudge encoded form of the object to
   * @throws IOException if the target {@code OutputStream} errors
   */
  public void writeObject(Object object, OutputStream outputStream) throws IOException {
    if(object == null) {
      return;
    }
    FudgeObjectWriter osw = createObjectWriter (outputStream);
    osw.write (object);
  }
  
  /**
   * Reads a Java object from an {@link InputStream} using the Fudge serialisation framework. The
   * current {@link FudgeObjectDictionary} will be used to identify any custom object builders or apply
   * default deserialisation behaviour. Always reads the next available Fudge message from the
   * stream even if the message cannot be converted to the requested Object.
   * 
   * @param <T> the target type to decode the message to
   * @param objectClass the target {@link Class} to decode a message of. If an object of this or a sub-class is not available, an exception will be thrown.
   * @param inputStream the {@code InputStream} to read the next Fudge message from
   * @throws IOException if the source {@code InputStream} errors
   * @return the object read
   */
  public <T> T readObject(Class<T> objectClass, InputStream inputStream) throws IOException {
    FudgeObjectReader osr = createObjectReader (inputStream);
    T result = osr.read (objectClass);
    return result;
  }
  
  /**
   * Type conversion for secondary types using information registered in the current type dictionary.
   * See {@link FudgeTypeDictionary#getFieldValue} for more information.
   * 
   * @param <T> type to convert to
   * @param clazz target class for the converted value
   * @param field field containing the value to convert
   * @return the converted value
   */
  public <T> T getFieldValue (final Class<T> clazz, final FudgeField field) {
    return getTypeDictionary ().getFieldValue (clazz, field);
  }
  
}
