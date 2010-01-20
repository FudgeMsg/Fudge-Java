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

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.taxon.FudgeTaxonomy;

public interface FudgeStreamReader {
  
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
     * Issued when the end of a sub-Message field is reached
     */
    SUBMESSAGE_FIELD_END
  }
  
  public boolean hasNext () throws IOException;
  
  public FudgeStreamElement next () throws IOException;
  
  public FudgeStreamElement getCurrentElement ();
  
  public Object getFieldValue ();
  
  public int getProcessingDirectives ();
  
  public int getSchemaVersion();

  public short getTaxonomyId();

  public int getEnvelopeSize();
  
  public FudgeFieldType<?> getFieldType();

  public Integer getFieldOrdinal();

  public String getFieldName();
  
  public FudgeTaxonomy getTaxonomy();
  
  public FudgeContext getFudgeContext ();
  
  public void close ();

}