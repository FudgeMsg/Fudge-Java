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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 *
 * @author Kirk Wylie
 */
public class FudgeFieldPrefixCodecTest {

  /**
   * 
   */
  @Test
  public void fieldPrefixComposition() {
    assertEquals(0x20, FudgeFieldPrefixCodec.composeFieldPrefix(false, 10, false, false));
    assertEquals(0x40, FudgeFieldPrefixCodec.composeFieldPrefix(false, 1024, false, false));
    assertEquals(0x60, FudgeFieldPrefixCodec.composeFieldPrefix(false, Short.MAX_VALUE + 1000, false, false));
    assertEquals(0x98, FudgeFieldPrefixCodec.composeFieldPrefix(true, 0, true, true));
  }
  
  /**
   * 
   */
  @Test
  public void hasNameChecks() {
    assertFalse(FudgeFieldPrefixCodec.hasName(0x20));
    assertTrue(FudgeFieldPrefixCodec.hasName(0x98));
  }
  
  /**
   * 
   */
  @Test
  public void fixedWidthChecks() {
    assertFalse(FudgeFieldPrefixCodec.isFixedWidth(0x20));
    assertTrue(FudgeFieldPrefixCodec.isFixedWidth(0x98));
  }
  
  /**
   * 
   */
  @Test
  public void hasOrdinalChecks() {
    assertFalse(FudgeFieldPrefixCodec.hasOrdinal(0x20));
    assertTrue(FudgeFieldPrefixCodec.hasOrdinal(0x98));
  }
  
  /**
   * 
   */
  @Test
  public void varWidthSizeChecks() {
    assertEquals(0, FudgeFieldPrefixCodec.getFieldWidthByteCount(0x98));
    assertEquals(1, FudgeFieldPrefixCodec.getFieldWidthByteCount(0x20));
    assertEquals(2, FudgeFieldPrefixCodec.getFieldWidthByteCount(0x40));
    assertEquals(4, FudgeFieldPrefixCodec.getFieldWidthByteCount(0x60));
  }

}
