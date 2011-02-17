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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

/**
 */
public class FudgeDefaultBuilderFactoryTest {
  
  /**
   * Just a test builder. Won't actually build anything.
   */
  public static class SimpleBeanBuilder implements FudgeBuilder<SimpleBean> {

    /**
     * 
     */
    @Override
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, SimpleBean object) {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * 
     */
    @Override
    public SimpleBean buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

  @HasFudgeBuilder(builder=SimpleBeanBuilder.class)
  private static class SimpleBean {
  }
  
  /**
   */
  @Test
  public void builderAnnotationDetected() {
    FudgeDefaultBuilderFactory factory = new FudgeDefaultBuilderFactory();
    FudgeMessageBuilder<SimpleBean> messageBuilder = factory.createMessageBuilder(SimpleBean.class);
    assertNotNull(messageBuilder);
    assertTrue(messageBuilder instanceof SimpleBeanBuilder);

    FudgeObjectBuilder<SimpleBean> objectBuilder = factory.createObjectBuilder(SimpleBean.class);
    assertNotNull(objectBuilder);
    assertTrue(objectBuilder instanceof SimpleBeanBuilder);
  }
}
