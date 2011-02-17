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

import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeProvider;

import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.types.TimeFieldType;

/**
 * Secondary type for JSR-310 object conversion.
 *
 * @author Andrew Griffin
 */
public class JSR310OffsetTimeFieldType extends SecondaryFieldTypeBase<OffsetTime,TimeProvider,FudgeTime> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310OffsetTimeFieldType INSTANCE = new JSR310OffsetTimeFieldType ();
  
  private JSR310OffsetTimeFieldType () {
    super (TimeFieldType.INSTANCE, OffsetTime.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTime secondaryToPrimary(final OffsetTime object) {
    return new FudgeTime (object);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public OffsetTime primaryToSecondary (final TimeProvider object) {
    if (object instanceof FudgeDateTime) {
      return primaryToSecondary ((FudgeDateTime)object);
    } else if (object instanceof FudgeTime) {
      return primaryToSecondary ((FudgeTime)object);
    } else {
      throw new IllegalArgumentException ("cannot convert from type " + object.getClass ().getName ());
    }
  }
  
  /**
   * Primary to secondary conversion where the primary object is a {@link FudgeDateTime} object.
   * 
   * @param object primary object
   * @return secondary object
   */
  protected OffsetTime primaryToSecondary (final FudgeDateTime object) {
    return object.toOffsetTime ();
  }
  
  /**
   * Primary to secondary conversion where the primary object is a {@link FudgeTime} object.
   * 
   * @param object primary object
   * @return secondary object
   */
  protected OffsetTime primaryToSecondary (final FudgeTime object) {
    return object.toOffsetTime ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary (final Class<? extends TimeProvider> clazz) {
    return FudgeDateTime.class.isAssignableFrom (clazz) || FudgeTime.class.isAssignableFrom (clazz);
  }

}