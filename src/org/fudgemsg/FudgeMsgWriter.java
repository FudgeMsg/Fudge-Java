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

import java.io.Flushable;

/**
 * A writer for passing Fudge messages ({@link FudgeFieldContainer} instances) to an underlying {@link FudgeStreamWriter} instance. This implementation
 * assumes that the whole message (or envelope) is available to the caller before writing starts. This is provided for convenience - greater runtime
 * efficiency may be possible by working directly with a {@link FudgeStreamWriter} to emit Fudge stream elements as they are generated.
 * 
 * @author Andrew Griffin
 */
public class FudgeMsgWriter implements Flushable {
  
  /**
   * The underlying target for Fudge stream elements.
   */
  private final FudgeStreamWriter _streamWriter;
  
  /**
   * The taxonomy identifier to use for any messages that are passed without envelopes. 
   */
  private short _defaultTaxonomyId = 0;
  
  /**
   * The schema version to add to the envelope header for any messages that are passed without envelopes.
   */
  private int _defaultMessageVersion = 0;
  
  /**
   * The processing directive flags to add to the envelope header for any messages that are passed without envelopes.
   */
  private int _defaultMessageProcessingDirectives = 0;

  /**
   * Creates a new {@link FudgeMsgWriter} around an existing {@link FudgeStreamWriter}.
   * 
   * @param streamWriter target to write Fudge stream elements to
   */
  public FudgeMsgWriter (final FudgeStreamWriter streamWriter) {
    if (streamWriter == null) {
      throw new NullPointerException ("streamWriter cannot be null");
    }
    _streamWriter = streamWriter;
  }
  
  /**
   * Flushes the underlying {@link FudgeStreamWriter}.
   */
  public void flush () {
    getStreamWriter ().flush ();
  }
  
  /**
   * Flushes and closes the underlying {@link FudgeStreamWriter}.
   */
  public void close () {
    flush ();
    getStreamWriter ().close ();
  }
  
  /**
   * Returns the underlying {@link FudgeStreamWriter}.
   * 
   * @return the {@code FudgeStreamWriter}
   */
  protected FudgeStreamWriter getStreamWriter () {
    return _streamWriter;
  }
  
  /**
   * Returns the {@link FudgeContext} of the current underlying {@link FudgeStreamWriter}.
   * 
   * @return the {@code FudgeContext}
   */ 
  public FudgeContext getFudgeContext () {
    return getStreamWriter ().getFudgeContext ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString () {
    final StringBuilder sb = new StringBuilder ("FudgeMessageStreamWriter{");
    if (getStreamWriter () != null) sb.append (getStreamWriter ());
    return sb.append ('}').toString ();
  }

  /**
   * Returns the current default taxonomy identifier.
   * 
   * @return the taxonomy identifier
   */
  public int getDefaultTaxonomyId () {
    return _defaultTaxonomyId;
  }
  
  /**
   * Sets the default taxonomy identifier for messages that are passed without an envelope.
   * 
   * @param taxonomyId the taxonomy identifier
   */
  public void setDefaultTaxonomyId (final int taxonomyId) {
    if ((taxonomyId < Short.MIN_VALUE) || (taxonomyId > Short.MAX_VALUE)) {
      throw new IllegalArgumentException ("Provided taxonomy ID " + taxonomyId + " out of range.");
    }
    _defaultTaxonomyId = (short)taxonomyId;
  }
  
  /**
   * Returns the current schema version used for messages that are passed without an envelope.
   * 
   * @return the schema version
   */
  public int getDefaultMessageVersion () {
    return _defaultMessageVersion;
  }
  
  /**
   * Sets the schema version to be used for messages that are passed without an envelope.
   * 
   * @param version new schema version value
   */
  public void setDefaultMessageVersion (final int version) {
    if ((version < 0) || (version > 255)) {
      throw new IllegalArgumentException("Provided version " + version + " which doesn't fit within one byte.");
    }
    _defaultMessageVersion = version;
  }
  
  /**
   * Returns the current processing directive flags for messages that are passed without an envelope.
   * 
   * @return the processing directive flags
   */
  public int getDefaultMessageProcessingDirectives () {
    return _defaultMessageProcessingDirectives;
  }
  
  /**
   * Sets the processing directive flags to be used for messages that are passed without an envelope.
   * 
   * @param processingDirectives processing directive flags
   */
  public void setDefaultMessageProcessingDirectives (final int processingDirectives) {
    if ((processingDirectives < 0) || (processingDirectives > 255)) {
      throw new IllegalArgumentException ("Provided processing directives " + processingDirectives + " which doesn't fit within one byte.");
    }
    _defaultMessageProcessingDirectives = processingDirectives;
  }
  
  /**
   * Writes a message with the given taxonomy, schema version and processing directive flags.
   * 
   * @param message message to write
   * @param taxonomyId identifier of the taxonomy to use. If the taxonomy is recognized by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   * @param version schema version
   * @param processingDirectives processing directive flags
   */
  public void writeMessage (final FudgeFieldContainer message, final int taxonomyId, final int version, final int processingDirectives) {
    writeMessageEnvelope (new FudgeMsgEnvelope (message, version, processingDirectives), taxonomyId);
  }
  
  /**
   * Writes a message with the given taxonomy. Default schema version and processing directive flags are used.
   * 
   * @param message message to write
   * @param taxonomyId identifier of the taxonomy to use. If the taxonomy is recognized by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   */ 
  public void writeMessage (final FudgeFieldContainer message, final int taxonomyId) {
    writeMessage (message, taxonomyId, getDefaultMessageVersion (), getDefaultMessageProcessingDirectives ());
  }
  
  /**
   * Writes a message. Default taxonomy, schema version and processing directive flags are used.
   *
   * @param message message to write
   * @throws NullPointerException if the default taxonomy has not been specified
   */
  public void writeMessage (final FudgeFieldContainer message) {
    writeMessage (message, getDefaultTaxonomyId ());
  }
  
  /**
   * Writes a message envelope with the given taxonomy.
   * 
   * @param envelope message envelope to write
   * @param taxonomyId identifier of the taxonomy to use. If the taxonomy is recognized by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   */
  public void writeMessageEnvelope (final FudgeMsgEnvelope envelope, final int taxonomyId) {
    if (envelope == null) return;
    final FudgeStreamWriter writer = getStreamWriter ();
    if (taxonomyId != writer.getCurrentTaxonomyId ()) {
      writer.setCurrentTaxonomyId (taxonomyId);
    }
    int messageSize = FudgeSize.calculateMessageEnvelopeSize (writer.getCurrentTaxonomy (), envelope);
    writer.writeEnvelopeHeader (envelope.getProcessingDirectives (), envelope.getVersion (), messageSize);
    writer.writeFields (envelope.getMessage());
    writer.envelopeComplete ();
  }
  
  /**
   * Writes a message envelope using the default taxonomy.
   * 
   * @param envelope message envelope to write
   * @throws NullPointerException if the default taxonomy has not been specified
   */
  public void writeMessageEnvelope (final FudgeMsgEnvelope envelope) {
    writeMessageEnvelope (envelope, getDefaultTaxonomyId ());
  }
  
}