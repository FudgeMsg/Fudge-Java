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

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * A very short test just to establish some simple performance metrics
 * for Fudge encoding compared with Java Serialization. 
 *
 * @author Kirk Wylie
 */
public class ShortPerformanceTest {
  private static final int HOT_SPOT_WARMUP_CYCLES = 50000;
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  /**
   * @throws Exception [documentation not available]
   */
  @BeforeClass
  public static void warmUpHotSpot() throws Exception {
    System.out.println("Fudge size, Names Only: " + fudgeCycle(true, false));
    System.out.println("Fudge size, Ordinals Only: " + fudgeCycle(false, true));
    System.out.println("Fudge size, Names And Ordinals: " + fudgeCycle(true, true));
    System.out.println("Fudge size, Object Mapping: " + fudgeObjectMappingCycle());
    System.out.println("Serialization size: " + serializationCycle());
    for(int i = 0; i < HOT_SPOT_WARMUP_CYCLES; i++) {
      fudgeCycle(true, false);
      fudgeCycle(false, true);
      fudgeCycle(true, true);
      fudgeObjectMappingCycle();
      serializationCycle();
    }
  }
  
  /**
   * @throws Exception [documentation not available]
   */
  @Test
  public void performanceVersusSerialization10000Cycles() throws Exception {
    performanceVersusSerialization(10000);
  }
  
  /**
   * @throws Exception [documentation not available]
   */
  @Test
  @Ignore("This is just for really large tests")
  public void performanceVersusSerialization1000000Cycles() throws Exception {
    performanceVersusSerialization(1000000);
  }
  
  private static void performanceVersusSerialization(int nCycles) throws Exception {
    // If you're running the tests from a code coverage framework or anything else that disrupts the
    // performance of the library, set system property "disable.instrumentation.sensitive" to "true".
    final String disableInstrumentationSensitive = System.getProperty("disable.instrumentation.sensitive");
    assumeTrue((disableInstrumentationSensitive == null) || !"true".equalsIgnoreCase(disableInstrumentationSensitive));

    long startTime = 0;
    long endTime = 0;
    
    System.out.println("Starting Fudge names only.");
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaNamesOnly = endTime - startTime;
    double fudgeSplitNamesOnly = convertToCyclesPerSecond(nCycles, fudgeDeltaNamesOnly);
    System.gc();
    
    System.out.println("Starting Fudge ordinals only.");
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(false, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaOrdinalsOnly = endTime - startTime;
    double fudgeSplitOrdinalsOnly = convertToCyclesPerSecond(nCycles, fudgeDeltaOrdinalsOnly);
    System.gc();
    
    System.out.println("Starting Fudge names and ordinals.");
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaBoth = endTime - startTime;
    double fudgeSplitBoth = convertToCyclesPerSecond(nCycles, fudgeDeltaBoth);
    System.gc();
    
    System.out.println("Starting Fudge Object mapping, no taxonomy.");
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeObjectMappingCycle();
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaObjectNoTaxonomy = endTime - startTime;
    double fudgeSplitObjectNoTaxonomy = convertToCyclesPerSecond(nCycles, fudgeDeltaObjectNoTaxonomy);
    System.gc();
    
    System.out.println("Starting Java Serialization.");
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      serializationCycle();
    }
    endTime = System.currentTimeMillis();
    long serializationDelta = endTime - startTime;
    double serializationSplit = convertToCyclesPerSecond(nCycles, serializationDelta);
    System.gc();
    
    StringBuilder sb = new StringBuilder();
    sb.append("For ").append(nCycles).append(" cycles");
    System.out.println(sb.toString());
    
    sb = new StringBuilder();
    sb.append("Fudge Names Only ").append(fudgeDeltaNamesOnly).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Ordinals Only ").append(fudgeDeltaOrdinalsOnly).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names And Ordinals ").append(fudgeDeltaBoth).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Objects No Taxonomy ").append(fudgeDeltaObjectNoTaxonomy).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Serialization ").append(serializationDelta).append("ms");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names Only: ").append(fudgeSplitNamesOnly).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Ordinals Only: ").append(fudgeSplitOrdinalsOnly).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Names And Ordinals: ").append(fudgeSplitBoth).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Fudge Objects No Taxonomy: ").append(fudgeSplitObjectNoTaxonomy).append("cycles/sec");
    System.out.println(sb.toString());
    sb = new StringBuilder();
    sb.append("Serialization: ").append(serializationSplit).append("cycles/sec");
    System.out.println(sb.toString());
    assertTrue("Serialization faster by " + (fudgeDeltaObjectNoTaxonomy - serializationDelta) + "ms",
        serializationDelta > fudgeDeltaObjectNoTaxonomy);
  }
  
  /**
   * @param nCycles
   * @param delta
   * @return
   */
  private static double convertToCyclesPerSecond(int nCycles, long delta) {
    double fudgeSplit = (double)delta;
    fudgeSplit = fudgeSplit / nCycles;
    fudgeSplit = fudgeSplit / 1000.0;
    fudgeSplit = 1/fudgeSplit;
    return fudgeSplit;
  }
  
  private static int fudgeCycle(final boolean useNames, final boolean useOrdinals) throws Exception {
    SmallFinancialTick tick = new SmallFinancialTick();
    MutableFudgeFieldContainer msgIn = s_fudgeContext.newMessage();
    if(useNames && useOrdinals) {
      msgIn.add("ask", 1, tick.getAsk());
      msgIn.add("askVolume", 2, tick.getAskVolume());
      msgIn.add("bid", 3, tick.getBid());
      msgIn.add("bidVolume", 4, tick.getBidVolume());
      msgIn.add("ts", 5, tick.getTimestamp());
    } else if(useNames) {
      msgIn.add("ask", tick.getAsk());
      msgIn.add("askVolume", tick.getAskVolume());
      msgIn.add("bid", tick.getBid());
      msgIn.add("bidVolume", tick.getBidVolume());
      msgIn.add("ts", tick.getTimestamp());
    } else if(useOrdinals) {
      msgIn.add(1, tick.getAsk());
      msgIn.add(2, tick.getAskVolume());
      msgIn.add(3, tick.getBid());
      msgIn.add(4, tick.getBidVolume());
      msgIn.add(5, tick.getTimestamp());
    }
    byte[] data = s_fudgeContext.toByteArray(msgIn);

    FudgeFieldContainer msg = s_fudgeContext.deserialize(data).getMessage ();
    
    tick = new SmallFinancialTick();
    if(useOrdinals) {
      tick.setAsk(msg.getDouble((short)1));
      tick.setAskVolume(msg.getDouble((short)2));
      tick.setBid(msg.getDouble((short)3));
      tick.setBidVolume(msg.getDouble((short)4));
      tick.setTimestamp(msg.getLong((short)5));
    } else if(useNames) {
      tick.setAsk(msg.getDouble("ask"));
      tick.setAskVolume(msg.getDouble("askVolume"));
      tick.setBid(msg.getDouble("bid"));
      tick.setBidVolume(msg.getDouble("bidVolume"));
      tick.setTimestamp(msg.getLong("ts"));
    } else {
      throw new UnsupportedOperationException("Names or ordinals, pick at least one.");
    }
    return data.length;
  }

  private static int serializationCycle() throws Exception {
    return serializationFromMsg (serializationToMsg ());
  }
  
  private static byte[] serializationToMsg () throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    SmallFinancialTick tick = new SmallFinancialTick();
    oos.writeObject(tick);
    return baos.toByteArray();
  }
  
  private static int serializationFromMsg (final byte[] data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ObjectInputStream ois = new ObjectInputStream(bais);
    ois.readObject();
    return data.length;
  }
  
  private static int fudgeObjectMappingCycle() throws Exception {
    return fudgeObjectMappingFromMsg (fudgeObjectMappingToMsg ());
  }
  
  private static byte[] fudgeObjectMappingToMsg () throws Exception {
    SmallFinancialTick tick = new SmallFinancialTick();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FudgeObjectWriter osw = s_fudgeContext.createObjectWriter (baos);
    osw.write (tick);
    return baos.toByteArray();
  }
  
  private static int fudgeObjectMappingFromMsg (final byte[] data) throws Exception {
    FudgeObjectReader osr = s_fudgeContext.createObjectReader (new ByteArrayInputStream (data));
    osr.read(SmallFinancialTick.class);
    return data.length;
  }

  /**
   * Split the serialization cycles into two so that we can identify where the bottle neck is.
   * 
   * @throws Exception [documentation not available]
   */
  @Test
  @Ignore
  public void performanceCheck () throws Exception {
    final int nCycles = 100000;
    long start, end;
    long deltaSerialisationToMsg, deltaSerialisationFromMsg, deltaFudgeObjectMappingToMsg, deltaFudgeObjectMappingFromMsg;
    byte[] data = null;
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      data = serializationToMsg ();
    }
    end = System.currentTimeMillis ();
    deltaSerialisationToMsg = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      serializationFromMsg (data);
    }
    end = System.currentTimeMillis ();
    deltaSerialisationFromMsg = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      data = fudgeObjectMappingToMsg ();
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingToMsg = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      fudgeObjectMappingFromMsg (data);
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingFromMsg = end - start;
    System.gc ();
    
    System.out.println ("Java serialisation (enc) " + deltaSerialisationToMsg + "ms");
    System.out.println ("Java serialisation (dec) " + deltaSerialisationFromMsg + "ms");
    System.out.println ("Java serialisation " + (deltaSerialisationToMsg + deltaSerialisationFromMsg) + "ms");
    System.out.println ("Fudge serialisation (enc) " + deltaFudgeObjectMappingToMsg + "ms");
    System.out.println ("Fudge serialisation (dec) " + deltaFudgeObjectMappingFromMsg + "ms");
    System.out.println ("Fudge serialisation " + (deltaFudgeObjectMappingToMsg + deltaFudgeObjectMappingFromMsg) + "ms");
    assertTrue ("Java cycle faster by " + ((deltaFudgeObjectMappingToMsg + deltaFudgeObjectMappingFromMsg) - (deltaSerialisationToMsg + deltaSerialisationFromMsg)) + "ms", (deltaFudgeObjectMappingToMsg + deltaFudgeObjectMappingFromMsg) < (deltaSerialisationToMsg + deltaSerialisationFromMsg));
    assertTrue ("Java encoding faster by " + (deltaFudgeObjectMappingToMsg - deltaSerialisationToMsg) + "ms", deltaFudgeObjectMappingToMsg < deltaSerialisationToMsg);
    assertTrue ("Java decoding faster by " + (deltaFudgeObjectMappingFromMsg - deltaSerialisationFromMsg) + "ms", deltaFudgeObjectMappingFromMsg < deltaSerialisationFromMsg);
  }

}
