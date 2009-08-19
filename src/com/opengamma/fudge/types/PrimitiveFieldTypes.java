/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.types;

import static com.opengamma.fudge.FudgeTypeDictionary.*;
import com.opengamma.fudge.FudgeFieldType;

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
