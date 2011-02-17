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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Implementation of FudgeObjectBuilder for a class with a public constructor that
 * accepts a {@link FudgeFieldContainer} or a {@link FudgeDeserializationContext} and {@link FudgeFieldContainer}.
 * 
 * @param <T> class supporting a {@code FudgeFieldContainer} constructor that can be deserialized by this builder
 * @author Andrew Griffin
 */
/* package */ class FudgeMsgConstructorObjectBuilder<T> implements FudgeObjectBuilder<T> {

  /**
   * Creates a new {@link FudgeMsgConstructorObjectBuilder} for the class if possible.
   * 
   * @param <T> class the builder should create objects of
   * @param clazz class the builder should create objects of
   * @return the {@code FudgeMsgConstructorObjectBuilder} or {@code null} if none is available
   */
  /* package */ static <T> FudgeMsgConstructorObjectBuilder<T> create (final Class<T> clazz) {
    try {
      return new FudgeMsgConstructorObjectBuilder<T> (clazz.getConstructor (FudgeDeserializationContext.class, FudgeFieldContainer.class), true);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    try {
      return new FudgeMsgConstructorObjectBuilder<T> (clazz.getConstructor (FudgeFieldContainer.class), false);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    return null;
  }

  private final Constructor<T> _constructor;
  private final boolean _passContext;
  
  private FudgeMsgConstructorObjectBuilder (final Constructor<T> constructor, final boolean passContext) {
    _constructor = constructor;
    _passContext = passContext;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public T buildObject (final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    try {
      return _passContext ? _constructor.newInstance (context, message) : _constructor.newInstance (message);
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (InstantiationException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    }
  }
  
}