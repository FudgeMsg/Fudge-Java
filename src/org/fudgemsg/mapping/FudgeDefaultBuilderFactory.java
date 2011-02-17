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

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

import com.mongodb.DBObject;

/**
 * <p>Default factory for building Fudge message encoders and decoders.</p>
 * 
 * <p>Building a Fudge message:</p>
 * <ul>
 *   <li>If the object has a public {@code toFudgeMsg} method, that will be used</li>
 *   <li>Otherwise the {@link JavaBeanBuilder} will be used</li>
 * </ul>
 *    
 * <p>Building an object:</p>
 * <ul>
 *   <li>If the object has a public {@code fromFudgeMsg} method, that will be used</li>
 *   <li>If the object has a public constructor that takes a {@link FudgeFieldContainer}, that will be used</li>
 *   <li>Otherwise the {@link JavaBeanBuilder} will be used</li>
 * </ul>
 *  
 * <p>Generic builders are provided for {@link Map}, {@link List} (and {@link Set}), {@link FudgeFieldContainer}, {@link DBObject} and array types.</p>
 * 
 * @author Andrew Griffin
 */ 
public class FudgeDefaultBuilderFactory implements FudgeBuilderFactory {
  
  private static class MessageBuilderMapEntry {
    private final Class<?> _clazz;
    private final FudgeMessageBuilder<?> _builder;
    <T> MessageBuilderMapEntry (Class<T> clazz, FudgeMessageBuilder<? extends T> builder) {
      _clazz = clazz;
      _builder = builder;
    }
    Class<?> getClazz () {
      return _clazz;
    }
    FudgeMessageBuilder<?> getMessageBuilder () {
      return _builder;
    }
  }
  
  private final ConcurrentMap<Class<?>,FudgeObjectBuilder<?>> _genericObjectBuilders;
  private final List<MessageBuilderMapEntry> _genericMessageBuilders;
  
  // TODO 2010-01-29 Andrew -- we could have a builder builder, e.g. search for static methods that return a FudgeObjectBuilder/FudgeMessageBuilder/FudgeBuilder instance for that class
  
  /**
   * Creates a new factory. {@code org.fudgemsg.mapping.FudgeDefaultBuilderFactory.properties} will be read and used to initialize
   * the generic builders.
   */
  @SuppressWarnings("unchecked")
  public FudgeDefaultBuilderFactory () {
    _genericObjectBuilders = new ConcurrentHashMap<Class<?>,FudgeObjectBuilder<?>> ();
    _genericMessageBuilders = new CopyOnWriteArrayList<MessageBuilderMapEntry> ();
    final ResourceBundle genericBuilders = ResourceBundle.getBundle (getClass ().getName ());
    for (final String javaClassName : genericBuilders.keySet ()) {
      final String builderName = genericBuilders.getString (javaClassName);
      try {
        addGenericBuilderInternal (Class.forName (javaClassName), (FudgeBuilder)Class.forName (builderName).getDeclaredField ("INSTANCE").get (null));
      } catch (ClassNotFoundException e) {
        // ignore; e.g. if DBObject isn't in the classpath
      } catch (Exception e) {
        throw new FudgeRuntimeException ("couldn't register builder for " + javaClassName + " (" + builderName + ")", e);
      }
    }
  }
  
  /**
   * Creates a new factory as a clone of another.
   * 
   * @param other the factory to clone
   */
  /* package */ FudgeDefaultBuilderFactory (final FudgeDefaultBuilderFactory other) {
    _genericObjectBuilders = new ConcurrentHashMap<Class<?>,FudgeObjectBuilder<?>> (other._genericObjectBuilders);
    _genericMessageBuilders = new CopyOnWriteArrayList<MessageBuilderMapEntry> (other._genericMessageBuilders);
  }
  
  private Map<Class<?>,FudgeObjectBuilder<?>> getGenericObjectBuilders () {
    return _genericObjectBuilders;
  }
  
  private List<MessageBuilderMapEntry> getGenericMessageBuilders () {
    return _genericMessageBuilders;
  }
  
  /**
   * If the object has a public fromFudgeMsg method, that will be used. Otherwise, if it has a
   * public constructor that takes a FudgeFieldContainer, that will be used. Registered default
   * builders for classes list Map and List will be tried, failing that the JavaBeanBuilder will
   * be used.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz Java class a builder is requested for 
   * @return a {@link FudgeObjectBuilder} or {@code null} if no suitable builder can be created
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> FudgeObjectBuilder<T> createObjectBuilder (final Class<T> clazz) {
    FudgeObjectBuilder<T> builder;
    if ((builder = createObjectBuilderFromAnnotation(clazz)) != null) return builder;
    if ((builder = FromFudgeMsgObjectBuilder.create (clazz)) != null) return builder;
    if ((builder = FudgeMsgConstructorObjectBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    if (Enum.class.isAssignableFrom(clazz)) return new EnumBuilder (clazz);
    if ((builder = (FudgeObjectBuilder<T>)getGenericObjectBuilders ().get (clazz)) != null) return builder;
    if (clazz.isInterface ()) return null;
    //return ReflectionObjectBuilder.create (clazz);
    return JavaBeanBuilder.create (clazz);
  }
  
  /**
   * Attempt to construct a {@link FudgeObjectBuilder} for the specified type based on the presence
   * of a {@link HasFudgeBuilder} annotation on that type.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz Java class a builder is requested for
   * @return A {@link FudgeObjectBuilder} based on {@link HasFudgeBuilder} annotation, or {@code null}.
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeObjectBuilder<T> createObjectBuilderFromAnnotation(final Class<T> clazz) {
    if (!clazz.isAnnotationPresent(HasFudgeBuilder.class)) {
      return null;
    }
    HasFudgeBuilder annotation = clazz.getAnnotation(HasFudgeBuilder.class);
    Class<?> objectBuilderClass = null;
    if (!Object.class.equals(annotation.builder())) {
      objectBuilderClass = annotation.builder();
    } else if (!Object.class.equals(annotation.objectBuilder())) {
      objectBuilderClass = annotation.objectBuilder();
    }
    
    if (objectBuilderClass == null) {
      return null;
    }
    
    if (!FudgeObjectBuilder.class.isAssignableFrom(objectBuilderClass)) {
      return null;
    }
    
    FudgeObjectBuilder<T> result = null;
    try {
      result = (FudgeObjectBuilder<T>) objectBuilderClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate annotated object builder class " + objectBuilderClass, e);
    }
    
    return result;
  }
  
  /**
   * If the object has a public toFudgeMsg method, that will be used. Otherwise the
   * JavaBeanBuilder will be used.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz Java class a builder is requested for
   * @return a {@link FudgeMessageBuilder} or {@code null} if no suitable builder can be created
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> FudgeMessageBuilder<T> createMessageBuilder (final Class<T> clazz) {
    FudgeMessageBuilder<T> builder;
    if ((builder = createMessageBuilderFromAnnotation(clazz)) != null) return builder;
    if ((builder = ToFudgeMsgMessageBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    if (Enum.class.isAssignableFrom(clazz)) return new EnumBuilder (clazz);
    for (MessageBuilderMapEntry defaultBuilder : getGenericMessageBuilders ()) {
      if (defaultBuilder.getClazz ().isAssignableFrom (clazz)) return (FudgeMessageBuilder<T>)defaultBuilder.getMessageBuilder ();
    }
    //return ReflectionMessageBuilder.create (clazz);
    return JavaBeanBuilder.create (clazz);
  }
  
  /**
   * Attempt to construct a {@link FudgeObjectBuilder} for the specified type based on the presence
   * of a {@link HasFudgeBuilder} annotation on that type.
   * 
   * @param <T> Java type of the class a builder is requested for
   * @param clazz Java class a builder is requested for
   * @return A {@link FudgeObjectBuilder} based on {@link HasFudgeBuilder} annotation, or {@code null}.
   */
  @SuppressWarnings("unchecked")
  protected <T> FudgeMessageBuilder<T> createMessageBuilderFromAnnotation(final Class<T> clazz) {
    if (!clazz.isAnnotationPresent(HasFudgeBuilder.class)) {
      return null;
    }
    HasFudgeBuilder annotation = clazz.getAnnotation(HasFudgeBuilder.class);
    Class<?> messageBuilderClass = null;
    if (!Object.class.equals(annotation.builder())) {
      messageBuilderClass = annotation.builder();
    } else if (!Object.class.equals(annotation.messageBuilder())) {
      messageBuilderClass = annotation.messageBuilder();
    }
    
    if (messageBuilderClass == null) {
      return null;
    }
    
    if (!FudgeMessageBuilder.class.isAssignableFrom(messageBuilderClass)) {
      return null;
    }
    
    FudgeMessageBuilder<T> result = null;
    try {
      result = (FudgeMessageBuilder<T>) messageBuilderClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate annotated message builder class " + messageBuilderClass, e);
    }
    
    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void addGenericBuilder (final Class<T> clazz, final FudgeBuilder<T> builder) {
    addGenericBuilderInternal (clazz, builder);
  }
  
  private <T> void addGenericBuilderInternal (final Class<T> clazz, final FudgeBuilder<? extends T> builder) {
    getGenericObjectBuilders ().put (clazz, builder);
    getGenericMessageBuilders ().add (0, new MessageBuilderMapEntry (clazz, builder));
  }

}
