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

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Abstract implementation of a {@code FudgeStreamWriter} that detects major state changes and invokes
 * other methods. Can be used to build alternative stream writers for converting streamed Fudge messages
 * to XML, JSON or other formats.
 * 
 * @author Andrew Griffin
 */
public abstract class AlternativeFudgeStreamWriter implements FudgeStreamWriter {
  
  private final FudgeContext _fudgeContext;
  private FudgeTaxonomy _taxonomy = null;
  private int _taxonomyId = 0;
  
  /**
   * Creates a new {@link AlternativeFudgeStreamWriter} instance.
   * 
   * @param fudgeContext the associated {@link FudgeContext}
   */
  protected AlternativeFudgeStreamWriter (final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  /**
   * No action taken.
   */
  @Override
  public void close() {
    // no-op
  }

  /**
   * No action taken.
   */
  @Override
  public void flush() {
    // no-op
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
  public void writeEnvelopeHeader(
      int processingDirectives,
      int schemaVersion,
      int messageSize) {
    fudgeEnvelopeStart (processingDirectives, schemaVersion);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void envelopeComplete () {
    fudgeEnvelopeEnd ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeFields(FudgeFieldContainer msg) {
    for(FudgeField field : msg.getAllFields()) {
      writeField(field);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void writeField (FudgeField field) {
    if (field == null) {
      throw new NullPointerException ("Cannot write a null field to a Fudge stream");
    }
    writeField (field.getOrdinal (), field.getName (), field.getType (), field.getValue ());
  }
  
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void writeField(
      Short ordinal,
      String name,
      FudgeFieldType type,
      Object fieldValue) {
    if (fudgeFieldStart (ordinal, name, type)) {
      if (type.getTypeId () == FudgeTypeDictionary.FUDGE_MSG_TYPE_ID) {
        fudgeSubMessageStart ();
        writeFields ((FudgeFieldContainer)fieldValue);
        fudgeSubMessageEnd ();
      } else {
        fudgeFieldValue (type, fieldValue);
      }
      fudgeFieldEnd ();
    }
  }
  
  /**
   * Called when a Fudge message envelope is starting.
   * 
   * @param processingDirectives the envelope processing directives
   * @param schemaVersion the envelope schema version
   */
  protected void fudgeEnvelopeStart (final int processingDirectives, final int schemaVersion) {
    // no-op
  }
  
  /**
   * Called at the end of the envelope after all fields have been processed.
   */
  protected void fudgeEnvelopeEnd () {
    // no-op
  }
  
  /**
   * Called as a field starts.
   * 
   * @param ordinal the field ordinal
   * @param name the field name
   * @param type the field type
   * @return {@code true} to continue processing the field, {@code false} to ignore it ({@link #fudgeFieldValue}, {@link #fudgeSubMessageStart}, {@link #fudgeSubMessageEnd} and {@link #fudgeFieldEnd} will not be called for this field)
   */
  protected boolean fudgeFieldStart (Short ordinal, String name, FudgeFieldType<?> type) {
    return true;
  }
  
  /**
   * Called after a field has been processed.
   */
  protected void fudgeFieldEnd () {
    // no-op
  }
  
  /**
   * Called between {@link #fudgeFieldStart} and {@link #fudgeFieldEnd} for fields that are not sub messages.
   * 
   * @param type the field type
   * @param fieldValue the value
   */
  protected void fudgeFieldValue (FudgeFieldType<?> type, Object fieldValue) {
    // no-op
  }
  
  /**
   * Called after {@link #fudgeFieldStart} when a sub-message is starting.
   */
  protected void fudgeSubMessageStart () {
    // no-op
  }
  
  /**
   * Called when a sub-message has been processed, before {@link #fudgeFieldEnd} is called for the field.
   */
  protected void fudgeSubMessageEnd () {
    // no-op
  }
  
}