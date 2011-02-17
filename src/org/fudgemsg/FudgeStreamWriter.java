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

import java.io.Closeable;
import java.io.Flushable;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Abstract interface for writing Fudge elements to a target. This base can be used
 * to build full Fudge message writers or serializers to convert Java objects directly
 * to Fudge streams.
 */
public interface FudgeStreamWriter extends Flushable, Closeable {

  /**
   * Returns the bound {@link FudgeContext} used for type and taxonomy resolution.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext ();
  
  /**
   * Returns the taxonomy (if any) that is currently being used to encode fields. Returns {@code null}
   * if no taxonomy is specified or the taxonomy identifier cannot be resolved by the bound {@link FudgeContext}.
   * 
   *  @return the {@code FudgeTaxonomy}
   */
  public FudgeTaxonomy getCurrentTaxonomy ();
  
  /**
   * Sets the current taxonomy, by identifier, to be used to encode fields.
   * 
   * @param taxonomyId the taxonomy identifier
   */
  public void setCurrentTaxonomyId (final int taxonomyId);
  
  /**
   * Returns the current taxonomy identifier.
   * 
   * @return current taxonomy identifier
   */
  public int getCurrentTaxonomyId ();
  
  /**
   * Writes a message envelope header.
   * 
   * @param processingDirectives the processing directive flags
   * @param schemaVersion the schema version value
   * @param messageSize the Fudge encoded size of the underlying message, including the message envelope
   */
  public void writeEnvelopeHeader (int processingDirectives, int schemaVersion, int messageSize);
  
  /**
   * Signal the end of the message contained within an envelope. An implementation may not need to take
   * any action at this point as the end of the envelope can be detected based on the message size in the
   * header.
   */
  public void envelopeComplete ();
  
  /**
   * Writes a message field.
   * 
   * @param field the message field to write
   */
  public void writeField (FudgeField field);
  
  /**
   * Writes a message field.
   * 
   * @param ordinal the ordinal index of the field, or {@code null} to omit.
   * @param name the name of the field, {@code null} to omit. If the ordinal is omitted and the name matches an entry in the current taxonomy the name will be replaced by the taxonomy resolved ordinal.
   * @param type the type of the underlying data
   * @param fieldValue value of the field
   */
  public void writeField (Short ordinal, String name, FudgeFieldType<?> type, Object fieldValue);
  
  /**
   * Writes a set of fields.
   * 
   * @param fields the fields to write.
   */
  public void writeFields (FudgeFieldContainer fields);
  
  /**
   * Flushes any data from the internal buffers to the target stream and attempts to flush the underlying stream if appropriate.
   */
  public void flush ();
  
  /**
   * Flushes and closes this writer and attempts to close the underlying stream if appropriate.
   */
  public void close ();
  
}