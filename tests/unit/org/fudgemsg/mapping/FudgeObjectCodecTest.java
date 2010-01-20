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

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SetBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectCodecTest {
  
  @Test
  public void simpleBean() throws IOException {
    FudgeContext fudgeContext = new FudgeContext();
    SimpleBean inputBean = ObjectMappingTestUtil.constructSimpleBean();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    fudgeContext.writeObject(inputBean, baos);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    SimpleBean resultBean = fudgeContext.readObject(SimpleBean.class, bais);
    
    assertNotNull(resultBean);
    assertEquals(inputBean.getFieldOne(), resultBean.getFieldOne());
    assertEquals(inputBean.getFieldThree(), resultBean.getFieldThree());
    // Can't actually test this, as input is a HashMap and result is a TreeMap
    // Other tests are far more comprehensive though.
    assertEquals(inputBean.getFieldFour().size(), resultBean.getFieldFour().size());
    assertEquals(inputBean.getFieldFive(), resultBean.getFieldFive());
    
    inputBean = inputBean.getFieldTwo();
    resultBean = resultBean.getFieldTwo();
    assertNull(resultBean.getFieldOne());
    assertEquals(inputBean.getFieldThree(), resultBean.getFieldThree());
    assertNull(resultBean.getFieldFour());
    assertNull(resultBean.getFieldFive());
    
  }

  @Test
  public void setBean() throws IOException {
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
