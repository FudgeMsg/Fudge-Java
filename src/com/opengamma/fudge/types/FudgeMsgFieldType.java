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
import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * The type definition for a sub-message in a hierarchical message format.
 *
 * @author kirk
 */
public class FudgeMsgFieldType extends FudgeFieldType<FudgeMsg> {
  public static final FudgeMsgFieldType INSTANCE = new FudgeMsgFieldType();
  
  public FudgeMsgFieldType() {
    super(FudgeTypeDictionary.FUDGE_MSG_TYPE_ID, FudgeMsg.class, true, 0);
  }

  @Override
  public int getVariableSize(FudgeMsg value, FudgeTaxonomy taxonomy) {
    return value.getSize(taxonomy);
  }

  @Override
  public FudgeMsg readValue(DataInput input, int dataSize) throws IOException {
    FudgeMsg msg = new FudgeMsg();
    // REVIEW kirk 2009-09-01 -- This is right. We have to use the same taxonomy,
    // so the parent taxonomy resolver will be fixed up later on.
    int nRead = FudgeStreamDecoder.readMsgFields(input, null, msg);
    assert dataSize == nRead : "Sub-message reading failed in size; envelope unpacking will throw exception in prod.";
    return msg;
  }

  @Override
  public void writeValue(DataOutput output, FudgeMsg value, FudgeTaxonomy taxonomy) throws IOException {
    FudgeStreamEncoder.writeMsgFields(output, value, taxonomy);
  }

}
