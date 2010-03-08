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
import java.util.Calendar;
import java.util.Date;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * The type definition for a composite date and time (a {@link Calendar} object). A {@link Date}
 * is defined as a secondary type.
 *
 * @author Andrew Griffin
 */
public class DateTimeFieldType extends FudgeFieldType<Calendar> {

  /**
   * Standard Fudge field type: combined date and time. See {@link FudgeTypeDictionary#DATETIME_TYPE_ID}.
   */
  public static final DateTimeFieldType INSTANCE = new DateTimeFieldType();
  
  /**
   * 
   */
  protected static final int MASK_ACCURACY = 0x0F;
  /**
   * 
   */
  protected static final int FLAG_HASTIMEZONE = 0x10;
  
  private DateTimeFieldType() {
    super(FudgeTypeDictionary.DATETIME_TYPE_ID, Calendar.class, false, 12);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Calendar readValue(DataInput input, int dataSize) throws IOException {
    final long n = input.readLong ();
    // byte 0 is the options
    final int options = (int)(n >> 56) & 0xFF;
    // byte 1 is the TZ offset, signed 8-bit integer
    final int timezoneOffset = (int)(byte)((int)(n >> 48) & 0xFF);
    // bytes 2-7 are millis since epoch, signed 48-bit integer
    long seconds = n & 0x0000FFFFFFFFFFFFl;
    if ((seconds & 0x0000800000000000l) != 0) seconds |= 0xFFFF000000000000l;
    // bytes 8-11 are nanos, unsigned 32-bit integer
    final long nanos = (long)input.readInt () & 0xFFFFFFFF;
    // convert to a Date (ignoring nanoseconds)
    final Calendar calendar = Calendar.getInstance ();
    calendar.clear ();
    if ((options & FLAG_HASTIMEZONE) != 0) {
      // TZ offset is in 15 minute chunks
      calendar.set (Calendar.ZONE_OFFSET, timezoneOffset * 15 * 60 * 1000);
    }
    calendar.setTimeInMillis (seconds * 1000l + (nanos / 1000000l));
    // Clear out the fields that aren't part of the requested precision
    final int accuracy = options & MASK_ACCURACY;
    if (accuracy > DateTimeAccuracy.MILLISECOND.getEncodedValue ()) calendar.clear (Calendar.MILLISECOND);
    if (accuracy > DateTimeAccuracy.SECOND.getEncodedValue ()) calendar.clear (Calendar.SECOND);
    if (accuracy > DateTimeAccuracy.MINUTE.getEncodedValue ()) calendar.clear (Calendar.MINUTE);
    if (accuracy > DateTimeAccuracy.HOUR.getEncodedValue ()) calendar.clear (Calendar.HOUR_OF_DAY);
    if (accuracy > DateTimeAccuracy.DAY.getEncodedValue ()) calendar.clear (Calendar.DAY_OF_MONTH);
    if (accuracy > DateTimeAccuracy.MONTH.getEncodedValue ()) calendar.clear (Calendar.MONTH);
    if (accuracy > DateTimeAccuracy.YEAR.getEncodedValue ()) {
      // lose the year bit and just leave the century
      int year = calendar.get (Calendar.YEAR);
      year = year - (year % 100);
      calendar.set (Calendar.YEAR, year);
    }
    return calendar;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, Calendar calendar) throws IOException {
    long n = calendar.getTimeInMillis ();
    int nanos = 0;
    int options = 0;
    nanos = (int)(n % 1000) * 1000000;
    n = (n / 1000l) & 0x0000FFFFFFFFFFFFl;
    if (calendar.isSet (Calendar.ZONE_OFFSET) || calendar.isSet (Calendar.DST_OFFSET)) {
      options |= FLAG_HASTIMEZONE;
      n |= (long)(((calendar.get (Calendar.ZONE_OFFSET) + calendar.get (Calendar.DST_OFFSET)) / (15 * 60 * 1000)) % 0xFF) << 48;
    }
    final DateTimeAccuracy accuracy;
    if (calendar.isSet (Calendar.MILLISECOND)) {
      accuracy = DateTimeAccuracy.MILLISECOND;
    } else if (calendar.isSet (Calendar.SECOND)) {
      accuracy = DateTimeAccuracy.SECOND;
    } else if (calendar.isSet (Calendar.MINUTE)) {
      accuracy = DateTimeAccuracy.MINUTE;
    } else if (calendar.isSet (Calendar.HOUR_OF_DAY)) {
      accuracy = DateTimeAccuracy.HOUR;
    } else if (calendar.isSet (Calendar.DAY_OF_MONTH)) {
      accuracy = DateTimeAccuracy.DAY;
    } else if (calendar.isSet (Calendar.MONTH)) {
      accuracy = DateTimeAccuracy.MONTH;
    } else if (calendar.isSet (Calendar.YEAR)) {
      accuracy = DateTimeAccuracy.YEAR;
    } else {
      accuracy = DateTimeAccuracy.CENTURY;
    }
    options |= accuracy.getEncodedValue ();
    n |= (long)options << 56;
    output.writeLong (n);
    output.writeInt (nanos);
  }

}