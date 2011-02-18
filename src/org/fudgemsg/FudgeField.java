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

/**
 * A single field in the Fudge system.
 * <p>
 * Each Fudge message consists of a list of fields.
 * Each field consists of a Fudge type and value, with an optional name and ordinal.
 * All four combinations of name and ordinal are possible - from both present to both absent.
 * <p>
 * The type of the value should match the stored Fudge type:
 * {@code getType().getJavaType().isAssignableFrom(getValue().getClass())} should be true.
 * <p>
 * Applications are recommended to use this interface rather than a concrete class.
 * <p>
 * This interface makes no guarantees about the mutability or thread-safety of implementations.
 */
public interface FudgeField {

  /**
   * Gets the Fudge type of the value.
   * <p>
   * The type should match the value.
   * 
   * @return the Fudge field type, not null
   */
  FudgeFieldType<?> getType();

  /**
   * Gets the field value.
   * <p>
   * The value is the payload of the field.
   * 
   * @return the field value, may be null
   */
  Object getValue();

  /**
   * Gets the optional field ordinal.
   * <p>
   * The ordinal is a number that identifies the meaning of the data.
   * It is typically a reference into the taxonomy.
   * 
   * @return the field ordinal, null if the field has no ordinal
   */
  Short getOrdinal();

  /**
   * Gets the optional field name.
   * <p>
   * The name is a string that identifies the meaning of the data.
   * This is similar to the tag name in XML.
   * 
   * @return the field name, null if the field has no name
   */
  String getName();

}
