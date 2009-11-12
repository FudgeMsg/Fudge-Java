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
import java.io.DataInputStream;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeStreamReader;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectStreamParserTest {
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  @SuppressWarnings("unchecked")
  public static class SimpleBean {
    private String _fieldOne;
    private SimpleBean _fieldTwo;
    private int _fieldThree;
    private Map _fieldFour;
    private List _fieldFive;
    /**
     * @return the fieldOne
     */
    public String getFieldOne() {
      return _fieldOne;
    }
    /**
     * @param fieldOne the fieldOne to set
     */
    public void setFieldOne(String fieldOne) {
      _fieldOne = fieldOne;
    }
    /**
     * @return the fieldTwo
     */
    public SimpleBean getFieldTwo() {
      return _fieldTwo;
    }
    /**
     * @param fieldTwo the fieldTwo to set
     */
    public void setFieldTwo(SimpleBean fieldTwo) {
      _fieldTwo = fieldTwo;
    }
    /**
     * @return the fieldThree
     */
    public int getFieldThree() {
      return _fieldThree;
    }
    /**
     * @param fieldThree the fieldThree to set
     */
    public void setFieldThree(int fieldThree) {
      _fieldThree = fieldThree;
    }
    /**
     * @return the fieldFour
     */
    public Map getFieldFour() {
      return _fieldFour;
    }
    /**
     * @param fieldFour the fieldFour to set
     */
    public void setFieldFour(Map fieldFour) {
      _fieldFour = fieldFour;
    }
    /**
     * @return the fieldFive
     */
    public List getFieldFive() {
      return _fieldFive;
    }
    /**
     * @param fieldFive the fieldFive to set
     */
    public void setFieldFive(List fieldFive) {
      _fieldFive = fieldFive;
    }
  }
  
  protected static FudgeMsg constructSimpleMessage(FudgeContext fudgeContext) {
    FudgeMsg msg = fudgeContext.newMessage();
    msg.add("fieldOne", "Kirk Wylie");
    msg.add("fieldThree", 98);
    
    FudgeMsg subMsg = fudgeContext.newMessage();
    subMsg.add("fieldThree", 99999);
    msg.add("fieldTwo", subMsg);
    
    subMsg = fudgeContext.newMessage();
    subMsg.add("Kirk Wylie", "Wrote This Test");
    subMsg.add("Life, Universe, and Everything", 42);
    msg.add("fieldFour", subMsg);
    
    msg.add("fieldFive", "Kirk Wylie");
    msg.add("fieldFive", "Yan Tordoff");
    msg.add("fieldFive", "Jim Moores");
    
    return msg;
  }
  
  @Test
  public void simpleBean() {
    FudgeObjectStreamParser parser = new FudgeObjectStreamParser();
    byte[] msgBytes = s_fudgeContext.toByteArray(constructSimpleMessage(s_fudgeContext));
    ByteArrayInputStream bais = new ByteArrayInputStream(msgBytes);
    FudgeStreamReader streamReader = s_fudgeContext.allocateReader();
    streamReader.reset(new DataInputStream(bais));
    Object obj = parser.parse(SimpleBean.class, streamReader);
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

}
