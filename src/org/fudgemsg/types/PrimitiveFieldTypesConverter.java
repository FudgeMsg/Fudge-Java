/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg.types;

/**
 * Basic conversions between the types defined in {@link PrimitiveFieldTypes}. Conversion which will lose
 * data (i.e. out of range) are not permitted and will trigger exceptions. The main use of these is to
 * support field value conversion between the various width integral types.
 * 
 * @author Andrew Griffin
 * @param <Target> primitive field type to convert to
 */
public abstract class PrimitiveFieldTypesConverter<Target> implements FudgeTypeConverter<Object, Target> {

  /**
   * Singleton converter for the {@link Boolean} type.
   */
  public static final PrimitiveFieldTypesConverter<Boolean> BOOLEAN_CONVERTER = new PrimitiveFieldTypesConverter<Boolean>("boolean", 0, 0) {
    @Override
    public Boolean primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (Boolean) value;
      if (value instanceof Byte)
        return ((Byte) value != 0);
      if (value instanceof Short)
        return ((Short) value != 0);
      if (value instanceof Integer)
        return ((Integer) value != 0);
      if (value instanceof Long)
        return ((Long) value != 0);
      if (value instanceof Float)
        return ((Float) value != 0);
      if (value instanceof Double)
        return ((Double) value != 0);
      if (value instanceof String)
        return Boolean.parseBoolean((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Byte} type.
   */
  public static final PrimitiveFieldTypesConverter<Byte> BYTE_CONVERTER = new PrimitiveFieldTypesConverter<Byte>("byte", Byte.MIN_VALUE, Byte.MAX_VALUE) {
    @Override
    public Byte primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (byte) (((Boolean) value) ? 1 : 0);
      if (value instanceof Byte)
        return (Byte) value;
      if (value instanceof Short)
        return (byte) rangeCheck((Short) value);
      if (value instanceof Integer)
        return (byte) rangeCheck((Integer) value);
      if (value instanceof Long)
        return (byte) rangeCheck((Long) value);
      if (value instanceof Float)
        return (byte) rangeCheck((Float) value);
      if (value instanceof Double)
        return (byte) rangeCheck((Double) value);
      if (value instanceof String)
        return Byte.parseByte((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Short} type.
   */
  public static final PrimitiveFieldTypesConverter<Short> SHORT_CONVERTER = new PrimitiveFieldTypesConverter<Short>("short", Short.MIN_VALUE, Short.MAX_VALUE) {
    @Override
    public Short primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (short) (((Boolean) value) ? 1 : 0);
      if (value instanceof Byte)
        return (short) (Byte) value;
      if (value instanceof Short)
        return (Short) value;
      if (value instanceof Integer)
        return (short) rangeCheck((Integer) value);
      if (value instanceof Long)
        return (short) rangeCheck((Long) value);
      if (value instanceof Float)
        return (short) rangeCheck((Float) value);
      if (value instanceof Double)
        return (short) rangeCheck((Double) value);
      if (value instanceof String)
        return Short.parseShort((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Integer} type.
   */
  public static final PrimitiveFieldTypesConverter<Integer> INT_CONVERTER = new PrimitiveFieldTypesConverter<Integer>("int", Integer.MIN_VALUE, Integer.MAX_VALUE) {
    @Override
    public Integer primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (int) (((Boolean) value) ? 1 : 0);
      if (value instanceof Byte)
        return (int) (Byte) value;
      if (value instanceof Short)
        return (int) (Short) value;
      if (value instanceof Integer)
        return (Integer) value;
      if (value instanceof Long)
        return (int) rangeCheck((Long) value);
      if (value instanceof Float)
        return (int) rangeCheck((Float) value);
      if (value instanceof Double)
        return (int) rangeCheck((Double) value);
      if (value instanceof String)
        return Integer.parseInt((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Long} type.
   */
  public static final PrimitiveFieldTypesConverter<Long> LONG_CONVERTER = new PrimitiveFieldTypesConverter<Long>("long", 0, 0) {
    @Override
    public Long primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (long) (((Boolean) value) ? 1 : 0);
      if (value instanceof Float)
        return (long) rangeCheck(Long.MIN_VALUE, Long.MAX_VALUE, (Float) value);
      if (value instanceof Double)
        return (long) rangeCheck(Long.MIN_VALUE, Long.MAX_VALUE, (Double) value);
      if (value instanceof Number)
        return ((Number) value).longValue();
      if (value instanceof String)
        return Long.parseLong((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Float} type.
   */
  public static final PrimitiveFieldTypesConverter<Float> FLOAT_CONVERTER = new PrimitiveFieldTypesConverter<Float>("float", 0, 0) {
    @Override
    public Float primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (float) (((Boolean) value) ? 1 : 0);
      if (value instanceof Number)
        return ((Number) value).floatValue();
      if (value instanceof String)
        return Float.parseFloat((String) value);
      return super.primaryToSecondary(value);
    }
  };

  /**
   * Singleton converter for the {@link Double} type.
   */
  public static final PrimitiveFieldTypesConverter<Double> DOUBLE_CONVERTER = new PrimitiveFieldTypesConverter<Double>("double", 0, 0) {
    @Override
    public Double primaryToSecondary(final Object value) {
      if (value instanceof Boolean)
        return (double) (((Boolean) value) ? 1 : 0);
      if (value instanceof Number)
        return ((Number) value).doubleValue();
      if (value instanceof String)
        return Double.parseDouble((String) value);
      return super.primaryToSecondary(value);
    }
  };

  private final String _targetTypeName;
  private final int _rangeLo;
  private final int _rangeHi;

  private PrimitiveFieldTypesConverter(final String targetType, final int lo, final int hi) {
    _targetTypeName = targetType;
    _rangeLo = lo;
    _rangeHi = hi;
  }

  /**
   * Returns the descriptive target type name (e.g. for error messages). 
   * 
   * @return the target type name
   */
  protected String getTargetTypeName() {
    return _targetTypeName;
  }

  /**
   * Checks a value is within the range specified at construction.
   * 
   * @param value value to check
   * @return the value
   * @throws IllegalArgumentException if the value is out of range
   */
  protected int rangeCheck(int value) {
    if ((value >= _rangeLo) && (value <= _rangeHi))
      return value;
    throw new IllegalArgumentException("value " + value + " out of range for " + getTargetTypeName());
  }

  /**
   * Checks a value is within the range specified at construction.
   * 
   * @param value value to check
   * @return the value
   * @throws IllegalArgumentException if the value is out of range
   */
  protected long rangeCheck(long value) {
    if ((value >= _rangeLo) && (value <= _rangeHi))
      return value;
    throw new IllegalArgumentException("value " + value + " out of range for " + getTargetTypeName());
  }

  /**
   * Checks a value is within the range specified at construction
   * 
   * @param value value to check
   * @return the value
   * @throws IllegalArgumentException if the value is out of range
   */
  protected double rangeCheck(double value) {
    return rangeCheck(_rangeLo, _rangeHi, value);
  }

  /**
   * Checks a value is within the range (inclusive)
   * 
   * @param lo lower-bound of the range
   * @param hi upper-bound of the range
   * @param value value to check
   * @return the value
   * @throws IllegalArgumentException if the value is out of range
   */
  protected double rangeCheck(double lo, double hi, double value) {
    if ((value >= lo) && (value <= hi))
      return value;
    throw new IllegalArgumentException("value " + value + " out of range for " + getTargetTypeName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary(Class<?> clazz) {
    if (Boolean.class.isAssignableFrom(clazz))
      return true;
    if (Number.class.isAssignableFrom(clazz))
      return true;
    if (String.class.isAssignableFrom(clazz))
      return true;
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Target primaryToSecondary(final Object value) {
    throw new IllegalArgumentException("cannot convert class " + value.getClass().getName() + " to " + getTargetTypeName());
  }

}
