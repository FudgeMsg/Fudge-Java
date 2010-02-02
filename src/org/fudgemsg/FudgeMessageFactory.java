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
package org.fudgemsg;

import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Cut down version of the newMessage operation from FudgeContext and FudgeSerialisationContext
 * as most object/message conversion operations only require the initial message creation hook
 * and not the full {@link FudgeContext} or {@link FudgeSerializationContext}.
 * 
 * @author Andrew
 */
public interface FudgeMessageFactory {
  
  /**
   * Creates an initially empty message.
   * 
   * @return the empty message container
   */
  public MutableFudgeFieldContainer newMessage ();
  
  /**
   * Creates a new message initially populated with the supplied message.
   * 
   * @param fromMessage source message to copy fields from
   * @return the new message container
   */
  public MutableFudgeFieldContainer newMessage (FudgeFieldContainer fromMessage);
  
}