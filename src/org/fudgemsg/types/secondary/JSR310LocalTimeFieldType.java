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

import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeProvider;

import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.types.TimeFieldType;

/**
 * Secondary type for JSR-310 object conversion.
 *
 * @author Andrew Griffin
 */
public class JSR310LocalTimeFieldType extends SecondaryFieldTypeBase<LocalTime,TimeProvider,FudgeTime> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310LocalTimeFieldType INSTANCE = new JSR310LocalTimeFieldType ();
  
  private JSR310LocalTimeFieldType () {
    super (TimeFieldType.INSTANCE, LocalTime.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTime secondaryToPrimary(final LocalTime object) {
    return new FudgeTime (object);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public LocalTime primaryToSecondary (final TimeProvider object) {
    return object.toLocalTime ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary (final Class<? extends TimeProvider> clazz) {
    return TimeProvider.class.isAssignableFrom (clazz);
  }

}