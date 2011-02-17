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

import javax.time.Instant;

import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for JSR-310 object conversion.
 *
 * @author Andrew Griffin
 */
public class JSR310InstantFieldType extends SecondaryFieldType<Instant,FudgeDateTime> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JSR310InstantFieldType INSTANCE = new JSR310InstantFieldType ();
  
  private JSR310InstantFieldType () {
    super (DateTimeFieldType.INSTANCE, Instant.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeDateTime secondaryToPrimary(final Instant object) {
    return new FudgeDateTime (object);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Instant primaryToSecondary (final FudgeDateTime object) {
    return object.toInstant ();
  }

}