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
 * The only value of a field with the Indicator type. This value is used
 * to indicate that there is a field in a file with the given name/index/ordinal,
 * but that the type does not matter.
 * It is intentionally a pure singleton.
 *
 * @author kirk
 */
public final class IndicatorType {
  private IndicatorType() {
  }
  
  /**
   * The only instance of this type.
   */
  public static final IndicatorType INSTANCE = new IndicatorType();

}
