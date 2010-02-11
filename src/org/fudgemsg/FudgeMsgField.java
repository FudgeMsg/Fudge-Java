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

/**
 * A concrete implementation of {@link FudgeField} suitable for inclusion in
 * a pre-constructed {@link FudgeMsg} or a stream of data.
 *
 * @author Kirk Wylie
 */
public class FudgeMsgField implements FudgeField, Serializable, Cloneable {
  @SuppressWarnings("unchecked")
  private final FudgeFieldType _type;
  private final Object _value;
  private final String _name;
  private final Short _ordinal;
  
  /**
   * Creates a new {@link FudgeMsgField}.
   * 
   * @param type the underlying field type
   * @param value the field value
   * @param name the name of the field, or {@code null} to omit
   * @param ordinal the ordinal index of the field, or {@code null} to omit
   */
  public FudgeMsgField(FudgeFieldType<?> type, Object value, String name, Short ordinal) {
    if(type == null) {
      throw new NullPointerException("Must specify a type for this field.");
    }
    _type = type;
    _value = value;
    _name = name;
    _ordinal = ordinal;
  }
  
  /**
   * Creates a new {@link FudgeMsgField} as a copy of another.
   * 
   * @param field the {@code FudgeMsgField} to copy.
   */
  public FudgeMsgField(FudgeField field) {
    this(field.getType(), field.getValue(), field.getName(), field.getOrdinal());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeFieldType<?> getType() {
    return _type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getValue() {
    return _value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Short getOrdinal() {
    return _ordinal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeMsgField clone() {
    Object cloned;
    try {
      cloned = super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("This can't happen.");
    }
    return (FudgeMsgField) cloned;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Field[");
    if(_name != null) {
      sb.append(_name);
      if(_ordinal == null) {
        sb.append(":");
      } else {
        sb.append(",");
      }
    }
    if(_ordinal != null) {
      sb.append(_ordinal).append(":");
    }
      
    sb.append(_type);
    sb.append("-").append(_value);
    sb.append("]");
    return sb.toString();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof FudgeMsgField)) return false;
    FudgeMsgField fmf = (FudgeMsgField)o;
    if (!getType ().equals (fmf.getType ())
      || refsDifferent (getOrdinal (), fmf.getOrdinal ())
      || refsDifferent (getName (), fmf.getName ())
      || refsDifferent (getValue (), fmf.getValue ())) return false;
    return true;
  }
  
  private <T> boolean refsDifferent (final T a, final T b) {
    if (a == b) return false;
    if ((a == null) || (b == null)) return true;
    return !a.equals (b);
  }
  
}
