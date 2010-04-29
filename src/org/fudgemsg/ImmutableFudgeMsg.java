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

/**
 * <p>An implementation of a {@link ImmutableFudgeFieldContainer} that can be created as a copy of an
 * existing {@link FudgeFieldContainer}. For efficiency, the reference to a {@link FudgeContext} is
 * kept - the context is not copied, and so changes made to the context will be made visible through
 * an instance, for example the behavior of {@link #getFieldValue}. If this is not desired, create
 * a {@link ImmutableFudgeContext} from your underlying {@code FudgeContext} for use in cloning
 * messages.</p>
 * 
 * <p>Message fields are copied at one level deep only. Any sub-messages, or referenced objects may
 * be still be mutable.</p>
 * 
 * @author Andrew Griffin
 */
public class ImmutableFudgeMsg extends FudgeMsgBase implements ImmutableFudgeFieldContainer {

  /**
   * Creates a new {@link ImmutableFudgeMsg} by copying a {@link FudgeMsg} or {@link ImmutableFudgeMsg} object.
   * The new message will use the same {@link FudgeContext} context as the original message.
   * 
   * @param fudgeMsg the message to copy
   */
  public ImmutableFudgeMsg (final FudgeMsgBase fudgeMsg) {
    this (fudgeMsg, fudgeMsg.getFudgeContext ()); 
  }
  
  /**
   * Creates a new {@link ImmutableFudgeMsg} by copying fields from another {@link FudgeFieldContainer} using
   * the specified {@link FudgeContext} for type resolution. 
   * 
   * @param fields the message to copy
   * @param fudgeContext the context to use for the new message
   */
  public ImmutableFudgeMsg (final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    super (fields, fudgeContext);
  }
  
  /**
   * Creates an immutable empty message.
   * 
   *  @param fudgeContext host context
   */
  protected ImmutableFudgeMsg (final FudgeContext fudgeContext) {
    super (fudgeContext);
  }
  
}