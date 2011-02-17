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

import java.util.Date;

import javax.time.Instant;

import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.SecondaryFieldTypeBase;

/**
 * Secondary type for {@link Date} conversion to/from a {@link FudgeDate} or {@link FudgeDateTime}
 * transport object. Note that once the Java Time Framework is part of the main JDK the {@code Date}
 * class will implement {@code InstantProvider} and this type can be deprecated.
 *
 * @author Andrew Griffin
 */
public class JavaUtilDateFieldType extends SecondaryFieldTypeBase<Date,Object,FudgeDateTime> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JavaUtilDateFieldType INSTANCE = new JavaUtilDateFieldType ();
  
  private JavaUtilDateFieldType () {
    super (DateTimeFieldType.INSTANCE, Date.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDateTime secondaryToPrimary (Date object) {
    return new FudgeDateTime (DateTimeAccuracy.MILLISECOND, Instant.ofEpochMillis (object.getTime ())); // Interim measure
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Date primaryToSecondary (Object object) {
    if (object instanceof FudgeDateTime) {
      return primaryToSecondary ((FudgeDateTime)object);
    } else if (object instanceof FudgeDate) {
      return primaryToSecondary ((FudgeDate)object);
    } else {
      throw new IllegalArgumentException ("cannot convert from type " + object.getClass ().getName ());
    }
  }
  
  /**
   * Primary to secondary conversion, where the primary type is a {@link FudgeDateTime}.
   * 
   * @param object primary object
   * @return the converted {@link Date} object
   */
  protected Date primaryToSecondary (FudgeDateTime object) {
    return JavaUtilCalendarFieldType.fudgeDateTimeToCalendar (object.getDate (), object.getTime ()).getTime ();
  }
  
  /**
   * Primary secondary conversion, where the primary type is a {@link FudgeDate}.
   * 
   * @param object primary object
   * @return the converted {@link Date} object
   */
  protected Date primaryToSecondary (FudgeDate object) {
    return JavaUtilCalendarFieldType.fudgeDateTimeToCalendar (object, null).getTime ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary (Class<?> javaType) {
    return FudgeDateTime.class.isAssignableFrom (javaType) || FudgeDate.class.isAssignableFrom (javaType);
  }
  
}