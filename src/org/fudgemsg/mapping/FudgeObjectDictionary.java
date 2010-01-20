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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;

/**
 * Contains mappings from Java objects to Fudge messages for the current classloader.
 * There are a set of default mappings built into this implementation
 * (see FudgeDefaultBuilder) which will be used, or custom mappings can be supplied.
 * 
 * @author Andrew
 */
public final class FudgeObjectDictionary {
  
  private static final class NullBuilder<T> implements FudgeBuilder<T> {
    @Override
    public FudgeMsg buildMessage(FudgeSerialisationContext context, T object) {
      return null;
    }
    @Override
    public T buildObject(FudgeDeserialisationContext context, FudgeFieldContainer message) {
      return null;
    }
  }
  
  private static final NullBuilder<?> NULL_BUILDER = new NullBuilder<Object> ();
  
  private final ConcurrentMap<Class<?>, FudgeObjectBuilder<?>> _objectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>> ();
  private final ConcurrentMap<Class<?>, FudgeMessageBuilder<?>> _messageBuilders = new ConcurrentHashMap<Class<?>, FudgeMessageBuilder<?>> ();
  
  public FudgeObjectDictionary () {
  }
  
  public <T> void addObjectBuilder (final Class<T> clazz, final FudgeObjectBuilder<T> builder) {
    _objectBuilders.put (clazz, builder);
  }
  
  public <T> void addMessageBuilder (final Class<T> clazz, final FudgeMessageBuilder<T> builder) {
    _messageBuilders.put (clazz, builder);
  }
  
  public <T> void addBuilder (final Class<T> clazz, final FudgeBuilder<T> builder) {
    addMessageBuilder (clazz, builder);
    addObjectBuilder (clazz, builder);
  }
  
  @SuppressWarnings("unchecked")
  public <T> FudgeObjectBuilder<T> getObjectBuilder (final Class<T> clazz) {
    FudgeObjectBuilder<T> builder = (FudgeObjectBuilder<T>)_objectBuilders.get (clazz);
    if (builder == null) {
      FudgeObjectBuilder<T> freshBuilder = FudgeDefaultBuilder.defaultObjectBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeObjectBuilder<T>)NULL_BUILDER;
      builder = (FudgeObjectBuilder<T>)_objectBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) builder = freshBuilder;
    }
    return (builder == NULL_BUILDER) ? null : builder;
  }
  
  @SuppressWarnings("unchecked")
  public <T> FudgeMessageBuilder<T> getMessageBuilder (final Class<T> clazz) {
    FudgeMessageBuilder<T> builder = (FudgeMessageBuilder<T>)_messageBuilders.get (clazz);
    if (builder == null) {
      FudgeMessageBuilder<T> freshBuilder = FudgeDefaultBuilder.defaultMessageBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeMessageBuilder<T>)NULL_BUILDER;
      builder = (FudgeMessageBuilder<T>)_messageBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) builder = freshBuilder;
    }
    return (builder == NULL_BUILDER) ? null : builder;
  }
  
}