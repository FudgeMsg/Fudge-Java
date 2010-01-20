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

import java.io.IOException;
import java.io.Flushable;

import org.fudgemsg.taxon.FudgeTaxonomy;

public interface FudgeStreamWriter extends Flushable {

  public FudgeContext getFudgeContext ();
  
  /**
   * Returns the taxonomy (if any) that is currently being used to encode fields. 
   */
  public FudgeTaxonomy getCurrentTaxonomy ();
  
  public void setCurrentTaxonomyId (final int taxonomyId);
  
  public int getCurrentTaxonomyId ();
  
  /**
   * Writes a message envelope header.
   */
  public int writeEnvelopeHeader (int processingDirectives, int schemaVersion, int messageSize) throws IOException;
  
  public int writeField (FudgeField field) throws IOException;
  
  public int writeField (Short ordinal, String name, FudgeFieldType<?> type, Object fieldValue) throws IOException;
  
  public int writeFields (FudgeFieldContainer fields) throws IOException;
  
  public void flush () throws IOException;
  
  public void close () throws IOException;
  
}