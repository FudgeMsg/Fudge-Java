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

import com.opengamma.fudge.types.PrimitiveFieldTypes;

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
    
    msg.add(0.5f, "float");
    msg.add(new Float(0.5f), "Float");
    msg.add(0.27362, "double");
    msg.add(new Double(0.27362), "Double");
    
    msg.add("Kirk Wylie", "String");
    
    msg.add(new float[24], "float array");
    msg.add(new double[273], "double array");
    
    return msg;
  }
  
  protected static FudgeMsg createMessageAllOrdinals() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add(Boolean.TRUE, (short)1);
    msg.add(new Boolean(false), (short)2);
    msg.add((byte)5, (short)3);
    msg.add(new Byte((byte)5), (short)4);
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add(shortValue, (short)5);
    msg.add(new Short(shortValue), (short)6);
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add(intValue, (short)7);
    msg.add(new Integer(intValue), (short)8);
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add(longValue, (short)9);
    msg.add(new Long(longValue), (short)10);
    
    msg.add(0.5f, (short)11);
    msg.add(new Float(0.5f), (short)12);
    msg.add(0.27362, (short)13);
    msg.add(new Double(0.27362), (short)14);
    
    msg.add("Kirk Wylie", (short)15);
    
    msg.add(new float[24], (short)16);
    msg.add(new double[273], (short)17);
    
    return msg;
  }
  
  @Test
  public void lookupByNameSingleValue() {
    FudgeMsg msg = createMessageAllNames();
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    field = msg.getByName("boolean");
    assertNotNull(field);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = msg.getByName("Boolean");
    assertNotNull(field);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
    assertEquals(new Boolean(false), field.getValue());
    assertEquals("Boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(1, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
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
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    fields = msg.getAllByName("boolean");
    assertNotNull(fields);
    assertEquals(2, fields.size());
    field = fields.get(0);
    assertNotNull(field);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.TRUE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
    
    field = fields.get(1);
    assertNotNull(field);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE, field.getType());
    assertEquals(Boolean.FALSE, field.getValue());
    assertEquals("boolean", field.getName());
    assertNull(field.getOrdinal());
  }
  
  @Test
  public void primitiveExactQueriesNamesMatch() {
    FudgeMsg msg = createMessageAllNames();
    
    assertEquals(new Byte((byte)5), msg.getByte("byte"));
    assertEquals(new Byte((byte)5), msg.getByte("Byte"));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Short(shortValue), msg.getShort("short"));
    assertEquals(new Short(shortValue), msg.getShort("Short"));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Integer(intValue), msg.getInt("int"));
    assertEquals(new Integer(intValue), msg.getInt("Integer"));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong("long"));
    assertEquals(new Long(longValue), msg.getLong("Long"));
    
    assertEquals(new Float(0.5f), msg.getFloat("float"));
    assertEquals(new Float(0.5f), msg.getFloat("Float"));
    assertEquals(new Double(0.27362), msg.getDouble("double"));
    assertEquals(new Double(0.27362), msg.getDouble("Double"));
    
    assertEquals("Kirk Wylie", msg.getString("String"));
  }
  
  @Test
  public void primitiveExactQueriesNamesNoMatch() {
    FudgeMsg msg = createMessageAllNames();
    
    assertNull(msg.getByte("int"));
    assertNull(msg.getShort("int"));
    assertNull(msg.getInt("byte"));
    assertNull(msg.getLong("int"));
    assertNull(msg.getFloat("double"));
    assertNull(msg.getDouble("float"));
  }
  
  @Test
  public void primitiveExactQueriesNoNames() {
    FudgeMsg msg = createMessageAllNames();
    
    assertNull(msg.getByte("foobar"));
    assertNull(msg.getShort("foobar"));
    assertNull(msg.getInt("foobar"));
    assertNull(msg.getLong("foobar"));
    assertNull(msg.getFloat("foobar"));
    assertNull(msg.getDouble("foobar"));
    assertNull(msg.getString("foobar"));
  }
  
  @Test
  public void asQueriesToLongNames() {
    FudgeMsg msg = createMessageAllNames();
    
    assertEquals(new Long((byte)5), msg.getAsLong("byte"));
    assertEquals(new Long((byte)5), msg.getAsLong("Byte"));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getAsLong("short"));
    assertEquals(new Long(shortValue), msg.getAsLong("Short"));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getAsLong("int"));
    assertEquals(new Long(intValue), msg.getAsLong("Integer"));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getAsLong("long"));
    assertEquals(new Long(longValue), msg.getAsLong("Long"));
    
    assertEquals(new Long(0), msg.getAsLong("float"));
    assertEquals(new Long(0), msg.getAsLong("Float"));
    assertEquals(new Long(0), msg.getAsLong("double"));
    assertEquals(new Long(0), msg.getAsLong("Double"));
  }
  
  @Test
  public void asQueriesToLongNoNames() {
    FudgeMsg msg = createMessageAllNames();
    
    assertNull(msg.getByte("foobar"));
    assertNull(msg.getShort("foobar"));
    assertNull(msg.getInt("foobar"));
    assertNull(msg.getLong("foobar"));
    assertNull(msg.getFloat("foobar"));
    assertNull(msg.getDouble("foobar"));
    assertNull(msg.getString("foobar"));
  }
  
  // ------------
  @Test
  public void primitiveExactQueriesOrdinalsMatch() {
    FudgeMsg msg = createMessageAllOrdinals();
    
    assertEquals(new Byte((byte)5), msg.getByte((short)3));
    assertEquals(new Byte((byte)5), msg.getByte((short)4));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Short(shortValue), msg.getShort((short)5));
    assertEquals(new Short(shortValue), msg.getShort((short)6));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Integer(intValue), msg.getInt((short)7));
    assertEquals(new Integer(intValue), msg.getInt((short)8));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong((short)9));
    assertEquals(new Long(longValue), msg.getLong((short)10));
    
    assertEquals(new Float(0.5f), msg.getFloat((short)11));
    assertEquals(new Float(0.5f), msg.getFloat((short)12));
    assertEquals(new Double(0.27362), msg.getDouble((short)13));
    assertEquals(new Double(0.27362), msg.getDouble((short)14));
    
    assertEquals("Kirk Wylie", msg.getString((short)15));
  }
  
  @Test
  public void primitiveExactQueriesOrdinalsNoMatch() {
    FudgeMsg msg = createMessageAllOrdinals();
    
    assertNull(msg.getByte((short)7));
    assertNull(msg.getShort((short)7));
    assertNull(msg.getInt((short)9));
    assertNull(msg.getLong((short)7));
    assertNull(msg.getFloat((short)13));
    assertNull(msg.getDouble((short)11));
  }
  
  @Test
  public void primitiveExactOrdinalsNoOrdinals() {
    FudgeMsg msg = createMessageAllOrdinals();
    
    assertNull(msg.getByte((short)100));
    assertNull(msg.getShort((short)100));
    assertNull(msg.getInt((short)100));
    assertNull(msg.getLong((short)100));
    assertNull(msg.getFloat((short)100));
    assertNull(msg.getDouble((short)100));
    assertNull(msg.getString((short)100));
  }
  
  @Test
  public void asQueriesToLongOrdinals() {
    FudgeMsg msg = createMessageAllOrdinals();
    
    assertEquals(new Long((byte)5), msg.getAsLong((short)3));
    assertEquals(new Long((byte)5), msg.getAsLong((short)4));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getAsLong((short)5));
    assertEquals(new Long(shortValue), msg.getAsLong((short)6));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getAsLong((short)7));
    assertEquals(new Long(intValue), msg.getAsLong((short)8));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getAsLong((short)9));
    assertEquals(new Long(longValue), msg.getAsLong((short)10));
    
    assertEquals(new Long(0), msg.getAsLong((short)11));
    assertEquals(new Long(0), msg.getAsLong((short)12));
    assertEquals(new Long(0), msg.getAsLong((short)13));
    assertEquals(new Long(0), msg.getAsLong((short)14));
  }
  
  @Test
  public void toByteArray() {
    FudgeMsg msg = createMessageAllNames();
    byte[] bytes = msg.toByteArray();
    assertNotNull(bytes);
    assertTrue(bytes.length > 10);
  }

}
