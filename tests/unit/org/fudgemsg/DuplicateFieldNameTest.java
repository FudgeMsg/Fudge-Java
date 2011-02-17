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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.types.IndicatorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Check that the {@code getXXX} methods in FudgeFieldContainer implementations
 * do as expected when duplicate field names are found. It is parameterized so
 * that all of the concrete instances can be passed in.
 */
@RunWith(Parameterized.class)
public class DuplicateFieldNameTest {
  
  private static final String FIELD_NAME = "name";
  private static final Integer FIELD_ORDINAL = 1;
  
  private static final Boolean CONST_BOOLEAN = true;
  private static final Double CONST_DOUBLE = 3.14;
  private static final Float CONST_FLOAT = 1.0f;
  private static final Long CONST_LONG = 0x100000000l;
  private static final Integer CONST_INTEGER = 0x10000;
  private static final Short CONST_SHORT = 0x100;
  private static final Byte CONST_BYTE = 0x01;
  private static final String CONST_STRING = "Hello world";
  private static final FudgeFieldContainer CONST_MESSAGE = FudgeContext.GLOBAL_DEFAULT.newMessage ();
  
  private static void populateMutableFudgeFieldContainer (final MutableFudgeFieldContainer msg) {
    msg.add (FIELD_NAME, IndicatorType.INSTANCE);
    msg.add (FIELD_ORDINAL, IndicatorType.INSTANCE);
    msg.add (FIELD_NAME, CONST_BOOLEAN);
    msg.add (FIELD_ORDINAL, CONST_BOOLEAN);
    msg.add (FIELD_NAME, CONST_BYTE);
    msg.add (FIELD_ORDINAL, CONST_BYTE);
    msg.add (FIELD_NAME, CONST_SHORT);
    msg.add (FIELD_ORDINAL, CONST_SHORT);
    msg.add (FIELD_NAME, CONST_INTEGER);
    msg.add (FIELD_ORDINAL, CONST_INTEGER);
    msg.add (FIELD_NAME, CONST_LONG);
    msg.add (FIELD_ORDINAL, CONST_LONG);
    msg.add (FIELD_NAME, CONST_FLOAT);
    msg.add (FIELD_ORDINAL, CONST_FLOAT);
    msg.add (FIELD_NAME, CONST_DOUBLE);
    msg.add (FIELD_ORDINAL, CONST_DOUBLE);
    msg.add (FIELD_NAME, CONST_MESSAGE);
    msg.add (FIELD_ORDINAL, CONST_MESSAGE);
    msg.add (FIELD_NAME, CONST_STRING);
    msg.add (FIELD_ORDINAL, CONST_STRING);
  }
  
  private static FudgeMsg createFudgeMsg () {
    final FudgeMsg msg = new FudgeMsg (FudgeContext.GLOBAL_DEFAULT);
    populateMutableFudgeFieldContainer (msg);
    return msg;
  }
  
  private static ImmutableFudgeMsg createImmutableFudgeMsg () {
    return new ImmutableFudgeMsg (createFudgeMsg ());
  }
  
  private final FudgeFieldContainer _message;
  
  /**
   * [Documentation not available]
   * 
   * @param message [Documentation not available]
   */
  public DuplicateFieldNameTest (final FudgeFieldContainer message) {
    _message = message;
  }
  
  /**
   * Returns an array of instances of each of the {@link FudgeFieldContainer} implementations for testing.
   * 
   * @return collection of single element arrays of {@code FudgeFieldContainer} implementations
   */
  @Parameters
  public static Collection<Object[]> getParameters () {
    final List<Object[]> params = new ArrayList<Object[]> (2);
    params.add (new Object[] { createFudgeMsg () });
    params.add (new Object[] { createImmutableFudgeMsg () });
    return params;
  }
  
  /**
   * [Documentation not available]
   * 
   * @return [Documentation not available]
   */
  protected FudgeFieldContainer getMessage () {
    return _message;
  }
  
  /**
   * 
   */
  @Test
  public void testGetBooleanByName () {
    assertEquals (CONST_BOOLEAN, getMessage ().getBoolean (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetBooleanByOrdinal () {
    assertEquals (CONST_BOOLEAN, getMessage ().getBoolean (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetDoubleByName () {
    assertEquals (CONST_DOUBLE, getMessage ().getDouble (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetDoubleByOrdinal () {
    assertEquals (CONST_DOUBLE, getMessage ().getDouble (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetFloatByName () {
    assertEquals (CONST_FLOAT, getMessage ().getFloat (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetFloatByOrdinal () {
    assertEquals (CONST_FLOAT, getMessage ().getFloat (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetLongByName () {
    assertEquals (CONST_LONG, getMessage ().getLong (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetLongByOrdinal () {
    assertEquals (CONST_LONG, getMessage ().getLong (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetIntByName () {
    assertEquals (CONST_INTEGER, getMessage ().getInt (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetIntByOrdinal () {
    assertEquals (CONST_INTEGER, getMessage ().getInt (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetShortByName () {
    assertEquals (CONST_SHORT, getMessage ().getShort (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetShortByOrdinal () {
    assertEquals (CONST_SHORT, getMessage ().getShort (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetByteByName () {
    assertEquals (CONST_BYTE, getMessage ().getByte (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetByteByOrdinal () {
    assertEquals (CONST_BYTE, getMessage ().getByte (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetStringByName () {
    assertEquals (CONST_STRING, getMessage ().getString (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetStringByOrdinal () {
    assertEquals (CONST_STRING, getMessage ().getString (FIELD_ORDINAL));
  }
  
  /**
   * 
   */
  @Test
  public void testGetMessageByName () {
    assertEquals (CONST_MESSAGE, getMessage ().getMessage (FIELD_NAME));
  }
  
  /**
   * 
   */
  @Test
  public void testGetMessageByOrdinal () {
    assertEquals (CONST_MESSAGE, getMessage ().getMessage (FIELD_ORDINAL));
  }
  
}