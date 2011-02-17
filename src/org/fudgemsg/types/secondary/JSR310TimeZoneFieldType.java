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
package org.fudgemsg.types.secondary;

import javax.time.calendar.TimeZone;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * Secondary type for JSR310 {@link TimeZone} conversion to/from a {@link String} transport object. 
 */
public class JSR310TimeZoneFieldType extends SecondaryFieldType<TimeZone,String> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310TimeZoneFieldType INSTANCE = new JSR310TimeZoneFieldType ();
  
  private JSR310TimeZoneFieldType () {
    super (StringFieldType.INSTANCE, TimeZone.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String secondaryToPrimary (TimeZone object) {
    return object.getID();
  }
    
  /**
   * Primary to secondary conversion, where the primary type is a {@link String}.
   * 
   * @param object primary object
   * @return the converted {@link TimeZone} object
   */
  public TimeZone primaryToSecondary (String object) {
    return TimeZone.of(object); 
  }
  
  
}