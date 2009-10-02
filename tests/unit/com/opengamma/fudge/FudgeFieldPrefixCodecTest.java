/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeFieldPrefixCodecTest {

  @Test
  public void fieldPrefixComposition() {
    assertEquals(0x20, FudgeFieldPrefixCodec.composeFieldPrefix(false, 10, false, false));
    assertEquals(0x40, FudgeFieldPrefixCodec.composeFieldPrefix(false, 1024, false, false));
    assertEquals(0x60, FudgeFieldPrefixCodec.composeFieldPrefix(false, Short.MAX_VALUE + 1000, false, false));
    assertEquals(0x98, FudgeFieldPrefixCodec.composeFieldPrefix(true, 0, true, true));
  }
  
  @Test
  public void hasNameChecks() {
    assertFalse(FudgeFieldPrefixCodec.hasName((byte)0x20));
    assertTrue(FudgeFieldPrefixCodec.hasName((byte)0x98));
  }
  
  @Test
  public void fixedWidthChecks() {
    assertFalse(FudgeFieldPrefixCodec.isFixedWidth((byte)0x20));
    assertTrue(FudgeFieldPrefixCodec.isFixedWidth((byte)0x98));
  }
  
  @Test
  public void hasOrdinalChecks() {
    assertFalse(FudgeFieldPrefixCodec.hasOrdinal((byte)0x20));
    assertTrue(FudgeFieldPrefixCodec.hasOrdinal((byte)0x98));
  }
  
  public void varWidthSizeChecks() {
    assertEquals(0, FudgeFieldPrefixCodec.getFieldWidthByteCount((byte)0x98));
    assertEquals(1, FudgeFieldPrefixCodec.getFieldWidthByteCount((byte)0x20));
    assertEquals(2, FudgeFieldPrefixCodec.getFieldWidthByteCount((byte)0x40));
    assertEquals(4, FudgeFieldPrefixCodec.getFieldWidthByteCount((byte)0x60));
  }

}
