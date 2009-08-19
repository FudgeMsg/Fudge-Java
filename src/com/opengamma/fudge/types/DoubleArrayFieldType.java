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

/**
 * 
 *
 * @author kirk
 */
public class DoubleArrayFieldType extends FudgeFieldType<double[]> {
  public static final DoubleArrayFieldType INSTANCE = new DoubleArrayFieldType();
  
  public DoubleArrayFieldType() {
    super(FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID, double[].class, true, 0);
  }

  @Override
  public int getVariableSize(double[] value) {
    return value.length * 8;
  }

  @Override
  public double[] readValue(DataInput input, int dataSize) throws IOException {
    int nDoubles = dataSize / 8;
    double[] result = new double[nDoubles];
    for(int i = 0; i < nDoubles; i++) {
      result[i] = input.readDouble();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, double[] value) throws IOException {
    for(double d : value) {
      output.writeDouble(d);
    }
  }

}
