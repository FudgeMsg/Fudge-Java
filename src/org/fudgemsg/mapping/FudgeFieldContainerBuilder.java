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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Dummy builder wrapper for objects that are already fudge messages.
 * 
 * @author Andrew
 */
/* package */ class FudgeFieldContainerBuilder implements FudgeBuilder<FudgeFieldContainer> {
  
  /* package */ static final FudgeFieldContainerBuilder INSTANCE = new FudgeFieldContainerBuilder (); 
  
  private FudgeFieldContainerBuilder () {
  }

  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, FudgeFieldContainer fields) {
    return context.newMessage (fields);
  }
  
  @Override
  public FudgeFieldContainer buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    return context.getFudgeContext ().newMessage (message);
  }

}