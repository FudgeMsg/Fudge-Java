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

/**
 * A read-only representation of a field which is contained in a fudge
 * message, or a stream of fudge encoded data.
 *
 * @author kirk
 */
public interface FudgeField {
  
  /**
   * Returns the {@link FudgeFieldType} of the field data.
   * 
   * @return the {@code FudgeFieldType}
   */
  FudgeFieldType<?> getType();
  
  /**
   * Returns the value of the field.
   * 
   * @return the field value
   */
  Object getValue();
  
  /**
   * Returns the ordinal index of the field, or {@code null} if none is specified.
   * 
   * @return the field ordinal, or {@code null} if none
   */
  Short getOrdinal();
  
  /**
   * Returns the name of the field, or {@code null} if none is specified.
   * 
   * @return the field name, or {@code null} if none 
   */
  String getName();

}
