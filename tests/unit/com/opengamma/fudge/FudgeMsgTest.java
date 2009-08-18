/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeMsgTest {
  
  protected static FudgeMsg createMessageAllNames() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add(Boolean.TRUE, "boolean");
    msg.add(new Boolean(false), "Boolean");
    msg.add((byte)5, "byte");
    msg.add(new Byte((byte)5), "Byte");
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add(shortValue, "short");
    msg.add(new Short(shortValue), "Short");
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add(intValue, "int");
    msg.add(new Integer(intValue), "Integer");
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add(longValue, "long");
    msg.add(new Long(longValue), "Long");
    
    return msg;
  }
  
  @Test
  public void lookupByNameSingleValue() {
    FudgeMsg msg = createMessageAllNames();
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    field = msg.getByName("boolean");
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = msg.getByName("Boolean");
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(new Boolean(false), field.getValue());
    assertEquals("Boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(1, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
  }

  @Test
  public void lookupByNameMultipleValues() {
    FudgeMsg msg = createMessageAllNames();
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    // Now add a second by name.
    msg.add(Boolean.FALSE, "boolean");

    field = msg.getByName("boolean");
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(2, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = fields.get(1);
    assertNotNull(field);
    assertEquals(FudgeTypeDictionary.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.FALSE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
  }
  
  @Test
  public void toByteArray() {
    FudgeMsg msg = createMessageAllNames();
    byte[] bytes = msg.toByteArray();
    assertNotNull(bytes);
    assertTrue(bytes.length > 10);
  }

}
