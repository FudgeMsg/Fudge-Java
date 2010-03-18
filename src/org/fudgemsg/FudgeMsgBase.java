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

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * <p>Base class for common functionality of {@link FudgeMsg} and {@link ImmutableFudgeMsg}.</p>
 *
 * @author Andrew Griffin
 */
public class FudgeMsgBase implements Serializable, FudgeFieldContainer, Iterable<FudgeField> {
  
  private final FudgeContext _fudgeContext;
  private final List<FudgeMsgField> _fields = new ArrayList<FudgeMsgField>();

  /**
   * Constructs a new {@link FudgeMsgBase} instance bound to the given {@link FudgeContext}.
   * 
   * @param fudgeContext the {@code FudgeContext} to use for type resolution and other services 
   */
  protected FudgeMsgBase(FudgeContext fudgeContext) {
    if(fudgeContext == null) {
      throw new NullPointerException("Context must be provided.");
    }
    _fudgeContext = fudgeContext;
  }
  
  /**
   * Creates a new {@link FudgeMsgBase} from a set of fields bound to the given {@link FudgeContext}.
   * 
   * @param fields the initial set of fields
   * @param fudgeContext the {@link FudgeContext} to use
   */ 
  protected FudgeMsgBase (final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    if (fields == null) throw new NullPointerException ("Cannot initialize from a null FudgeFieldContainer");
    if (fudgeContext == null) throw new NullPointerException ("Context must be provided");
    _fudgeContext = fudgeContext;
    for (FudgeField field : fields.getAllFields ()) {
      _fields.add (new FudgeMsgField (field));
    }
  }
  
  /**
   * Returns this message's {@link FudgeContext}.
   * 
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  /**
   * Returns this message's list of fields.
   * 
   * @return the list of fields
   */
  protected List<FudgeMsgField> getFields () {
    return _fields;
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
  
  private boolean fieldNameEquals (final String name, final FudgeField field) {
    if (name == null) {
      return field.getName () == null;
    } else {
      return name.equals (field.getName ());
    }
  }
  
  private boolean fieldOrdinalEquals (final Short ordinal, final FudgeField field) {
    if (ordinal == null) {
      return field.getOrdinal () == null;
    } else {
      return ordinal.equals (field.getOrdinal ());
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<FudgeField> getAllByName(String name) {
    List<FudgeField> fields = new ArrayList<FudgeField>();
    for(FudgeMsgField field : _fields) {
      if(fieldNameEquals(name, field)) {
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
      if(fieldNameEquals(name, field)) {
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
      if((name != null) && name.equals (field.getName ())) {
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
      if(fieldOrdinalEquals(ordinalAsShort, field)) {
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
      if((name != null) && name.equals (field.getName ())) {
        return field.getValue();
      }
    }
    return null;
  }
  
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Double}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Double} or {@code null} if no matching or compatible field was found
   */
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Float}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Float} or {@code null} if no matching or compatible field was found
   */
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Long}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Long} or {@code null} if no matching or compatible field was found
   */
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Integer}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Integer} or {@code null} if no matching or compatible field was found
   */
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Short}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Short} or {@code null} if no matching or compatible field was found
   */
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
  
  /**
   * Returns the value from {@link #getValue} as a {@link Byte}.
   * 
   * @param fieldName the field name
   * @param ordinal the field ordinal
   * @return the {@code Byte} or {@code null} if no matching or compatible field was found
   */
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

  /**
   * Returns the first field value with the given field name and requested type identifier.
   * 
   * @param fieldName the field name
   * @param typeId the type identifier
   * @return the field value or {@code null} if no matching field was found
   */
  protected final Object getFirstTypedValue(String fieldName, int typeId) {
    for(FudgeMsgField field : _fields) {
      if(fieldNameEquals(fieldName, field)
          && (field.getType().getTypeId() == typeId)) {
        return field.getValue();
      }
    }
    return null;
  }
  
  /**
   * Returns the first field value with the given ordinal index and requested type identifier.
   * 
   * @param ordinal the ordinal index
   * @param typeId the type identifier
   * @return the field value or {@code null} if no matching field was found
   */
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getFieldValue (final Class<T> clazz, final FudgeField field) throws IllegalArgumentException {
    return getFudgeContext ().getFieldValue (clazz, field);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof FudgeMsgBase)) return false;
    final FudgeMsgBase fm = (FudgeMsgBase)o;
    if (!getFudgeContext ().equals (fm.getFudgeContext ())) return false;
    Iterator<FudgeField> me = iterator ();
    Iterator<FudgeField> other = fm.iterator ();
    while (me.hasNext () && other.hasNext ()) {
      if (!me.next ().equals (other.next ())) return false;
    }
    return me.hasNext () == other.hasNext ();
  }
  
}