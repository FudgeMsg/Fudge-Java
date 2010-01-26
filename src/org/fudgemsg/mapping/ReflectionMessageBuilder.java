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
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Attempt to create an Fudge message containing values from Java-bean style getX
 * methods. If the nearest superclass supports ToFudgeMsgMessageBuilder then that will
 * be used to create the initial message that is supplemented by subclass getX
 * methods.
 * 
 * @author Andrew
 */
/* package */ class ReflectionMessageBuilder<T> extends ReflectionBuilderBase<T> implements FudgeMessageBuilder<T> {
  
  /* package */ static <T> ReflectionMessageBuilder<T> create (final Class<T> clazz) {
    FudgeMessageBuilder<? super T> builder = null;
    Class<? super T> superclazz = clazz;
    while ((superclazz = superclazz.getSuperclass ()) != null) {
      builder = ToFudgeMsgMessageBuilder.create (superclazz);
      if (builder != null) break;
    }
    return new ReflectionMessageBuilder<T> (clazz, superclazz, builder);
  }
  
  private final FudgeMessageBuilder<? super T> _baseBuilder;
  
  private ReflectionMessageBuilder (final Class<T> clazz, final Class<? super T> upstream, final FudgeMessageBuilder<? super T> baseBuilder) {
    super (clazz, "get", 0, upstream);
    _baseBuilder = baseBuilder;
  }
  
  @Override
  public FudgeMsg buildMessage (final FudgeSerialisationContext context, final T object) {
    //System.out.println ("ReflectionMessageBuilder::buildMessage (" + context + ", " + object + ")");
    final FudgeMsg message;
    if (_baseBuilder != null) {
      message = _baseBuilder.buildMessage (context, object);
    } else {
      message = context.newMessage ();
    }
    try {
      for (Map.Entry<String, Method> accessor : getMethods ().entrySet ()) {
        //System.out.println ("\t" + accessor.getValue ());
        context.objectToFudgeMsg (message, accessor.getKey (), null, accessor.getValue ().invoke (object));
      }
      context.addClassHeader (message, object.getClass ());
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    }
    return message;
  }
  
}