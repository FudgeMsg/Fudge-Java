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

import java.math.BigDecimal;

import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;

/**
 * Secondary type for BigDecimal conversion to/from String.
 *
 * @author Andrew Griffin
 */
public class JavaMathBigDecimalFieldType extends SecondaryFieldType<BigDecimal,String> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JavaMathBigDecimalFieldType INSTANCE = new JavaMathBigDecimalFieldType ();
  
  private JavaMathBigDecimalFieldType () {
    super (StringFieldType.INSTANCE, BigDecimal.class);
  }
  
  /**
   * 
   */
  @Override
  public String secondaryToPrimary (final BigDecimal object) {
    return object.toString ();
  }
  
  /**
   * 
   */
  @Override
  public BigDecimal primaryToSecondary (final String data) {
    return new BigDecimal (data);
  }

}