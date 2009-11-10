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
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;


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
      FudgeTaxonomy taxonomy) throws IOException {
    for(long l : value) {
      output.writeLong(l);
    }
  }

}
