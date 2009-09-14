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
 * The type definition for an array of single-precision floating point numbers.
 *
 * @author kirk
 */
public class FloatArrayFieldType extends FudgeFieldType<float[]> {
  public static final FloatArrayFieldType INSTANCE = new FloatArrayFieldType();
  
  public FloatArrayFieldType() {
    super(FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID, float[].class, true, 0);
  }

  @Override
  public int getVariableSize(float[] value, FudgeTaxonomy taxonomy) {
    return value.length * 4;
  }

  @Override
  public float[] readValue(DataInput input, int dataSize) throws IOException {
    int nFloats = dataSize / 4;
    float[] result = new float[nFloats];
    for(int i = 0; i < nFloats; i++) {
      result[i] = input.readFloat();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, float[] value, FudgeTaxonomy taxonomy) throws IOException {
    for(float f : value) {
      output.writeFloat(f);
    }
  }

}
