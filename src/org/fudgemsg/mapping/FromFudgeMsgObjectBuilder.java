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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Implementation of FudgeObjectBuilder for a class which supports a fromFudgeMsg
 * function of the form:
 * 
 *    static <T> fromFudgeMsg (FudgeFieldContainer) or
 *    static <T> fromFudgeMsg (FudgeDeserialisationContext, FudgeFieldContainer)
 * 
 * @param <T> class supporting a {@code fromFudgeMsg} method which can be deserialised by this builder
 * @author Andrew Griffin
 */
/* package */ class FromFudgeMsgObjectBuilder<T> implements FudgeObjectBuilder<T> {
  
  /**
   * Creates a new {@link FromFudgeMsgObjectBuilder} if possible.
   * 
   * @param <T> target class to build from the message 
   * @param clazz target class to build from the message
   * @return the {@link FromFudgeMsgObjectBuilder} or {@code null} if none is available
   */
  /* package */ static <T> FromFudgeMsgObjectBuilder<T> create (final Class<T> clazz) {
    try {
      return new FromFudgeMsgObjectBuilder<T> (clazz.getMethod ("fromFudgeMsg", FudgeDeserializationContext.class, FudgeFieldContainer.class), true);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new FromFudgeMsgObjectBuilder<T> (clazz.getMethod ("fromFudgeMsg", FudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    return null;
  }

  private final Method _fromFudgeMsg;
  private final boolean _passContext;
  
  private FromFudgeMsgObjectBuilder (final Method fromFudgeMsg, final boolean passContext) {
    _fromFudgeMsg = fromFudgeMsg;
    _passContext = passContext;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    try {
      return (T)(_passContext ? _fromFudgeMsg.invoke (null, context, message) : _fromFudgeMsg.invoke (null, message));
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't call fromFudgeMsg", e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't call fromFudgeMsg", e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't call fromFudgeMsg", e);
    }
  }
  
}