/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.Serializable;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * A concrete implementation of {@link FudgeField} suitable for inclusion in
 * a pre-constructed {@link FudgeMsg} or a stream of data.
 *
 * @author kirk
 */
public class FudgeMsgField extends FudgeEncodingObject implements FudgeField, Serializable, Cloneable {
  @SuppressWarnings("unchecked")
  private final FudgeFieldType _type;
  private final Object _value;
  private final String _name;
  private final Short _ordinal;
  
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
  
  @SuppressWarnings("unchecked")
  @Override
  public int computeSize(FudgeTaxonomy taxonomy) {
    int size = 0;
    // Field prefix
    size += 2;
    boolean hasOrdinal = _ordinal != null;
    boolean hasName = _name != null;
    if((_name != null) && (taxonomy != null)) {
      if(taxonomy.getFieldOrdinal(_name) != null) {
        hasOrdinal = true;
        hasName = false;
      }
    }
    if(hasOrdinal) {
      size += 2;
    }
    if(hasName) {
      // One for the size prefix
      size++;
      // Then for the UTF Encoding
      size += ModifiedUTF8Util.modifiedUTF8Length(_name);
    }
    if(_type.isVariableSize()) {
      int valueSize = _type.getVariableSize(_value, taxonomy);
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
