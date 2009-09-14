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
public class IntArrayFieldType extends FudgeFieldType<int[]> {
  
  public static final IntArrayFieldType INSTANCE = new IntArrayFieldType();

  /**
   */
  public IntArrayFieldType() {
    super(FudgeTypeDictionary.INT_ARRAY_TYPE_ID, int[].class, true, 0);
  }

  @Override
  public int getVariableSize(int[] value, FudgeTaxonomy taxonomy) {
    return value.length * 4;
  }

  @Override
  public int[] readValue(DataInput input, int dataSize) throws IOException {
    int nInts = dataSize / 4;
    int[] result = new int[nInts];
    for(int i = 0; i < nInts; i++) {
      result[i] = input.readInt();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, int[] value,
      FudgeTaxonomy taxonomy) throws IOException {
    for(int i : value) {
      output.writeInt(i);
    }
  }

}
