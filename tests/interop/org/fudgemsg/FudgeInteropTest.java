/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
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

import org.junit.AfterClass;
import org.junit.Test;

/**
 * This saves (and subsequently reloads) data files containing the binary fudge representation of the messages.
 *
 * @author jim
 */
public class FudgeInteropTest {
  private static final boolean LEAVE_FILES_IN_PLACE = false;
  private static Set<File> s_filesToRemove = new HashSet<File>();
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  @AfterClass
  public static void removeFiles() {
    if(!LEAVE_FILES_IN_PLACE) {
      for(File f : s_filesToRemove) {
        f.delete();
      }
    }
    s_filesToRemove.clear();
  }
  
  @Test
  public void allNames() throws IOException {
    FudgeMsg inputMsg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    FudgeMsg outputMsg = cycleMessage(inputMsg, "allNames.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }

  @Test
  public void allOrdinals() throws IOException {
    FudgeMsg inputMsg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    FudgeMsg outputMsg = cycleMessage(inputMsg, "allOrdinals.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
    
  }
  
  @Test
  public void variableWidthColumnSizes() throws IOException {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("100", new byte[100]);
    inputMsg.add("1000", new byte[1000]);
    inputMsg.add("10000", new byte[100000]);

    FudgeMsg outputMsg = cycleMessage(inputMsg, "variableWidthColumnSizes.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void subMsg() throws IOException {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
    FudgeMsg sub1 = s_fudgeContext.newMessage();
    sub1.add("bibble", "fibble");
    sub1.add(827, "Blibble");
    FudgeMsg sub2 = s_fudgeContext.newMessage();
    sub2.add("bibble9", 9837438);
    sub2.add(828, 82.77f);
    inputMsg.add("sub1", sub1);
    inputMsg.add("sub2", sub2);

    FudgeMsg outputMsg = cycleMessage(inputMsg, "subMsg.dat");
    
    assertNotNull(outputMsg);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void unknown() throws IOException {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
    inputMsg.add("unknown", new UnknownFudgeFieldValue(new byte[10], FudgeTypeDictionary.INSTANCE.getUnknownType(200)));
    FudgeMsg outputMsg = cycleMessage(inputMsg, "unknown.dat");
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  protected static byte[] createPopulatedArray(int length) {
    byte[] bytes = new byte[length];
    for (int i=0; i<length; i++) {
      bytes[i] = (byte)i;
    }
    return bytes;
  }

  @Test
  public void fixedWidthByteArrays() throws IOException {
    FudgeMsg inputMsg = s_fudgeContext.newMessage();
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
    s_filesToRemove.add(new File(fullPath));
    FileOutputStream stream = new FileOutputStream(fullPath);
    DataOutputStream dos = new DataOutputStream(stream);
    s_fudgeContext.serialize(msg, dos);
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
    FudgeMsgEnvelope outputMsgEnvelope = (new FudgeContext()).deserialize(dis);
    assertNotNull(outputMsgEnvelope);
    assertNotNull(outputMsgEnvelope.getMessage());
    return outputMsgEnvelope.getMessage();
  }
}
