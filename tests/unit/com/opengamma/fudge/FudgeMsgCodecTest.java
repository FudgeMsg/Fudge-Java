/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

/**
 * A test class that will encode and decode a number of different Fudge messages
 * to test that encoding and decoding works properly.
 *
 * @author kirk
 */
public class FudgeMsgCodecTest {
  private final Random _random = new Random();
  
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
  
  @Test
  public void unknown() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add(new UnknownFudgeFieldValue(new byte[10], FudgeTypeDictionary.INSTANCE.getUnknownType(200)), "unknown");
    FudgeMsg outputMsg = cycleMessage(inputMsg);
    assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  protected byte[] createRandomArray(int length) {
    byte[] bytes = new byte[length];
    _random.nextBytes(bytes);
    return bytes;
  }

  @Test
  public void fixedWidthByteArrays() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add(createRandomArray(4), "byte[4]");
    inputMsg.add(createRandomArray(8), "byte[8]");
    inputMsg.add(createRandomArray(16), "byte[16]");
    inputMsg.add(createRandomArray(20), "byte[20]");
    inputMsg.add(createRandomArray(32), "byte[32]");
    inputMsg.add(createRandomArray(64), "byte[64]");
    inputMsg.add(createRandomArray(128), "byte[128]");
    inputMsg.add(createRandomArray(256), "byte[256]");
    inputMsg.add(createRandomArray(512), "byte[512]");
    
    inputMsg.add(createRandomArray(28), "byte[28]");
    
    FudgeMsg outputMsg = cycleMessage(inputMsg);
    assertAllFieldsMatch(inputMsg, outputMsg);
  }

  // REVIEW kirk 2009-08-21 -- This should be moved to a utility class.
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
        if(expectedField.getValue() instanceof byte[]) {
          assertArraysMatch((byte[]) expectedField.getValue(), (byte[])actualField.getValue());
        } else if(expectedField.getValue() instanceof short[]) {
          assertArraysMatch((short[]) expectedField.getValue(), (short[])actualField.getValue());
        } else if(expectedField.getValue() instanceof int[]) {
          assertArraysMatch((int[]) expectedField.getValue(), (int[])actualField.getValue());
        } else if(expectedField.getValue() instanceof long[]) {
          assertArraysMatch((long[]) expectedField.getValue(), (long[])actualField.getValue());
        } else if(expectedField.getValue() instanceof float[]) {
          assertArraysMatch((float[]) expectedField.getValue(), (float[])actualField.getValue());
        } else if(expectedField.getValue() instanceof double[]) {
          assertArraysMatch((double[]) expectedField.getValue(), (double[])actualField.getValue());
        }
      } else if(expectedField.getValue() instanceof FudgeMsg) {
        assertTrue(actualField.getValue() instanceof FudgeMsg);
        assertAllFieldsMatch((FudgeMsg) expectedField.getValue(),
            (FudgeMsg) actualField.getValue());
      } else if(expectedField.getValue() instanceof UnknownFudgeFieldValue) {
        assertTrue(actualField.getValue() instanceof UnknownFudgeFieldValue);
        UnknownFudgeFieldValue expectedValue = (UnknownFudgeFieldValue) expectedField.getValue();
        UnknownFudgeFieldValue actualValue = (UnknownFudgeFieldValue) actualField.getValue();
        assertEquals(expectedField.getType().getTypeId(), actualField.getType().getTypeId());
        assertEquals(expectedValue.getType().getTypeId(), actualField.getType().getTypeId());
        assertArraysMatch(expectedValue.getContents(), actualValue.getContents());
      } else {
        assertEquals(expectedField.getValue(), actualField.getValue());
      }
    }
    assertFalse(actualIter.hasNext());
  }
  
  // TODO kirk 2009-09-04 -- These belong in a utility class.
  private static void assertArraysMatch(double[] expected, double[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i],actual[i], 0.0);
    }
  }

  private static void assertArraysMatch(float[] expected, float[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i],actual[i], 0.0);
    }
  }

  private static void assertArraysMatch(long[] expected, long[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  private static void assertArraysMatch(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  private static void assertArraysMatch(short[] expected, short[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  private static void assertArraysMatch(byte[] expected, byte[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  protected static FudgeMsg cycleMessage(FudgeMsg msg) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    FudgeStreamEncoder.writeMsg(dos, msg);
    
    byte[] content = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(content);
    DataInputStream dis = new DataInputStream(bais);
    FudgeMsgEnvelope outputMsgEnvelope = FudgeStreamDecoder.readMsg(dis);
    assertNotNull(outputMsgEnvelope);
    assertNotNull(outputMsgEnvelope.getMessage());
    return outputMsgEnvelope.getMessage();
  }

}
