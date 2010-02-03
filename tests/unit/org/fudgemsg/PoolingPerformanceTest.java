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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeDataOutputStreamWriter;
import org.fudgemsg.FudgeDataInputStreamReader;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeObjectReader;
import org.fudgemsg.mapping.FudgeObjectWriter;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test of serialisation efficiencies with and without the FudgeContext pools 
 *
 * @author Andrew
 */
public class PoolingPerformanceTest {
  private static final int HOT_SPOT_WARMUP_CYCLES = 50000;
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  @BeforeClass
  public static void warmUpHotSpot() throws Exception {
    for(int i = 0; i < HOT_SPOT_WARMUP_CYCLES; i++) {
      fudgeCycle(true, false, true);
      fudgeCycle(false, true, true);
      fudgeCycle(true, true, true);
      fudgeObjectMappingCycle(true);
      fudgeCycle(true, false, false);
      fudgeCycle(false, true, false);
      fudgeCycle(true, true, false);
      fudgeObjectMappingCycle(false);
    }
    printAndResetCounts ("hotspot warmup");
  }
  
  @Test
  public void performance10000Cycles() throws Exception {
    performance(10000);
  }
  
  @Test
  @Ignore("This is just for really large tests")
  public void performance1000000Cycles() throws Exception {
    performance(1000000);
  }
  
  private static int printAndReset (String prefix, Class<?> clazz, int count) {
    System.out.println (prefix + " " + clazz.getName () + ".s_constructions = " + count);
    return 0;
  }
  
  private static void printAndResetCounts (String prefix) {
    FudgeDataOutputStreamWriter.s_constructions = printAndReset (prefix, FudgeDataOutputStreamWriter.class, FudgeDataOutputStreamWriter.s_constructions);
    FudgeDataInputStreamReader.s_constructions = printAndReset (prefix, FudgeDataInputStreamReader.class, FudgeDataInputStreamReader.s_constructions);
    FudgeMsgReader.s_constructions = printAndReset (prefix, FudgeMsgReader.class, FudgeMsgReader.s_constructions);
    FudgeMsgWriter.s_constructions = printAndReset (prefix, FudgeMsgWriter.class, FudgeMsgWriter.s_constructions);
    FudgeObjectReader.s_constructions = printAndReset (prefix, FudgeObjectReader.class, FudgeObjectReader.s_constructions);
    FudgeObjectWriter.s_constructions = printAndReset (prefix, FudgeObjectWriter.class, FudgeObjectWriter.s_constructions);
    FudgeSerializationContext.s_constructions = printAndReset (prefix, FudgeSerializationContext.class, FudgeSerializationContext.s_constructions);
    FudgeDeserializationContext.s_constructions = printAndReset (prefix, FudgeDeserializationContext.class, FudgeDeserializationContext.s_constructions);
    System.gc ();
  }
  
  private static void performance(int nCycles) throws Exception {
    long startTime = 0;
    long endTime = 0;
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, false, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaNamesOnlyPooled = endTime - startTime;
    double fudgeSplitNamesOnlyPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaNamesOnlyPooled);
    printAndResetCounts ("names only pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(false, true, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaOrdinalsOnlyPooled = endTime - startTime;
    double fudgeSplitOrdinalsOnlyPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaOrdinalsOnlyPooled);
    printAndResetCounts ("ordinals only pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, true, true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaBothPooled = endTime - startTime;
    double fudgeSplitBothPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaBothPooled);
    printAndResetCounts ("names and ordinals pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeObjectMappingCycle(true);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaObjectNoTaxonomyPooled = endTime - startTime;
    double fudgeSplitObjectNoTaxonomyPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaObjectNoTaxonomyPooled);
    printAndResetCounts ("objects no taxonomy");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, false, false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaNamesOnlyNonPooled = endTime - startTime;
    double fudgeSplitNamesOnlyNonPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaNamesOnlyNonPooled);
    printAndResetCounts ("names only non-pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(false, true, false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaOrdinalsOnlyNonPooled = endTime - startTime;
    double fudgeSplitOrdinalsOnlyNonPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaOrdinalsOnlyNonPooled);
    printAndResetCounts ("ordinals only non-pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeCycle(true, true, false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaBothNonPooled = endTime - startTime;
    double fudgeSplitBothNonPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaBothNonPooled);
    printAndResetCounts ("names and ordinals non-pooled");
    
    startTime = System.currentTimeMillis();
    for(int i = 0; i < nCycles; i++) {
      fudgeObjectMappingCycle(false);
    }
    endTime = System.currentTimeMillis();
    long fudgeDeltaObjectNoTaxonomyNonPooled = endTime - startTime;
    double fudgeSplitObjectNoTaxonomyNonPooled = convertToCyclesPerSecond(nCycles, fudgeDeltaObjectNoTaxonomyNonPooled);
    printAndResetCounts ("objects no taxonomy");
    
    System.out.println ("For " + nCycles + " cycles (pooled)");
    System.out.println ("\tFudge names only " + fudgeDeltaNamesOnlyPooled + "ms (" + fudgeSplitNamesOnlyPooled + " cycles/sec)");
    System.out.println ("\tFudge ordinals only " + fudgeDeltaOrdinalsOnlyPooled + "ms (" + fudgeSplitOrdinalsOnlyPooled + " cycles/sec)");
    System.out.println ("\tFudge names and ordinals " + fudgeDeltaBothPooled + "ms (" + fudgeSplitBothPooled + " cycles/sec)");
    System.out.println ("\tFudge objects no taxonomy " + fudgeDeltaObjectNoTaxonomyPooled + "ms (" + fudgeSplitObjectNoTaxonomyPooled + " cycles/sec)");
    
    System.out.println ("For " + nCycles + " cycles (non-pooled)");
    System.out.println ("\tFudge names only " + fudgeDeltaNamesOnlyNonPooled + "ms (" + fudgeSplitNamesOnlyNonPooled + " cycles/sec)");
    System.out.println ("\tFudge ordinals only " + fudgeDeltaOrdinalsOnlyNonPooled + "ms (" + fudgeSplitOrdinalsOnlyNonPooled + " cycles/sec)");
    System.out.println ("\tFudge names and ordinals " + fudgeDeltaBothNonPooled + "ms (" + fudgeSplitBothNonPooled + " cycles/sec)");
    System.out.println ("\tFudge objects no taxonomy " + fudgeDeltaObjectNoTaxonomyNonPooled + "ms (" + fudgeSplitObjectNoTaxonomyNonPooled + " cycles/sec)");

    assertTrue ("Fudge names only non-pooled faster", fudgeDeltaNamesOnlyPooled < fudgeDeltaNamesOnlyNonPooled);
    assertTrue ("Fudge ordinals only non-pooled faster", fudgeDeltaOrdinalsOnlyPooled < fudgeDeltaOrdinalsOnlyNonPooled);
    assertTrue ("Fudge names and ordinals non-pooled faster", fudgeDeltaBothPooled < fudgeDeltaBothNonPooled);
    assertTrue ("Fudge objects no taxonomy non-pooled faster", fudgeDeltaObjectNoTaxonomyPooled < fudgeDeltaObjectNoTaxonomyNonPooled);
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
  
  private static SmallFinancialTick fudgeCycle(final boolean useNames, final boolean useOrdinals, final boolean pooled) throws Exception {
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
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    if (pooled) {
      final FudgeMsgWriter w = s_fudgeContext.allocateMessageWriter (s_fudgeContext.allocateWriter (baos));
      w.writeMessage (msgIn, 0);
      s_fudgeContext.releaseMessageWriter (w);
    } else {
      final FudgeMsgWriter w = new FudgeMsgWriter (new FudgeDataOutputStreamWriter (s_fudgeContext, baos));
      w.writeMessage (msgIn, 0);
      w.closeWithoutRelease ();
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream (baos.toByteArray ());
    final FudgeFieldContainer msg;
    if (pooled) {
      final FudgeMsgReader r = s_fudgeContext.allocateMessageReader (s_fudgeContext.allocateReader (bais));
      msg = r.nextMessage ();
      s_fudgeContext.releaseMessageReader (r);
    } else {
      final FudgeMsgReader r = new FudgeMsgReader (new FudgeDataInputStreamReader (s_fudgeContext, bais));
      msg = r.nextMessage ();
      r.closeWithoutRelease ();
    }
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
    return tick;
  }

  private static void fudgeObjectMappingCycle(final boolean pooled) throws Exception {
    fudgeObjectMappingFromMsg (fudgeObjectMappingToMsg (pooled), pooled);
  }
  
  private static byte[] fudgeObjectMappingToMsg (final boolean pooled) throws Exception {
    SmallFinancialTick tick = new SmallFinancialTick();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    if (pooled) {
      FudgeObjectWriter osw = s_fudgeContext.allocateObjectWriter (baos);
      osw.write (tick);
      s_fudgeContext.releaseObjectWriter(osw);
    } else {
      // we don't have a closeWithoutRelease on FudgeObjectWriter, so just close the underlying
      FudgeMsgWriter fmw = new FudgeMsgWriter (new FudgeDataOutputStreamWriter (s_fudgeContext, baos));
      FudgeObjectWriter osw = new FudgeObjectWriter (fmw);
      osw.write (tick);
      fmw.closeWithoutRelease ();
    }
    return baos.toByteArray();
  }
  
  private static void fudgeObjectMappingFromMsg (final byte[] data, final boolean pooled) throws Exception {
    final ByteArrayInputStream bais = new ByteArrayInputStream (data);
    if (pooled) {
      FudgeObjectReader osr = s_fudgeContext.allocateObjectReader (bais);
      osr.read(SmallFinancialTick.class);
      s_fudgeContext.releaseObjectReader(osr);
    } else {
      // we don't have a closeWithoutRelease on FudgeObjectReader, so just close the underlying
      FudgeMsgReader fmr = new FudgeMsgReader (new FudgeDataInputStreamReader (s_fudgeContext, bais));
      FudgeObjectReader osr = new FudgeObjectReader (fmr);
      osr.read (SmallFinancialTick.class);
      fmr.closeWithoutRelease ();
    }
  }

  /**
   * Split the serialisation cycles into two so that we can identify where the bottle neck is.
   */
  @Test
  public void performanceCheck () throws Exception {
    final int nCycles = 100000;
    long start, end;
    long deltaFudgeObjectMappingToMsgPooled, deltaFudgeObjectMappingFromMsgPooled;
    long deltaFudgeObjectMappingToMsgNonPooled, deltaFudgeObjectMappingFromMsgNonPooled;
    byte[] data = null;
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      data = fudgeObjectMappingToMsg (true);
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingToMsgPooled = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      fudgeObjectMappingFromMsg (data, true);
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingFromMsgPooled = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      data = fudgeObjectMappingToMsg (false);
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingToMsgNonPooled = end - start;
    System.gc ();
    
    start = System.currentTimeMillis ();
    for (int i = 0; i < nCycles; i++) {
      fudgeObjectMappingFromMsg (data, false);
    }
    end = System.currentTimeMillis ();
    deltaFudgeObjectMappingFromMsgNonPooled = end - start;
    System.gc ();
    
    System.out.println ("Fudge serialisation (enc) pooled " + deltaFudgeObjectMappingToMsgPooled + "ms");
    System.out.println ("Fudge serialisation (dec) pooled " + deltaFudgeObjectMappingFromMsgPooled + "ms");
    System.out.println ("Fudge serialisation pooled " + (deltaFudgeObjectMappingToMsgPooled + deltaFudgeObjectMappingFromMsgPooled) + "ms");
    System.out.println ("Fudge serialisation (enc) non-pooled " + deltaFudgeObjectMappingToMsgNonPooled + "ms");
    System.out.println ("Fudge serialisation (dec) non-pooled " + deltaFudgeObjectMappingFromMsgNonPooled + "ms");
    System.out.println ("Fudge serialisation non-pooled " + (deltaFudgeObjectMappingToMsgNonPooled + deltaFudgeObjectMappingFromMsgNonPooled) + "ms");
    assertTrue ("Non-pooled faster (enc)", deltaFudgeObjectMappingToMsgPooled < deltaFudgeObjectMappingToMsgNonPooled);
    assertTrue ("Non-pooled faster (dec)", deltaFudgeObjectMappingFromMsgPooled < deltaFudgeObjectMappingFromMsgNonPooled);
  }
  
}
