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
import java.io.IOException;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;


/**
 * Type definition for arrays of 16-bit integers.
 *
 * @author Kirk Wylie
 */
public class ShortArrayFieldType extends FudgeFieldType<short[]> {
  
  /**
   * Standard Fudge field type: array of 16-bit integers. See {@link FudgeTypeDictionary#SHORT_ARRAY_TYPE_ID}.
   */
  public static final ShortArrayFieldType INSTANCE = new ShortArrayFieldType();

  private ShortArrayFieldType() {
    super(FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID, short[].class, true, 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getVariableSize(short[] value, FudgeTaxonomy taxonomy) {
    return value.length * 2;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public short[] readValue(DataInput input, int dataSize) throws IOException {
    int nShorts = dataSize / 2;
    short[] result = new short[nShorts];
    for(int i = 0; i < nShorts; i++) {
      result[i] = input.readShort();
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, short[] value) throws IOException {
    for(short f : value) {
      output.writeShort(f);
    }
  }

}
