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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeUtils;
import org.fudgemsg.mapping.ObjectMappingTestUtil.SimpleBean;
import org.fudgemsg.mapping.ObjectMappingTestUtil.StaticTransientBean;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectMessageFactoryTest {

  @Test
  public void simpleBean() {
    FudgeContext fudgeContext = new FudgeContext();
    SimpleBean simpleBean = ObjectMappingTestUtil.constructSimpleBean();
    FudgeMsg msg = FudgeObjectMessageFactory.serializeToMessage(simpleBean, fudgeContext);
    assertNotNull(msg);
    FudgeUtils.assertAllFieldsMatch(ObjectMappingTestUtil.constructSimpleMessage(fudgeContext), msg, false);
  }
  
  @Test
  public void staticAndTransient() {
    FudgeContext fudgeContext = new FudgeContext();
    StaticTransientBean bean = new StaticTransientBean();
    FudgeMsg msg = FudgeObjectMessageFactory.serializeToMessage(bean, fudgeContext);
    assertNotNull(msg);
    assertEquals(0, msg.getNumFields());
  }

}
