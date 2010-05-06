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
  private final List<FudgeField> _fields = new ArrayList<FudgeField>();

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
  protected List<FudgeField> getFields () {
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
  public boolean isEmpty () {
    return getNumFields () == 0;
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
    Short ordinalAsShort = (short)ordinal;
    for(FudgeField field : _fields) {
      if (fieldOrdinalEquals (ordinalAsShort, field)) {
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
    Short ordinalAsShort = (short)ordinal;
    for(FudgeField field : _fields) {
      if (fieldOrdinalEquals (ordinalAsShort, field)) {
        return field;
      }
    }
    return null;
  }
  
  /**
   * Tests the field name for equality against a string - {@code null} matches with {@code null}.
   * 
   * @param name name to match against
   * @param field field to test
   * @return {@code true} if the field name matches, {@code false} otherwise 
   */
  protected boolean fieldNameEquals (final String name, final FudgeField field) {
    if (name == null) {
      return field.getName () == null;
    } else {
      return name.equals (field.getName ());
    }
  }
  
  /**
   * Tests the field ordinal for equality against a string - {@code null} matches with {@code null}.
   * 
   * @param ordinal ordinal index to match against
   * @param field field to test
   * @return {@code true} if the field ordinal matches, {@code false} otherwise
   */
  protected boolean fieldOrdinalEquals (final Short ordinal, final FudgeField field) {
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
    for(FudgeField field : _fields) {
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
    for(FudgeField field : _fields) {
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
    FudgeField field = getByName (name);
    return (field != null) ? field.getValue () : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getValue(int ordinal) {
    FudgeField field = getByOrdinal (ordinal);
    return (field != null) ? field.getValue () : null;
  }

  // Primitive Queries:
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Double getDouble(String fieldName) {
    return getFirstTypedValue (Double.class, fieldName, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Double getDouble(int ordinal) {
    return getFirstTypedValue (Double.class, ordinal, FudgeTypeDictionary.DOUBLE_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Float getFloat(String fieldName) {
    return getFirstTypedValue (Float.class, fieldName, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Float getFloat(int ordinal) {
    return getFirstTypedValue (Float.class, ordinal, FudgeTypeDictionary.FLOAT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Long getLong(String fieldName) {
    return getFirstTypedValue (Long.class, fieldName, FudgeTypeDictionary.LONG_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getLong(int ordinal) {
    return getFirstTypedValue (Long.class, ordinal, FudgeTypeDictionary.LONG_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getInt(String fieldName) {
    return getFirstTypedValue (Integer.class, fieldName, FudgeTypeDictionary.INT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getInt(int ordinal) {
    return getFirstTypedValue (Integer.class, ordinal, FudgeTypeDictionary.INT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Short getShort(String fieldName) {
    return getFirstTypedValue (Short.class, fieldName, FudgeTypeDictionary.SHORT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Short getShort(int ordinal) {
    return getFirstTypedValue (Short.class, ordinal, FudgeTypeDictionary.SHORT_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Byte getByte(String fieldName) {
    return getFirstTypedValue (Byte.class, fieldName, FudgeTypeDictionary.BYTE_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Byte getByte(int ordinal) {
    return getFirstTypedValue (Byte.class, ordinal, FudgeTypeDictionary.BYTE_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String getString(String fieldName) {
    return getFirstTypedValue(String.class, fieldName, FudgeTypeDictionary.STRING_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getString(int ordinal) {
    return getFirstTypedValue(String.class, ordinal, FudgeTypeDictionary.STRING_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean getBoolean(String fieldName) {
    return getFirstTypedValue(Boolean.class, fieldName, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean getBoolean(int ordinal) {
    return getFirstTypedValue(Boolean.class, ordinal, FudgeTypeDictionary.BOOLEAN_TYPE_ID);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldContainer getMessage(int ordinal) {
    return getFirstTypedValue(FudgeFieldContainer.class, ordinal, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldContainer getMessage(String name) {
    return getFirstTypedValue(FudgeFieldContainer.class, name, FudgeTypeDictionary.FUDGE_MSG_TYPE_ID);
  }

  /**
   * Returns the first field value with the given field name and requested type identifier if one exists, otherwise
   * invokes the secondary type converter in the dictionary on the first viable field found.
   * 
   * @param <T> class to convert to
   * @param clazz Java class to convert to
   * @param fieldName the field name
   * @param typeId the type identifier
   * @return the field value or {@code null} if no matching field was found
   */
  @SuppressWarnings("unchecked")
  protected <T> T getFirstTypedValue(Class<T> clazz, String fieldName, int typeId) {
    FudgeField secondBest = null;
    for(FudgeField field : _fields) {
      if (fieldNameEquals (fieldName, field)) {
        if (field.getType().getTypeId() == typeId) {
          return (T)field.getValue();
        } else {
          if (secondBest == null) {
            if (getFudgeContext ().getTypeDictionary ().canConvertField (clazz, field)) {
              secondBest = field;
            }
          }
        }
      }
    }
    return (secondBest != null) ? getFudgeContext ().getTypeDictionary ().getFieldValue (clazz, secondBest) : null;
  }
  
  /**
   * Returns the first field value with the given field name and requested type identifier if one exists, otherwise
   * invokes the secondary type converter in the dictionary on the first viable field found.
   * 
   * @param <T> class to convert to
   * @param clazz Java class to convert to
   * @param ordinal the ordinal index
   * @param typeId the type identifier
   * @return the field value or {@code null} if no matching field was found
   */
  @SuppressWarnings("unchecked")
  protected <T> T getFirstTypedValue(Class<T> clazz, int ordinal, int typeId) {
    FudgeField secondBest = null;
    Short ordinalAsShort = (short)ordinal;
    for(FudgeField field : _fields) {
      if (fieldOrdinalEquals (ordinalAsShort, field)) {
        if (field.getType().getTypeId() == typeId) {
          return (T)field.getValue();
        } else {
          if (secondBest == null) {
            if (getFudgeContext ().getTypeDictionary ().canConvertField (clazz, field)) {
              secondBest = field;
            }
          }
        }
      }
    }
    return (secondBest != null) ? getFudgeContext ().getTypeDictionary ().getFieldValue (clazz, secondBest) : null;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getValue (final Class<T> clazz, final String name) {
    final FudgeTypeDictionary dictionary = getFudgeContext ().getTypeDictionary ();
    for (FudgeField field : _fields) {
      if (fieldNameEquals (name, field) && dictionary.canConvertField (clazz, field)) {
        return dictionary.getFieldValue (clazz, field);
      }
    }
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getValue (final Class<T> clazz, final int ordinal) {
    final FudgeTypeDictionary dictionary = getFudgeContext ().getTypeDictionary ();
    final Short ordinalAsShort = (short)ordinal;
    for (FudgeField field : _fields) {
      if (fieldOrdinalEquals (ordinalAsShort, field) && dictionary.canConvertField (clazz, field)) {
        return dictionary.getFieldValue (clazz, field);
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
      FudgeField field = _fields.get(i);
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
    return Collections.unmodifiableList (_fields).iterator ();
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
   * Tests equality of the fields contained within this message. The other message must have the same number of 
   * fields and all fields must be equal according to their {@code .equals} methods.  
   */
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FudgeMsgBase)) return false;
    final FudgeMsgBase fm = (FudgeMsgBase)o;
    Iterator<FudgeField> me = iterator ();
    Iterator<FudgeField> other = fm.iterator ();
    while (me.hasNext () && other.hasNext ()) {
      if (!me.next ().equals (other.next ())) return false;
    }
    return me.hasNext () == other.hasNext ();
  }
  
}
