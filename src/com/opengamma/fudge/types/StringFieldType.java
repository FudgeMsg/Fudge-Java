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
import com.opengamma.fudge.ModifiedUTF8Util;

/**
 * 
 *
 * @author kirk
 */
public class StringFieldType extends FudgeFieldType<String> {
  public static final StringFieldType INSTANCE = new StringFieldType();
  
  public StringFieldType() {
    super(FudgeTypeDictionary.STRING_TYPE_ID, String.class, true, 0);
  }

  @Override
  public int getVariableSize(String value) {
    return ModifiedUTF8Util.modifiedUTF8Length(value);
  }

  @Override
  public String readValue(DataInput input, int dataSize) throws IOException {
    return ModifiedUTF8Util.readString(input, dataSize);
  }

  @Override
  public void writeValue(DataOutput output, String value) throws IOException {
    ModifiedUTF8Util.writeModifiedUTF8(value, output);
  }

}
