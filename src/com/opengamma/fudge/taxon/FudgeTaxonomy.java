/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
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
