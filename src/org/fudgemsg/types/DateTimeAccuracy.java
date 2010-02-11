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
 * Granularity options for {@link DateTimeFieldType} and {@link TimeFieldType}.
 * 
 * @author Andrew
 */
public enum DateTimeAccuracy {
  
  /**
   * Century precision.
   */
  CENTURY (9),
  /**
   * Year precision.
   */
  YEAR (8),
  /**
   * Month precision. 
   */
  MONTH (7),
  /**
   * Day precision. 
   */
  DAY (6),
  /**
   * Hour precision. 
   */
  HOUR (5),
  /**
   * Minute precision.
   */
  MINUTE (4),
  /**
   * Second precision.
   */
  SECOND (3),
  /**
   * Millisecond precision.
   */
  MILLISECOND (2),
  /**
   * Microsecond precision.
   */
  MICROSECOND (1),
  /**
   * Nanosecond precision.
   */
  NANOSECOND (0);
  
  private final int _encodedValue;
  
  private DateTimeAccuracy (final int encodedValue) {
    _encodedValue = encodedValue;
  }
  
  /**
   * The numeric value to be encoded in Fudge time and datetime representations. See {@link "http://wiki.fudgemsg.org/display/FDG/DateTime+encoding"}.
   * 
   * @return the numeric value
   */
  /* package */ int getEncodedValue () {
    return _encodedValue;
  }
  
  /**
   * Resolves the symbolic enum value from an encoded value (e.g. one returned by {@link #getEncodedValue} or received in a Fudge message).
   * 
   * @param n numeric value
   * @return the {@link DateTimeAccuracy} or {@code null} if the value is invalid
   */
  /* package */ static DateTimeAccuracy fromEncodedValue (int n) {
    switch (n) {
    case 0 : return NANOSECOND;
    case 1 : return MICROSECOND;
    case 2 : return MILLISECOND;
    case 3 : return SECOND;
    case 4 : return MINUTE;
    case 5 : return HOUR;
    case 6 : return DAY;
    case 7 : return MONTH;
    case 8 : return YEAR;
    case 9 : return CENTURY;
    default : return null;
    }
  }
  
}