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

/**
 * Converts any object values to the indicator type.
 * 
 * @author Andrew
 */
public class IndicatorFieldTypeConverter implements FudgeTypeConverter<Object,IndicatorType> {
  
  public static IndicatorFieldTypeConverter INSTANCE = new IndicatorFieldTypeConverter ();
  
  private IndicatorFieldTypeConverter () {
  }

  /**
   * Always returns {@code true}. Any object is converted to the singleton instance.
   */
  @Override
  public boolean canConvertPrimary(Class<? extends Object> clazz) {
    return true;
  }

  /**
   * Always returns the singleton instance.
   */
  @Override
  public IndicatorType primaryToSecondary(Object object) {
    return IndicatorType.INSTANCE;
  }
  
}