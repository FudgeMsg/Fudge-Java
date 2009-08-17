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
    assertEquals(0x40, FudgeStreamEncoder.composeFieldPrefix(false, Short.MAX_VALUE + 1, false, false));
    assertEquals(0x98, FudgeStreamEncoder.composeFieldPrefix(true, 0, true, true));
  }

}
