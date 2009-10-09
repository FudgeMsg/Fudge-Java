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
package com.opengamma.fudge.taxon;

/**
 * A Fudge Taxonomy is a mapping from ordinals to names for
 * fields in a Fudge Encoded data format. 
 *
 * @author kirk
 */
public interface FudgeTaxonomy {
  
  /**
   * Obtain the field name appropriate for a field with the
   * specified ordinal within this taxonomy.
   * 
   * @param ordinal The ordinal to locate a field name.
   * @return The field name, or {@code null} if no name available for
   *         a field with the specified ordinal in this taxonomy.
   */
  String getFieldName(short ordinal);
  
  /**
   * Obtain the field ordinal appropriate for a field with the
   * specified name within this taxonomy.
   * 
   * @param fieldName The name to locate an ordinal for.
   * @return The field ordinal, or {@code null} if no ordinal available
   *         for a field with the specified name in this taxonomy.
   */
  Short getFieldOrdinal(String fieldName);

}
