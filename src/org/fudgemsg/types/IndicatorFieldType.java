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
 * The type handler for the singleton {@link IndicatorType} value.
 *
 * @author kirk
 */
public class IndicatorFieldType extends FudgeFieldType<IndicatorType> {
  public static final IndicatorFieldType INSTANCE = new IndicatorFieldType();

  public IndicatorFieldType() {
    super(FudgeTypeDictionary.INDICATOR_TYPE_ID, IndicatorType.class, false, 0);
  }

  @Override
  public IndicatorType readValue(DataInput input, int dataSize)
      throws IOException {
    return IndicatorType.INSTANCE;
  }

  @Override
  public void writeValue(DataOutput output, IndicatorType value,
      FudgeTaxonomy taxonomy) throws IOException {
    // Intentional no-op.
  }

}
