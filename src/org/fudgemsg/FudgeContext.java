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
import org.fudgemsg.mapping.FudgeObjectStreamReader;
import org.fudgemsg.mapping.FudgeObjectStreamWriter;
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
  private final Queue<FudgeMsgStreamReader> _messageStreamReaders = new LinkedBlockingQueue<FudgeMsgStreamReader>();
  private final Queue<FudgeMsgStreamWriter> _messageStreamWriters = new LinkedBlockingQueue<FudgeMsgStreamWriter>();
  private final Queue<FudgeObjectStreamReader> _objectStreamReaders = new LinkedBlockingQueue<FudgeObjectStreamReader>();
  private final Queue<FudgeObjectStreamWriter> _objectStreamWriters = new LinkedBlockingQueue<FudgeObjectStreamWriter>();
  private FudgeTypeDictionary _typeDictionary = new FudgeTypeDictionary();
  private FudgeObjectDictionary _objectDictionary = new FudgeObjectDictionary ();
  private TaxonomyResolver _taxonomyResolver;
  
  /**
   * @return the taxonomyResolver
   */
  public TaxonomyResolver getTaxonomyResolver() {
    return _taxonomyResolver;
  }

  /**
   * @param taxonomyResolver the taxonomyResolver to set
   */
  public void setTaxonomyResolver(TaxonomyResolver taxonomyResolver) {
    _taxonomyResolver = taxonomyResolver;
  }
  
  public FudgeTypeDictionary getTypeDictionary() {
    return _typeDictionary;
  }

  public void setTypeDictionary(FudgeTypeDictionary typeDictionary) {
    if(typeDictionary == null) {
      throw new NullPointerException("Every fudge context must have a type dictionary.");
    }
    _typeDictionary = typeDictionary;
  }
  
  public FudgeObjectDictionary getObjectDictionary() {
    return _objectDictionary;
  }

  public void setObjectDictionary(FudgeObjectDictionary objectDictionary) {
    if(objectDictionary == null) {
      throw new NullPointerException("Every fudge context must have an object dictionary.");
    }
    _objectDictionary = objectDictionary;
  }
  
  @Override
  public MutableFudgeFieldContainer newMessage() {
    return new FudgeMsg(this);
  }
  
  @Override
  public MutableFudgeFieldContainer newMessage (final FudgeFieldContainer fromMessage) {
    return new FudgeMsg (fromMessage, this);
  }
  
  /**
   * Encodes a {@link FudgeMsg} object to an {@link OutputStream} without any
   * taxonomy reference.
   * 
   * @param msg
   *          the {@code FudgeMsg} to write
   * @param os
   *          the {@code OutputStream} to write to
   */
  public void serialize(FudgeFieldContainer msg, OutputStream os) throws IOException {
    serialize(msg, null, os);
  }

  /**
   * Encodes a {@link FudgeMsg} object to an {@link OutputStream} with an
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
   */
  public void serialize(FudgeFieldContainer msg, Short taxonomyId, OutputStream os) throws IOException {
    int realTaxonomyId = (taxonomyId == null) ? 0 : taxonomyId.intValue();
    FudgeMsgStreamWriter writer = allocateMessageWriter (os);
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    writer.writeMessageEnvelope(envelope, realTaxonomyId);
    releaseMessageWriter(writer);
  }
  
  /**
   * Returns the Fudge encoded form of a {@link FudgeMsg} as a {@code byte} array
   * without a taxonomy reference. 
   * 
   * @param msg the {@code FudgeMsg} to encode
   * @param taxonomyId the identifier of the taxonomy to use. Specify {@code null} for no taxonomy
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
  
  public byte[] toByteArray (FudgeFieldContainer msg) {
    return toByteArray (msg, null);
  }
  
  /**
   * Decodes a {@link FudgeMsg} from an {@link InputStream}.
   * 
   *  @param is the {@code InputStream} to read encoded data from
   *  @return the next {@link FudgeMsgEnvelope} encoded on the stream
   */
  public FudgeMsgEnvelope deserialize(InputStream is) throws IOException {
    FudgeMsgStreamReader reader = allocateMessageReader (is);
    FudgeMsgEnvelope envelope = reader.nextMessageEnvelope ();
    releaseMessageReader (reader);
    return envelope;
  }

  /**
   * Decodes a {@link FudgeMsg} from a {@code byte} array. If the array is
   * larger than the Fudge envelope, any additional data is ignored.
   * 
   * @param bytes
   *          an array containing the envelope encoded {@code FudgeMsg}
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
  
  // TODO 2010-01-20 Andrew -- the pooling of reader and writer objects needs reviewing; how big can the pools get etc...? Is there a standard component in the Java libraries for managing it? 
  
  public FudgeStreamReader allocateReader (final InputStream is) {
    FudgeDataInputStreamReader reader = _streamReaders.poll();
    if (reader == null) {
      reader = new FudgeDataInputStreamReader (this, is);
    } else {
      reader.reset (is);
    }
    return reader;
  }
  
  public FudgeStreamReader allocateReader (final DataInput di) {
    FudgeDataInputStreamReader reader = _streamReaders.poll();
    if (reader == null) {
      reader = new FudgeDataInputStreamReader (this, di);
    } else {
      reader.reset (di);
    }
    return reader;
  }
  
  public void releaseReader(FudgeStreamReader reader) {
    if(reader == null) {
      return;
    }
    reader.close ();
    if (reader instanceof FudgeDataInputStreamReader) {
      _streamReaders.add((FudgeDataInputStreamReader)reader);
    }
  }
  
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
  
  public void releaseWriter(FudgeStreamWriter writer) {
    if(writer == null) {
      return;
    }
    try {
      writer.close ();
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("Couldn't release writer " + writer, ioe);
    }
    if (writer instanceof FudgeDataOutputStreamWriter) {
      _streamWriters.add((FudgeDataOutputStreamWriter)writer);
    }
  }
  
  public FudgeMsgStreamReader allocateMessageReader (final FudgeStreamReader streamReader) {
    FudgeMsgStreamReader reader = _messageStreamReaders.poll ();
    if (reader == null) {
      reader = new FudgeMsgStreamReader (streamReader);
    } else {
      reader.reset (streamReader);
    }
    return reader;
  }
  
  public FudgeMsgStreamReader allocateMessageReader (final DataInput dataInput) {
    return allocateMessageReader (allocateReader (dataInput));
  }
  
  public FudgeMsgStreamReader allocateMessageReader (final InputStream inputStream) {
    return allocateMessageReader (allocateReader (inputStream));
  }
  
  public void releaseMessageReader (final FudgeMsgStreamReader reader) {
    if (reader == null) {
      return;
    }
    reader.close ();
    _messageStreamReaders.add (reader);
  }
  
  public FudgeMsgStreamWriter allocateMessageWriter (final FudgeStreamWriter streamWriter) {
    FudgeMsgStreamWriter writer = _messageStreamWriters.poll ();
    if (writer == null) {
      writer = new FudgeMsgStreamWriter (streamWriter);
    } else {
      try {
        writer.reset (streamWriter);
      } catch (IOException ioe) {
        throw new FudgeRuntimeException ("Couldn't reallocate writer " + writer, ioe);
      }
    }
    return writer;
  }
  
  public FudgeMsgStreamWriter allocateMessageWriter (final DataOutput dataOutput) {
    return allocateMessageWriter (allocateWriter (dataOutput));
  }
  
  public FudgeMsgStreamWriter allocateMessageWriter (final OutputStream outputStream) {
    return allocateMessageWriter (allocateWriter (outputStream));
  }
  
  public void releaseMessageWriter (final FudgeMsgStreamWriter writer) {
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
  
  public FudgeObjectStreamReader allocateObjectReader (final FudgeMsgStreamReader messageReader) {
    FudgeObjectStreamReader reader = _objectStreamReaders.poll ();
    if (reader == null) {
      reader = new FudgeObjectStreamReader (messageReader);
    } else {
      reader.reset (messageReader);
    }
    return reader;
  }
  
  public FudgeObjectStreamReader allocateObjectReader (final FudgeStreamReader streamReader) {
    return allocateObjectReader (allocateMessageReader (streamReader));
  }
  
  public FudgeObjectStreamReader allocateObjectReader (final DataInput dataInput) {
    return allocateObjectReader (allocateMessageReader (dataInput));
  }
  
  public FudgeObjectStreamReader allocateObjectReader (final InputStream inputStream) {
    return allocateObjectReader (allocateMessageReader (inputStream));
  }
  
  public void releaseObjectReader (final FudgeObjectStreamReader reader) {
    if (reader == null) return;
    reader.close ();
    _objectStreamReaders.add (reader);
  }
  
  public FudgeObjectStreamWriter allocateObjectWriter (final FudgeMsgStreamWriter messageWriter) {
    FudgeObjectStreamWriter writer = _objectStreamWriters.poll ();
    if (writer == null) {
      writer = new FudgeObjectStreamWriter (messageWriter);
    } else {
      writer.reset (messageWriter);
    }
    return writer;
  }
  
  public FudgeObjectStreamWriter allocateObjectWriter (final FudgeStreamWriter streamWriter) {
    return allocateObjectWriter (allocateMessageWriter (streamWriter));
  }
  
  public FudgeObjectStreamWriter allocateObjectWriter (final DataOutput dataOutput) {
    return allocateObjectWriter (allocateMessageWriter (dataOutput));
  }
  
  public FudgeObjectStreamWriter allocateObjectWriter (final OutputStream outputStream) {
    return allocateObjectWriter (allocateMessageWriter (outputStream));
  }
  
  public void releaseObjectWriter (final FudgeObjectStreamWriter writer) {
    if (writer == null) return;
    writer.close ();
    _objectStreamWriters.add (writer);
  }
  
  public void writeObject(Object object, OutputStream outputStream) throws IOException {
    if(object == null) {
      return;
    }
    FudgeObjectStreamWriter osw = allocateObjectWriter (outputStream);
    osw.write (object);
    releaseObjectWriter (osw);
  }
  
  public <T> T readObject(Class<T> objectClass, InputStream inputStream) throws IOException {
    FudgeObjectStreamReader osr = allocateObjectReader (inputStream);
    T result = osr.read (objectClass);
    releaseObjectReader (osr);
    return result;
  }
  
}
