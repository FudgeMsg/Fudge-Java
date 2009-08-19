/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.fudge.types.StringFieldType;

/**
 * Contains all the {@link FudgeFieldType} definitions for a particular
 * Fudge installation.
 * This class will usually be used as a classic Singleton, although the constructor
 * is public so that it can be used in a Dependency Injection framework. 
 *
 * @author kirk
 */
public final class FudgeTypeDictionary {
  public static final FudgeTypeDictionary INSTANCE = new FudgeTypeDictionary();
  
  // REVIEW kirk 2009-08-13 -- This implementation is intentionally extremely slow.
  // Once we have working code, we'll speed it up considerably.
  private final Map<Byte, FudgeFieldType<?>> _typesById = Collections.synchronizedMap(new HashMap<Byte, FudgeFieldType<?>>());
  private final Map<Class<?>, FudgeFieldType<?>> _typesByJavaType = Collections.synchronizedMap(new HashMap<Class<?>, FudgeFieldType<?>>());
  
  public void addType(FudgeFieldType<?> type, Class<?>... alternativeTypes) {
    if(type == null) {
      throw new NullPointerException("Must not provide a null FudgeFieldType to add.");
    }
    _typesById.put(type.getTypeId(), type);
    _typesByJavaType.put(type.getJavaType(), type);
    for(Class<?> alternativeType : alternativeTypes) {
      _typesByJavaType.put(alternativeType, type);
    }
  }
  
  public FudgeFieldType<?> getByJavaType(Class<?> javaType) {
    if(javaType == null) {
      return null;
    }
    return _typesByJavaType.get(javaType);
  }
  
  public FudgeFieldType<?> getByTypeId(byte typeId) {
    return _typesById.get(typeId);
  }
  
  // --------------------------
  // STANDARD FUDGE FIELD TYPES
  // --------------------------
  
  public static final byte BOOLEAN_TYPE_ID = (byte)0;

  public static final FudgeFieldType<Boolean> BOOLEAN_TYPE = new FudgeFieldType<Boolean>(BOOLEAN_TYPE_ID, Boolean.TYPE, false, 1);
  public static final byte BYTE_TYPE_ID = (byte)1;
  public static final FudgeFieldType<Byte> BYTE_TYPE = new FudgeFieldType<Byte>(BYTE_TYPE_ID, Byte.TYPE, false, 1);
  public static final byte SHORT_TYPE_ID = (byte)2;
  public static final FudgeFieldType<Short> SHORT_TYPE = new FudgeFieldType<Short>(SHORT_TYPE_ID, Short.TYPE, false, 2);
  public static final byte INT_TYPE_ID = (byte)3;
  public static final FudgeFieldType<Integer> INT_TYPE = new FudgeFieldType<Integer>(INT_TYPE_ID, Integer.TYPE, false, 4);
  public static final byte LONG_TYPE_ID = (byte)4;
  public static final FudgeFieldType<Long> LONG_TYPE = new FudgeFieldType<Long>(LONG_TYPE_ID, Long.TYPE, false, 8);
  public static final byte STRING_TYPE_ID = (byte)22;
  public static final StringFieldType STRING_TYPE = new StringFieldType();
  
  static {
    INSTANCE.addType(BOOLEAN_TYPE, Boolean.class);
    INSTANCE.addType(BYTE_TYPE, Byte.class);
    INSTANCE.addType(SHORT_TYPE, Short.class);
    INSTANCE.addType(INT_TYPE, Integer.class);
    INSTANCE.addType(LONG_TYPE, Long.class);
    INSTANCE.addType(STRING_TYPE);
  }

}
