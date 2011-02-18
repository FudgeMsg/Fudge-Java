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

import java.io.Serializable;

/**
 * A single immutable field in the Fudge system.
 * <p>
 * This is the standard immutable implementation of {@link FudgeField}.
 * <p>
 * This class is immutable and thread-safe but is not final.
 */
public class FudgeMsgField implements FudgeField, Serializable {
  // TODO make final, rename to ImmutableFudgeField

  /**
   * The Fudge field type.
   */
  private final FudgeFieldType<?> _type;
  /**
   * The value.
   */
  private final Object _value;
  /**
   * The optional field name.
   */
  private final String _name;
  /**
   * The optional field ordinal.
   */
  private final Short _ordinal;

  /**
   * Obtains an immutable version of the specified field.
   * <p>
   * If the field is an instance of this class, it is returned, otherwise a new
   * instance is created.
   * 
   * @param field  the field to obtain data from, not null
   * @return the equivalent immutable field, not null
   */
  public static FudgeMsgField of(FudgeField field) {
    if (field instanceof FudgeMsgField) {
      return (FudgeMsgField) field;
    }
    return FudgeMsgField.of(field.getType(), field.getValue(), field.getName(), field.getOrdinal());
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @return the created immutable field, not null
   */
  public static FudgeMsgField of(FudgeFieldType<?> type, Object value) {
    return new FudgeMsgField(type, value, null, null);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @return the created immutable field, not null
   */
  public static FudgeMsgField of(FudgeFieldType<?> type, Object value, String name) {
    return new FudgeMsgField(type, value, name, null);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param ordinal  the optional field ordinal, null if no ordinal
   * @return the created immutable field, not null
   */
  public static FudgeMsgField of(FudgeFieldType<?> type, Object value, Short ordinal) {
    return new FudgeMsgField(type, value, null, ordinal);
  }

  /**
   * Obtains a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @param ordinal  the optional field ordinal, null if no ordinal
   * @return the created immutable field, not null
   */
  public static FudgeMsgField of(FudgeFieldType<?> type, Object value, String name, Short ordinal) {
    return new FudgeMsgField(type, value, name, ordinal);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a field from the type, value, name and ordinal.
   * 
   * @param type  the Fudge field type, not null
   * @param value  the payload value, may be null
   * @param name  the optional field name, null if no name
   * @param ordinal  the optional field ordinal, null if no ordinal
   */
  public FudgeMsgField(FudgeFieldType<?> type, Object value, String name, Short ordinal) {
    if (type == null) {
      throw new NullPointerException("Type must not be null");
    }
    _type = type;
    _value = value;
    _name = name;
    _ordinal = ordinal;
  }

    /**
   * Constructs a field as a copy of another.
   * 
   * @param field the {@code FudgeMsgField} to copy.
   */
  public FudgeMsgField(FudgeField field) {
    this(field.getType(), field.getValue(), field.getName(), field.getOrdinal());
  }

//-------------------------------------------------------------------------
  /** {@inheritDoc} */
  @Override
  public FudgeFieldType<?> getType() {
    return _type;
  }

  /** {@inheritDoc} */
  @Override
  public Object getValue() {
    return _value;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return _name;
  }

  /** {@inheritDoc} */
  @Override
  public Short getOrdinal() {
    return _ordinal;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this field to another field.
   * <p>
   * This checks the type, value, name and ordinal.
   * 
   * @param obj  the other field, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof FudgeMsgField) {
      FudgeMsgField other = (FudgeMsgField) obj;
      return getType().equals(other.getType()) &&
          equal(getOrdinal(), other.getOrdinal()) &&
          equal(getName(), other.getName()) &&
          equal(getValue(), other.getValue());
    }
    return false;
  }

  private boolean equal(final Object a, final Object b) {
    return a == b || (a != null && a.equals(b));
  }

  /**
   * Gets a string description of the field.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Field[");
    if (_name != null) {
      sb.append(_name);
      if (_ordinal == null) {
        sb.append(":");
      } else {
        sb.append(",");
      }
    }
    if (_ordinal != null) {
      sb.append(_ordinal).append(":");
    }

    sb.append(_type);
    sb.append("-").append(_value);
    sb.append("]");
    return sb.toString();
  }

}
