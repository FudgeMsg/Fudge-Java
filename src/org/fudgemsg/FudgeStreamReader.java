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

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Abstract interface for reading Fudge elements from a source. This base can be used
 * to build full Fudge message parsers or deserialisers to construct Java objects directly
 * from Fudge streams.
 */
public interface FudgeStreamReader extends Closeable {
  
  /**
   * Constants for the four stream element types as returned by {@link #next()} and {@link #getCurrentElement()}.
   */
  public static enum FudgeStreamElement {
    /**
     * Issued when the envelope header is parsed.
     */
    MESSAGE_ENVELOPE,
    /**
     * Issued when a simple (non-hierarchical) field is encountered.
     */
    SIMPLE_FIELD,
    /**
     * Issued when a sub-Message field is encountered.
     */
    SUBMESSAGE_FIELD_START,
    /**
     * Issued when the end of a sub-Message field is reached.
     */
    SUBMESSAGE_FIELD_END
  }
  
  /**
   * <p>Returns true if there is at least one more element to be returned by a call to {@link #next()}. A return of {@code false}
   * indicates the end of a message (or submessage) has been reached. After the end of a sub-message, the next immediate call will
   * indicate whether there are further elements or the end of the outer message. After the end of the main message referenced by
   * the envelope header, the next immediate call may:</p>
   * <ol>
   * <li>Return {@code false} if the source does not contain any subsequent Fudge messages; or</li>
   * <li>Return {@code true} if the source may contain further Fudge messages. Calling {@code next()} will return the envelope header
   * of the next message if one is present, or {@code null} if the source does not contain any further messages.</li>
   * </ol>
   * 
   * @return {@code true} if there is at least one more element to read
   */
  public boolean hasNext ();
  
  /**
   * Reads the next stream element from the source and returns the element type.
   * 
   * @return the type of the next element in the stream, or {@code null} if the end of stream has been reached at a message
   *         boundary (i.e. attempting to read the first byte of an envelope)
   */
  public FudgeStreamElement next ();
  
  /**
   * Returns the value last returned by {@link #next()}.
   * 
   * @return the type of the current element in the stream
   */
  public FudgeStreamElement getCurrentElement ();
  
  /**
   * If the current stream element is a field, returns the field value.
   * 
   * @return current field value
   */
  public Object getFieldValue ();
  
  /**
   * Returns the processing directivies specified in the last envelope header read.
   * 
   * @return current processing directive flags 
   */
  public int getProcessingDirectives ();
  
  /**
   * Returns the schema version specified in the last envelope header read.
   * 
   * @return current message schema version
   */
  public int getSchemaVersion();

  /**
   * Returns the taxonomy identifier specified in the last envelope header read.
   * 
   * @return current taxonomy identifier
   */
  public short getTaxonomyId();

  /**
   * If the current stream element is a field, returns the {@link FudgeFieldType}.
   * 
   * @return current field type
   */ 
  public FudgeFieldType<?> getFieldType();

  /**
   * If the current stream element is a field, returns the ordinal index, or {@code null} if the field did not include an ordinal.
   * 
   * @return current field ordinal
   */
  public Integer getFieldOrdinal();

  /**
   * If the current stream element is a field, returns the field name. If the underlying stream does not specify a field
   * name, but the ordinal can be resolved through a taxonomy, returns the resolved name.
   * 
   * @return current field name
   */
  public String getFieldName();
  
  /**
   * Returns the current {@link FudgeTaxonomy} corresponding to the taxonomy identifier specified in the message envelope. Returns
   * {@code null} if the message did not specify a taxonomy or the taxonomy identifier cannot be resolved by the bound {@link FudgeContext}.
   * 
   * @return current taxonomy if available
   */
  public FudgeTaxonomy getTaxonomy();
  
  /**
   * Returns the {@link FudgeContext} bound to the reader used for type and taxonomy resolution.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext ();
  
  /**
   * Closes the {@link FudgeStreamReader} and attempts to close the underlying data source if appropriate.
   */
  public void close ();

}