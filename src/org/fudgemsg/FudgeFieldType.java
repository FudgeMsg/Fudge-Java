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
 * The type of a field as defined by Fudge.
 * <p>
 * In order to efficiently send messages, Fudge needs to know the type of each piece of data.
 * A standard set of types is supported by all Fudge-compliant systems.
 * This set may be extended with custom types within a closed Fudge implementation.
 * Custom types must be registered with {@link FudgeTypeDictionary}.
 * <p>
 * This class is not final but is thread-safe in isolation.
 * Subclasses must be immutable and thread-safe.
 *
 * @param <T> underlying Java class this type represents
 */
public class FudgeFieldType<T> implements Serializable {

  /**
   * The Fudge type id, from the specification.
   */
  private final int _typeId;
  /**
   * The Java equivalent type.
   */
  private final Class<T> _javaType;
  /**
   * Whether the type is sent as a variable size in the protocol.
   */
  private final boolean _isVariableSize;
  /**
   * The size of the type in bytes when the size is fixed.
   */
  private final int _fixedSize;

  /**
   * Constructs a new type based on the underlying Java type.
   * <p>
   * The Fudge type identifier must be unique within the {@link FudgeTypeDictionary}.
   * 
   * @param typeId  the type dictionary unique type identifier, from 0 to 255
   * @param javaType  the underlying Java type, not null
   * @param isVariableSize  true if the field may contain variable width data
   * @param fixedSize  the size in bytes if fixed size, zero for variable width
   */
  public FudgeFieldType(int typeId, Class<T> javaType, boolean isVariableSize, int fixedSize) {
    if (javaType == null) {
      throw new NullPointerException("Must specify a valid Java type for conversion.");
    }
    if (typeId < 0 || typeId > 255) {
      throw new IllegalArgumentException("The type id must fit in an unsigned byte.");
    }
    _typeId = typeId;
    _javaType = javaType;
    _isVariableSize = isVariableSize;
    _fixedSize = fixedSize;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge type identifier.
   * 
   * @return the type identifier, from 0 to 255
   */
  public final int getTypeId() {
    return _typeId;
  }

  /**
   * Gets the underlying Java type for values of this type.
   * 
   * @return the equivalent Java type, not null
   */
  public final Class<T> getJavaType() {
    return _javaType;
  }

  /**
   * Checks if the field may contain variable width data.
   * 
   * @return true if variable width, false for fixed width
   */
  public final boolean isVariableSize() {
    return _isVariableSize;
  }

  /**
   * Gets the number of bytes used to encode a value if the type is fixed width.
   * 
   * @return the fixed width size in bytes, zero if variable width
   */
  public final int getFixedSize() {
    return _fixedSize;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of bytes used to encode a value.
   * <p>
   * A variable width type must override this method.
   * A fixed width type will return the {@link #getFixedSize() fixed size}.
   * 
   * @param value  the value to check, not used for fixed width types
   * @param taxonomy  the taxonomy being used for the encoding, not used for fixed width types
   * @return the size in bytes
   */
  public int getVariableSize(T value, FudgeTaxonomy taxonomy) {
    if (isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types");
    }
    return getFixedSize();
  }

  //-------------------------------------------------------------------------
  /**
   * Writes a value of this type to the output.
   * <p>
   * This is intended for use by variable width types and must write the given value.
   * The implementation must write exactly the number of bytes returned by the
   * {@link #getVariableSize(Object,FudgeTaxonomy) size calculation}.
   * 
   * @param output  the output target to write the value to, not null
   * @param value  the value to write
   * @throws IOException if an error occurs, which must be wrapped by the caller
   */
  @SuppressWarnings("unused")
  public void writeValue(DataOutput output, T value) throws IOException {
    if (isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types");
    }
    // REVIEW 2010-01-28 Andrew -- this is a no-op for fixed width types; shouldn't it trigger an exception regardless as it's not going to do anything, or should we make it abstract and implement the primitive types properly
  }

  /**
   * Reads a value of this type to the output.
   * <p>
   * This is intended for use by variable width types and must read the given value.
   * The implementation must read exactly the number of bytes passed into the method.
   * 
   * @param input  the input source to read the value from, not null
   * @param dataSize  the number of bytes of data to read
   * @return the value that was read
   * @throws IOException if an error occurs, which must be wrapped by the caller
   */
  @SuppressWarnings("unused")
  public T readValue(DataInput input, int dataSize) throws IOException {
    if (isVariableSize()) {
      throw new UnsupportedOperationException("This method must be overridden for variable size types");
    }
    // REVIEW 2010-01-28 Andrew -- this is a no-op for fixed width types; shouldn't it trigger an exception regardless as it's not going to do anything, or should we make it abstract and implement the primitive types properly
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this type equals another.
   * <p>
   * For performance, this only checks the type identifier.
   * 
   * @param obj  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof FudgeFieldType<?>) {
      FudgeFieldType<?> other = (FudgeFieldType<?>) obj;
      return getTypeId() == other.getTypeId();  // assume system is correctly setup and type is unique
    }
    return false;
  }

  /**
   * Gets a suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public final int hashCode() {
    return getTypeId();
  }

  /**
   * Returns a description of the type.
   * 
   * @return the descriptive string, not null
   */
  @Override
  public final String toString() {
    return "FudgeFieldType[" + getTypeId() + "-" + getJavaType() + "]";
  }

}
