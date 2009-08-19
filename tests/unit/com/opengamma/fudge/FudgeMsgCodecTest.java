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
    FudgeMsg outputMsg = cycleMessage(inputMsg);
    
    assertNotNull(outputMsg);
    
    assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void variableWidthColumnSizes() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add(new byte[100], "100");
    inputMsg.add(new byte[1000], "1000");
    inputMsg.add(new byte[100000], "10000");

    FudgeMsg outputMsg = cycleMessage(inputMsg);
    
    assertNotNull(outputMsg);
    
    assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void subMsg() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    FudgeMsg sub1 = new FudgeMsg();
    sub1.add("fibble", "bibble");
    sub1.add("Blibble", (short)827);
    FudgeMsg sub2 = new FudgeMsg();
    sub2.add(9837438, "bibble9");
    sub2.add(82.77f, (short)828);
    inputMsg.add(sub1, "sub1");
    inputMsg.add(sub2, "sub2");

    FudgeMsg outputMsg = cycleMessage(inputMsg);
    
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
      if(expectedField.getValue().getClass().isArray()) {
        assertEquals(expectedField.getValue().getClass(), actualField.getValue().getClass());
        // TODO wyliekir 2009-08-19 -- Check something better.
      } else if(expectedField.getValue() instanceof FudgeMsg) {
        assertTrue(actualField.getValue() instanceof FudgeMsg);
        assertAllFieldsMatch((FudgeMsg) expectedField.getValue(),
            (FudgeMsg) actualField.getValue());
      } else {
        assertEquals(expectedField.getValue(), actualField.getValue());
      }
    }
    assertFalse(actualIter.hasNext());
  }
  
  protected static FudgeMsg cycleMessage(FudgeMsg msg) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    FudgeStreamEncoder.writeMsg(dos, msg);
    
    byte[] content = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(content);
    DataInputStream dis = new DataInputStream(bais);
    FudgeMsg outputMsg = FudgeStreamDecoder.readMsg(dis);
    return outputMsg;
  }

}
