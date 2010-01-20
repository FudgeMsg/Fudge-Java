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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.original.FudgeStreamWriter;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;


/**
 * A container for {@link FudgeMsgField}s.
 * This instance will contain all data fully extracted from a Fudge-encoded
 * stream, unlike other systems where fields are unpacked as required.
 * Therefore, constructing a {@code FudgeMsg} from a field is relatively more
 * expensive in CPU and memory usage than just holding the original byte array,
 * but lookups are substantially faster.
 *
 * @author kirk
 */
public class FudgeMsg extends FudgeEncodingObject implements Serializable, MutableFudgeFieldContainer, Iterable<FudgeField> {
  private final FudgeContext _fudgeContext;
  private final List<FudgeMsgField> _fields = new ArrayList<FudgeMsgField>();

  @Deprecated
  public FudgeMsg() {
    this(new FudgeContext());
  }
  
  public FudgeMsg(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
  }
  
  public FudgeMsg(FudgeMsg other) {
    if(other == null) {
      throw new NullPointerException("Cannot initialize from a null other FudgeMsg");
    }
    _fudgeContext = other._fudgeContext;
    initializeFromByteArray(other.toByteArray());
  }
  
  public FudgeMsg(byte[] byteArray, FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
    initializeFromByteArray(byteArray);
  }
  
  public FudgeMsg (final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    if (fields == null) throw new NullPointerException ("Cannot initialize from a null FudgeFieldContainer");
    if (fudgeContext == null) throw new NullPointerException ("Context must be provided");
    _fudgeContext = fudgeContext;
    for (FudgeField field : fields.getAllFields ()) {
      _fields.add (new FudgeMsgField (field));
    }
  }
  
  protected void initializeFromByteArray(byte[] byteArray) {
    FudgeMsgEnvelope other = getFudgeContext().deserialize(byteArray);
    _fields.addAll(other.getMessage()._fields);
  }
  
  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void add(FudgeField field) {
    if(field == null) {
      throw new NullPointerException("Cannot add an empty field");
    }
    _fields.add(new FudgeMsgField(field));
  }
  
  public void add(String name, Object value) {
    add(name, null, value);
  }
  
  public void add(Integer ordinal, Object value) {
    add(null, ordinal, value);
  }
  
  public void add(String name, Integer ordinal, Object value) {
    FudgeFieldType<?> type = determineTypeFromValue(value);
    if(type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    }
    add(name, ordinal, type, value);
  }
  
  public void add(String name, Integer ordinal, FudgeFieldType<?> type, Object value) {
    if(_fields.size() >= Short.MAX_VALUE) {
      throw new IllegalStateException("Can only add " + Short.MAX_VALUE + " to a single message.");
    }
    if((ordinal != null) && ((ordinal > Short.MAX_VALUE) || (ordinal < Short.MIN_VALUE))) {
      throw new IllegalArgumentException("Ordinal must be within signed 16-bit range.");
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
    
    Short ordinalAsShort = null;
    if(ordinal != null) {
      ordinalAsShort = ordinal.shortValue();
    }
    FudgeMsgField field = new FudgeMsgField(type, value, name, ordinalAsShort);
    _fields.add(field);
  }
  
  protected FudgeFieldType<?> determineTypeFromValue(Object value) {
    if(value == null) {
      throw new NullPointerException("Cannot determine type for null value.");
    }
    if(value instanceof byte[]) {
      return ByteArrayFieldType.getBestMatch((byte[])value);
    }
    FudgeFieldType<?> type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if((type == null) && (value instanceof UnknownFudgeFieldValue)) {
      UnknownFudgeFieldValue unknownValue = (UnknownFudgeFieldValue) value;
      type = unknownValue.getType();
    }
    return type;
  }
  
  public short getNumFields() {
    int size = _fields.size();
    assert size <= Short.MAX_VALUE;
    return (short)size;
  }
  
  @SuppressWarnings("unchecked")
  public List<FudgeField> getAllFields() {
    return (List) Collections.unmodifiableList(_fields);
  }
  
  @Override
  public Set<String> getAllFieldNames() {
    Set<String> result = new TreeSet<String>();
    for(FudgeField field: _fields) {
      if(field.getName() != null) {
        result.add(field.getName());
      }
    }
    return result;
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
  
  /**
   * Returns a list of all fields with the given ordinal value, in the order they are represented in the Fudge message.
   * If the message contains no matching fields an empty list is returned.  
   */
  public List<FudgeField> getAllByOrdinal(int ordinal) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for(FudgeMsgField field : _fields) {
      if((field.getOrdinal() != null) && (ordinal == field.getOrdinal())) {
        fields.add(field);
      }
    }
    return fields;
  }
  
  /**
   * Returns a field with the given ordinal value.
   */
  public FudgeField getByOrdinal(int ordinal) {
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

  public Object getValue(int ordinal) {
    Short ordinalAsShort = (short) ordinal;
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinalAsShort, field.getOrdinal())) {
        return field.getValue();
      }
    }
    return null;
  }

  public Object getValue(String name, Integer ordinal) {
    for(FudgeMsgField field : _fields) {
      if((ordinal != null) && (field.getOrdinal() != null) && (ordinal == field.getOrdinal().intValue())) {
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
    FudgeMessageStreamWriter writer = getFudgeContext().allocateMessageWriter(baos);
    try {
      writer.writeMessage (this, 0);
    } catch (IOException e) {
      return null;
    }
    getFudgeContext().releaseMessageWriter(writer);
    return baos.toByteArray();
  }
  
  @Override
  public int computeSize(FudgeTaxonomy taxonomy) {
    int size = 0;
    for(FudgeMsgField field : _fields) {
      size += field.getSize(taxonomy);
    }
    return size;
  }
  
  // Primitive Queries:
  
  public Double getDouble(String fieldName) {
    return getAsDoubleInternal(fieldName, null);
  }
  
  public Double getDouble(int ordinal) {
    return getAsDoubleInternal(null, ordinal);
  }
  
  protected Double getAsDoubleInternal(String fieldName, Integer ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.doubleValue();
    }
    return null;
  }
  
  public Float getFloat(String fieldName) {
    return getAsFloatInternal(fieldName, null);
  }
  
  public Float getFloat(int ordinal) {
    return getAsFloatInternal(null, ordinal);
  }
  
  protected Float getAsFloatInternal(String fieldName, Integer ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.floatValue();
    }
    return null;
  }
  
  public Long getLong(String fieldName) {
    return getAsLongInternal(fieldName, null);
  }

  public Long getLong(int ordinal) {
    return getAsLongInternal(null, ordinal);
  }
  
  protected Long getAsLongInternal(String fieldName, Integer ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.longValue();
    }
    return null;
  }  
  
  public Integer getInt(String fieldName) {
    return getAsIntInternal(fieldName, null);
  }
  
  public Integer getInt(int ordinal) {
    return getAsIntInternal(null, ordinal);
  }
  
  protected Integer getAsIntInternal(String fieldName, Integer ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.intValue();
    }
    return null;
  }
  
  public Short getShort(String fieldName) {
    return getAsShortInternal(fieldName, null);
  }
  
  public Short getShort(int ordinal) {
    return getAsShortInternal(null, ordinal);
  }
  
  protected Short getAsShortInternal(String fieldName, Integer ordinal) {
    Object value = getValue(fieldName, ordinal);
    if(value instanceof Number) {
      Number numberValue = (Number) value;
      return numberValue.shortValue();
    }
    return null;
  }

  
  public Byte getByte(String fieldName) {
    return getAsByteInternal(fieldName, null);
  }
  
  public Byte getByte(int ordinal) {
    return getAsByteInternal(null, ordinal);
  }
  
  protected Byte getAsByteInternal(String fieldName, Integer ordinal) {
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

  public String getString(int ordinal) {
    return (String) getFirstTypedValue(ordinal, FudgeTypeDictionary.STRING_TYPE_ID);
  }
  
  public Boolean getBoolean(String fieldName) {
    return (Boolean) getFirstTypedValue(fieldName, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }

  public Boolean getBoolean(int ordinal) {
    return (Boolean) getFirstTypedValue(ordinal, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }
  
  @Override
  public FudgeFieldContainer getMessage(int ordinal) {
    return (FudgeFieldContainer) getFirstTypedValue(ordinal, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  @Override
  public FudgeFieldContainer getMessage(String name) {
    return (FudgeFieldContainer) getFirstTypedValue(name, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  protected final Object getFirstTypedValue(String fieldName, int typeId) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(fieldName, field.getName())
          && (field.getType().getTypeId() == typeId)) {
        return field.getValue();
      }
    }
    return null;
  }
  
  protected final Object getFirstTypedValue(int ordinal, int typeId) {
    for(FudgeMsgField field : _fields) {
      if(field.getOrdinal() == null) {
        continue;
      }
      
      if((field.getOrdinal() == ordinal)
          && (field.getType().getTypeId() == typeId)) {
        return field.getValue();
      }
    }
    return null;
  }

  public void setNamesFromTaxonomy(FudgeTaxonomy taxonomy) {
    if(taxonomy == null) {
      return;
    }
    for(int i = 0; i < _fields.size(); i++) {
      FudgeMsgField field = _fields.get(i);
      if((field.getOrdinal() != null) && (field.getName() == null)) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if(nameFromTaxonomy == null) {
          continue;
        }
        FudgeMsgField replacementField = new FudgeMsgField(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
        _fields.set(i, replacementField);
      }
      
      if(field.getValue() instanceof FudgeMsg) {
        FudgeMsg subMsg = (FudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      }
    }
  }

  @Override
  public Iterator<FudgeField> iterator() {
    return new ArrayList<FudgeField>(_fields).iterator();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FudgeMsg[");
    Iterator<FudgeField> iterator = iterator();
    while (iterator.hasNext()) {
      FudgeField field = iterator.next();
      if (field.getOrdinal() != null) {
        sb.append(field.getOrdinal());
        sb.append(": ");
      }
      if (field.getName() != null) {
        sb.append(field.getName());
      }
      sb.append(" => ");
      sb.append(field.getValue());
      sb.append(", ");
    }
    if (sb.length() > 13) {
      sb.delete(sb.length()-2, sb.length());
    }
    sb.append("]");
    return sb.toString();
  }
  
}
