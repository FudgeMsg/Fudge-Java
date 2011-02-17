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
 * The type definition for an array of single-precision floating point numbers.
 *
 * @author Kirk Wylie
 */
public class FloatArrayFieldType extends FudgeFieldType<float[]> {

  /**
   * Standard Fudge field type: arbitrary length 32-bit floating point array. See {@link FudgeTypeDictionary#FLOAT_ARRAY_TYPE_ID}.
   */
  public static final FloatArrayFieldType INSTANCE = new FloatArrayFieldType();
  
  private FloatArrayFieldType() {
    super(FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID, float[].class, true, 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getVariableSize(float[] value, FudgeTaxonomy taxonomy) {
    return value.length * 4;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float[] readValue(DataInput input, int dataSize) throws IOException {
    int nFloats = dataSize / 4;
    float[] result = new float[nFloats];
    for(int i = 0; i < nFloats; i++) {
      result[i] = input.readFloat();
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, float[] value) throws IOException {
    for(float f : value) {
      output.writeFloat(f);
    }
  }

}
