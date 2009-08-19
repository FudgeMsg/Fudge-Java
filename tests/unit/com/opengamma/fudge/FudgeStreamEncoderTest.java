/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamEncoderTest {
  
  @Test
  public void fieldPrefixComposition() {
    assertEquals(0x20, FudgeStreamEncoder.composeFieldPrefix(false, 10, false, false));
    assertEquals(0x40, FudgeStreamEncoder.composeFieldPrefix(false, 1024, false, false));
    assertEquals(0x60, FudgeStreamEncoder.composeFieldPrefix(false, Short.MAX_VALUE + 1000, false, false));
    assertEquals(0x98, FudgeStreamEncoder.composeFieldPrefix(true, 0, true, true));
  }

}
