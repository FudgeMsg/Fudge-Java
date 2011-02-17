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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.ObjectMappingTestUtil.MappedNameBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.StaticTransientBean;
import org.fudgemsg.test.FudgeUtils;
import org.junit.Test;

/**
 * 
 *
 * @author Kirk Wylie
 */
public class FudgeObjectMessageFactoryTest {

  /**
   * 
   */
  @Test
  @Deprecated
  public void simpleBeanOld() {
    SimpleBean simpleBean = ObjectMappingTestUtil.constructSimpleBean();
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage(simpleBean, FudgeContext.GLOBAL_DEFAULT);
    assertNotNull(msg);
    FudgeUtils.assertAllFieldsMatch(ObjectMappingTestUtil.constructSimpleMessage(FudgeContext.GLOBAL_DEFAULT), msg, false);
  }
  
  /**
   * 
   */
  @Test
  @Deprecated
  public void staticAndTransientOld() {
    StaticTransientBean bean = new StaticTransientBean();
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage(bean, FudgeContext.GLOBAL_DEFAULT);
    System.out.println (msg);
    assertNotNull(msg);
    assertEquals(1, msg.getNumFields()); // the class identifier only
  }
  
  /**
   * 
   */
  @Test
  @Deprecated
  public void fudgeFieldMappingsOld () {
    MappedNameBean bean = new MappedNameBean ();
    bean.setFieldOne ("field 1");
    bean.setFieldTwo ("field 2");
    bean.setFieldThree ("field 3");
    bean.setFieldFour ("field 4");
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage(bean, FudgeContext.GLOBAL_DEFAULT);
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
    bean = FudgeObjectMessageFactory.deserializeToObject (MappedNameBean.class, msg, FudgeContext.GLOBAL_DEFAULT);
    assertNotNull (bean);
    assertEquals ("field 1", bean.getFieldOne ());
    assertEquals ("field 2", bean.getFieldTwo ());
    assertEquals ("field 3", bean.getFieldThree ());
    assertEquals ("field 4", bean.getFieldFour ());
  }
  
  /**
   * 
   */
  @Test(expected=UnsupportedOperationException.class)
  @Deprecated
  public void objectGraphOld () {
    SimpleBean recursiveBean = ObjectMappingTestUtil.constructSimpleBean ();
    recursiveBean.getFieldTwo ().setFieldTwo (recursiveBean);
    FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage (recursiveBean, FudgeContext.GLOBAL_DEFAULT);
    System.out.println (msg);
  }
  
  /**
   * 
   */
  @Test
  public void simpleBean() {
    SimpleBean simpleBean = ObjectMappingTestUtil.constructSimpleBean();
    FudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (simpleBean).getMessage ();
    assertNotNull(msg);
    FudgeUtils.assertAllFieldsMatch(ObjectMappingTestUtil.constructSimpleMessage(FudgeContext.GLOBAL_DEFAULT), msg, false);
  }
  
  /**
   * 
   */
  @Test
  public void staticAndTransient() {
    StaticTransientBean bean = new StaticTransientBean();
    FudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (bean).getMessage ();
    System.out.println (msg);
    assertNotNull(msg);
    assertEquals(1, msg.getNumFields()); // the class identifier only
  }
  
  /**
   * 
   */
  @Test
  public void fudgeFieldMappings () {
    MappedNameBean bean = new MappedNameBean ();
    bean.setFieldOne ("field 1");
    bean.setFieldTwo ("field 2");
    bean.setFieldThree ("field 3");
    bean.setFieldFour ("field 4");
    FudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (bean).getMessage ();
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
    bean = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg (MappedNameBean.class, msg);
    assertNotNull (bean);
    assertEquals ("field 1", bean.getFieldOne ());
    assertEquals ("field 2", bean.getFieldTwo ());
    assertEquals ("field 3", bean.getFieldThree ());
    assertEquals ("field 4", bean.getFieldFour ());
  }
  
  /**
   * 
   */
  @Test(expected=UnsupportedOperationException.class)
  public void objectGraph () {
    SimpleBean recursiveBean = ObjectMappingTestUtil.constructSimpleBean ();
    recursiveBean.getFieldTwo ().setFieldTwo (recursiveBean);
    FudgeMsgEnvelope msg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (recursiveBean);
    System.out.println (msg);
  }
  
}
