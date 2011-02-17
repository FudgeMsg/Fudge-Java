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

import static org.junit.Assert.assertNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.test.FudgeUtils;
import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeTime;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * This saves (and subsequently reloads) data files containing the binary fudge representation of the messages.
 *
 * @author Jim Moores
 */
public class FudgeInteropTest {
  private static final boolean LEAVE_FILES_IN_PLACE = false;
  private static Set<File> s_filesToRemove = new HashSet<File>();
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  /**
   *
   */
  @AfterClass
  public static void removeFiles() {
    if(!LEAVE_FILES_IN_PLACE) {
      for(File f : s_filesToRemove) {
        f.delete();
      }
    }
    s_filesToRemove.clear();
  }
  
  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void allNames() throws IOException {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "allNames.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }

  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void allOrdinals() throws IOException {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "allOrdinals.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }
  
  /**
   * @param fudgeContext [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeFieldContainer createVariableWidthColumnSizes(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer inputMsg = fudgeContext.newMessage();
    inputMsg.add("100", new byte[100]);
    inputMsg.add("1000", new byte[1000]);
    inputMsg.add("10000", new byte[100000]);
    return inputMsg;
  }
  
  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void variableWidthColumnSizes() throws IOException {
    FudgeFieldContainer inputMsg = createVariableWidthColumnSizes(s_fudgeContext);

    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "variableWidthColumnSizes.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void subMsg() throws IOException {
    FudgeFieldContainer inputMsg = StandardFudgeMessages.createMessageWithSubMsgs(s_fudgeContext);

    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "subMsg.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * @param fudgeContext [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeFieldContainer createUnknown(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer inputMsg = fudgeContext.newMessage();
    inputMsg.add("unknown", new UnknownFudgeFieldValue(new byte[10], fudgeContext.getTypeDictionary ().getUnknownType(200)));
    return inputMsg;
  }
  
  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void unknown() throws IOException {
    MutableFudgeFieldContainer inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("unknown", new UnknownFudgeFieldValue(new byte[10], s_fudgeContext.getTypeDictionary ().getUnknownType(200)));
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "unknown.dat");
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  /**
   * @param length [documentation not available]
   * @return [documentation not available]
   */
  protected static byte[] createPopulatedArray(int length) {
    byte[] bytes = new byte[length];
    for (int i=0; i<length; i++) {
      bytes[i] = (byte)i;
    }
    return bytes;
  }
  
  /**
   * @param fudgeContext [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeFieldContainer createFixedWidthByteArrayMsg(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer inputMsg = fudgeContext.newMessage();
    inputMsg.add("byte[4]", createPopulatedArray(4));
    inputMsg.add("byte[8]", createPopulatedArray(8));
    inputMsg.add("byte[16]", createPopulatedArray(16));
    inputMsg.add("byte[20]", createPopulatedArray(20));
    inputMsg.add("byte[32]", createPopulatedArray(32));
    inputMsg.add("byte[64]", createPopulatedArray(64));
    inputMsg.add("byte[128]", createPopulatedArray(128));
    inputMsg.add("byte[256]", createPopulatedArray(256));
    inputMsg.add("byte[512]", createPopulatedArray(512));
    
    inputMsg.add("byte[28]", createPopulatedArray(28));
    return inputMsg;
  }

  /**
   * @throws IOException [documentation not available]
   */
  @Test
  public void fixedWidthByteArrays() throws IOException {
    FudgeFieldContainer inputMsg = createFixedWidthByteArrayMsg(s_fudgeContext);
    
    FudgeFieldContainer outputMsg = cycleMessage(inputMsg, "fixedWidthByteArrays.dat");
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  public static FudgeFieldContainer createDateTimes (final FudgeContext fudgeContext) {
    MutableFudgeFieldContainer inputMsg = fudgeContext.newMessage ();
    inputMsg.add ("date-Year", new FudgeDate (2010));
    inputMsg.add ("date-Month", new FudgeDate (2010, 3));
    inputMsg.add ("date-Day", new FudgeDate (2010, 3, 4));
    inputMsg.add ("time-Hour-UTC", new FudgeTime (DateTimeAccuracy.HOUR, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Minute-UTC", new FudgeTime (DateTimeAccuracy.MINUTE, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Second-UTC", new FudgeTime (DateTimeAccuracy.SECOND, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Milli-UTC", new FudgeTime (DateTimeAccuracy.MILLISECOND, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Micro-UTC", new FudgeTime (DateTimeAccuracy.MICROSECOND, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Nano-UTC", new FudgeTime (DateTimeAccuracy.NANOSECOND, 0, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Nano", new FudgeTime (DateTimeAccuracy.NANOSECOND, -128, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("time-Nano-+1h", new FudgeTime (DateTimeAccuracy.NANOSECOND, 4, 11 * 3600 + 12 * 60 + 13, 987654321));
    inputMsg.add ("datetime-Millenia", new FudgeDateTime (new FudgeDate (1000), new FudgeTime (DateTimeAccuracy.MILLENIUM, -128, 0, 0)));
    inputMsg.add ("datetime-Century", new FudgeDateTime (new FudgeDate (1900), new FudgeTime (DateTimeAccuracy.CENTURY, -128, 0, 0)));
    inputMsg.add ("datetime-Nano-UTC", new FudgeDateTime (new FudgeDate (2010, 3, 4), new FudgeTime (DateTimeAccuracy.NANOSECOND, 0, 11 * 3600 + 12 * 60 + 13, 987654321)));
    inputMsg.add ("datetime-Nano", new FudgeDateTime (new FudgeDate (2010, 3, 4), new FudgeTime (DateTimeAccuracy.NANOSECOND, -128, 11 * 3600 + 12 * 60 + 13, 987654321)));
    inputMsg.add ("datetime-Nano-+1h", new FudgeDateTime (new FudgeDate (2010, 3, 4), new FudgeTime (DateTimeAccuracy.NANOSECOND, 4, 11 * 3600 + 12 * 60 + 13, 987654321)));
    return inputMsg;
  }
  
  @Test
  public void dateTypeMessage () throws IOException {
    FudgeFieldContainer inputMsg = createDateTimes (s_fudgeContext);
    FudgeFieldContainer outputMsg = cycleMessage (inputMsg, "dateTimes.dat");
    FudgeUtils.assertAllFieldsMatch (inputMsg, outputMsg);
  }
  
  /**
   * @param msg [documentation not available]
   * @param filename [documentation not available]
   * @return [documentation not available]
   * @throws IOException [documentation not available]
   */
  protected static FudgeFieldContainer cycleMessage(FudgeFieldContainer msg, String filename) throws IOException {
    saveMessage(msg, filename);
    return loadMessage(filename);
  }

  /**
   * @param msg [documentation not available]
   * @param filename [documentation not available]
   * @throws IOException [documentation not available]
   */
  protected static void saveMessage(FudgeFieldContainer msg, String filename) throws IOException {
    String interopDir = System.getProperty("InteropDir");
    String fullPath;
    if (interopDir != null) {
      fullPath = interopDir + File.pathSeparator + filename;
    } else {
      fullPath = filename; // fall back to current directory.
    }
    System.out.println("Creating file " + fullPath);
    s_filesToRemove.add(new File(fullPath));
    FileOutputStream stream = new FileOutputStream(fullPath);
    DataOutputStream dos = new DataOutputStream(stream);
    s_fudgeContext.serialize(msg, dos);
  }
  
  /**
   * @param filename [documentation not available]
   * @return [documentation not available]
   * @throws IOException [documentation not available]
   */
  protected static FudgeFieldContainer loadMessage(String filename) throws IOException {
    String interopDir = System.getProperty("InteropDir");
    String fullPath;
    if (interopDir != null) {
      fullPath = interopDir + File.pathSeparator + filename;
    } else {
      fullPath = filename; // fall back to current directory.
    }
    FileInputStream stream = new FileInputStream(fullPath);
    DataInputStream dis = new DataInputStream(stream);
    FudgeMsgEnvelope outputMsgEnvelope = (new FudgeContext()).deserialize(dis);
    assertNotNull(outputMsgEnvelope);
    assertNotNull(outputMsgEnvelope.getMessage ());
    return outputMsgEnvelope.getMessage ();
  }
}
