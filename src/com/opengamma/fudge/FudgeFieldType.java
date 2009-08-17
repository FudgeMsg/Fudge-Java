/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.Serializable;

/**
 * The class defining the type of a particular field.
 *
 * @author kirk
 */
public final class FudgeFieldType implements Serializable {
  private final byte _typeId;
  private final Class<?> _javaType;
  private final boolean _isVariableSize;
  private final int _fixedSize;
  
  private final String _toStringValue;
  
  public FudgeFieldType(byte typeId, Class<?> javaType, boolean isVariableSize, int fixedSize)
  {
    if(javaType == null)
    {
      throw new NullPointerException("Must specify a valid Java type for conversion.");
    }
    _typeId = typeId;
    _javaType = javaType;
    _isVariableSize = isVariableSize;
    _fixedSize = fixedSize;
    
    _toStringValue = generateToString();
  }

  /**
   * @return the typeId
   */
  public byte getTypeId() {
    return _typeId;
  }

  /**
   * @return the javaType
   */
  public Class<?> getJavaType() {
    return _javaType;
  }

  /**
   * @return the isVariableSize
   */
  public boolean isVariableSize() {
    return _isVariableSize;
  }

  /**
   * @return the fixedSize
   */
  public int getFixedSize() {
    return _fixedSize;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof FudgeFieldType)) {
      return false;
    }
    FudgeFieldType other = (FudgeFieldType) obj;
    if(getTypeId() != other.getTypeId()) {
      return false;
    }
    // Don't bother checking the rest of it.
    return true;
  }

  @Override
  public int hashCode() {
    return getTypeId();
  }
  
  protected final String generateToString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FudgeFieldType[");
    sb.append(getTypeId()).append("-");
    sb.append(getJavaType());
    sb.append("]");
    return sb.toString().intern();
  }

  @Override
  public String toString() {
    return _toStringValue;
  }

}
