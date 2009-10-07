/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

/**
 * This saves (and subsequently reloads) data files containing the binary fudge representation of the messages.
 *
 * @author jim
 */
public class FudgeInteropTest {
  private final Random _random = new Random();
  
  @Test
  public void allNames() throws IOException {
    FudgeMsg inputMsg = FudgeMsgTest.createMessageAllNames();
    FudgeMsg outputMsg = cycleMessage(inputMsg, "allNames.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }

  @Test
  public void allOrdinals() throws IOException {
    FudgeMsg inputMsg = FudgeMsgTest.createMessageAllOrdinals();
    FudgeMsg outputMsg = cycleMessage(inputMsg, "allOrdinals.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }
  
  @Test
  public void variableWidthColumnSizes() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add(new byte[100], "100");
    inputMsg.add(new byte[1000], "1000");
    inputMsg.add(new byte[100000], "10000");

    FudgeMsg outputMsg = cycleMessage(inputMsg, "variableWidthColumnSizes.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
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

    FudgeMsg outputMsg = cycleMessage(inputMsg, "subMsg.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void unknown() throws IOException {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add(new UnknownFudgeFieldValue(new byte[10], FudgeTypeDictionary.INSTANCE.getUnknownType(200)), "unknown");
    FudgeMsg outputMsg = cycleMessage(inputMsg, "unknown.dat");
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  protected byte[] createRandomArray(int length) {
    byte[] bytes = new byte[length];
    for (int i=0; i<length; i++) {
      bytes[i] = (byte)i;
    }
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
    
    FudgeMsg outputMsg = cycleMessage(inputMsg, "fixedWidthByteArrays.dat");
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  protected static FudgeMsg cycleMessage(FudgeMsg msg, String filename) throws IOException {
    saveMessage(msg, filename);
    return loadMessage(filename);
  }

  protected static void saveMessage(FudgeMsg msg, String filename) throws IOException {
    String interopDir = System.getProperty("InteropDir");
    String fullPath;
    if (interopDir != null) {
      fullPath = interopDir + File.pathSeparator + filename;
    } else {
      fullPath = filename; // fall back to current directory.
    }
    FileOutputStream stream = new FileOutputStream(fullPath);
    DataOutputStream dos = new DataOutputStream(stream);
    FudgeStreamEncoder.writeMsg(dos, msg);
  }
  
  protected static FudgeMsg loadMessage(String filename) throws IOException {
    String interopDir = System.getProperty("InteropDir");
    String fullPath;
    if (interopDir != null) {
      fullPath = interopDir + File.pathSeparator + filename;
    } else {
      fullPath = filename; // fall back to current directory.
    }
    FileInputStream stream = new FileInputStream(fullPath);
    DataInputStream dis = new DataInputStream(stream);
    FudgeMsgEnvelope outputMsgEnvelope = FudgeStreamDecoder.readMsg(dis);
    assertNotNull(outputMsgEnvelope);
    assertNotNull(outputMsgEnvelope.getMessage());
    return outputMsgEnvelope.getMessage();
  }
}
