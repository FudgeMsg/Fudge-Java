/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg.types;

/**
 * Converts any object values to a string using the object's toString method.
 * 
 * @author Andrew Griffin
 */
public class StringFieldTypeConverter implements FudgeTypeConverter<Object, String> {

  /**
   * Singleton instance of the converter.
   */
  public static StringFieldTypeConverter INSTANCE = new StringFieldTypeConverter();

  private StringFieldTypeConverter() {
  }

  /**
   * Always returns {@code true}. Any object has a {@code toString} method.
   */
  @Override
  public boolean canConvertPrimary(Class<? extends Object> clazz) {
    return true;
  }

  /**
   * Returns {@code toString} called on the object.
   */
  @Override
  public String primaryToSecondary(Object object) {
    return object.toString();
  }

}
