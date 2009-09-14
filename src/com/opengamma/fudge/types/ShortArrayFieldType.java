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
public class ShortArrayFieldType extends FudgeFieldType<short[]> {
  
  public static final ShortArrayFieldType INSTANCE = new ShortArrayFieldType();

  /**
   */
  public ShortArrayFieldType() {
    super(FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID, short[].class, true, 0);
  }

  @Override
  public int getVariableSize(short[] value, FudgeTaxonomy taxonomy) {
    return value.length * 2;
  }

  @Override
  public short[] readValue(DataInput input, int dataSize) throws IOException {
    int nShorts = dataSize / 2;
    short[] result = new short[nShorts];
    for(int i = 0; i < nShorts; i++) {
      result[i] = input.readShort();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, short[] value,
      FudgeTaxonomy taxonomy) throws IOException {
    for(short f : value) {
      output.writeShort(f);
    }
  }

}
