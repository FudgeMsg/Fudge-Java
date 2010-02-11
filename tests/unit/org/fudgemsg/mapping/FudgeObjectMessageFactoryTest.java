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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeUtils;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.StaticTransientBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.MappedNameBean;
import org.fudgemsg.mapping.FudgeObjectMessageFactory;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectMessageFactoryTest {

  /**
   * 
   */
  @Test
  public void simpleBean() {
    FudgeContext fudgeContext = new FudgeContext();
    SimpleBean simpleBean = ObjectMappingTestUtil.constructSimpleBean();
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage(simpleBean, fudgeContext);
    assertNotNull(msg);
    FudgeUtils.assertAllFieldsMatch(ObjectMappingTestUtil.constructSimpleMessage(fudgeContext), msg, false);
  }
  
  /**
   * 
   */
  @Test
  public void staticAndTransient() {
    FudgeContext fudgeContext = new FudgeContext();
    StaticTransientBean bean = new StaticTransientBean();
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage(bean, fudgeContext);
    System.out.println (msg);
    assertNotNull(msg);
    assertEquals(1, msg.getNumFields()); // the class identifier only
  }
  
  /**
   * 
   */
  @Test
  public void fudgeFieldMappings () {
    FudgeContext fudgeContext = new FudgeContext ();
    MappedNameBean bean = new MappedNameBean ();
    bean.setFieldOne ("field 1");
    bean.setFieldTwo ("field 2");
    bean.setFieldThree ("field 3");
    bean.setFieldFour ("field 4");
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage (bean, fudgeContext);
    bean = null;
    assertNotNull (msg);
    assertEquals (5, msg.getNumFields ()); // our 4 + the class identifier
    assertEquals (null, msg.getString ("fieldOne"));
    assertEquals ("field 1", msg.getString ("foo"));
    assertEquals (null, msg.getString ("fieldTwo"));
    assertEquals ("field 2", msg.getString ("bar"));
    assertEquals ("field 3", msg.getString (99));
    assertEquals ("field 3", msg.getString ("fieldThree"));
    assertEquals (null, msg.getString ("fieldFour"));
    assertEquals ("field 4", msg.getString (100));
    bean = FudgeObjectMessageFactory.deserializeToObject (MappedNameBean.class, msg, fudgeContext);
    assertNotNull (bean);
    assertEquals ("field 1", bean.getFieldOne ());
    assertEquals ("field 2", bean.getFieldTwo ());
    assertEquals ("field 3", bean.getFieldThree ());
    assertEquals ("field 4", bean.getFieldFour ());
  }
  
  /**
   * 
   */
  @Test
  public void objectGraph () {
    FudgeContext fudgeContext = new FudgeContext ();
    SimpleBean recursiveBean = ObjectMappingTestUtil.constructSimpleBean ();
    recursiveBean.getFieldTwo ().setFieldTwo (recursiveBean);
    try {
      FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage (recursiveBean, fudgeContext);
      System.out.println (msg);
      assert false;
    } catch (FudgeRuntimeException fre) {
      assertEquals ("Serialization framework can't support cyclic references", fre.getMessage ());
    }
  }
  
}
