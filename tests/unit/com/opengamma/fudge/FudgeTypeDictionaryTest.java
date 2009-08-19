/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
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
