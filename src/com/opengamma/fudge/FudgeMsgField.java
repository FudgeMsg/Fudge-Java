/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.Serializable;

/**
 * A concrete implementation of {@link FudgeField} suitable for inclusion in
 * a pre-constructed {@link FudgeMsg} or a stream of data.
 *
 * @author kirk
 */
public class FudgeMsgField implements FudgeField, Serializable, Cloneable {
  @SuppressWarnings("unchecked")
  private final FudgeFieldType _type;
  private final Object _value;
  private final String _name;
  private final Short _ordinal;
  private volatile int _size = -1;
  
  public FudgeMsgField(FudgeFieldType<?> type, Object value, String name, Short ordinal) {
    if(type == null) {
      throw new NullPointerException("Must specify a type for this field.");
    }
    _type = type;
    _value = value;
    _name = name;
    _ordinal = ordinal;
  }
  
  public FudgeMsgField(FudgeField field) {
    this(field.getType(), field.getValue(), field.getName(), field.getOrdinal());
  }

  @Override
  public FudgeFieldType<?> getType() {
    return _type;
  }

  @Override
  public Object getValue() {
    return _value;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public Short getOrdinal() {
    return _ordinal;
  }

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
  
  public int getSize() {
    if(_size == -1) {
      _size = computeSize();
    }
    return _size;
  }
  
  @SuppressWarnings("unchecked")
  protected int computeSize() {
    int size = 0;
    // Field prefix
    size += 2;
    if(_ordinal != null) {
      size += 2;
    }
    if(_name != null) {
      // One for the size prefix
      size++;
      // Then for the UTF Encoding
      size += ModifiedUTF8Util.modifiedUTF8Length(_name);
    }
    if(_type.isVariableSize()) {
      int valueSize = _type.getVariableSize(_value);
      if(valueSize <= 255) {
        size += valueSize + 1;
      } else if(valueSize <= Short.MAX_VALUE) {
        size += valueSize + 2;
      } else {
        size += valueSize + 4;
      }
    } else {
      size += _type.getFixedSize();
    }
    return size;
  }

}
