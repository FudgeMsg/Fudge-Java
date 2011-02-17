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

package org.fudgemsg.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

/**
 * 
 */
public class EnumTest {
  
  private Object cycleObject (final Object o) {
    final FudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (o).getMessage ();
    FudgeMsgFormatter.outputToSystemOut (msg);
    return FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg (msg);
  }
  
  private FudgeFieldContainer cycleMessage (final FudgeFieldContainer msg) {
    return FudgeContext.GLOBAL_DEFAULT.deserialize (FudgeContext.GLOBAL_DEFAULT.toByteArray (msg)).getMessage ();
  }
  
  private static boolean s_fromFudgeMessage;
  private static boolean s_toFudgeMessage;
  
  /**
   * 
   */
  public static enum EnumWithMethods {
    /**
     * 
     */
    PUT,
    /**
     * 
     */
    CALL;
    /**
     * 
     * @param msg [documentation not available] 
     * @return [documentation not available]
     */
    public static EnumWithMethods fromFudgeMsg (final FudgeFieldContainer msg) {
      s_fromFudgeMessage = true;
      return valueOf (EnumWithMethods.class, msg.getString (1));
    }
    /**
     * 
     * @param messageFactory [documentation not available]
     * @return [documentation not available]
     */
    public MutableFudgeFieldContainer toFudgeMsg (final FudgeMessageFactory messageFactory) {
      s_toFudgeMessage = true;
      final MutableFudgeFieldContainer msg = messageFactory.newMessage ();
      msg.add (null, 0, EnumWithMethods.class.getName ());
      msg.add (null, 1, name ());
      return msg;
    }
  }
  
  /**
   * 
   */
  public static enum EnumWithoutMethods {
    /**
     * 
     */
    PUT,
    /**
     * 
     */
    CALL
  }
  
  private <T extends Enum<?>> void testEnumCycle (T value) {
    Object o = cycleObject (value);
    assertNotNull (o);
    assertEquals (value, o);
  }
  
  /**
   * 
   */
  @Test
  public void testEnumWithMethods () {
    s_fromFudgeMessage = s_toFudgeMessage = false;
    testEnumCycle (EnumWithMethods.PUT);
    assertTrue (s_fromFudgeMessage);
    assertTrue (s_toFudgeMessage);
  }
  
  /**
   * 
   */
  @Test
  public void testEnumWithoutMethods () {
    testEnumCycle (EnumWithoutMethods.CALL);
  }
  
  /**
   * 
   */
  @Test
  public void testEnumViaDictionary () {
    final MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage ();
    msg.add (null, 1, EnumWithoutMethods.PUT.name ());
    final FudgeFieldContainer msg2 = cycleMessage (msg);
    final FudgeField field = msg2.getByOrdinal (1);
    assertNotNull (field);
    assertEquals (EnumWithoutMethods.PUT.name (), field.getValue ());
    assertEquals (EnumWithoutMethods.PUT, msg2.getFieldValue (EnumWithoutMethods.class, field));
  }
  
}