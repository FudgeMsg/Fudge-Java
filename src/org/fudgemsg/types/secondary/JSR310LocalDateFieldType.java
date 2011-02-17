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

import javax.time.calendar.DateProvider;
import javax.time.calendar.LocalDate;

import org.fudgemsg.types.DateFieldType;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.SecondaryFieldTypeBase;

/**
 * Secondary type for JSR-310 object conversion.
 *
 * @author Andrew Griffin
 */
public class JSR310LocalDateFieldType extends SecondaryFieldTypeBase<LocalDate,DateProvider,FudgeDate> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310LocalDateFieldType INSTANCE = new JSR310LocalDateFieldType ();
  
  private JSR310LocalDateFieldType () {
    super (DateFieldType.INSTANCE, LocalDate.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDate secondaryToPrimary(final LocalDate object) {
    return new FudgeDate (object);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public LocalDate primaryToSecondary (final DateProvider object) {
    return object.toLocalDate ();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canConvertPrimary (final Class<? extends DateProvider> clazz) {
    return DateProvider.class.isAssignableFrom (clazz);
  }

}