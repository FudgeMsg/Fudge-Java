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

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;

/**
 * The type definition for a time. Java doesn't have a standard class for just a time of
 * day, so we are currently using {@link FudgeTime}. When Java can support a time
 * on its own, that will become the primary mapping and the {@code FudgeTime} type will be
 * supported through the secondary type mechanism.
 *
 * @author Andrew Griffin
 */
public class TimeFieldType extends FudgeFieldType<FudgeTime> {
  
  /**
   * Standard Fudge field type: date. See {@link FudgeTypeDictionary#TIME_TYPE_ID}.
   */
  public static final TimeFieldType INSTANCE = new TimeFieldType();
  
  private static final int MASK_ACCURACY = 0x0F;
  private static final int FLAG_TIMEZONE = 0x10;
  
  private TimeFieldType() {
    super(FudgeTypeDictionary.TIME_TYPE_ID, FudgeTime.class, false, 8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTime readValue(DataInput input, int dataSize) throws IOException {
    long nanos = input.readLong ();
    final int options = (int)(nanos >> 56) & 0xFF;
    final int timezoneOffset = (int)(nanos >> 48) & 0xFF;
    nanos &= 0x0000FFFFFFFFFFFFl;
    if ((options & FLAG_TIMEZONE) != 0) {
      return new FudgeTime (DateTimeAccuracy.fromEncodedValue (options & MASK_ACCURACY), timezoneOffset, nanos);
    } else {
      return new FudgeTime (DateTimeAccuracy.fromEncodedValue (options & MASK_ACCURACY), nanos);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, FudgeTime value) throws IOException {
    int options = 0;
    int timezoneOffset = 0;
    if (value.hasTimezoneOffset ()) {
      options |= FLAG_TIMEZONE;
      timezoneOffset = value.getTimezoneOffset () & 0xFF;
    }
    options |= value.getAccuracy ().getEncodedValue ();
    long l = value.getNanos ();
    l &= 0xFFFFFFFFFFFFl;
    l |= (long)options << 56;
    l |= (long)timezoneOffset << 48;
    output.writeLong (l); 
  }

}
