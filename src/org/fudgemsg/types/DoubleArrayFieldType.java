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
 * The type definition for an array of double-precision floating point numbers.
 *
 * @author kirk
 */
public class DoubleArrayFieldType extends FudgeFieldType<double[]> {
  public static final DoubleArrayFieldType INSTANCE = new DoubleArrayFieldType();
  
  public DoubleArrayFieldType() {
    super(FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID, double[].class, true, 0);
  }

  @Override
  public int getVariableSize(double[] value, FudgeTaxonomy taxonomy) {
    return value.length * 8;
  }

  @Override
  public double[] readValue(DataInput input, int dataSize) throws IOException {
    int nDoubles = dataSize / 8;
    double[] result = new double[nDoubles];
    for(int i = 0; i < nDoubles; i++) {
      result[i] = input.readDouble();
    }
    return result;
  }

  @Override
  public void writeValue(DataOutput output, double[] value, FudgeTaxonomy taxonomy) throws IOException {
    for(double d : value) {
      output.writeDouble(d);
    }
  }

}
