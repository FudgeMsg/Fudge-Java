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

import java.io.Flushable;
import java.io.IOException;

public class FudgeMessageStreamWriter implements Flushable {
  
  private FudgeStreamWriter _streamWriter;
  
  private Short _defaultTaxonomyId = null;
  
  private int _defaultMessageVersion = 0;
  
  private int _defaultMessageProcessingDirectives = 0;

  public FudgeMessageStreamWriter (final FudgeStreamWriter streamWriter) {
    if (streamWriter == null) {
      throw new NullPointerException ("streamWriter cannot be null");
    }
    _streamWriter = streamWriter;
  }
  
  public void flush () throws IOException {
    getStreamWriter ().flush ();
  }
  
  public void close () throws IOException {
    if (_streamWriter == null) return;
    flush ();
    getFudgeContext ().releaseWriter (_streamWriter);
    _streamWriter = null;
  }
  
  public void reset (final FudgeStreamWriter streamWriter) throws IOException {
    close ();
    if (streamWriter == null) {
      throw new NullPointerException ("streamWriter cannot be null");
    }
    _streamWriter = streamWriter;
  }
  
  protected FudgeStreamWriter getStreamWriter () {
    return _streamWriter;
  }
  
  public FudgeContext getFudgeContext () {
    return getStreamWriter ().getFudgeContext ();
  }
  
  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder ("FudgeMessageStreamWriter{");
    if (getStreamWriter () != null) sb.append (getStreamWriter ());
    return sb.append ('}').toString ();
  }
  
  public int getDefaultTaxonomyId () {
    if (_defaultTaxonomyId == null) {
      throw new NullPointerException ("default taxonomy has not been specified");
    }
    return _defaultTaxonomyId;
  }
  
  public void setDefaultTaxonomyId (final int taxonomyId) {
    if ((taxonomyId < Short.MIN_VALUE) || (taxonomyId > Short.MAX_VALUE)) {
      throw new IllegalArgumentException ("Provided taxonomy ID " + taxonomyId + " out of range.");
    }
    _defaultTaxonomyId = (short)taxonomyId;
  }
  
  public int getDefaultMessageVersion () {
    return _defaultMessageVersion;
  }
  
  public void setDefaultMessageVersion (final int version) {
    if ((version < 0) || (version > 255)) {
      throw new IllegalArgumentException("Provided version " + version + " which doesn't fit within one byte.");
    }
    _defaultMessageVersion = version;
  }
  
  public int getDefaultMessageProcessingDirectives () {
    return _defaultMessageProcessingDirectives;
  }
  
  public void setDefaultMessageProcessingDirectives (final int processingDirectives) {
    if ((processingDirectives < 0) || (processingDirectives > 255)) {
      throw new IllegalArgumentException ("Provided processing directives " + processingDirectives + " which doesn't fit within one byte.");
    }
    _defaultMessageProcessingDirectives = processingDirectives;
  }
  
  public int writeMessage (final FudgeFieldContainer message, final int taxonomyId, final int version, final int processingDirectives) throws IOException {
    return writeMessage ((message instanceof FudgeMsg) ? (FudgeMsg)message : new FudgeMsg (message, getFudgeContext ()), taxonomyId, version, processingDirectives);
  }
  
  public int writeMessage (final FudgeFieldContainer message, final int taxonomyId) throws IOException {
    return writeMessage (message, taxonomyId, getDefaultMessageVersion (), getDefaultMessageProcessingDirectives ());
  }
  
  public int writeMessage (final FudgeFieldContainer message) throws IOException {
    return writeMessage (message, getDefaultTaxonomyId ());
  }
  
  public int writeMessage (final FudgeMsg message, final int taxonomyId, final int version, final int processingDirectives) throws IOException {
    return writeMessageEnvelope (new FudgeMsgEnvelope (message, version, processingDirectives), taxonomyId);
  }

  public int writeMessage (final FudgeMsg message, final int taxonomyId) throws IOException {
    return writeMessage (message, taxonomyId, getDefaultMessageVersion (), getDefaultMessageProcessingDirectives ());
  }

  public int writeMessage (final FudgeMsg message) throws IOException {
    return writeMessage (message, getDefaultTaxonomyId ());
  }

  public int writeMessageEnvelope (final FudgeMsgEnvelope envelope, final int taxonomyId) throws IOException {
    if (envelope == null) return 0;
    final FudgeStreamWriter writer = getStreamWriter ();
    int nWritten = 0;
    if (taxonomyId != writer.getCurrentTaxonomyId ()) {
      writer.setCurrentTaxonomyId (taxonomyId);
    }
    int messageSize = envelope.getSize(writer.getCurrentTaxonomy());
    nWritten += writer.writeEnvelopeHeader (envelope.getProcessingDirectives (), envelope.getVersion (), messageSize);
    nWritten += writer.writeFields (envelope.getMessage());
    assert messageSize == nWritten;
    return nWritten;
  }
  
  public int writeMessageEnvelope (final FudgeMsgEnvelope envelope) throws IOException {
    return writeMessageEnvelope (envelope, getDefaultTaxonomyId ());
  }
  
}