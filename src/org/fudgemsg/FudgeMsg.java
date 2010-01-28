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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
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
 * Instead of constructing an instance of this directly, a preferred approach is
 * to request a MutableFudgeFieldContainer from the main context, or a
 * serialisation context as that may return an implementation more appropriate
 * to the underlying or target stream.
 *
 * @author kirk
 */
public class FudgeMsg implements Serializable, MutableFudgeFieldContainer, Iterable<FudgeField> {
  private final FudgeContext _fudgeContext;
  private final List<FudgeMsgField> _fields = new ArrayList<FudgeMsgField>();

  /**
   * Constructs a new {@link FudgeMsg} instance bound to the given {@link FudgeContext}.
   * 
   * @param fudgeContext the {@code FudgeContext} to use for type resolution and other services 
   */
  protected FudgeMsg(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
  }
  
  /*public FudgeMsg(FudgeMsg other) {
    this (other, other.getFudgeContext ());
  }*/
  
  /*public FudgeMsg(byte[] byteArray, FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
    initializeFromByteArray(byteArray);
  }*/
  
  /**
   * Creates a new {@link FudgeMsg} from a set of fields bound to the given {@link FudgeContext}.
   * 
   * @param fields the initial set of fields
   * @param fudgeContext the {@link FudgeContext} to use
   */ 
  protected FudgeMsg (final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    if (fields == null) throw new NullPointerException ("Cannot initialize from a null FudgeFieldContainer");
    if (fudgeContext == null) throw new NullPointerException ("Context must be provided");
    _fudgeContext = fudgeContext;
    for (FudgeField field : fields.getAllFields ()) {
      _fields.add (new FudgeMsgField (field));
    }
  }
  
  /*protected void initializeFromByteArray(byte[] byteArray) {
    final FudgeMessageStreamReader reader = getFudgeContext ().allocateMessageReader (new ByteArrayInputStream (byteArray));
    try {
      _fields.addAll(reader.nextMessage ()._fields);
    } catch (IOException ioe) {
      throw new FudgeRuntimeException ("error reading byte[] data", ioe);
    }
    getFudgeContext ().releaseMessageReader (reader);
  }*/
  
  /**
   * Returns this message's {@link FudgeContext}.
   * 
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(FudgeField field) {
    if(field == null) {
      throw new NullPointerException("Cannot add an empty field");
    }
    _fields.add(new FudgeMsgField(field));
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, Object value) {
    add(name, null, value);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Integer ordinal, Object value) {
    add(null, ordinal, value);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, Integer ordinal, Object value) {
    FudgeFieldType<?> type = determineTypeFromValue(value);
    if(type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    }
    add(name, ordinal, type, value);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public short getNumFields() {
    int size = _fields.size();
    assert size <= Short.MAX_VALUE;
    return (short)size;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<FudgeField> getAllFields() {
    return (List) Collections.unmodifiableList(_fields);
  }
  
  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
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
   * {@inheritDoc}
   */
  @Override
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
   * {@inheritDoc}
   */
  @Override
  public FudgeField getByOrdinal(int ordinal) {
    for(FudgeMsgField field : _fields) {
      if((field.getOrdinal() != null) && (ordinal == field.getOrdinal())) {
        return field;
      }
    }
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<FudgeField> getAllByName(String name) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(name, field.getName())) {
        fields.add(field);
      }
    }
    return fields;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeField getByName(String name) {
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(name, field.getName())) {
        return field;
      }
    }
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Object getValue(String name) {
    for(FudgeMsgField field : _fields) {
      if((name != null) && ObjectUtils.equals(name, field.getName())) {
        return field.getValue();
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getValue(int ordinal) {
    Short ordinalAsShort = (short) ordinal;
    for(FudgeMsgField field : _fields) {
      if(ObjectUtils.equals(ordinalAsShort, field.getOrdinal())) {
        return field.getValue();
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  
  // Andrew - removed the byte[] conversions because they are working with a message envelope which I
  // think is misleading as it isn't valid when called for nested submessages and is a different length
  // to the calculated message size 
  
  /*public byte[] toByteArray() {
    return toByteArray ((short)0);
  }
  
  public byte[] toByteArray(short taxonomy) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(FudgeSize.calculateMessageSize(null, this));
    FudgeMessageStreamWriter writer = getFudgeContext().allocateMessageWriter(baos);
    try {
      writer.writeMessage (this, taxonomy);
    } catch (IOException e) {
      return null;
    }
    getFudgeContext().releaseMessageWriter(writer);
    return baos.toByteArray();
  }*/
  
  // Primitive Queries:
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Double getDouble(String fieldName) {
    return getAsDoubleInternal(fieldName, null);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Float getFloat(String fieldName) {
    return getAsFloatInternal(fieldName, null);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Long getLong(String fieldName) {
    return getAsLongInternal(fieldName, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getInt(String fieldName) {
    return getAsIntInternal(fieldName, null);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Short getShort(String fieldName) {
    return getAsShortInternal(fieldName, null);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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

  
  /**
   * {@inheritDoc}
   */
  @Override
  public Byte getByte(String fieldName) {
    return getAsByteInternal(fieldName, null);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String getString(String fieldName) {
    return (String) getFirstTypedValue(fieldName, FudgeTypeDictionary.STRING_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getString(int ordinal) {
    return (String) getFirstTypedValue(ordinal, FudgeTypeDictionary.STRING_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean getBoolean(String fieldName) {
    return (Boolean) getFirstTypedValue(fieldName, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean getBoolean(int ordinal) {
    return (Boolean) getFirstTypedValue(ordinal, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldContainer getMessage(int ordinal) {
    return (FudgeFieldContainer) getFirstTypedValue(ordinal, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * Resolves any field ordinals to field names from the given taxonomy.
   * 
   * @param taxonomy the taxonomy to use
   */
  public void setNamesFromTaxonomy(FudgeTaxonomy taxonomy) {
    if(taxonomy == null) {
      return;
    }
    for(int i = 0; i < _fields.size(); i++) {
      FudgeMsgField field = _fields.get(i);
      if((field.getOrdinal() != null) && (field.getName() == null)) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if(nameFromTaxonomy != null) {
          field = new FudgeMsgField(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
          _fields.set(i, field);
        }
      }
      if(field.getValue() instanceof FudgeMsg) {
        FudgeMsg subMsg = (FudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      } else if (field.getValue () instanceof FudgeFieldContainer) {
        FudgeMsg subMsg = new FudgeMsg ((FudgeFieldContainer)field.getValue (), getFudgeContext ());
        subMsg.setNamesFromTaxonomy (taxonomy);
        field = new FudgeMsgField (field.getType (), subMsg, field.getName (), field.getOrdinal ());
        _fields.set (i, field);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<FudgeField> iterator() {
    return new ArrayList<FudgeField>(_fields).iterator();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
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
