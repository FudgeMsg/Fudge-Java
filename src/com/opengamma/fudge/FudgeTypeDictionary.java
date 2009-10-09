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
package com.opengamma.fudge;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.fudge.types.ByteArrayFieldType;
import com.opengamma.fudge.types.DoubleArrayFieldType;
import com.opengamma.fudge.types.FloatArrayFieldType;
import com.opengamma.fudge.types.FudgeMsgFieldType;
import com.opengamma.fudge.types.IndicatorFieldType;
import com.opengamma.fudge.types.IntArrayFieldType;
import com.opengamma.fudge.types.LongArrayFieldType;
import com.opengamma.fudge.types.PrimitiveFieldTypes;
import com.opengamma.fudge.types.ShortArrayFieldType;
import com.opengamma.fudge.types.StringFieldType;
import com.opengamma.fudge.types.UnknownFudgeFieldType;

/**
 * Contains all the {@link FudgeFieldType} definitions for a particular
 * Fudge installation.
 * You can control it through your {@link FudgeContext}.
 *
 * @author kirk
 */
public final class FudgeTypeDictionary {
  /*package*/ static final FudgeTypeDictionary INSTANCE = new FudgeTypeDictionary();
  
  private volatile FudgeFieldType<?>[] _typesById = new FudgeFieldType<?>[0];
  private volatile UnknownFudgeFieldType[] _unknownTypesById = new UnknownFudgeFieldType[0];
  private final Map<Class<?>, FudgeFieldType<?>> _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType<?>>();
  
  public FudgeTypeDictionary() {
    addType(ByteArrayFieldType.LENGTH_4_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_8_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_16_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_20_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_32_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_64_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_128_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_256_INSTANCE);
    addType(ByteArrayFieldType.LENGTH_512_INSTANCE);
    
    addType(PrimitiveFieldTypes.BOOLEAN_TYPE, Boolean.class);
    addType(PrimitiveFieldTypes.BYTE_TYPE, Byte.class);
    addType(PrimitiveFieldTypes.SHORT_TYPE, Short.class);
    addType(PrimitiveFieldTypes.INT_TYPE, Integer.class);
    addType(PrimitiveFieldTypes.LONG_TYPE, Long.class);
    addType(PrimitiveFieldTypes.FLOAT_TYPE, Float.class);
    addType(ShortArrayFieldType.INSTANCE);
    addType(IntArrayFieldType.INSTANCE);
    addType(LongArrayFieldType.INSTANCE);
    addType(IndicatorFieldType.INSTANCE);
    addType(FloatArrayFieldType.INSTANCE);
    addType(PrimitiveFieldTypes.DOUBLE_TYPE, Double.class);
    addType(DoubleArrayFieldType.INSTANCE);
    addType(ByteArrayFieldType.VARIABLE_SIZED_INSTANCE);
    addType(StringFieldType.INSTANCE);
    addType(FudgeMsgFieldType.INSTANCE);
  }
  
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    if(type == null) {
      throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
    }
    synchronized(this) {
      int newLength = Math.max(type.getTypeId() + 1, _typesById.length);
      FudgeFieldType<?>[] newArray = Arrays.copyOf(_typesById, newLength);
      newArray[type.getTypeId()] = type;
      _typesById = newArray;
      
      _typesByJavaType.put(type.getJavaType(), type);
      for(Class<?> alternativeType : alternativeTypes) {
        _typesByJavaType.put(alternativeType, type);
      }
    }
  }
  
  public FudgeFieldType<?> getByJavaType(Class<?> javaType) {
    if(javaType == null) {
      return null;
    }
    return _typesByJavaType.get(javaType);
  }
  
  /**
   * Obtain a <em>known</em> type by the type ID specified.
   * For processing unhandled variable-width field types, this method will return
   * {@code null}, and {@link #getUnknownType(int)} should be used if unhandled-type
   * processing is desired.
   * 
   * @param typeId
   * @return
   */
  public FudgeFieldType<?> getByTypeId(int typeId) {
    if(typeId >= _typesById.length) {
      return null;
    }
    return _typesById[typeId];
  }
  
  public UnknownFudgeFieldType getUnknownType(int typeId) {
    int newLength = Math.max(typeId + 1, _unknownTypesById.length);
    if((_unknownTypesById.length < newLength) || (_unknownTypesById[typeId] == null)) {
      synchronized(this) {
        if((_unknownTypesById.length < newLength) || (_unknownTypesById[typeId] == null)) {
          UnknownFudgeFieldType[] newArray = Arrays.copyOf(_unknownTypesById, newLength);
          newArray[typeId] = new UnknownFudgeFieldType(typeId);
          _unknownTypesById = newArray;
        }
      }
    }
    assert _unknownTypesById[typeId] != null;
    return _unknownTypesById[typeId];
  }
  
  // --------------------------
  // STANDARD FUDGE FIELD TYPES
  // --------------------------
  
  public static final byte INDICATOR_TYPE_ID = (byte)0;
  public static final byte BOOLEAN_TYPE_ID = (byte)1;
  public static final byte BYTE_TYPE_ID = (byte)2;
  public static final byte SHORT_TYPE_ID = (byte)3;
  public static final byte INT_TYPE_ID = (byte)4;
  public static final byte LONG_TYPE_ID = (byte)5;
  public static final byte BYTE_ARRAY_TYPE_ID = (byte)6;
  public static final byte SHORT_ARRAY_TYPE_ID = (byte)7;
  public static final byte INT_ARRAY_TYPE_ID = (byte)8;
  public static final byte LONG_ARRAY_TYPE_ID = (byte)9;
  public static final byte FLOAT_TYPE_ID = (byte)10;
  public static final byte DOUBLE_TYPE_ID = (byte)11;
  public static final byte FLOAT_ARRAY_TYPE_ID = (byte)12;
  public static final byte DOUBLE_ARRAY_TYPE_ID = (byte)13;
  public static final byte STRING_TYPE_ID = (byte)14;
  // Indicators for controlling stack-based sub-message expressions:
  public static final byte FUDGE_MSG_TYPE_ID = (byte)15;
  // End message indicator type removed as unnecessary, hence no 16
  // The fixed-width byte arrays:
  public static final byte BYTE_ARR_4_TYPE_ID = (byte)17;
  public static final byte BYTE_ARR_8_TYPE_ID = (byte)18;
  public static final byte BYTE_ARR_16_TYPE_ID = (byte)19;
  public static final byte BYTE_ARR_20_TYPE_ID = (byte)20;
  public static final byte BYTE_ARR_32_TYPE_ID = (byte)21;
  public static final byte BYTE_ARR_64_TYPE_ID = (byte)22;
  public static final byte BYTE_ARR_128_TYPE_ID = (byte)23;
  public static final byte BYTE_ARR_256_TYPE_ID = (byte)24;
  public static final byte BYTE_ARR_512_TYPE_ID = (byte)25;
}
