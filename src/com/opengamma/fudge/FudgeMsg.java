/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.fudge.taxon.FudgeTaxonomy;
import com.opengamma.fudge.types.PrimitiveFieldTypes;

/**
 * A container for {@link FudgeMsgField}s.
 *
 * @author kirk
 */
public class FudgeMsg implements Serializable, SizeComputable {
  private final SizeCache _sizeCache = new SizeCache(this);
  private final List<FudgeMsgField> _fields = new ArrayList<FudgeMsgField>();
  
  public FudgeMsg() {
  }
  
  public FudgeMsg(FudgeMsg other) {
    if(other == null) {
      throw new NullPointerException("Cannot initialize from a null other FudgeMsg");
    }
    initializeFromByteArray(other.toByteArray());
  }
  
  public FudgeMsg(byte[] byteArray, FudgeTaxonomy taxonomy) {
    initializeFromByteArray(byteArray);
  }
  
  protected void initializeFromByteArray(byte[] byteArray) {
    ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
    DataInputStream is = new DataInputStream(bais);
    FudgeMsg other;
    try {
      other = FudgeStreamDecoder.readMsg(is);
    } catch (IOException e) {
      throw new RuntimeException("IOException thrown using ByteArrayInputStream", e);
    }
    _fields.addAll(other._fields);
  }
  
  public void add(FudgeField field) {
    if(field == null) {
      throw new NullPointerException("Cannot add an empty field");
    }
    _fields.add(new FudgeMsgField(field));
  }
  
  public void add(Object value, String name) {
    add(value, name, null);
  }
  
  public void add(Object value, Short ordinal) {
    add(value, null, ordinal);
  }
  
  public void add(Object value, String name, Short ordinal) {
    FudgeFieldType<?> type = determineTypeFromValue(value);
    if(type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    }
    add(type, value, name, ordinal);
  }
  
  public void add(FudgeFieldType<?> type, Object value, String name, Short ordinal) {
    if(_fields.size() >= Short.MAX_VALUE) {
      throw new IllegalStateException("Can only add " + Short.MAX_VALUE + " to a single message.");
    }
    if(type == null) {
      throw new NullPointerException("Cannot add a field without a type specified.");
    }
    
    // Adjust integral values to the lowest possible representation.
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.SHORT_TYPE_ID:
    case FudgeTypeDictionary.INT_TYPE_ID:
    case FudgeTypeDictionary.LONG_TYPE_ID:
      long valueAsLong = ((Number)value).longValue();
      if((valueAsLong >= Byte.MIN_VALUE) && (valueAsLong <= Byte.MAX_VALUE)) {
        value = new Byte((byte)valueAsLong);
        type = PrimitiveFieldTypes.BYTE_TYPE;
      } else if((valueAsLong >= Short.MIN_VALUE) && (valueAsLong <= Short.MAX_VALUE)) {
        value = new Short((short)valueAsLong);
        type = PrimitiveFieldTypes.SHORT_TYPE;
      } else if((valueAsLong >= Integer.MIN_VALUE) && (valueAsLong <= Integer.MAX_VALUE)) {
        value = new Integer((int)valueAsLong);
        type = PrimitiveFieldTypes.INT_TYPE;
      }
      break;
    }
    
    FudgeMsgField field = new FudgeMsgField(type, value, name, ordinal);
    _fields.add(field);
  }
  
  protected FudgeFieldType<?> determineTypeFromValue(Object value) {
    if(value == null) {
      throw new NullPointerException("Cannot determine type for null value.");
    }
    FudgeFieldType<?> type = FudgeTypeDictionary.INSTANCE.getByJavaType(value.getClass());
    return type;
  }
  
  public short getNumFields() {
    int size = _fields.size();
    assert size <= Short.MAX_VALUE;
    return (short)size;
  }
  
  /**
   * Return an <em>unmodifiable</em> list of all the fields in this message, in the index
   * order for those fields.
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<FudgeField> getAllFields() {
    return (List) Collections.unmodifiableList(_fields);
  }
  
  public FudgeField getByIndex(int index) {
    if(index < 0) {
      throw new ArrayIndexOutOfBoundsException("Cannot specify a negative index into a FudgeMsg.");
    }
    if(index >= _fields.size()) {
      return null;
    }
    return _fields.get(index);
  }
  
  // REVIEW kirk 2009-08-16 -- All of these getters are currently extremely unoptimized.
  // There may be an option required if we have a lot of random access to the field content
  // to speed things up by building an index.
  
  public List<FudgeField> getAllByOrdinal(short ordinal) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for(FudgeMsgField field : _fields) {
      if((field.getOrdinal() != null) && (ordinal == field.getOrdinal())) {
        fields.add(field);
      }
    }
    return fields;
  }
  
  
  public FudgeField getByOrdinal(short ordinal) {
    for(FudgeMsgField field : _fields) {
      if((field.getOrdinal() != null) && (ordinal == field.getOrdinal())) {
        return field;
      }
    }
    return null;
  }
  
  public List<FudgeField> getAllByName(String name) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(name, field.getName())) {
        fields.add(field);
      }
    }
    return fields;
  }
  
  public FudgeField getByName(String name) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(name, field.getName())) {
        return field;
      }
    }
    return null;
  }
  
  public Object getValue(String name) {
    for(FudgeMsgField field : _fields) {
      if((name != null) && ObjectUtils.equals(name, field.getName())) {
        return field.getValue();
      }
    }
    return null;
  }

  public Object getValue(short ordinal) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinal, field.getOrdinal())) {
        return field.getValue();
      }
    }
    return null;
  }

  public Object getValue(String name, Short ordinal) {
    for(FudgeMsgField field : _fields) {
      if((ordinal != null) && ObjectUtils.equals(ordinal, field.getOrdinal())) {
        return field.getValue();
      }
      if((name != null) && ObjectUtils.equals(name, field.getName())) {
        return field.getValue();
      }
    }
    return null;
  }

  public byte[] toByteArray() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(computeSize(null));
    DataOutputStream dos = new DataOutputStream(baos);
    try {
      FudgeStreamEncoder.writeMsg(dos, this);
    } catch (IOException e) {
      throw new RuntimeException("Had an IOException writing to a ByteArrayOutputStream.", e);
    }
    return baos.toByteArray();
  }
  
  public int getSize(FudgeTaxonomy taxonomy) {
    return _sizeCache.getSize(taxonomy);
  }

  /**
   * @return
   */
  @Override
  public int computeSize(FudgeTaxonomy taxonomy) {
    int size = 0;
    // Message prefix
    size += 8;
    for(FudgeMsgField field : _fields) {
      size += field.getSize(taxonomy);
    }
    return size;
  }
  
  // Primitive Queries:
  public Double getDouble(String fieldName) {
    return (Double) getFirstTypedValue(fieldName, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }
  
  public Double getDouble(short ordinal) {
    return (Double) getFirstTypedValue(ordinal, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }
  
  public Double getAsDouble(String fieldName) {
    return getAsDoubleInternal(fieldName, null);
  }
  
  public Double getAsDouble(short ordinal) {
    return getAsDoubleInternal(null, ordinal);
  }
  
  protected Double getAsDoubleInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.doubleValue();
    }
    return null;
  }
  
  public Float getFloat(String fieldName) {
    return (Float) getFirstTypedValue(fieldName, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }
  
  public Float getFloat(short ordinal) {
    return (Float) getFirstTypedValue(ordinal, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }
  
  public Float getAsFloat(String fieldName) {
    return getAsFloatInternal(fieldName, null);
  }
  
  public Float getAsFloat(short ordinal) {
    return getAsFloatInternal(null, ordinal);
  }
  
  protected Float getAsFloatInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.floatValue();
    }
    return null;
  }
  
  public Long getLong(String fieldName) {
    return (Long) getFirstTypedValue(fieldName, FudgeTypeDictionary.LONG_TYPE_ID);
  }

  public Long getLong(short ordinal) {
    return (Long) getFirstTypedValue(ordinal, FudgeTypeDictionary.LONG_TYPE_ID);
  }
  
  public Long getAsLong(String fieldName) {
    return getAsLongInternal(fieldName, null);
  }
  
  public Long getAsLong(short ordinal) {
    return getAsLongInternal(null, ordinal);
  }
  
  protected Long getAsLongInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.longValue();
    }
    return null;
  }
  
  public Integer getInt(String fieldName) {
    return (Integer) getFirstTypedValue(fieldName, FudgeTypeDictionary.INT_TYPE_ID);
  }

  public Integer getInt(short ordinal) {
    return (Integer) getFirstTypedValue(ordinal, FudgeTypeDictionary.INT_TYPE_ID);
  }
  
  public Integer getAsInt(String fieldName) {
    return getAsIntInternal(fieldName, null);
  }
  
  public Integer getAsInt(short ordinal) {
    return getAsIntInternal(null, ordinal);
  }
  
  protected Integer getAsIntInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.intValue();
    }
    return null;
  }
  
  public Short getShort(String fieldName) {
    return (Short) getFirstTypedValue(fieldName, FudgeTypeDictionary.SHORT_TYPE_ID);
  }

  public Short getShort(short ordinal) {
    return (Short) getFirstTypedValue(ordinal, FudgeTypeDictionary.SHORT_TYPE_ID);
  }
  
  public Short getAsShort(String fieldName) {
    return getAsShortInternal(fieldName, null);
  }
  
  public Short getAsShort(short ordinal) {
    return getAsShortInternal(null, ordinal);
  }
  
  protected Short getAsShortInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.shortValue();
    }
    return null;
  }
  
  public Byte getByte(String fieldName) {
    return (Byte) getFirstTypedValue(fieldName, FudgeTypeDictionary.BYTE_TYPE_ID);
  }

  public Byte getByte(short ordinal) {
    return (Byte) getFirstTypedValue(ordinal, FudgeTypeDictionary.BYTE_TYPE_ID);
  }
  
  public Byte getAsByte(String fieldName) {
    return getAsByteInternal(fieldName, null);
  }
  
  public Byte getAsByte(short ordinal) {
    return getAsByteInternal(null, ordinal);
  }
  
  protected Byte getAsByteInternal(String fieldName, Short ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.byteValue();
    }
    return null;
  }
  
  public String getString(String fieldName) {
    return (String) getFirstTypedValue(fieldName, FudgeTypeDictionary.STRING_TYPE_ID);
  }

  public String getString(short ordinal) {
    return (String) getFirstTypedValue(ordinal, FudgeTypeDictionary.STRING_TYPE_ID);
  }
  
  protected Object getFirstTypedValue(String fieldName, int typeId) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(fieldName, field.getName())
          && (field.getType().getTypeId() == typeId)) {
        return field.getValue();
      }
    }
    return null;
  }
  
  protected Object getFirstTypedValue(short ordinal, int typeId) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinal, field.getOrdinal())
          && (field.getType().getTypeId() == typeId)) {
        return field.getValue();
      }
    }
    return null;
  }
  
}
