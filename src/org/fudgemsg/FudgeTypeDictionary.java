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
package org.fudgemsg;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.DateFieldType;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.DoubleArrayFieldType;
import org.fudgemsg.types.FloatArrayFieldType;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.FudgeTypeConverter;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorFieldTypeConverter;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.IntArrayFieldType;
import org.fudgemsg.types.LongArrayFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.PrimitiveFieldTypesConverter;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.types.ShortArrayFieldType;
import org.fudgemsg.types.StringFieldType;
import org.fudgemsg.types.StringFieldTypeConverter;
import org.fudgemsg.types.TimeFieldType;
import org.fudgemsg.types.UnknownFudgeFieldType;
import org.fudgemsg.types.secondary.SecondaryTypeLoader;

/**
 * The dictionary of all known Fudge types.
 * <p>
 * In order to efficiently send messages, Fudge needs to know the type of each piece of data.
 * This dictionary keeps track of all the known types.
 * <p>
 * This class is mutable and thread-safe via concurrent collections.
 */
public class FudgeTypeDictionary {

  /**
   * The types indexed in an array.
   */
  private volatile FudgeFieldType<?>[] _typesById = new FudgeFieldType<?>[0];
  /**
   * The unknown types indexed in an array.
   */
  private volatile UnknownFudgeFieldType[] _unknownTypesById = new UnknownFudgeFieldType[0];
  /**
   * The types indexed by Java type.
   */
  private final ConcurrentMap<Class<?>, FudgeFieldType<?>> _typesByJavaType;
  /**
   * The types converters indexed by Java type.
   */
  private final ConcurrentMap<Class<?>, FudgeTypeConverter<?,?>> _convertersByJavaType;
  /**
   * A flag to indicate if the classpath is scanned.
   */
  private final AtomicBoolean _haveScannedClasspath = new AtomicBoolean(false);

  /**
   * Creates a new dictionary configured with the default types from the Fudge specification.
   * Some standard secondary types will also be loaded.
   */
  public FudgeTypeDictionary() {
    _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType<?>>();
    _convertersByJavaType = new ConcurrentHashMap<Class<?>, FudgeTypeConverter<?, ?>>();
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
    addType(PrimitiveFieldTypes.BOOLEAN_TYPE, Boolean.class, Boolean.TYPE);
    addType(PrimitiveFieldTypes.BYTE_TYPE, Byte.class, Byte.TYPE);
    addType(PrimitiveFieldTypes.SHORT_TYPE, Short.class, Short.TYPE);
    addType(PrimitiveFieldTypes.INT_TYPE, Integer.class, Integer.TYPE);
    addType(PrimitiveFieldTypes.LONG_TYPE, Long.class, Long.TYPE);
    addType(PrimitiveFieldTypes.FLOAT_TYPE, Float.class, Float.TYPE);
    addType(ShortArrayFieldType.INSTANCE);
    addType(IntArrayFieldType.INSTANCE);
    addType(LongArrayFieldType.INSTANCE);
    addType(IndicatorFieldType.INSTANCE);
    addType(FloatArrayFieldType.INSTANCE);
    addType(PrimitiveFieldTypes.DOUBLE_TYPE, Double.class, Double.TYPE);
    addType(DoubleArrayFieldType.INSTANCE);
    addType(ByteArrayFieldType.VARIABLE_SIZED_INSTANCE);
    addType(StringFieldType.INSTANCE);
    addType(FudgeMsgFieldType.INSTANCE);
    addType(DateFieldType.INSTANCE);
    addType(TimeFieldType.INSTANCE);
    addType(DateTimeFieldType.INSTANCE);
    // default type conversions
    addTypeConverter(PrimitiveFieldTypesConverter.INT_CONVERTER, Integer.class, Integer.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.BOOLEAN_CONVERTER, Boolean.class, Boolean.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.BYTE_CONVERTER, Byte.class, Byte.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.SHORT_CONVERTER, Short.class, Short.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.LONG_CONVERTER, Long.class, Long.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.FLOAT_CONVERTER, Float.class, Float.TYPE);
    addTypeConverter(PrimitiveFieldTypesConverter.DOUBLE_CONVERTER, Double.class, Double.TYPE);
    addTypeConverter(IndicatorFieldTypeConverter.INSTANCE, IndicatorType.class);
    addTypeConverter(StringFieldTypeConverter.INSTANCE, String.class);
    // secondary types
    SecondaryTypeLoader.addTypes(this);
  }

  /**
   * Creates a new dictionary as a clone of another.
   * 
   * @param other  the dictionary to copy data from
   */
  protected FudgeTypeDictionary(final FudgeTypeDictionary other) {
    _typesById = other._typesById;
    _unknownTypesById = other._unknownTypesById;
    _typesByJavaType = new ConcurrentHashMap<Class<?>, FudgeFieldType<?>>(other._typesByJavaType);
    _convertersByJavaType = new ConcurrentHashMap<Class<?>, FudgeTypeConverter<?, ?>>(other._convertersByJavaType);
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a new type conversion with the dictionary.
   * <p>
   * A converter will be used by {@link #getFieldValue} to expand a non-matching type.
   * The secondary type mechanism will register the appropriate conversion
   * automatically when {@link #addType} is called.
   * 
   * @param converter  the converter to register
   * @param types  the types to register against
   */
  public void addTypeConverter(FudgeTypeConverter<?, ?> converter, Class<?>... types) {
    for (Class<?> type : types) {
      _convertersByJavaType.put(type, converter);
      type = type.getSuperclass();
      while (type != null && !Object.class.equals(type)) {
        if (_convertersByJavaType.putIfAbsent(type, converter) != null) {
          break;
        }
        type = type.getSuperclass();
      }
    }
  }

  /**
   * Register a new type with the dictionary.
   * <p>
   * Custom types that are not part of the Fudge specification should use IDs allocated downwards
   * from 255 for compatibility with future versions that might include additional standard types.
   * 
   * @param type  the {@code FudgeFieldType} definition of the type, not null
   * @param alternativeTypes  any additional Java classes that are synonymous with this type
   */
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    if (type == null) {
      throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
    }
    if (type instanceof SecondaryFieldTypeBase<?, ?, ?>) {
      addTypeConverter((SecondaryFieldTypeBase<?, ?, ?>) type, type.getJavaType());
    } else {
      synchronized (this) {
        int newLength = Math.max(type.getTypeId() + 1, _typesById.length);
        FudgeFieldType<?>[] newArray = Arrays.copyOf(_typesById, newLength);
        newArray[type.getTypeId()] = type;
        _typesById = newArray;
        /*for (int i = 0; i < newArray.length; i++) {
          System.out.println (i + "=" + newArray[i]);
        }
        System.out.println ("\n\n");*/
      }
    }
    _typesByJavaType.put(type.getJavaType(), type);
    for (Class<?> alternativeType : alternativeTypes) {
      _typesByJavaType.put(alternativeType, type);
    }
  }

  /**
   * Resolves a Java class to a {@link FudgeFieldType} registered with this dictionary.
   * 
   * @param javaType the class to resolve
   * @return the matching Fudge type, null if none is found
   */
  public FudgeFieldType<?> getByJavaType(final Class<?> javaType) {
    if (javaType == null) {
      return null;
    }
    FudgeFieldType<?> fieldType = _typesByJavaType.get(javaType);
    if (fieldType != null) {
      return fieldType;
    }
    for (Class<?> cls : javaType.getInterfaces()) {
      fieldType = getByJavaType(cls);
      if (fieldType != null) {
        return fieldType;
      }
    }
    return getByJavaType(javaType.getSuperclass());
  }

  /**
   * Resolves a Java class to a {@link FudgeTypeConverter}.
   * <p>
   * A converter may be derived from registration of a {@link SecondaryFieldType},
   * a default conversion between the Java classes that represent the Fudge primitive
   * types, or explicitly registered with {@link #addTypeConverter}.
   * 
   * @param <T> Java type of the class to look up
   * @param javaType  the class to look up
   * @return the registered converter, null if none is available
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeTypeConverter<Object, T> getTypeConverter(final Class<T> javaType) {
    return (FudgeTypeConverter<Object, T>) _convertersByJavaType.get(javaType);
  }

  /**
   * Obtain a <em>known</em> type by the type ID specified.
   * <p>
   * For processing unhandled variable-width field types, this method will return
   * {@code null}, and {@link #getUnknownType(int)} should be used if unhandled-type
   * processing is desired.
   * 
   * @param typeId  the numeric type identifier
   * @return the type with the specified type identifier, null if no type for the id
   */
  public FudgeFieldType<?> getByTypeId(int typeId) {
    if (typeId >= _typesById.length) {
      return null;
    }
    return _typesById[typeId];
  }

  /**
   * Obtain an <em>unknown</em> type wrapper for the type ID specified.
   * <p>
   * Unknown types allow data to be preserved within a Fudge message even if the
   * application is unable to process it.
   * 
   * @param typeId the numeric type identifier
   * @return A type representing this identifier
   */
  public UnknownFudgeFieldType getUnknownType(int typeId) {
    int newLength = Math.max(typeId + 1, _unknownTypesById.length);
    if (_unknownTypesById.length < newLength || _unknownTypesById[typeId] == null) {
      synchronized (this) {
        if (_unknownTypesById.length < newLength || _unknownTypesById[typeId] == null) {
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
   * @param clazz  the target class for the converted value, not null
   * @param field  the field containing the value to convert, null returns null
   * @return the converted value, null if no value
   * @throws IllegalArgumentException if the parameters are not valid for conversion
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> T getFieldValue(final Class<T> clazz, final FudgeField field) throws IllegalArgumentException {
    if (field == null) {
      return null;
    }
    final Object value = field.getValue();
    if (value == null) {
      return null;
    }
    if (clazz.isAssignableFrom(value.getClass())) {
      return (T) value;
    }
    final FudgeFieldType<?> type = field.getType();
    if (type instanceof SecondaryFieldType) {
      final SecondaryFieldType sourceType = (SecondaryFieldType) type;
      if (clazz.isAssignableFrom(sourceType.getPrimaryType().getJavaType())) {
        // been asked for the primary type
        return (T) sourceType.secondaryToPrimary(value);
      } else {
        final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
        if (converter == null) {
          // don't recognize the requested type
          throw new IllegalArgumentException("cannot convert " + sourceType + " to unregistered secondary type " + clazz.getName());
        } else {
          if (converter.canConvertPrimary(sourceType.getPrimaryType().getJavaType())) {
            // primary and requested have a common base
            return converter.primaryToSecondary(sourceType.secondaryToPrimary(value));
          } else {
            // no common ground
            throw new IllegalArgumentException("no Fudge primary type allows conversion from " + sourceType + " to " + clazz.getName());
          }
        }
      }
    } else if (type instanceof IndicatorFieldType) {
      // indicators always get converted to NULL when cast to another type
      return null;
    } else {
      final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
      if (converter == null) {
        // don't recognize the requested type
        if (clazz.isEnum()) {
          // get the field as a string and then try to inflate the enum
          return (T) Enum.valueOf((Class<? extends Enum>) clazz, getFieldValue(String.class, field));
        } else {
          throw new IllegalArgumentException("cannot convert " + type + " to unregistered secondary type " + clazz.getName());
        }
      } else {
        if (converter.canConvertPrimary(value.getClass())) {
          // secondary type extends our current type
          return converter.primaryToSecondary(value);
        } else {
          // secondary type doesn't extend our current type
          throw new IllegalArgumentException("secondary type " + clazz.getName() + " does not allow conversion from " + value.getClass().getName());
        }
      }
    }
  }

  /**
   * Type conversion test for secondary types.
   * <p>
   * Returns {@code true} if {@link #getFieldValue} would return an object instance.
   * 
   * @param <T> type to convert to
   * @param clazz  the target class for the converted value, not null
   * @param field  the field containing the value to convert, null returns false
   * @return {@code true} if a conversion is possible, {@code false} otherwise
   *  (when {@link #getFieldValue} might return {@code null} or throw an exception)
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> boolean canConvertField (final Class<T> clazz, final FudgeField field) {
    if (field == null) {
      return false;
    }
    final Object value = field.getValue();
    if (value == null) {
      return false;
    }
    if (clazz.isAssignableFrom(value.getClass())) {
      return true;
    }
    final FudgeFieldType<?> type = field.getType();
    if (type instanceof SecondaryFieldType) {
      final SecondaryFieldType sourceType = (SecondaryFieldType) type;
      if (clazz.isAssignableFrom(sourceType.getPrimaryType().getJavaType())) {
        // been asked for the primary type
        return true;
      } else {
        final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
        if (converter == null) {
          // don't recognize the requested type
          return false;
        } else {
          // check common base
          return converter.canConvertPrimary(sourceType.getPrimaryType().getJavaType());
        }
      }
    } else if (type instanceof IndicatorFieldType) {
      // indicators can't be converted to instances
      return false;
    } else {
      final FudgeTypeConverter<Object, T> converter = getTypeConverter(clazz);
      if (converter == null) {
        // don't recognize the requested type
        return false;
      } else {
        // does secondary type extend current type
        return converter.canConvertPrimary(value.getClass());
      }
    }
  }

  /**
   * Scans all files available to common classpath loading system heuristics to determine
   * which ones have the {@link FudgeSecondaryType} annotation, and registers those as appropriate
   * secondary types.
   * This is potentially a <em>very</em> expensive operation, and as such is optional.
   */
  public void addAllAnnotatedSecondaryTypes() {
    if (_haveScannedClasspath.getAndSet(true)) {
      return;
    }
    final Set<String> classNamesWithAnnotation = ClasspathUtilities.getClassNamesWithAnnotation(FudgeSecondaryType.class);
    if (classNamesWithAnnotation == null) {
      return;
    }
    for (String className : classNamesWithAnnotation) {
      addAnnotatedSecondaryTypeClass(className);
    }
  }

  /**
   * Add a class which is known to have a {@link FudgeBuilderFor} annotation as an
   * object or message builder (or both). 
   * 
   * @param className  the fully qualified name of the builder class.
   */
  @SuppressWarnings("rawtypes")
  public void addAnnotatedSecondaryTypeClass(String className) {
    Class<?> builderClass = null;
    try {
      builderClass = Class.forName(className);
    } catch (Exception e) {
      // Silently swallow. Can't actually populate it.
      // This should be rare, and you can just stop at this breakpoint
      // (which is why the stack trace is here at all).
      e.printStackTrace();
      return;
    }
    
    for (Field field : builderClass.getFields()) {
      if (!field.isAnnotationPresent(FudgeSecondaryType.class)) {
        continue;
      }
      int fieldModifiers = field.getModifiers();
      if (!Modifier.isStatic(fieldModifiers)) {
        continue;
      }
      if (!Modifier.isPublic(fieldModifiers)) {
        continue;
      }
      if (!Modifier.isStatic(fieldModifiers)) {
        continue;
      }
      FudgeFieldType fudgeType;
      try {
        fudgeType = (FudgeFieldType) field.get(null);
      } catch (Exception e) {
        throw new FudgeRuntimeException("Cannot access field " + field.getName() + " on class " + builderClass.getName() + " with @FudgeSecondaryType annotation", e);
      }
      addType(fudgeType);
    }
  }

  // --------------------------
  // STANDARD FUDGE FIELD TYPES
  // --------------------------
  /**
   * Standard Fudge field type: unsized indicator value. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INDICATOR_TYPE_ID = (byte)0;
  /**
   * Standard Fudge field type: boolean. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BOOLEAN_TYPE_ID = (byte)1;
  /**
   * Standard Fudge field type: 8-bit signed integer. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_TYPE_ID = (byte)2;
  /**
   * Standard Fudge field type: 16-bit signed integer. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte SHORT_TYPE_ID = (byte)3;
  /**
   * Standard Fudge field type: 32-bit signed integer. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INT_TYPE_ID = (byte)4;
  /**
   * Standard Fudge field type: 64-bit signed integer. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte LONG_TYPE_ID = (byte)5;
  /**
   * Standard Fudge field type: byte array. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARRAY_TYPE_ID = (byte)6;
  /**
   * Standard Fudge field type: array of 16-bit signed integers. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte SHORT_ARRAY_TYPE_ID = (byte)7;
  /**
   * Standard Fudge field type: array of 32-bit signed integers. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte INT_ARRAY_TYPE_ID = (byte)8;
  /**
   * Standard Fudge field type: array of 64-bit signed integers. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte LONG_ARRAY_TYPE_ID = (byte)9;
  /**
   * Standard Fudge field type: 32-bit floating point. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte FLOAT_TYPE_ID = (byte)10;
  /**
   * Standard Fudge field type: 64-bit floating point. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DOUBLE_TYPE_ID = (byte)11;
  /**
   * Standard Fudge field type: array of 32-bit floating point. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte FLOAT_ARRAY_TYPE_ID = (byte)12;
  /**
   * Standard Fudge field type: array of 64-bit floating point. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DOUBLE_ARRAY_TYPE_ID = (byte)13;
  /**
   * Standard Fudge field type: string. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte STRING_TYPE_ID = (byte)14;
  
  // Indicators for controlling stack-based sub-message expressions:
  
  /**
   * Standard Fudge field type: embedded Fudge sub-message. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte FUDGE_MSG_TYPE_ID = (byte)15;
  
  // End message indicator type removed as unnecessary, hence no 16
  
  // The fixed-width byte arrays:
  
  /**
   * Standard Fudge field type: byte array of length 4. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_4_TYPE_ID = (byte)17;
  /**
   * Standard Fudge field type: byte array of length 8. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_8_TYPE_ID = (byte)18;
  /**
   * Standard Fudge field type: byte array of length 16. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_16_TYPE_ID = (byte)19;
  /**
   * Standard Fudge field type: byte array of length 20. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_20_TYPE_ID = (byte)20;
  /**
   * Standard Fudge field type: byte array of length 32. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_32_TYPE_ID = (byte)21;
  /**
   * Standard Fudge field type: byte array of length 64. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_64_TYPE_ID = (byte)22;
  /**
   * Standard Fudge field type: byte array of length 128. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_128_TYPE_ID = (byte)23;
  /**
   * Standard Fudge field type: byte array of length 256. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_256_TYPE_ID = (byte)24;
  /**
   * Standard Fudge field type: byte array of length 512. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte BYTE_ARR_512_TYPE_ID = (byte)25;
  /**
   * Standard Fudge field type: date. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DATE_TYPE_ID = (byte)26;
  /**
   * Standard Fudge field type: time. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte TIME_TYPE_ID = (byte)27;
  /**
   * Standard Fudge field type: combined date and time. See <a href="http://wiki.fudgemsg.org/display/FDG/Types">Fudge Types</a> for more details.
   */
  public static final byte DATETIME_TYPE_ID = (byte)28;

}
