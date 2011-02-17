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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeFieldContainer;

/**
 * 
 *
 * @author Kirk Wylie
 */
public class ObjectMappingTestUtil {

  /**
   * 
   */
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
  
  /**
   * @param fudgeContext [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeFieldContainer constructSimpleMessage(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add("fieldOne", "Kirk Wylie");
    msg.add("fieldThree", 98);
    
    MutableFudgeFieldContainer subMsg = fudgeContext.newMessage();
    subMsg.add("fieldThree", 99999);
    msg.add("fieldTwo", subMsg);
    
    subMsg = fudgeContext.newMessage();
    subMsg.add (1, "Kirk Wylie");
    subMsg.add (2, "Wrote This Test");
    subMsg.add (1, "Life, Universe, and Everything");
    subMsg.add (2, 42);
    msg.add("fieldFour", subMsg);
    
    subMsg = fudgeContext.newMessage ();
    subMsg.add (1, "Kirk Wylie");
    subMsg.add (1, "Yan Tordoff");
    subMsg.add (1, "Jim Moores");
    msg.add ("fieldFive", subMsg);
    
    return msg;
  }
  
  /**
   * @return [documentation not available]
   */
  @SuppressWarnings("unchecked")
  public static SimpleBean constructSimpleBean() {
    SimpleBean simpleBean = new SimpleBean();
    simpleBean.setFieldOne("Kirk Wylie");
    simpleBean.setFieldThree(98);
    SimpleBean subBean = new SimpleBean();
    simpleBean.setFieldTwo(subBean);
    subBean.setFieldThree(99999);
    Map map = new HashMap();
    map.put("Kirk Wylie", "Wrote This Test");
    map.put("Life, Universe, and Everything", 42);
    simpleBean.setFieldFour(map);
    List list = new ArrayList();
    list.add("Kirk Wylie");
    list.add("Yan Tordoff");
    list.add("Jim Moores");
    simpleBean.setFieldFive(list);
    return simpleBean;
  }
  
  /**
   * 
   */
  public static class StaticTransientBean {
    
    /**
     * 
     */
    public static int s_static = 92;
    
    /**
     * 
     */
    public transient String _transient = "Transient";
    
    /**
     * @return [documentation not available]
     */
    public static int getStatic () {
      return s_static;
    }
    
    /**
     * @param s [documentation not available]
     */
    public static void setStatic (final int s) {
      s_static = s;
    }
    
    /**
     * @return [documentation not available]
     */
    @FudgeTransient
    public String getTransient () {
      return _transient;
    }
    
    /**
     * @param value [documentation not available]
     */
    @FudgeTransient
    public void setTransient (final String value) {
      _transient = value;
    }
    
  }
  
  /**
   * 
   */
  public static class SetBean {
    private Set<String> _strings;

    /**
     * @return the strings [documentation not available]
     */
    public Set<String> getStrings() {
      return _strings;
    }

    /**
     * @param strings the strings to set [documentation not available]
     */
    public void setStrings(Set<String> strings) {
      _strings = strings;
    }
  }

  /**
   * @param fudgeContext [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeFieldContainer constructSetMessage(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add("strings", "Kirk Wylie");
    msg.add("strings", "Yomi Ayodele");
    msg.add("strings", "Yan Tordoff");
    return msg;
  }
  
  /**
   * @return [documentation not available]
   */
  public static SetBean constructSetBean() {
    SetBean setBean = new SetBean();
    Set<String> strings = new HashSet<String>();
    strings.add("Kirk Wylie");
    strings.add("Yomi Ayodele");
    strings.add("Yan Tordoff");
    
    return setBean;
  }
  
  /**
   * 
   */
  public static class MappedNameBean {
    private String _fieldOne;
    private String _fieldTwo;
    private String _fieldThree;
    private String _fieldFour;
    /**
     * @param fieldOne [documentation not available]
     */
    public void setFieldOne (final String fieldOne) {
      _fieldOne = fieldOne;
    }
    /**
     * @return [documentation not available]
     */
    @FudgeFieldName ("foo")
    public String getFieldOne () {
      return _fieldOne;
    }
    /**
     * @param fieldTwo [documentation not available]
     */
    public void setFieldTwo (final String fieldTwo) {
      _fieldTwo = fieldTwo;
    }
    /**
     * @return [documentation not available]
     */
    @FudgeFieldName ("bar")
    public String getFieldTwo () {
      return _fieldTwo;
    }
    /**
     * @param fieldThree [documentation not available]
     */
    public void setFieldThree (final String fieldThree) {
      _fieldThree = fieldThree;
    }
    /**
     * @return [documentation not available]
     */
    @FudgeFieldOrdinal (99)
    public String getFieldThree () {
      return _fieldThree;
    }
    /**
     * @param fieldFour [documentation not available]
     */
    public void setFieldFour (final String fieldFour) {
      _fieldFour = fieldFour;
    }
    /**
     * @return [documentation not available]
     */
    @FudgeFieldOrdinal (value = 100, noFieldName = true)
    public String getFieldFour () {
      return _fieldFour;
    }
  }
}
