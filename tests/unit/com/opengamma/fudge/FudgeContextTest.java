/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeContextTest {

  @Test
  public void allNamesCodecNoTaxonomy() {
    FudgeMsg inputMsg = FudgeMsgTest.createMessageAllNames();
    FudgeContext context = new FudgeContext();
    FudgeMsg outputMsg = cycleMessage(inputMsg, context);
    
    assertNotNull(outputMsg);
    
    FudgeMsgCodecTest.assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * @param inputMsg
   * @param context
   * @return
   */
  private FudgeMsg cycleMessage(FudgeMsg msg, FudgeContext context) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    context.serialize(msg, baos);
    
    byte[] content = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(content);
    FudgeMsg outputMsg = context.deserialize(bais);
    return outputMsg;
  }
  
}
