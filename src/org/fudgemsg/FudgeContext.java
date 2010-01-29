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
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.TaxonomyResolver;

/**
 * The primary entry-point for code to interact with the rest of the Fudge system.
 * For performance reasons, there are many options that are passed around as parameters
 * inside static methods for encoding and decoding, and many lightweight objects that
 * ideally don't know of their configuration context.
 * However, in a large application, it is often desirable to collect all configuration
 * parameters in one location and inject options into it.
 * <p/>
 * {@code FudgeContext} allows application developers to have a single location
 * to inject dependent parameters and instances, and make them available through
 * simple method invocations. In addition, because it wraps all checked exceptions
 * into instances of {@link FudgeRuntimeException}, it is the ideal way to use
 * the Fudge encoding system from within Spring applications.
 * <p/>
 * While most applications will have a single instance of {@code FudgeContext},
 * some applications will have one instance per unit of encoding/decoding parameters.
 * For example, if an application is consuming data from two messaging feeds, each
 * of which reuses the same taxonomy ID to represent a different
 * {@link FudgeTaxonomy}, it would configure two different instances of
 * {@code FudgeContext}, one per feed.
 *
 * @author kirk
 */
public class FudgeContext implements FudgeMessageFactory {
  private final Queue<FudgeDataInputStreamReader> _streamReaders = new LinkedBlockingQueue<FudgeDataInputStreamReader>();
  private final Queue<FudgeDataOutputStreamWriter> _streamWriters = new LinkedBlockingQueue<FudgeDataOutputStreamWriter>();
  private final Queue<FudgeMsgReader> _messageStreamReaders = new LinkedBlockingQueue<FudgeMsgReader>();
  private final Queue<FudgeMsgWriter> _messageStreamWriters = new LinkedBlockingQueue<FudgeMsgWriter>();
  private final Queue<FudgeObjectReader> _objectStreamReaders = new LinkedBlockingQueue<FudgeObjectReader>();
  private final Queue<FudgeObjectWriter> _objectStreamWriters = new LinkedBlockingQueue<FudgeObjectWriter>();
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
    FudgeMsgWriter writer = allocateMessageWriter (os);
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    writer.writeMessageEnvelope(envelope, realTaxonomyId);
    releaseMessageWriter(writer);
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
    FudgeMsgReader reader = allocateMessageReader (is);
    FudgeMsgEnvelope envelope = reader.nextMessageEnvelope ();
    releaseMessageReader (reader);
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
  
  // TODO 2010-01-20 Andrew -- the pooling of reader and writer objects needs
  // reviewing; how big can the pools get etc...? Is there a standard component
  // in the Java libraries for managing it? Do we need to pool them?
  
  /**
   * Creates a new, or returns a pooled, reader for extracting Fudge stream elements from an {@link InputStream}.
   * 
   * @param is the {@code InputStream} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader allocateReader (final InputStream is) {
    FudgeDataInputStreamReader reader = _streamReaders.poll();
    if (reader == null) {
      reader = new FudgeDataInputStreamReader (this, is);
    } else {
      reader.reset (is);
    }
    return reader;
  }
  
  /**
   * Creates a new, or returns a pooled, reader for extracting Fudge stream elements from a {@link DataInput}.
   * 
   * @param di the {@code DataInput} to read from
   * @return the {@link FudgeStreamReader}
   */
  public FudgeStreamReader allocateReader (final DataInput di) {
    FudgeDataInputStreamReader reader = _streamReaders.poll();
    if (reader == null) {
      reader = new FudgeDataInputStreamReader (this, di);
    } else {
      reader.reset (di);
    }
    return reader;
  }
  
  /**
   * Releases a {@link FudgeStreamReader} allocated by a call to {@link #allocateReader(DataInput)} to a common pool. The
   * caller must not retain any references or continue to use the {@code FudgeStreamReader} after calling this
   * method. {@link FudgeStreamReader#close()} will be called on the reader first.
   * 
   * @param reader the {@code FudgeStreamReader} to return
   */
  public void releaseReader(FudgeStreamReader reader) {
    if(reader == null) {
      return;
    }
    reader.close ();
    if ((reader.getFudgeContext () == this) && (reader instanceof FudgeDataInputStreamReader)) {
      // Only put back in the pool if it was the correct type and for our context
      _streamReaders.add((FudgeDataInputStreamReader)reader);
    }
  }
  
  /**
   * Creates a new, or returns a pooled, writer for encoding Fudge stream elements to a {@link OutputStream}.
   * 
   * @param outputStream the {@code OutputStream} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter allocateWriter (final OutputStream outputStream) {
    FudgeDataOutputStreamWriter writer = _streamWriters.poll();
    if(writer == null) {
      writer = new FudgeDataOutputStreamWriter(this, outputStream);
    } else {
      try {
        writer.reset (outputStream);
      } catch (IOException ioe) {
        throw new FudgeRuntimeException ("Couldn't reallocate writer " + writer, ioe);
      }
    }
    return writer;
  }
  
  /**
   * Creates a new, or returns a pooled, writer for encoding Fudge stream elements to a {@link DataOutput}.
   * 
   * @param dataOutput the {@code DataOutput} to write to
   * @return the {@link FudgeStreamWriter}
   */
  public FudgeStreamWriter allocateWriter (final DataOutput dataOutput) {
    FudgeDataOutputStreamWriter writer = _streamWriters.poll();
    if(writer == null) {
      writer = new FudgeDataOutputStreamWriter(this, dataOutput);
    } else {
      try {
        writer.reset (dataOutput);
      } catch (IOException ioe) {
        throw new FudgeRuntimeException ("Couldn't reallocate writer " + writer, ioe);
      }
    }
    return writer;
  }
  
  /**
   * Releases a {@link FudgeStreamWriter} allocated by a call to {@link #allocateWriter(DataOutput)} to a common pool. The
   * caller must not retain any references or continue to use the {@code FudgeStreamWriter} after calling this
   * method. {@link FudgeStreamWriter#close()} will be called on the writer first. Any {@link IOException} from closing the
   * writer will be wrapped in a {@link FudgeRuntimeException}. If the underlying exception is required, the caller should
   * call {@code close()} on the stream before releasing it.
   * 
   * @param writer the {@code FudgeStreamWriter} to return
   */
  public void releaseWriter(FudgeStreamWriter writer) {
    if(writer == null) {
      return;
    }
    try {
      writer.close ();
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("Couldn't release writer " + writer, ioe);
    }
    if ((writer.getFudgeContext () == this) && (writer instanceof FudgeDataOutputStreamWriter)) {
      // Only put back in the pool if it was the correct type and our context
      _streamWriters.add((FudgeDataOutputStreamWriter)writer);
    }
  }
  
  /**
   * Creates a new, or returns a pooled, reader for extracting whole Fudge messages from a {@link FudgeStreamReader}.
   * 
   * @param streamReader the source of Fudge message elements
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader allocateMessageReader (final FudgeStreamReader streamReader) {
    FudgeMsgReader reader = _messageStreamReaders.poll ();
    if (reader == null) {
      reader = new FudgeMsgReader (streamReader);
    } else {
      reader.reset (streamReader);
    }
    return reader;
  }
  
  /**
   * Creates a new, or returns a pooled, reader for extracting whole Fudge messages from a {@link DataInput} source.
   * 
   * @param dataInput the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader allocateMessageReader (final DataInput dataInput) {
    return allocateMessageReader (allocateReader (dataInput));
  }
  
  /**
   * Creates a new, or returns a pooled, reader for extracting whole Fudge messages from a {@link InputStream} source.
   * 
   * @param inputStream the source of data
   * @return the {@code FudgeMsgReader}
   */
  public FudgeMsgReader allocateMessageReader (final InputStream inputStream) {
    return allocateMessageReader (allocateReader (inputStream));
  }
  
  /**
   * Releases a {@link FudgeMsgReader} allocated by a call to {@code allocateMessageReader} back to a common pool. The
   * caller must not retain any references or continue to use the {@code FudgeMsgReader} after calling this method.
   * {@link FudgeMsgReader#close()} will be called on the reader.
   * 
   * @param reader the {@code FudgeMsgReader} to release
   */
  public void releaseMessageReader (final FudgeMsgReader reader) {
    if (reader == null) {
      return;
    }
    reader.close ();
    _messageStreamReaders.add (reader);
  }
  
  /**
   * Creates a new, or returns a pooled, writer for sending whole Fudge messages to a {@link FudgeStreamWriter} target.
   * 
   * @param streamWriter the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter allocateMessageWriter (final FudgeStreamWriter streamWriter) {
    FudgeMsgWriter writer = _messageStreamWriters.poll ();
    if (writer == null) {
      writer = new FudgeMsgWriter (streamWriter);
    } else {
      try {
        writer.reset (streamWriter);
      } catch (IOException ioe) {
        throw new FudgeRuntimeException ("Couldn't reallocate writer " + writer, ioe);
      }
    }
    return writer;
  }
  
  /**
   * Creates a new, or returns a pooled, writer for sending whole Fudge messages to a {@link DataOutput} target.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter allocateMessageWriter (final DataOutput dataOutput) {
    return allocateMessageWriter (allocateWriter (dataOutput));
  }
  
  /**
   * Creates a new, or returns a pooled, writer for sending whole Fudge messages to a {@link OutputStream} target.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeMsgWriter}
   */
  public FudgeMsgWriter allocateMessageWriter (final OutputStream outputStream) {
    return allocateMessageWriter (allocateWriter (outputStream));
  }
  
  /**
   * Releases a {@link FudgeMsgWriter} allocated by a call to {@code allocateMessageWriter} back to a common pool. The
   * caller must not retain any references or continue to use the {@code FudgeMsgWriter} after calling this method.
   * {@link FudgeMsgWriter#close()} will be called on the writer first. Any {@link IOException} from closing the
   * writer will be wrapped in a {@link FudgeRuntimeException}. If the underlying exception is required, the caller should
   * call {@code close()} on the writer before releasing it.
   * 
   * @param writer the {@code FudgeMsgWriter} to return
   */
  public void releaseMessageWriter (final FudgeMsgWriter writer) {
    if (writer == null) {
      return;
    }
    try {
      writer.close ();
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("Couldn't release writer " + writer, ioe);
    }
    _messageStreamWriters.add (writer);
  }
  
  /**
   * Creates a new, or returns a pooled, reader for deserialising Java objects from a source of Fudge messages.
   * 
   * @param messageReader the {@link FudgeMsgReader} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader allocateObjectReader (final FudgeMsgReader messageReader) {
    FudgeObjectReader reader = _objectStreamReaders.poll ();
    if (reader == null) {
      reader = new FudgeObjectReader (messageReader);
    } else {
      reader.reset (messageReader);
    }
    return reader;
  }
  
  /**
   * Creates a new, or returns a pooled, reader for deserialising Java objects from a Fudge message stream.
   * 
   * @param streamReader the {@link FudgeStreamReader} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader allocateObjectReader (final FudgeStreamReader streamReader) {
    return allocateObjectReader (allocateMessageReader (streamReader));
  }
  
  /**
   * Creates a new, or returns a pooled, reader for deserialising Java objects from a Fudge data source.
   * 
   * @param dataInput the {@code DataInput} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader allocateObjectReader (final DataInput dataInput) {
    return allocateObjectReader (allocateMessageReader (dataInput));
  }
  
  /**
   * Creates a new, or returns a pooled, reader for deserialising Java objects from a Fudge data source.
   * 
   * @param inputStream the {@code InputStream} to read from
   * @return the {@link FudgeObjectReader}
   */
  public FudgeObjectReader allocateObjectReader (final InputStream inputStream) {
    return allocateObjectReader (allocateMessageReader (inputStream));
  }
  
  /**
   * Releases a {@link FudgeObjectReader} created by a call to {@code allocateObjectReader} back to a common pool.
   * The caller must not retain a reference to or continue to use the reader after this method call. {@link FudgeObjectReader#close()}
   * will be called on the reader first.
   * 
   * @param reader the {@code FudgeObjectReader} to release
   */
  public void releaseObjectReader (final FudgeObjectReader reader) {
    if (reader == null) return;
    reader.close ();
    _objectStreamReaders.add (reader);
  }
  
  /**
   * Creates a new, or returns a pooled, writer for serialising Java objects to a Fudge message stream.
   * 
   * @param messageWriter the target for Fudge messages
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter allocateObjectWriter (final FudgeMsgWriter messageWriter) {
    FudgeObjectWriter writer = _objectStreamWriters.poll ();
    if (writer == null) {
      writer = new FudgeObjectWriter (messageWriter);
    } else {
      writer.reset (messageWriter);
    }
    return writer;
  }
  
  /**
   * Creates a new, or returns a pooled, writer for serialising Java objects to a Fudge stream.
   * 
   * @param streamWriter the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter allocateObjectWriter (final FudgeStreamWriter streamWriter) {
    return allocateObjectWriter (allocateMessageWriter (streamWriter));
  }
  
  /**
   * Creates a new, or returns a pooled, writer for serialising Java objects to a Fudge stream.
   * 
   * @param dataOutput the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter allocateObjectWriter (final DataOutput dataOutput) {
    return allocateObjectWriter (allocateMessageWriter (dataOutput));
  }
  
  /**
   * Creates a new, or returns a pooled, writer for serialising Java objects to a Fudge stream.
   * 
   * @param outputStream the target to write to
   * @return the {@link FudgeObjectWriter}
   */
  public FudgeObjectWriter allocateObjectWriter (final OutputStream outputStream) {
    return allocateObjectWriter (allocateMessageWriter (outputStream));
  }
  
  /**
   * Releases a {@link FudgeObjectWriter} created by a call to {@code allocateObjectWriter} back to a common pool.
   * The caller must not retain a reference to or continue to use the writer after this method call. {@link FudgeObjectWriter#close()}
   * will be called on the writer first.
   * 
   * @param writer the {@code FudgeObjectWriter} to release
   */
  public void releaseObjectWriter (final FudgeObjectWriter writer) {
    if (writer == null) return;
    writer.close ();
    _objectStreamWriters.add (writer);
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
    FudgeObjectWriter osw = allocateObjectWriter (outputStream);
    osw.write (object);
    releaseObjectWriter (osw);
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
    FudgeObjectReader osr = allocateObjectReader (inputStream);
    T result = osr.read (objectClass);
    releaseObjectReader (osr);
    return result;
  }
  
}
