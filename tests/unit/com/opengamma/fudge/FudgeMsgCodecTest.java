/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

/**
 * A test class that will encode and decode a number of different Fudge messages
 * to test that encoding and decoding works properly.
 *
 * @author kirk
 */
public class FudgeMsgCodecTest {
  
  @Test
  public void allNames() throws IOException {
    FudgeMsg inputMsg = FudgeMsgTest.createMessageAllNames();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    FudgeStreamEncoder.writeMsg(dos, inputMsg);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);
    FudgeMsg outputMsg = FudgeStreamDecoder.readMsg(dis);
    
    assertNotNull(outputMsg);
    
    assertAllFieldsMatch(inputMsg, outputMsg);
  }

  /**
   * @param inputMsg
   * @param outputMsg
   */
  protected static void assertAllFieldsMatch(FudgeMsg expectedMsg, FudgeMsg actualMsg) {
    Iterator<FudgeField> expectedIter = expectedMsg.getAllFields().iterator();
    Iterator<FudgeField> actualIter = actualMsg.getAllFields().iterator();
    while(expectedIter.hasNext()) {
      assertTrue(actualIter.hasNext());
      FudgeField expectedField = expectedIter.next();
      FudgeField actualField = actualIter.next();
      
      assertEquals(expectedField.getName(), actualField.getName());
      assertEquals(expectedField.getType(), actualField.getType());
      assertEquals(expectedField.getOrdinal(), actualField.getOrdinal());
      assertEquals(expectedField.getValue(), actualField.getValue());
    }
    assertFalse(actualIter.hasNext());
  }

}
