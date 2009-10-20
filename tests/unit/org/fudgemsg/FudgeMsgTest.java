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
package org.fudgemsg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.junit.Test;


/**
 * 
 *
 * @author kirk
 */
public class FudgeMsgTest {
  
  protected static FudgeMsg createMessageAllNames() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add("boolean", Boolean.TRUE);
    msg.add("Boolean", new Boolean(false));
    msg.add("byte", (byte)5);
    msg.add("Byte", new Byte((byte)5));
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add("short", shortValue);
    msg.add("Short", new Short(shortValue));
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add("int", intValue);
    msg.add("Integer", new Integer(intValue));
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add("long", longValue);
    msg.add("Long", new Long(longValue));
    
    msg.add("float", 0.5f);
    msg.add("Float", new Float(0.5f));
    msg.add("double", 0.27362);
    msg.add("Double", new Double(0.27362));
    
    msg.add("String", "Kirk Wylie");
    
    msg.add("float array", new float[24]);
    msg.add("double array", new double[273]);
    msg.add("short array", new short[32]);
    msg.add("int array", new int[83]);
    msg.add("long array", new long[837]);
    
    msg.add("indicator", IndicatorType.INSTANCE);
    
    return msg;
  }
  
  protected static FudgeMsg createMessageAllOrdinals() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add(1, Boolean.TRUE);
    msg.add(2, new Boolean(false));
    msg.add(3, (byte)5);
    msg.add(4, new Byte((byte)5));
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add(5, shortValue);
    msg.add(6, new Short(shortValue));
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add(7, intValue);
    msg.add(8, new Integer(intValue));
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add(9, longValue);
    msg.add(10, new Long(longValue));
    
    msg.add(11, 0.5f);
    msg.add(12, new Float(0.5f));
    msg.add(13, 0.27362);
    msg.add(14, new Double(0.27362));
    
    msg.add(15, "Kirk Wylie");
    
    msg.add(16, new float[24]);
    msg.add(17, new double[273]);
    
    return msg;
  }
  
  protected static FudgeMsg createMessageAllByteArrayLengths() {
    FudgeMsg msg = new FudgeMsg();
    msg.add("byte[4]", new byte[4]);
    msg.add("byte[8]", new byte[8]);
    msg.add("byte[16]", new byte[16]);
    msg.add("byte[20]", new byte[20]);
    msg.add("byte[32]", new byte[32]);
    msg.add("byte[64]", new byte[64]);
    msg.add("byte[128]", new byte[128]);
    msg.add("byte[256]", new byte[256]);
    msg.add("byte[512]", new byte[512]);
    
    msg.add("byte[28]", new byte[28]);
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
    
    // Check the indicator type specially
    assertSame(IndicatorType.INSTANCE, msg.getValue("indicator"));
  }

  @Test
  public void lookupByNameMultipleValues() {
    FudgeMsg msg = createMessageAllNames();
    FudgeField field = null;
    List<FudgeField> fields = null;
    
    // Now add a second by name.
    msg.add("boolean", Boolean.FALSE);

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
    // now we've decided that get*() == getAs*() these work...
    assertNotNull(msg.getByte("int"));
    assertNotNull(msg.getShort("int"));
    assertNotNull(msg.getInt("byte"));
    assertNotNull(msg.getLong("int"));
    assertNotNull(msg.getFloat("double"));
    assertNotNull(msg.getDouble("float"));
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
    
    assertEquals(new Long((byte)5), msg.getLong("byte"));
    assertEquals(new Long((byte)5), msg.getLong("Byte"));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getLong("short"));
    assertEquals(new Long(shortValue), msg.getLong("Short"));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getLong("int"));
    assertEquals(new Long(intValue), msg.getLong("Integer"));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong("long"));
    assertEquals(new Long(longValue), msg.getLong("Long"));
    
    assertEquals(new Long(0), msg.getLong("float"));
    assertEquals(new Long(0), msg.getLong("Float"));
    assertEquals(new Long(0), msg.getLong("double"));
    assertEquals(new Long(0), msg.getLong("Double"));
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
    // these have changed since the decision to make get* == getAs*.
    // truncation may occur at the moment and we need to consider 
    // whether exceptions should be thrown instead
    assertNotNull(msg.getByte((short)7));
    assertNotNull(msg.getShort((short)7));
    assertNotNull(msg.getInt((short)9));
    assertNotNull(msg.getLong((short)7));
    assertNotNull(msg.getFloat((short)13));
    assertNotNull(msg.getDouble((short)11));
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
    
    assertEquals(new Long((byte)5), msg.getLong((short)3));
    assertEquals(new Long((byte)5), msg.getLong((short)4));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    assertEquals(new Long(shortValue), msg.getLong((short)5));
    assertEquals(new Long(shortValue), msg.getLong((short)6));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    assertEquals(new Long(intValue), msg.getLong((short)7));
    assertEquals(new Long(intValue), msg.getLong((short)8));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    assertEquals(new Long(longValue), msg.getLong((short)9));
    assertEquals(new Long(longValue), msg.getLong((short)10));
    
    assertEquals(new Long(0), msg.getLong((short)11));
    assertEquals(new Long(0), msg.getLong((short)12));
    assertEquals(new Long(0), msg.getLong((short)13));
    assertEquals(new Long(0), msg.getLong((short)14));
  }
  
  @Test
  public void toByteArray() {
    FudgeMsg msg = createMessageAllNames();
    byte[] bytes = msg.toByteArray();
    assertNotNull(bytes);
    assertTrue(bytes.length > 10);
  }
  
  @Test
  public void fixedLengthByteArrays() {
    FudgeMsg msg = createMessageAllByteArrayLengths();
    assertSame(ByteArrayFieldType.LENGTH_4_INSTANCE, msg.getByName("byte[4]").getType());
    assertSame(ByteArrayFieldType.LENGTH_8_INSTANCE, msg.getByName("byte[8]").getType());
    assertSame(ByteArrayFieldType.LENGTH_16_INSTANCE, msg.getByName("byte[16]").getType());
    assertSame(ByteArrayFieldType.LENGTH_20_INSTANCE, msg.getByName("byte[20]").getType());
    assertSame(ByteArrayFieldType.LENGTH_32_INSTANCE, msg.getByName("byte[32]").getType());
    assertSame(ByteArrayFieldType.LENGTH_64_INSTANCE, msg.getByName("byte[64]").getType());
    assertSame(ByteArrayFieldType.LENGTH_128_INSTANCE, msg.getByName("byte[128]").getType());
    assertSame(ByteArrayFieldType.LENGTH_256_INSTANCE, msg.getByName("byte[256]").getType());
    assertSame(ByteArrayFieldType.LENGTH_512_INSTANCE, msg.getByName("byte[512]").getType());
    
    assertSame(ByteArrayFieldType.VARIABLE_SIZED_INSTANCE, msg.getByName("byte[28]").getType());
  }
  
  @Test
  public void iterable() {
    FudgeMsg msg = createMessageAllNames();
    int fieldCount = 0;
    for(@SuppressWarnings("unused") FudgeField field : msg) {
      fieldCount++;
    }
    assertEquals(msg.getNumFields(), fieldCount);
  }

  @Test
  public void iterableContainer() {
    FudgeFieldContainer msg = createMessageAllNames();
    int fieldCount = 0;
    for(@SuppressWarnings("unused") FudgeField field : msg) {
      fieldCount++;
    }
    assertEquals(msg.getNumFields(), fieldCount);
  }

}
