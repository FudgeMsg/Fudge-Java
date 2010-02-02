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

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Contains mappings from Java objects to Fudge messages for the current classloader.
 * There are a set of default mappings built into this implementation
 * (see FudgeDefaultBuilder) which will be used, or custom mappings can be supplied.
 * 
 * For example, registering builders for {@link Map} or {@link List} with a {@code FudgeObjectDictionary}
 * before it is used by a {@link FudgeDeserializationContext} or {@link FudgeSerializationContext} will
 * override the default behaviours.
 * 
 * @author Andrew
 */
public final class FudgeObjectDictionary {
  
  private static final FudgeMessageBuilder<?> NULL_MESSAGEBUILDER = new FudgeMessageBuilder<Object> () {
    @Override
    public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, Object object) {
      return null;
    }
  };
  
  private static final FudgeObjectBuilder<?> NULL_OBJECTBUILDER = new FudgeObjectBuilder<Object> () {
    @Override
    public Object buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
      return null;
    }
  };
  
  private final ConcurrentMap<Class<?>, FudgeObjectBuilder<?>> _objectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>> ();
  private final ConcurrentMap<Class<?>, FudgeMessageBuilder<?>> _messageBuilders = new ConcurrentHashMap<Class<?>, FudgeMessageBuilder<?>> ();
  
  /**
   * Constructs a new (initially empty) {@link FudgeObjectDictionary}.
   */
  public FudgeObjectDictionary () {
  }
  
  /**
   * Registers a new {@link FudgeObjectBuilder} with this dictionary to be used for a given class. The same builder can be registered against
   * multiple classes. A class can only have one registered {@code FudgeObjectBuilder} - registering a second will overwrite the previous
   * registration.
   * 
   * @param <T> Java type of the objects created by the builder
   * @param clazz the Java class to register the builder against
   * @param builder the builder to register
   */
  public <T> void addObjectBuilder (final Class<T> clazz, final FudgeObjectBuilder<T> builder) {
    _objectBuilders.put (clazz, builder);
  }
  
  /**
   * Registers a new {@link FudgeMessageBuilder} with this dictionary to be used for a given class. The same builder can be registered against
   * multiple classes. A class can only have one registered {@code FudgeMessageBuilder} - registering a second will overwrite the previous
   * registration.
   * 
   * @param <T> Java type of the objects processed by the builder
   * @param clazz the Java class to register the builder against
   * @param builder builder to register
   */
  public <T> void addMessageBuilder (final Class<T> clazz, final FudgeMessageBuilder<T> builder) {
    _messageBuilders.put (clazz, builder);
  }
  
  /**
   * Registers a new {@link FudgeBuilder} with this dictionary to be used for a given class. A {@code FudgeBuilder} is simply a combined {@link FudgeMessageBuilder}
   * and {@link FudgeObjectBuilder} so this method is the same as calling {@link #addMessageBuilder(Class,FudgeMessageBuilder)} and {@link #addObjectBuilder(Class,FudgeObjectBuilder)}.
   * 
   * @param <T> Java type of the objects processed by the builder
   * @param clazz the Java class to register the builder against
   * @param builder builder to register
   */
  public <T> void addBuilder (final Class<T> clazz, final FudgeBuilder<T> builder) {
    addMessageBuilder (clazz, builder);
    addObjectBuilder (clazz, builder);
  }
  
  /**
   * Returns a {@link FudgeObjectBuilder} for the given class to convert a Fudge message to a Java object. If none is already registered for the class,
   * it will attempt to create one using {@link FudgeDefaultBuilder}. If it is not possible to create a builder (e.g. for an interface) returns {@code null}.
   * 
   * @param <T> Java type of the objects to be built
   * @param clazz the Java class to look up
   * @return the builder, or {@code null} if none is available
   */
  @SuppressWarnings("unchecked")
  public <T> FudgeObjectBuilder<T> getObjectBuilder (final Class<T> clazz) {
    FudgeObjectBuilder<T> builder = (FudgeObjectBuilder<T>)_objectBuilders.get (clazz);
    if (builder == null) {
      FudgeObjectBuilder<T> freshBuilder = FudgeDefaultBuilder.defaultObjectBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeObjectBuilder<T>)NULL_OBJECTBUILDER;
      builder = (FudgeObjectBuilder<T>)_objectBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) {
        // if the default object builder is also a message builder then store a reference now
        if (freshBuilder instanceof FudgeMessageBuilder<?>) {
          _messageBuilders.putIfAbsent (clazz, (FudgeMessageBuilder<?>)freshBuilder);
        }
        builder = freshBuilder;
      }
    }
    return (builder == NULL_OBJECTBUILDER) ? null : builder;
  }
  
  /**
   * Returns a {@link FudgeMessageBuilder} for the given class to convert a Fudge message to a Java object. If none is already registered for the class,
   * it will attempt to create one using {@link FudgeDefaultBuilder}. If it is not possible to create a builder returns {@code null}.
   * 
   * @param <T> Java type of the objects to be built
   * @param clazz the Java class to look up
   * @return the builder, or {@code null} if none is available
   */
  @SuppressWarnings("unchecked")
  public <T> FudgeMessageBuilder<T> getMessageBuilder (final Class<T> clazz) {
    FudgeMessageBuilder<T> builder = (FudgeMessageBuilder<T>)_messageBuilders.get (clazz);
    if (builder == null) {
      FudgeMessageBuilder<T> freshBuilder = FudgeDefaultBuilder.defaultMessageBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeMessageBuilder<T>)NULL_MESSAGEBUILDER;
      builder = (FudgeMessageBuilder<T>)_messageBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) {
        // if the default message builder is also an object builder then store a reference now
        if (freshBuilder instanceof FudgeObjectBuilder<?>) {
          _objectBuilders.putIfAbsent (clazz, (FudgeObjectBuilder<?>)freshBuilder);
        }
        builder = freshBuilder;
      }
    }
    return (builder == NULL_MESSAGEBUILDER) ? null : builder;
  }
  
}