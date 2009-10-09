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
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.opengamma.fudge.types.PrimitiveFieldTypes;

/**
 * 
 *
 * @author kirk
 */
public class FudgeTypeDictionaryTest {
  
  @Test
  public void simpleTypeLookup() {
    FudgeFieldType<?> type = null;
    
    type = FudgeTypeDictionary.INSTANCE.getByJavaType(Boolean.TYPE);
    assertNotNull(type);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE.getTypeId(), type.getTypeId());

    type = FudgeTypeDictionary.INSTANCE.getByJavaType(Boolean.class);
    assertNotNull(type);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE.getTypeId(), type.getTypeId());
  }
  
}
