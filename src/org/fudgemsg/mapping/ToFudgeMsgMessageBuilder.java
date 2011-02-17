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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * <p>Implementation of FudgeMessageBuilder for an object which supports a toFudgeMsg
 * function of the form (in order of search):</p>
 * <ol>
 * <li>void toFudgeMsg (FudgeSerialisationContext, MutableFudgeFieldContainer)</li>
 * <li>void toFudgeMsg (FudgeMessageFactory, MutableFudgeFieldContainer)</li>
 * <li>FudgeMsg toFudgeMsg (FudgeSerialisationContext)</li>
 * <li>FudgeMsg toFudgeMsg (FudgeMessageFactory)</li>
 * <li>void toFudgeMsg (FudgeContext, MutableFudgeFieldContainer)</li>
 * <li>FudgeMsg toFudgeMsg (FudgeContext)</li>
 * </ol>
 * 
 * @param <T> class that can be serialized using this builder
 * @author Andrew Griffin
 */
/* package */ abstract class ToFudgeMsgMessageBuilder<T> implements FudgeMessageBuilder<T> {
  
  /**
   * Attempts to create a new {@link ToFudgeMsgMessageBuilder} for a class.
   * 
   * @param <T> class to build messages for
   * @param clazz class to build messages for
   * @return Returns the {@code ToFudgeMsgMessageBuilder} or {@code null} if none is available
   */
  /* package */ static <T> ToFudgeMsgMessageBuilder<T> create (final Class<T> clazz) {
    try {
      return new AddFields<T> (clazz.getMethod ("toFudgeMsg", FudgeSerializationContext.class, MutableFudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new AddFields<T> (clazz.getMethod ("toFudgeMsg", FudgeMessageFactory.class, MutableFudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new CreateMessage<T> (clazz.getMethod ("toFudgeMsg", FudgeSerializationContext.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new CreateMessage<T> (clazz.getMethod ("toFudgeMsg", FudgeMessageFactory.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new AddFields<T>(clazz.getMethod("toFudgeMsg", FudgeContext.class, MutableFudgeFieldContainer.class), true);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new CreateMessage<T>(clazz.getMethod("toFudgeMsg", FudgeContext.class), true);
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
  
  /**
   * Invoke the {@code toFudgeMsg} method on the object.
   * 
   * @param obj object to invoke the method on
   * @param args parameters to pass
   * @return the value returned by the {@code toFudgeMsg} if any
   */
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
  
  private static class CreateMessage<T> extends ToFudgeMsgMessageBuilder<T> {
    
    private final boolean _passContext;
    
    private CreateMessage (final Method toFudgeMsg, final boolean passContext) {
      super (toFudgeMsg);
      _passContext = passContext;
    }
    
    @Override
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T object) {
      return (MutableFudgeFieldContainer)invoke (object, _passContext ? context.getFudgeContext () : context);
    }
    
  }
  
  private static class AddFields<T> extends ToFudgeMsgMessageBuilder<T> {
    
    private final boolean _passContext;
    
    private AddFields (final Method toFudgeMsg, final boolean passContext) {
      super (toFudgeMsg);
      _passContext = passContext;
    }
    
    @Override
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T object) {
      final MutableFudgeFieldContainer msg = context.newMessage ();
      invoke (object, _passContext ? context.getFudgeContext () : context, msg);
      return msg;
    }
    
  }
  
}