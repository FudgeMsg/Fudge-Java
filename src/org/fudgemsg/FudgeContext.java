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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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
public class FudgeContext {
  private final Queue<FudgeStreamReader> _streamReaders = new LinkedBlockingQueue<FudgeStreamReader>();
  private final Queue<FudgeStreamWriter> _streamWriters = new LinkedBlockingQueue<FudgeStreamWriter>();
  private final FudgeStreamParser _parser = new FudgeStreamParser(this);
  private final FudgeObjectStreamReader _objectStreamReader = new FudgeObjectStreamReader();
  private final FudgeObjectStreamWriter _objectStreamWriter = new FudgeObjectStreamWriter();
  private FudgeTypeDictionary _typeDictionary = new FudgeTypeDictionary();
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
  
  public FudgeMsg newMessage() {
    return new FudgeMsg(this);
  }

  public void serialize(FudgeMsg msg, OutputStream os) {
    serialize(msg, null, os);
  }
  
  public void serialize(FudgeMsg msg, Short taxonomyId, OutputStream os) {
    int realTaxonomyId = (taxonomyId == null) ? 0 : taxonomyId.intValue();
    FudgeStreamWriter writer = allocateWriter();
    writer.reset(os);
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    writer.writeMessageEnvelope(envelope, realTaxonomyId);
    releaseWriter(writer);
  }
  
  public byte[] toByteArray(FudgeMsg msg) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialize(msg, baos);
    return baos.toByteArray();
  }
  
  public FudgeMsgEnvelope deserialize(InputStream is) {
    DataInputStream dis = new DataInputStream(is);
    FudgeMsgEnvelope envelope = getParser().parse(dis);
    return envelope;
  }
  
  public FudgeMsgEnvelope deserialize(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    return deserialize(bais);
  }
  
  public FudgeStreamReader allocateReader() {
    FudgeStreamReader reader = _streamReaders.poll();
    if(reader == null) {
      reader = new FudgeStreamReader(this);
    }
    return reader;
  }
  
  public void releaseReader(FudgeStreamReader reader) {
    if(reader == null) {
      return;
    }
    _streamReaders.add(reader);
  }
  
  public FudgeStreamWriter allocateWriter() {
    FudgeStreamWriter writer = _streamWriters.poll();
    if(writer == null) {
      writer = new FudgeStreamWriter(this);
    }
    return writer;
  }
  
  public void releaseWriter(FudgeStreamWriter writer) {
    if(writer == null) {
      return;
    }
    _streamWriters.add(writer);
  }
  
  public FudgeStreamParser getParser() {
    return _parser;
  }

  /**
   * @return the objectStreamReader
   */
  public FudgeObjectStreamReader getObjectStreamReader() {
    return _objectStreamReader;
  }

  /**
   * @return the objectStreamWriter
   */
  public FudgeObjectStreamWriter getObjectStreamWriter() {
    return _objectStreamWriter;
  }
  
  public void writeObject(Object object, OutputStream outputStream) {
    if(object == null) {
      return;
    }
    FudgeStreamWriter fsw = allocateWriter();
    fsw.reset(outputStream);
    getObjectStreamWriter().write(object, fsw);
    releaseWriter(fsw);
  }
  
  public <T> T readObject(Class<T> objectClass, InputStream inputStream) {
    FudgeStreamReader fsr = allocateReader();
    fsr.reset(inputStream);
    T result = getObjectStreamReader().read(objectClass, fsr);
    releaseReader(fsr);
    return result;
  }
  
}
