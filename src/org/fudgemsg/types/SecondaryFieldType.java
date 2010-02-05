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
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.FudgeFieldType;

/**
 * The type definition for a secondary field type that converts Java objects
 * to a more fundamental Fudge type. This approach is more lightweight than
 * the tools available in the mapping package, but also limited as there is
 * no access to the {@link FudgeContext} when the conversion takes place.
 *
 * @author Andrew
 */
public abstract class SecondaryFieldType<SecondaryType,PrimitiveType> extends FudgeFieldType<SecondaryType> {
  
  private final FudgeFieldType<PrimitiveType> _delegate;

  /**
   * Creates a new secondary type on top of an existing Fudge type.
   * 
   * @param type existing Fudge primitive type
   * @param javaType Java type for conversion
   */
  protected SecondaryFieldType (FudgeFieldType<PrimitiveType> type, Class<SecondaryType> javaType) {
    super (type.getTypeId (), javaType, type.isVariableSize (), type.getFixedSize ());
    _delegate = type;
  }
  
  public FudgeFieldType<PrimitiveType> getPrimaryType () {
    return _delegate;
  }
  
  /**
   * Converts an object from the secondary type to a primitive Fudge type for writing. An implementation
   * may assume that the {@code object} parameter is not {@code null}.
   * 
   * @param object the secondary instance
   * @return the underlying Fudge data to write out
   */
  public abstract PrimitiveType secondaryToPrimary (SecondaryType object);
  
  /**
   * Converts Fudge primitive data to the secondary type. This is an optional operation - it will only be
   * invoked if the user attempts to convert from the underlying type used for transport back to the 
   * secondary type. An implementation may assume that the {@code object} parameter is not {@code null}.
   * 
   * @param object the Fudge data
   * @return a secondary type instance
   */
  public SecondaryType primaryToSecondary (PrimitiveType object) {
    throw new UnsupportedOperationException ("cannot convert from " + getTypeId () + " to " + getJavaType ()); 
  }

  /**
   * {@docInherit}
   */
  @Override
  public void writeValue(DataOutput output, SecondaryType value) throws IOException {
    getPrimaryType ().writeValue (output, secondaryToPrimary (value));
  }
  
  /**
   * {@docInherit}
   */
  @Override
  public SecondaryType readValue (DataInput input, int dataSize) throws IOException {
    return primaryToSecondary (getPrimaryType ().readValue (input, dataSize));
  }
  
}