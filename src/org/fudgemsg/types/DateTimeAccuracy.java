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
package org.fudgemsg.types;

/**
 * Granularity options for {@link DateTimeFieldType} and {@link TimeFieldType}.
 * 
 * @author Andrew Griffin
 */
public enum DateTimeAccuracy {
  
  /**
   * Millenia precision.
   */
  MILLENIUM (0),
  /**
   * Century precision.
   */
  CENTURY (1),
  /**
   * Year precision.
   */
  YEAR (2),
  /**
   * Month precision. 
   */
  MONTH (3),
  /**
   * Day precision. 
   */
  DAY (4),
  /**
   * Hour precision. 
   */
  HOUR (5),
  /**
   * Minute precision.
   */
  MINUTE (6),
  /**
   * Second precision.
   */
  SECOND (7),
  /**
   * Millisecond precision.
   */
  MILLISECOND (8),
  /**
   * Microsecond precision.
   */
  MICROSECOND (9),
  /**
   * Nanosecond precision.
   */
  NANOSECOND (10);
  
  private final int _encodedValue;
  
  private DateTimeAccuracy (final int encodedValue) {
    _encodedValue = encodedValue;
  }
  
  /**
   * The numeric value to be encoded in Fudge time and datetime representations. See <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime encoding</a>
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
    case 10 : return NANOSECOND;
    case 9 : return MICROSECOND;
    case 8 : return MILLISECOND;
    case 7 : return SECOND;
    case 6 : return MINUTE;
    case 5 : return HOUR;
    case 4 : return DAY;
    case 3 : return MONTH;
    case 2 : return YEAR;
    case 1 : return CENTURY;
    case 0 : return MILLENIUM;
    default : return null;
    }
  }
  
  /**
   * Tests if this accuracy is a greater precision than another. E.g. SECOND precision is greater than MINUTE precision.
   * 
   * @param accuracy other accuracy
   * @return {@code true} if greater, {@code false} otherwise
   */
  public boolean greaterThan (final DateTimeAccuracy accuracy) {
    return getEncodedValue () > accuracy.getEncodedValue ();
  }
  
  /**
   * Tests is this accuracy is a lower precision than another. E.g. MINUTE precision is less than SECOND precision.
   * 
   * @param accuracy other accuracy
   * @return {@code true} if lower, {@code false} otherwise.
   */
  public boolean lessThan (final DateTimeAccuracy accuracy) {
    return getEncodedValue () < accuracy.getEncodedValue ();
  }
  
}
