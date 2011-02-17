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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.fudgemsg.taxon.FudgeTaxonomy;


/**
 * The class defining the type of a particular field.
 * While Fudge comes with a set of required types which are fully supported
 * in all Fudge-compliant systems, if you have custom data, you can control the encoding
 * using your own instance of {@code FudgeFieldType}, making sure to register the
 * instance with the {@link FudgeTypeDictionary} at application startup.
 *
 * @author Kirk Wylie
 * @param <TValue> underlying Java class this type represents
 */
public class FudgeFieldType<TValue> implements Serializable {
  private final int _typeId;
  private final Class<TValue> _javaType;
  private final boolean _isVariableSize;
  private final int _fixedSize;
  
  /**
   * Constructs a new {@link FudgeFieldType} for the underlying Java type. The type identifier must be unique within the {@link FudgeTypeDictionary} the type
   * is registered with.
   * 
   * @param typeId {@link FudgeTypeDictionary} unique type identifier
   * @param javaType the underlying Java type
   * @param isVariableSize true if the field may contain variable width data
   * @param fixedSize the fixed width if the field does not contain variable width data. Can be set to 0 for types that always encode a variable width.
   */
  public FudgeFieldType(int typeId, Class<TValue> javaType, boolean isVariableSize, int fixedSize)
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
  }

  /**
   * Returns the type identifier.
   * 
   * @return the typeId
   */
  public final int getTypeId() {
    return _typeId;
  }

  /**
   * Returns the underlying Java type for values of this type.
   * 
   * @return the javaType
   */
  public final Class<TValue> getJavaType() {
    return _javaType;
  }

  /**
   * Returns true iff the field may contain variable width data.
   * 
   * @return the isVariableSize
   */
  public final boolean isVariableSize() {
    return _isVariableSize;
  }

  /**
   * Returns the number of bytes used to encode a value if the type does not use a variable width encoding. 
   * 
   * @return the fixedSize
   */
  public final int getFixedSize() {
    return _fixedSize;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public final int hashCode() {
    return getTypeId();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return "FudgeFieldType[" + getTypeId () + "-" + getJavaType () + "]";
  }
  
  /**
   * Returns the number of bytes used to encode a value of this type. If the type does not support a variable width, will always be the same as {@link #getFixedSize()}. This method must be overridden for variable size types.
   * 
   * @param value value to check (for variable width types)
   * @param taxonomy the taxonomy being used for the encoding (e.g. for sub-message fields)
   * @return size in bytes
   */
  public int getVariableSize(TValue value, FudgeTaxonomy taxonomy) {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    return getFixedSize();
  }
  
  /**
   * Writes this field value to the {@link DataOutput} target. This method must be overridden by any custom types. The implementation must write exactly the
   * number of bytes returned by {@link #getVariableSize(Object,FudgeTaxonomy)}.
   * 
   * @param output the target {@code DataOutput} to write to
   * @param value the value of the field to write
   * @throws IOException if the target raises one. This is declared so that each type implementation does not need to detect and wrap the IOExceptions from {@code DataOutput}'s methods. The main stream writer will do that. 
   */
  @SuppressWarnings("unused")
  public void writeValue(DataOutput output, TValue value) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    // REVIEW 2010-01-28 Andrew -- this is a no-op for fixed width types; shouldn't it trigger an exception regardless as it's not going to do anything, or should we make it abstract and implement the primitive types properly
  }
  
  /**
   * Reads a field value of this type from a {@link DataInput} source. This method must be overridden by any custom types. The implementation must read exactly the number
   * of bytes passed as {@code dataSize}.
   * 
   * @param input the source {@code DataInput} to read the value from
   * @param dataSize the number of bytes of data to read
   * @return the value read
   * @throws IOException if data cannot be read because of a source error, such as bad formatting or EOF. This is declared so that each type implementation does not need to detect and wrap the IOExceptions from {@code DataInput}'s methods. The main stream reader will do that.
   */
  @SuppressWarnings("unused")
  public TValue readValue(DataInput input, int dataSize) throws IOException {
    if(isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types.");
    }
    // REVIEW 2010-01-28 Andrew -- this is a no-op for fixed width types; shouldn't it trigger an exception regardless as it's not going to do anything, or should we make it abstract and implement the primitive types properly
    return null;
  }

}
