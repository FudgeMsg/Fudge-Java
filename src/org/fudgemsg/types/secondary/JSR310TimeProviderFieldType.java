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

import javax.time.calendar.TimeProvider;

import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.TimeFieldType;

/**
 * Secondary type for JSR-310 object conversion.
 *
 * @author Andrew Griffin
 */
public class JSR310TimeProviderFieldType extends SecondaryFieldType<TimeProvider,FudgeTime> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310TimeProviderFieldType INSTANCE = new JSR310TimeProviderFieldType ();
  
  private JSR310TimeProviderFieldType () {
    super (TimeFieldType.INSTANCE, TimeProvider.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTime secondaryToPrimary(final TimeProvider object) {
    return new FudgeTime (object);
  }
  
}