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

package org.fudgemsg.json;

/**
 * Tunable parameters for the JSON encoding/decoding. Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a> for details on
 * the representation.
 * 
 * @author Andrew Griffin
 */
public class JSONSettings {

  /**
   * Default name for the processing directives field.
   */
  public static final String DEFAULT_PROCESSINGDIRECTIVES_FIELD = "fudgeProcessingDirectives";
  
  /**
   * Default name for the schema version field.
   */
  public static final String DEFAULT_SCHEMAVERSION_FIELD = "fudgeSchemaVersion";
  
  /**
   * Default name for the taxonomy field.
   */
  public static final String DEFAULT_TAXONOMY_FIELD = "fudgeTaxonomy";
  
  private String _processingDirectivesField = DEFAULT_PROCESSINGDIRECTIVES_FIELD;
  private String _schemaVersionField = DEFAULT_SCHEMAVERSION_FIELD;
  private String _taxonomyField = DEFAULT_TAXONOMY_FIELD;
  
  private boolean _preferFieldNames = true;
  
  /**
   * Creates a new settings object with the default values.
   */
  public JSONSettings () {
  }
  
  /**
   * Creates a new settings object copying the current values from another.
   * 
   * @param copy object to copy the settings from
   */
  public JSONSettings (final JSONSettings copy) {
    setProcessingDirectivesField (copy.getProcessingDirectivesField ());
    setSchemaVersionField (copy.getSchemaVersionField ());
    setTaxonomyField (copy.getTaxonomyField ());
  }

  /**
   * Returns the name of the field to use for the processing directives, or {@code null} if it is to be omitted.
   * 
   * @return the field name, or {@code null} to omit
   */
  public String getProcessingDirectivesField () {
    return _processingDirectivesField;
  }
  
  /**
   * Returns the name of the field to use for the schema version, or {@code null} if it is to be omitted.
   * 
   * @return the field name, or {@code null} to omit
   */
  public String getSchemaVersionField () {
    return _schemaVersionField;
  }
  
  /**
   * Returns the name of the field to use for the taxonomy, or {@code null} if it is to be omitted.
   * 
   * @return the field name, or {@code null} to omit
   */
  public String getTaxonomyField () {
    return _taxonomyField;
  }
  
  /**
   * Sets the field name to use for adding the message processing directives to the JSON object. Set to {@code null} to
   * omit the field.
   * 
   * @param processingDirectivesField field name or {@code null} to omit
   */
  public void setProcessingDirectivesField (final String processingDirectivesField) {
    _processingDirectivesField = processingDirectivesField;
  }
  
  /**
   * Sets the field name to use for adding the schema version to the JSON object. Set to {@code null} to omit the field.
   * 
   * @param schemaVersionField field name or {@code null} to omit
   */
  public void setSchemaVersionField (final String schemaVersionField) {
    _schemaVersionField = schemaVersionField;
  }
  
  /**
   * Sets the field name to use for adding the taxonomy Id to the JSON object. Set to {@code null} to omit the field.
   * 
   * @param taxonomyField field name or {@code null} to omit
   */
  public void setTaxonomyField (final String taxonomyField) {
    _taxonomyField = taxonomyField;
  }
  
  public void setPreferFieldNames (final boolean preferFieldNames) {
    _preferFieldNames = preferFieldNames;
  }
  
  public boolean getPreferFieldNames () {
    return _preferFieldNames;
  }
  
}