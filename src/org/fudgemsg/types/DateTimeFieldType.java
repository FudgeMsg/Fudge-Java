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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * <p>The type definition for a date. This is currently backed by a {@link FudgeDateTime}. The secondary
 * type mechanism is used to support additional Java representations, such as {@link Date}, {@link Calendar}
 * and {@code javax.time} classes.</p>
 *
 * @author Andrew Griffin
 */
public class DateTimeFieldType extends FudgeFieldType<FudgeDateTime> {

  /**
   * Standard Fudge field type: combined date and time. See {@link FudgeTypeDictionary#DATETIME_TYPE_ID}.
   */
  public static final DateTimeFieldType INSTANCE = new DateTimeFieldType();
  
  private DateTimeFieldType() {
    super(FudgeTypeDictionary.DATETIME_TYPE_ID, FudgeDateTime.class, false, 12);
  }

  /**
   * Reads a Fudge date representation from an input source.
   * 
   * @param input input source
   * @return the date
   * @throws IOException if there is an error from the input source
   */
  /* package */ static FudgeDate readFudgeDate (final DataInput input) throws IOException {
    final int n = input.readInt ();
    final int dayOfMonth = (n & 31);
    final int monthOfYear = (n >> 5) & 15;
    final int year = n >> 9; // will sign-extend
    //System.out.println ("readFudgeDate: " + n + ", " + year + ", " + monthOfYear + ", " + dayOfMonth);
    return new FudgeDate (year, monthOfYear, dayOfMonth);
  }
  
  /**
   * Reads a Fudge time representation from an input source.
   * 
   * @param input input source
   * @return the time
   * @throws IOException if there is an error from the input source
   */
  /* package */ static FudgeTime readFudgeTime (final DataInput input) throws IOException {
    final int hi = input.readInt ();
    final int lo = input.readInt ();
    final int timezoneOffset = (hi >> 24); // sign extend
    final int accuracy = (hi >> 20) & 15;
    final int seconds = hi & 0x1FFFF;
    final int nanos = lo & 0x3FFFFFFF;
    //System.out.println ("readFudgeTime: " + hi + ", " + lo + ", " + timezoneOffset + ", " + accuracy + ", " + seconds + ", " + nanos);
    return new FudgeTime (DateTimeAccuracy.fromEncodedValue (accuracy), timezoneOffset, seconds, nanos);
  }
  
  /**
   * Writes a Fudge date representation to an output target.
   * 
   * @param output output target
   * @param value Fudge date
   * @throws IOException if there is an error from the output target
   */
  /* package */ static void writeFudgeDate (final DataOutput output, final FudgeDate value) throws IOException {
    final int dayOfMonth = value.getDayOfMonth ();
    final int monthOfYear = value.getMonthOfYear ();
    final int year = value.getYear ();
    final int n = (year << 9) | ((monthOfYear & 15) << 5) | (dayOfMonth & 31);
    //System.out.println ("writeFudgeDate: " + n + ", " + year + ", " + monthOfYear + ", " + dayOfMonth);
    output.writeInt (n);
  }
  
  /**
   * Writes a Fudge time representation to an output target.
   * 
   * @param output the output target
   * @param value the Fudge time
   * @throws IOException if there is an error from the output target
   */
  /* package */ static void writeFudgeTime (final DataOutput output, final FudgeTime value) throws IOException {
    final int hi = (value.getSecondsSinceMidnight () & 0x1FFFF) | (value.getAccuracy ().getEncodedValue () << 20) | (value.getRawTimezoneOffset () << 24);
    final int lo = value.getNanos () & 0x3FFFFFFF;
    //System.out.println ("writeFudgeTime: " + hi + ", " + lo);
    output.writeInt (hi);
    output.writeInt (lo);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDateTime readValue(DataInput input, int dataSize) throws IOException {
    final FudgeDate date = readFudgeDate (input);
    final FudgeTime time = readFudgeTime (input);
    return new FudgeDateTime (date, time);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, FudgeDateTime datetime) throws IOException {
    writeFudgeDate (output, datetime.getDate ());
    writeFudgeTime (output, datetime.getTime ());
  }

}