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
package org.fudgemsg;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.DoubleArrayFieldType;
import org.fudgemsg.types.FloatArrayFieldType;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IntArrayFieldType;
import org.fudgemsg.types.LongArrayFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.ShortArrayFieldType;
import org.fudgemsg.types.StringFieldType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.UnknownFudgeFieldType;
import org.fudgemsg.types.DateFieldType;
import org.fudgemsg.types.TimeFieldType;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.secondary.JavaUtilDateFieldType;
import org.fudgemsg.types.secondary.JavaUtilUUIDFieldType;

/**
 * Contains all the {@link FudgeFieldType} definitions for a particular
 * Fudge installation.
 * You can control it through your {@link FudgeContext}.
 *
 * @author kirk
 */
public final class FudgeTypeDictionary {
  
  private volatile FudgeFieldType<?>[] _typesById = new FudgeFieldType<?>[0];
  private volatile UnknownFudgeFieldType[] _unknownTypesById = new UnknownFudgeFieldType[0];
  private final Map<Class<?>, FudgeFieldType<?>> _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType<?>>();
  private final Map<Class<?>, SecondaryFieldType<?,?>> _secondaryTypesByJavaType = new ConcurrentHashMap<Class<?>, SecondaryFieldType<?,?>> ();
  
  /**
   * Creates a new {@link FudgeTypeDictionary} configured with the default types from the Fudge specification. Also
   * includes some standard secondary types.
   */
  public FudgeTypeDictionary() {
    // primary types
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
    addType(DateFieldType.INSTANCE);
    addType(TimeFieldType.INSTANCE);
    addType(DateTimeFieldType.INSTANCE);
    // secondary types
    addType(JavaUtilUUIDFieldType.INSTANCE);
    addType(JavaUtilDateFieldType.INSTANCE);
  }
  
  /**
   * Register a new type with the dictionary. Custom types that are not part of the Fudge specification should use IDs allocated downwards from 255 for compatibility
   * with future versions that might include additional standard types.
   * 
   * @param type the {@link FudgeFieldType} definition of the type
   * @param alternativeTypes any additional Java classes that are synonymous with this type.
   */
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    if(type == null) {
      throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
    }
    synchronized (this) {
      if (type instanceof SecondaryFieldType<?,?>) {
        _secondaryTypesByJavaType.put (type.getJavaType (), (SecondaryFieldType<?,?>)type);
      } else {
        int newLength = Math.max(type.getTypeId() + 1, _typesById.length);
        FudgeFieldType<?>[] newArray = Arrays.copyOf(_typesById, newLength);
        newArray[type.getTypeId()] = type;
        _typesById = newArray;
      }
    }
    _typesByJavaType.put(type.getJavaType(), type);
    for(Class<?> alternativeType : alternativeTypes) {
      _typesByJavaType.put(alternativeType, type);
    }
  }
  
  /**
   * Resolves a Java class to a {@link FudgeFieldType} registered with this dictionary.
   * 
   * @param javaType the class to resolve
   * @return the matching Fudge type, or {@code null} if none is found
   */
  public FudgeFieldType<?> getByJavaType(final Class<?> javaType) {
    if (javaType == null) return null;
    FudgeFieldType<?> fieldType = _typesByJavaType.get(javaType);
    if (fieldType != null) return fieldType;
    for (Class<?> i : javaType.getInterfaces ()) {
      fieldType = getByJavaType (i);
      if (fieldType != null) return fieldType;
    }
    return getByJavaType (javaType.getSuperclass ());
  }
  
  /**
   * Resolves a Java class to a {@link SecondaryFieldType} registered with this dictionary.
   * 
   * @param javaType class to look up
   * @return the registered field type, or {@code null} if none is registered
   */
  protected SecondaryFieldType<?,?> getSecondaryByJavaType (final Class<?> javaType) {
    return _secondaryTypesByJavaType.get (javaType);
  }
  
  /**
   * Obtain a <em>known</em> type by the type ID specified.
   * For processing unhandled variable-width field types, this method will return
   * {@code null}, and {@link #getUnknownType(int)} should be used if unhandled-type
   * processing is desired.
   * 
   * @param typeId the numeric type identifier
   * @return The type with the specified type identifier, or {@code null}.
   */
  public FudgeFieldType<?> getByTypeId(int typeId) {
    if(typeId >= _typesById.length) {
      return null;
    }
    return _typesById[typeId];
  }
  
  /**
   * Obtain an <em>unknown</em> type wrapper for the type ID specified. Unknown types allow data
   * to be preserved within a Fudge message even if the application is unable to process it.
   * 
   * @param typeId the numeric type identifier
   * @return A type representing this identifier
   */
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
  
  /**
   * Type conversion for secondary types.
   * 
   * @param <T> type to convert to
   * @param clazz target class for the converted value
   * @param field field containing the value to convert
   * @return the converted value
   */
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue (final Class<T> clazz, final FudgeField field) {
    final Object value = field.getValue ();
    if (value == null) return null;
    if (clazz.isAssignableFrom (value.getClass ())) return (T)value;
    final FudgeFieldType<?> type = field.getType ();
    if (type instanceof SecondaryFieldType) {
      final SecondaryFieldType sourceType = (SecondaryFieldType)type;
      if (clazz.isAssignableFrom (sourceType.getPrimaryType ().getJavaType ())) {
        // been asked for the primary type
        return (T)sourceType.secondaryToPrimary (value);
      } else {
        final SecondaryFieldType targetType = getSecondaryByJavaType (clazz);
        if (targetType == null) {
          // don't recognise the requested type
          throw new FudgeRuntimeException ("cannot convert " + sourceType + " to unregistered secondary type " + clazz.getName ());
        } else {
          if (targetType.getPrimaryType ().getJavaType ().isAssignableFrom (sourceType.getPrimaryType ().getJavaType ())) {
            // primary and requested have a common base
            return (T)targetType.primaryToSecondary (sourceType.secondaryToPrimary (value));
          } else {
            // no common ground
            throw new FudgeRuntimeException ("no Fudge primary type allows conversion from " + sourceType + " to " + targetType);
          }
        }
      }
    } else {
      final SecondaryFieldType targetType = getSecondaryByJavaType (clazz);
      if (targetType == null) {
        // don't recognise the requested type
        throw new FudgeRuntimeException ("cannot convert " + type + " to unregistered secondary type " + clazz.getName ());
      } else {
        if (targetType.getPrimaryType ().getJavaType ().isAssignableFrom (value.getClass ())) {
          // secondary type extends our current type
          return (T)targetType.primaryToSecondary (value);
        } else {
          // secondary type doesn't extend our current type
          throw new FudgeRuntimeException ("secondary type " + targetType + " does not allow conversion from " + value.getClass ().getName ());
        }
      }
    }
  }
  
  // --------------------------
  // STANDARD FUDGE FIELD TYPES
  // --------------------------
  
  /**
   * Standard Fudge field type: unsized indicator value. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte INDICATOR_TYPE_ID = (byte)0;
  
  /**
   * Standard Fudge field type: boolean. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BOOLEAN_TYPE_ID = (byte)1;
  
  /**
   * Standard Fudge field type: 8-bit signed integer. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_TYPE_ID = (byte)2;
  
  /**
   * Standard Fudge field type: 16-bit signed integer. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte SHORT_TYPE_ID = (byte)3;
  
  /**
   * Standard Fudge field type: 32-bit signed integer. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte INT_TYPE_ID = (byte)4;
  
  /**
   * Standard Fudge field type: 64-bit signed integer. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte LONG_TYPE_ID = (byte)5;
  
  /**
   * Standard Fudge field type: byte array. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARRAY_TYPE_ID = (byte)6;
  
  /**
   * Standard Fudge field type: array of 16-bit signed integers. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte SHORT_ARRAY_TYPE_ID = (byte)7;
  
  /**
   * Standard Fudge field type: array of 32-bit signed integers. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte INT_ARRAY_TYPE_ID = (byte)8;
  
  /**
   * Standard Fudge field type: array of 64-bit signed integers. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte LONG_ARRAY_TYPE_ID = (byte)9;
  
  /**
   * Standard Fudge field type: 32-bit floating point. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte FLOAT_TYPE_ID = (byte)10;
  
  /**
   * Standard Fudge field type: 64-bit floating point. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte DOUBLE_TYPE_ID = (byte)11;
  
  /**
   * Standard Fudge field type: array of 32-bit floating point. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte FLOAT_ARRAY_TYPE_ID = (byte)12;
  
  /**
   * Standard Fudge field type: array of 64-bit floating point. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte DOUBLE_ARRAY_TYPE_ID = (byte)13;
  
  /**
   * Standard Fudge field type: string. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte STRING_TYPE_ID = (byte)14;
  
  // Indicators for controlling stack-based sub-message expressions:
  
  /**
   * Standard Fudge field type: embedded Fudge sub-message. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte FUDGE_MSG_TYPE_ID = (byte)15;
  
  // End message indicator type removed as unnecessary, hence no 16
  
  // The fixed-width byte arrays:
  
  /**
   * Standard Fudge field type: byte array of length 4. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_4_TYPE_ID = (byte)17;
  
  /**
   * Standard Fudge field type: byte array of length 8. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_8_TYPE_ID = (byte)18;
  
  /**
   * Standard Fudge field type: byte array of length 16. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_16_TYPE_ID = (byte)19;
  
  /**
   * Standard Fudge field type: byte array of length 20. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_20_TYPE_ID = (byte)20;
  
  /**
   * Standard Fudge field type: byte array of length 32. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_32_TYPE_ID = (byte)21;
  
  /**
   * Standard Fudge field type: byte array of length 64. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_64_TYPE_ID = (byte)22;
  
  /**
   * Standard Fudge field type: byte array of length 128. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_128_TYPE_ID = (byte)23;
  
  /**
   * Standard Fudge field type: byte array of length 256. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_256_TYPE_ID = (byte)24;
  
  /**
   * Standard Fudge field type: byte array of length 512. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte BYTE_ARR_512_TYPE_ID = (byte)25;
  
  /**
   * Standard Fudge field type: date. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte DATE_TYPE_ID = (byte)26;
  
  /**
   * Standard Fudge field type: time. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte TIME_TYPE_ID = (byte)27;
  
  /**
   * Standard Fudge field type: combined date and time. See {@link "http://wiki.fudgemsg.org/display/FDG/Types"} for more details.
   */
  public static final byte DATETIME_TYPE_ID = (byte)28;
}
