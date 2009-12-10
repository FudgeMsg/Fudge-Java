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

package org.fudgemsg.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeStreamReader;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.StaticTransientBean;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectStreamReaderTest {
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  @Test
  public void simpleBean() {
    FudgeObjectStreamReader reader = new FudgeObjectStreamReader();
    byte[] msgBytes = s_fudgeContext.toByteArray(ObjectMappingTestUtil.constructSimpleMessage(s_fudgeContext));
    ByteArrayInputStream bais = new ByteArrayInputStream(msgBytes);
    FudgeStreamReader streamReader = s_fudgeContext.allocateReader(bais);
    Object obj = reader.read(SimpleBean.class, streamReader);
    assertNotNull(obj);
    assertTrue(obj instanceof SimpleBean);
    SimpleBean simple = (SimpleBean) obj;
    assertEquals("Kirk Wylie", simple.getFieldOne());
    assertEquals(98, simple.getFieldThree());
    
    @SuppressWarnings("unchecked")
    Map map = simple.getFieldFour();
    assertNotNull(map);
    assertEquals(2, map.size());
    assertEquals("Wrote This Test", map.get("Kirk Wylie"));
    assertEquals(new Byte((byte)42), map.get("Life, Universe, and Everything"));
    
    @SuppressWarnings("unchecked")
    List list = simple.getFieldFive();
    assertNotNull(list);
    assertEquals(3, list.size());
    assertEquals("Kirk Wylie", list.get(0));
    assertEquals("Yan Tordoff", list.get(1));
    assertEquals("Jim Moores", list.get(2));
    
    simple = simple.getFieldTwo();
    assertNotNull(simple);
    assertNull(simple.getFieldOne());
    assertEquals(99999, simple.getFieldThree());
  }
  
  @Test
  public void staticAndTransient() {
    FudgeObjectStreamReader reader = new FudgeObjectStreamReader();
    FudgeMsg msg = s_fudgeContext.newMessage();
    msg.add("s_static", 9999);
    msg.add("static", 9988);
    msg.add("transient", "Not Transient 1");
    msg.add("_transient", "Not Transient 2");
    byte[] msgBytes = s_fudgeContext.toByteArray(msg);
    ByteArrayInputStream bais = new ByteArrayInputStream(msgBytes);
    FudgeStreamReader streamReader = s_fudgeContext.allocateReader(bais);
    Object obj = reader.read(StaticTransientBean.class, streamReader);
    s_fudgeContext.releaseReader(streamReader);
    StaticTransientBean staticTransientBean = (StaticTransientBean) obj;
    assertEquals(92, StaticTransientBean.s_static);
    assertEquals("Transient", staticTransientBean._transient);
  }

}
