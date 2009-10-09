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
  
  public void writeValue(DataOutput output, TValue value, FudgeTaxonomy taxonomy) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
  }
  
  public TValue readValue(DataInput input, int dataSize, FudgeTypeDictionary typeDictionary) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    return null;
  }

}
