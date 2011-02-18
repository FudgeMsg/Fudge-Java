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
package org.fudgemsg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.SecondaryFieldType;
import org.junit.Test;

/**
 * Test the Fudge type dictionary.
 */
public class FudgeTypeDictionaryTest {

  @Test
  public void simpleTypeLookup() {
    FudgeFieldType<?> type = null;
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    
    type = dictionary.getByJavaType(Boolean.TYPE);
    assertNotNull(type);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE.getTypeId(), type.getTypeId());

    type = dictionary.getByJavaType(Boolean.class);
    assertNotNull(type);
    assertEquals(PrimitiveFieldTypes.BOOLEAN_TYPE.getTypeId(), type.getTypeId());
  }

  @Test
  public void simpleTypeConversion () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    
    final UUID uuidIn = new UUID (0x0F0E0D0C0B0A0908l, 0x0706050403020100l);
    final byte[] byteIn = new byte[] { 15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0 };
    
    final FudgeFieldType<?> uuidType = dictionary.getByJavaType (uuidIn.getClass ());
    final FudgeFieldType<?> byteType = dictionary.getByJavaType (byteIn.getClass ());
    
    assertNotSame (uuidType, byteType);
    assertEquals (((SecondaryFieldType<?,?>)uuidType).getPrimaryType ().getJavaType (), byteType.getJavaType ());
    
    final FudgeField uuidField = FudgeMsgField.of(uuidType, uuidIn);
    final FudgeField byteField = FudgeMsgField.of(byteType, byteIn);
    
    UUID uuidOut = dictionary.getFieldValue (uuidIn.getClass (), uuidField); // no conversion needed
    assertNotNull (uuidOut);
    assertEquals (uuidIn, uuidOut);
    uuidOut = dictionary.getFieldValue (uuidIn.getClass (), byteField); // promotion from byte[] to UUID
    assertNotNull (uuidOut);
    assertEquals (uuidIn, uuidOut);
    byte[] byteOut = dictionary.getFieldValue (byteIn.getClass (), uuidField); // demotion from UUID to byte[]
    assertNotNull (byteOut);
    assertArrayEquals (byteIn, byteOut);
    byteOut = dictionary.getFieldValue (byteIn.getClass (), byteField); // no conversion needed
    assertNotNull (byteOut);
    assertArrayEquals (byteIn, byteOut);
  }

  private static class Foo {
    private final byte[] _data;
    Foo (final byte[] data) {
      _data = data;
    }
    Foo () {
      ByteArrayOutputStream baos = new ByteArrayOutputStream ();
      UUID u = UUID.randomUUID ();
      DataOutputStream dos = new DataOutputStream (baos);
      try {
        dos.writeLong (u.getMostSignificantBits ());
        dos.writeLong (u.getLeastSignificantBits ());
      } catch (IOException e) {
        throw new FudgeRuntimeException ("unexpected I/O exception", e);
      }
      _data = baos.toByteArray ();
    }
    byte[] getData () {
      return _data;
    }
  }

  private static class Bar {
    private final int _data;
    Bar (final int data) {
      _data = data;
    }
    int getData () {
      return _data;
    }
  }

  private static class FooSecondaryType extends SecondaryFieldType<Foo,byte[]> {
    
    static final FooSecondaryType INSTANCE = new FooSecondaryType ();
    
    private FooSecondaryType () {
      super (ByteArrayFieldType.LENGTH_16_INSTANCE, Foo.class);
    }

    @Override
    public byte[] secondaryToPrimary(Foo object) {
      return object.getData ();
    }
    
    @Override
    public Foo primaryToSecondary (byte[] object) {
      return new Foo (object);
    }
    
  }

  private static class BarSecondaryType extends SecondaryFieldType<Bar,Integer> {
    
    static final BarSecondaryType INSTANCE = new BarSecondaryType ();
    
    BarSecondaryType () {
      super (PrimitiveFieldTypes.INT_TYPE, Bar.class);
    }
    
    @Override
    public Integer secondaryToPrimary (Bar object) {
      return object.getData ();
    }
    
    @Override
    public Bar primaryToSecondary (Integer object) {
      return new Bar (object);
    }
  }

  @Test
  public void complexTypeConversion () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    dictionary.addType (FooSecondaryType.INSTANCE);
    
    final Foo fooIn = new Foo ();
    final FudgeField fooField = FudgeMsgField.of(dictionary.getByJavaType(Foo.class), fooIn);
    final UUID uuid = dictionary.getFieldValue (UUID.class, fooField);
    assertNotNull (uuid);
    final FudgeField uuidField = FudgeMsgField.of(dictionary.getByJavaType(UUID.class), uuid);
    final Foo fooOut = dictionary.getFieldValue (Foo.class, uuidField);
    assertNotNull (fooOut);
    assertArrayEquals (fooIn.getData (), fooOut.getData ());
  }

  @Test(expected=IllegalArgumentException.class)
  public void secondaryToNullError () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    final UUID uuid = UUID.randomUUID ();
    final FudgeField uuidField = FudgeMsgField.of(dictionary.getByJavaType(UUID.class), uuid);
    dictionary.getFieldValue (Thread.class, uuidField);
  }

  @Test(expected=IllegalArgumentException.class)
  public void secondaryToNoCommonBaseError () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    dictionary.addType (BarSecondaryType.INSTANCE);
    final UUID uuid = UUID.randomUUID ();
    final FudgeField uuidField = FudgeMsgField.of(dictionary.getByJavaType(UUID.class), uuid);
    dictionary.getFieldValue (Bar.class, uuidField);
  }

  @Test(expected=IllegalArgumentException.class)
  public void primaryToNullError () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    final FudgeField stringField = FudgeMsgField.of(dictionary.getByJavaType(String.class), "hello world");
    dictionary.getFieldValue (Thread.class, stringField);
  }

  @Test(expected=IllegalArgumentException.class)
  public void primaryToBadSecondaryError () {
    final FudgeTypeDictionary dictionary = new FudgeTypeDictionary ();
    dictionary.addType (BarSecondaryType.INSTANCE);
    final FudgeField stringField = FudgeMsgField.of(dictionary.getByJavaType(String.class), "hello world");
    dictionary.getFieldValue (Bar.class, stringField);
  }

}
