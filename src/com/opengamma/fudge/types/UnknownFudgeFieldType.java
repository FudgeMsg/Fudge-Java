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
import com.opengamma.fudge.UnknownFudgeFieldValue;
import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * A type class for handling variable sized field values where the type
 * isn't available in the current {@link FudgeTypeDictionary}.
 *
 * @author kirk
 */
public class UnknownFudgeFieldType extends
    FudgeFieldType<UnknownFudgeFieldValue> {
  
  public UnknownFudgeFieldType(int typeId) {
    super(typeId, UnknownFudgeFieldValue.class, true, 0);
  }

  @Override
  public int getVariableSize(UnknownFudgeFieldValue value,
      FudgeTaxonomy taxonomy) {
    return value.getContents().length;
  }

  @Override
  public UnknownFudgeFieldValue readValue(DataInput input, int dataSize)
      throws IOException {
    byte[] contents = new byte[dataSize];
    input.readFully(contents);
    return new UnknownFudgeFieldValue(contents, this);
  }

  @Override
  public void writeValue(DataOutput output, UnknownFudgeFieldValue value,
      FudgeTaxonomy taxonomy) throws IOException {
    output.write(value.getContents());
  }

}
