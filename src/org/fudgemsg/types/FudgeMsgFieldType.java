/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeSize;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * The type definition for a sub-message in a hierarchical message format.
 *
 * @author Kirk Wylie
 */
public class FudgeMsgFieldType extends FudgeFieldType<FudgeFieldContainer> {

  /**
   * Standard Fudge field type: embedded sub-message. See {@link FudgeTypeDictionary#FUDGE_MSG_TYPE_ID}.
   */
  public static final FudgeMsgFieldType INSTANCE = new FudgeMsgFieldType();
  
  private FudgeMsgFieldType() {
    super(FudgeTypeDictionary.FUDGE_MSG_TYPE_ID, FudgeFieldContainer.class, true, 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getVariableSize(FudgeFieldContainer value, FudgeTaxonomy taxonomy) {
    return FudgeSize.calculateMessageSize (taxonomy, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldContainer readValue(DataInput input, int dataSize) {
    throw new UnsupportedOperationException("Sub-messages can only be decoded from FudgeStreamReader.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, FudgeFieldContainer value) {
    throw new UnsupportedOperationException("Sub-messages can only be written using FudgeStreamWriter.");
  }

}
