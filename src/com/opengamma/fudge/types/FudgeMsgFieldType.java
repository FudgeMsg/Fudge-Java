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
import com.opengamma.fudge.taxon.TaxonomyResolver;

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
  public FudgeMsg readValue(DataInput input, int dataSize, final FudgeTaxonomy taxonomy) throws IOException {
    return FudgeStreamDecoder.readMsg(input, new TaxonomyResolver() {
      @Override
      public FudgeTaxonomy resolveTaxonomy(short taxonomyId) {
        return taxonomy;
      }
    });
  }

  @Override
  public void writeValue(DataOutput output, FudgeMsg value, FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    FudgeStreamEncoder.writeMsg(output, value, taxonomy, taxonomyId);
  }

}
