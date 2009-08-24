/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.fudge.taxon.FudgeTaxonomy;
import com.opengamma.fudge.taxon.ImmutableMapTaxonomyResolver;
import com.opengamma.fudge.taxon.MapFudgeTaxonomy;

/**
 * 
 *
 * @author kirk
 */
public class FudgeContextTest {
  private static final short[] ORDINALS = new short[] {5, 14, 928, 74}; 
  private static final String[] NAMES = new String[] {"Kirk", "Wylie", "Jim", "Moores"}; 

  @Test
  public void allNamesCodecNoTaxonomy() {
    FudgeMsg inputMsg = FudgeMsgTest.createMessageAllNames();
    FudgeContext context = new FudgeContext();
    FudgeMsg outputMsg = cycleMessage(inputMsg, context, null);
    
    assertNotNull(outputMsg);
    
    FudgeMsgCodecTest.assertAllFieldsMatch(inputMsg, outputMsg);
  }
  
  @Test
  public void allNamesCodecWithTaxonomy() {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add("value1", NAMES[0]);
    inputMsg.add("value2", NAMES[1]);
    inputMsg.add("value3", NAMES[2]);
    inputMsg.add("value4", NAMES[3]);
    
    FudgeContext context = new FudgeContext();
    Map<Short, FudgeTaxonomy> resolverMap = new HashMap<Short, FudgeTaxonomy>();
    resolverMap.put((short)45, new MapFudgeTaxonomy(ORDINALS, NAMES));
    context.setTaxonomyResolver(new ImmutableMapTaxonomyResolver(resolverMap));
    
    FudgeMsg outputMsg = cycleMessage(inputMsg, context, (short)45);
    assertEquals("value1", outputMsg.getString(NAMES[0]));
    assertEquals("value1", outputMsg.getString(ORDINALS[0]));
    assertEquals("value2", outputMsg.getString(NAMES[1]));
    assertEquals("value2", outputMsg.getString(ORDINALS[1]));
    assertEquals("value3", outputMsg.getString(NAMES[2]));
    assertEquals("value3", outputMsg.getString(ORDINALS[2]));
    assertEquals("value4", outputMsg.getString(NAMES[3]));
    assertEquals("value4", outputMsg.getString(ORDINALS[3]));
  }

  @Test
  public void allOrdinalsCodecWithTaxonomy() {
    FudgeMsg inputMsg = new FudgeMsg();
    inputMsg.add("value1", ORDINALS[0]);
    inputMsg.add("value2", ORDINALS[1]);
    inputMsg.add("value3", ORDINALS[2]);
    inputMsg.add("value4", ORDINALS[3]);
    
    FudgeContext context = new FudgeContext();
    Map<Short, FudgeTaxonomy> resolverMap = new HashMap<Short, FudgeTaxonomy>();
    resolverMap.put((short)45, new MapFudgeTaxonomy(ORDINALS, NAMES));
    context.setTaxonomyResolver(new ImmutableMapTaxonomyResolver(resolverMap));
    
    FudgeMsg outputMsg = cycleMessage(inputMsg, context, (short)45);
    assertEquals("value1", outputMsg.getString(NAMES[0]));
    assertEquals("value1", outputMsg.getString(ORDINALS[0]));
    assertEquals("value2", outputMsg.getString(NAMES[1]));
    assertEquals("value2", outputMsg.getString(ORDINALS[1]));
    assertEquals("value3", outputMsg.getString(NAMES[2]));
    assertEquals("value3", outputMsg.getString(ORDINALS[2]));
    assertEquals("value4", outputMsg.getString(NAMES[3]));
    assertEquals("value4", outputMsg.getString(ORDINALS[3]));
  }

  /**
   * @param inputMsg
   * @param context
   * @return
   */
  private FudgeMsg cycleMessage(FudgeMsg msg, FudgeContext context, Short taxonomy) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    context.serialize(msg, taxonomy, baos);
    
    byte[] content = baos.toByteArray();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(content);
    FudgeMsg outputMsg = context.deserialize(bais);
    return outputMsg;
  }
  
}
