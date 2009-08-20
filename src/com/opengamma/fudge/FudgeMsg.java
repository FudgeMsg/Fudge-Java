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

import com.opengamma.fudge.types.PrimitiveFieldTypes;

/**
 * A container for {@link FudgeMsgField}s.
 *
 * @author kirk
 */
public class FudgeMsg implements Serializable {
  private final List<FudgeMsgField> _fields = new ArrayList<FudgeMsgField>();
  private volatile int _size = -1;
  
  public FudgeMsg() {
  }
  
  public FudgeMsg(FudgeMsg other) {
    if(other == null) {
      throw new NullPointerException("Cannot initialize from a null other FudgeMsg");
    }
    initializeFromByteArray(other.toByteArray());
  }
  
  public FudgeMsg(byte[] byteArray) {
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
  
  public byte[] toByteArray() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(getSize());
    DataOutputStream dos = new DataOutputStream(baos);
    try {
      FudgeStreamEncoder.writeMsg(dos, this);
    } catch (IOException e) {
      throw new RuntimeException("Had an IOException writing to a ByteArrayOutputStream.", e);
    }
    return baos.toByteArray();
  }
  
  public int getSize() {
    if(_size == -1) {
      _size = computeSize();
    }
    return _size;
  }

  /**
   * @return
   */
  protected int computeSize() {
    int size = 0;
    // Message prefix
    size += 8;
    for(FudgeMsgField field : _fields) {
      size += field.getSize();
    }
    return size;
  }
  
  // Primitive Queries:
  public Double getDouble(String fieldName) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(fieldName, field.getName())
          && (field.getType().getTypeId() == FudgeTypeDictionary.DOUBLE_TYPE_ID)) {
        return (Double) field.getValue();
      }
    }
    return null;
  }
  
  public Double getDouble(short ordinal) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinal, field.getOrdinal())
          && (field.getType().getTypeId() == FudgeTypeDictionary.DOUBLE_TYPE_ID)) {
        return (Double) field.getValue();
      }
    }
    return null;
  }
  
  public Long getLong(String fieldName) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(fieldName, field.getName())
          && (field.getType().getTypeId() == FudgeTypeDictionary.LONG_TYPE_ID)) {
        return (Long) field.getValue();
      }
    }
    return null;
  }

  public Long getLong(short ordinal) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinal, field.getOrdinal())
          && (field.getType().getTypeId() == FudgeTypeDictionary.LONG_TYPE_ID)) {
        return (Long) field.getValue();
      }
    }
    return null;
  }

}
