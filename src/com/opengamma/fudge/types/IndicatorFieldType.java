/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.opengamma.fudge.FudgeFieldType;
import com.opengamma.fudge.FudgeTypeDictionary;
import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * 
 *
 * @author kirk
 */
public class IndicatorFieldType extends FudgeFieldType<IndicatorType> {
  public static final IndicatorFieldType INSTANCE = new IndicatorFieldType();

  /**
   * @param typeId
   * @param javaType
   * @param isVariableSize
   * @param fixedSize
   */
  public IndicatorFieldType() {
    super(FudgeTypeDictionary.INDICATOR_TYPE_ID, IndicatorType.class, false, 0);
  }

  @Override
  public IndicatorType readValue(DataInput input, int dataSize)
      throws IOException {
    return IndicatorType.INSTANCE;
  }

  @Override
  public void writeValue(DataOutput output, IndicatorType value,
      FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    // Intentional no-op.
  }

}
