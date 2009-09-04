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
public class LongArrayFieldType extends FudgeFieldType<long[]> {
  
  public static final LongArrayFieldType INSTANCE = new LongArrayFieldType();

  /**
   */
  public LongArrayFieldType() {
    super(FudgeTypeDictionary.LONG_ARRAY_TYPE_ID, long[].class, true, 0);
  }

  @Override
  public int getVariableSize(long[] value, FudgeTaxonomy taxonomy) {
    return value.length * 8;
  }

  @Override
  public long[] readValue(DataInput input, int dataSize) throws IOException {
    int nLongs = dataSize / 8;
    long[] result = new long[nLongs];
    for(int i = 0; i < nLongs; i++) {
      result[i] = input.readLong();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, long[] value,
      FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    for(long l : value) {
      output.writeLong(l);
    }
  }

}
