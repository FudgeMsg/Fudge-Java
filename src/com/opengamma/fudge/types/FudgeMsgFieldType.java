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
import com.opengamma.fudge.FudgeMsg;
import com.opengamma.fudge.FudgeStreamDecoder;
import com.opengamma.fudge.FudgeStreamEncoder;
import com.opengamma.fudge.FudgeTypeDictionary;

/**
 * 
 *
 * @author kirk
 */
public class FudgeMsgFieldType extends FudgeFieldType<FudgeMsg> {
  public static final FudgeMsgFieldType INSTANCE = new FudgeMsgFieldType();
  
  public FudgeMsgFieldType() {
    super(FudgeTypeDictionary.FUDGE_MSG_TYPE_ID, FudgeMsg.class, true, 0);
  }

  @Override
  public int getVariableSize(FudgeMsg value) {
    return value.getSize();
  }

  @Override
  public FudgeMsg readValue(DataInput input, int dataSize) throws IOException {
    return FudgeStreamDecoder.readMsg(input);
  }

  @Override
  public void writeValue(DataOutput output, FudgeMsg value) throws IOException {
    FudgeStreamEncoder.writeMsg(output, value);
  }

}
