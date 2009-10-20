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

import static org.fudgemsg.FudgeTypeDictionary.*;

import org.fudgemsg.FudgeFieldType;

/**
 * A collection of all the simple fixed-width field types that represent
 * primitive values.
 * Because these are fast-pathed inside the encoder/decoder sequence,
 * there's no point in breaking them out to other classes.
 *
 * @author kirk
 */
public final class PrimitiveFieldTypes {
  private PrimitiveFieldTypes() {
  }

  public static final FudgeFieldType<Boolean> BOOLEAN_TYPE = new FudgeFieldType<Boolean>(BOOLEAN_TYPE_ID, Boolean.TYPE, false, 1);
  public static final FudgeFieldType<Byte> BYTE_TYPE = new FudgeFieldType<Byte>(BYTE_TYPE_ID, Byte.TYPE, false, 1);
  public static final FudgeFieldType<Short> SHORT_TYPE = new FudgeFieldType<Short>(SHORT_TYPE_ID, Short.TYPE, false, 2);
  public static final FudgeFieldType<Integer> INT_TYPE = new FudgeFieldType<Integer>(INT_TYPE_ID, Integer.TYPE, false, 4);
  public static final FudgeFieldType<Long> LONG_TYPE = new FudgeFieldType<Long>(LONG_TYPE_ID, Long.TYPE, false, 8);
  public static final FudgeFieldType<Float> FLOAT_TYPE = new FudgeFieldType<Float>(FLOAT_TYPE_ID, Float.TYPE, false, 4);
  public static final FudgeFieldType<Double> DOUBLE_TYPE = new FudgeFieldType<Double>(DOUBLE_TYPE_ID, Double.TYPE, false, 8);
}
