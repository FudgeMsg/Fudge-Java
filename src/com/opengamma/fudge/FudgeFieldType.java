/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * The class defining the type of a particular field.
 * While Fudge comes with a set of required types which are fully supported
 * in all Fudge-compliant systems, if you have custom data, you can control the encoding
 * using your own instance of {@code FudgeFieldType}, making sure to register the
 * instance with the {@link FudgeTypeDictionary} at application startup.
 *
 * @author kirk
 */
public class FudgeFieldType<TValue> implements Serializable {
  private final int _typeId;
  private final Class<?> _javaType;
  private final boolean _isVariableSize;
  private final int _fixedSize;
  
  private final String _toStringValue;
  
  public FudgeFieldType(int typeId, Class<?> javaType, boolean isVariableSize, int fixedSize)
  {
    if(javaType == null)
    {
      throw new NullPointerException("Must specify a valid Java type for conversion.");
    }
    if(typeId > 255) {
      throw new IllegalArgumentException("The type id must fit in an unsigned byte.");
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
  public final int getTypeId() {
    return _typeId;
  }

  /**
   * @return the javaType
   */
  public final Class<?> getJavaType() {
    return _javaType;
  }

  /**
   * @return the isVariableSize
   */
  public final boolean isVariableSize() {
    return _isVariableSize;
  }

  /**
   * @return the fixedSize
   */
  public final int getFixedSize() {
    return _fixedSize;
  }

  @Override
  public final boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof FudgeFieldType<?>)) {
      return false;
    }
    FudgeFieldType<?> other = (FudgeFieldType<?>) obj;
    if(getTypeId() != other.getTypeId()) {
      return false;
    }
    // Don't bother checking the rest of it.
    return true;
  }

  @Override
  public final int hashCode() {
    return getTypeId();
  }
  
  protected String generateToString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FudgeFieldType[");
    sb.append(getTypeId()).append("-");
    sb.append(getJavaType());
    sb.append("]");
    return sb.toString().intern();
  }

  @Override
  public final String toString() {
    return _toStringValue;
  }
  
  public int getVariableSize(TValue value, FudgeTaxonomy taxonomy) {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    return getFixedSize();
  }
  
  public void writeValue(DataOutput output, TValue value, FudgeTaxonomy taxonomy, short taxonomyId) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
  }
  
  public TValue readValue(DataInput input, int dataSize, FudgeTaxonomy taxonomy) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    return null;
  }

}
