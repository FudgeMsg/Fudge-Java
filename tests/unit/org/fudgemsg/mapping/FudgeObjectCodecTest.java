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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SetBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.junit.Test;

/**
 * 
 *
 * @author Kirk Wylie
 */
public class FudgeObjectCodecTest {
  
  private static void assertLooseEquals (Object a, Object b) {
    if ((a instanceof Number) && (b instanceof Number)) {
      assertEquals (a.toString (), b.toString ());
    } else {
      assertEquals (a, b);
    }
  }
  
  private static void assertMapsEqual (Map<?,?> a, Map<?,?> b) {
    if (a == b) return;
    for (Map.Entry<?,?> ae : a.entrySet ()) {
      assertEquals (true, b.containsKey (ae.getKey ()));
      assertLooseEquals (ae.getValue (), b.get (ae.getKey ()));
    }
  }
  
  /**
   *
   */
  @Test
  public void simpleBean() {
    FudgeContext fudgeContext = new FudgeContext();
    SimpleBean inputBean = ObjectMappingTestUtil.constructSimpleBean();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    fudgeContext.writeObject(inputBean, baos);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    SimpleBean resultBean = fudgeContext.readObject(SimpleBean.class, bais);
    
    assertNotNull(resultBean);
    assertEquals(inputBean.getFieldOne(), resultBean.getFieldOne());
    assertEquals(inputBean.getFieldThree(), resultBean.getFieldThree());
    assertMapsEqual(inputBean.getFieldFour(), resultBean.getFieldFour());
    assertEquals(inputBean.getFieldFive(), resultBean.getFieldFive());
    
    inputBean = inputBean.getFieldTwo();
    resultBean = resultBean.getFieldTwo();
    assertNull(resultBean.getFieldOne());
    assertEquals(inputBean.getFieldThree(), resultBean.getFieldThree());
    assertNull(resultBean.getFieldFour());
    assertNull(resultBean.getFieldFive());
    
  }

  /**
   *
   */
  @Test
  public void setBean() {
    FudgeContext fudgeContext = new FudgeContext();
    SetBean inputBean = ObjectMappingTestUtil.constructSetBean();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    fudgeContext.writeObject(inputBean, baos);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    SetBean resultBean = fudgeContext.readObject(SetBean.class, bais);
    
    assertNotNull(resultBean);
    assertEquals(inputBean.getStrings(), resultBean.getStrings());
    
  }

}
