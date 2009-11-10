/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeStreamEncoder;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;


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
  public FudgeMsg readValue(DataInput input, int dataSize, FudgeTypeDictionary typeDictionary) throws IOException {
    throw new UnsupportedOperationException("Sub-messages can only be decoded from FudgeStreamParser.");
  }

  @Override
  public void writeValue(DataOutput output, FudgeMsg value, FudgeTaxonomy taxonomy) throws IOException {
    FudgeStreamEncoder.writeMsgFields(output, value, taxonomy);
  }

}
