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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeMsg;

/**
 * Implementation of FudgeMessageBuilder for an object which supports a toFudgeMsg
 * function of the form (in order of search):
 * 
 *    void toFudgeMsg (FudgeSerialisationContext, MutableFudgeFieldContainer)
 *    void toFudgeMsg (FudgeMessageFactory, MutableFudgeFieldContainer)
 *    FudgeMsg toFudgeMsg (FudgeSerialisationContext)
 *    FudgeMsg toFudgeMsg (FudgeMessageFactory)
 *    void toFudgeMsg (FudgeContext, MutableFudgeFieldContainer)
 *    FudgeMsg toFudgeMsg (FudgeContext)
 * 
 * @author Andrew
 */
/* package */ abstract class ToFudgeMsgMessageBuilder<T> implements FudgeMessageBuilder<T> {
  
  /* package */ static <T> ToFudgeMsgMessageBuilder<T> create (final Class<T> clazz) {
    try {
      return new AddFields<T> (clazz.getMethod ("toFudgeMsg", FudgeSerialisationContext.class, MutableFudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new CreateMessage<T> (clazz.getMethod ("toFudgeMsg", FudgeSerialisationContext.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new AddFields<T> (clazz.getMethod ("toFudgeMsg", FudgeContext.class, MutableFudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new CreateMessage<T> (clazz.getMethod ("toFudgeMsg", FudgeContext.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    return null;
  }
  
  private final Method _toFudgeMsg;
  
  private ToFudgeMsgMessageBuilder (final Method toFudgeMsg) {
    _toFudgeMsg = toFudgeMsg;
  }
  
  protected Object invoke (Object obj, Object... args) {
    try {
      return _toFudgeMsg.invoke (obj, args);
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't call 'toFudgeMsg' on '" + obj + "'", e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't call 'toFudgeMsg' on '" + obj + "'", e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't call 'toFudgeMsg' on '" + obj + "'", e);
    }
  }
  
  /* package */ static class CreateMessage<T> extends ToFudgeMsgMessageBuilder<T> {
    
    private final boolean _passContext;
    
    private CreateMessage (final Method toFudgeMsg, final boolean passContext) {
      super (toFudgeMsg);
      _passContext = passContext;
    }
    
    @Override
    public FudgeMsg buildMessage(FudgeSerialisationContext context, T object) {
      return (FudgeMsg)invoke (object, _passContext ? context.getFudgeContext () : context);
    }
    
  }
  
  /* package */ static class AddFields<T> extends ToFudgeMsgMessageBuilder<T> {
    
    private final boolean _passContext;
    
    private AddFields (final Method toFudgeMsg, final boolean passContext) {
      super (toFudgeMsg);
      _passContext = passContext;
    }
    
    @Override
    public MutableFudgeFieldContainer buildMessage(FudgeSerialisationContext context, T object) {
      final MutableFudgeFieldContainer msg = context.newMessage ();
      invoke (object, _passContext ? context.getFudgeContext () : context, msg);
      return msg;
    }
    
  }
  
}