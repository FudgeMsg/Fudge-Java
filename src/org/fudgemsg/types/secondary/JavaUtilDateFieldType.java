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
package org.fudgemsg.types.secondary;

import java.util.Date;
import java.util.Calendar;

import org.fudgemsg.types.DateTimeFieldType;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for {@link Date} conversion to/from a {@link Calendar}.
 *
 * @author Andrew
 */
public class JavaUtilDateFieldType extends SecondaryFieldType<Date,Calendar> {
  
  public static final JavaUtilDateFieldType INSTANCE = new JavaUtilDateFieldType ();
  
  private JavaUtilDateFieldType () {
    super (DateTimeFieldType.INSTANCE, Date.class);
    
  }

  @Override
  public Calendar secondaryToPrimary(Date object) {
    final Calendar calendar = Calendar.getInstance ();
    calendar.setTime (object);
    return calendar;
  }
  
  @Override
  public Date primaryToSecondary (Calendar object) {
    return object.getTime ();
  }
  
}