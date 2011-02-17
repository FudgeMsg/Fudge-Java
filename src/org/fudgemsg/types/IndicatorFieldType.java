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

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;


/**
 * The type handler for the singleton {@link IndicatorType} value.
 *
 * @author Kirk Wylie
 */
public class IndicatorFieldType extends FudgeFieldType<IndicatorType> {

  /**
   * Standard Fudge field type: zero length indicator. See {@link FudgeTypeDictionary#INDICATOR_TYPE_ID}.
   */
  public static final IndicatorFieldType INSTANCE = new IndicatorFieldType();

  private IndicatorFieldType() {
    super(FudgeTypeDictionary.INDICATOR_TYPE_ID, IndicatorType.class, false, 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IndicatorType readValue(DataInput input, int dataSize) {
    return IndicatorType.INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, IndicatorType value) {
    // Intentional no-op.
  }

}
