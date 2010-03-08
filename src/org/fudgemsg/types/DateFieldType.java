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
 * <p>The type definition for a date. Java doesn't have a standard class for representing a date
 * without a time, so we are currently using {@link FudgeDate}. When Java can support a date
 * on its own, that will become the primary mapping and the {@code FudgeDate} type will be
 * supported through the secondary type mechanism.</p>
 * 
 * <p>This part of the specification is not finalized and should not be used. The {@code DateTime}
 * Fudge type should be used instead. For more details, please refer to <a href="http://wiki.fudgemsg.org/display/FDG/DateTime+encoding">DateTime Encoding</a>.</p>
 *
 * @author Andrew Griffin
 */
public class DateFieldType extends FudgeFieldType<FudgeDate> {

  /**
   * Standard Fudge field type: date. See {@link FudgeTypeDictionary#DATE_TYPE_ID}.
   */
  public static final DateFieldType INSTANCE = new DateFieldType();
  
  private DateFieldType() {
    super(FudgeTypeDictionary.DATE_TYPE_ID, FudgeDate.class, false, 4);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDate readValue(DataInput input, int dataSize) throws IOException {
    final int n = input.readInt ();
    return new FudgeDate (n);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, FudgeDate value) throws IOException {
    int n = value.getDays ();
    output.writeInt (n);
  }

}
