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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeFieldContainer;

/**
 * Default factory for building Fudge message encoders and decoders.
 * 
 * Building a Fudge message:
 * 
 *    If the object has a public toFudgeMsg method, that will be used
 *    Otherwise the JavaBeanBuilder will be used
 *    
 * Building an object
 *    If the object has a public fromFudgeMsg method, that will be used
 *    If the object has a public constructor that takes a FudgeFieldContainer, that will be used
 *    Otherwise the JavaBeanBuilder will be used
 *  
 * Generic builders are provided for {@link Map}, {@link List}, {@link FudgeFieldContainer} and array types. 
 * 
 * @author Andrew
 */ 
public class FudgeDefaultBuilderFactory implements FudgeBuilderFactory {
  
  private final ConcurrentMap<Class<?>,FudgeBuilder<?>> _genericBuilders = new ConcurrentHashMap<Class<?>,FudgeBuilder<?>> ();
  
  // TODO 2010-01-29 Andrew -- we could have a builder builder, e.g. search for static methods that return a FudgeObjectBuilder/FudgeMessageBuilder/FudgeBuilder instance for that class
  
  /**
   * 
   */
  public FudgeDefaultBuilderFactory () {
    addGenericBuilderInternal (Map.class, MapBuilder.INSTANCE);
    addGenericBuilderInternal (List.class, ListBuilder.INSTANCE);
    addGenericBuilderInternal (FudgeFieldContainer.class, FudgeFieldContainerBuilder.INSTANCE);
    addGenericBuilderInternal (Class.class, JavaClassBuilder.INSTANCE);
  }
  
  private Map<Class<?>,FudgeBuilder<?>> getGenericBuilders () {
    return _genericBuilders;
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
    if ((builder = FromFudgeMsgObjectBuilder.create (clazz)) != null) return builder;
    if ((builder = FudgeMsgConstructorObjectBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    if ((builder = (FudgeObjectBuilder<T>)getGenericBuilders ().get (clazz)) != null) return builder;
    if (clazz.isInterface ()) return null;
    //return ReflectionObjectBuilder.create (clazz);
    return JavaBeanBuilder.create (clazz);
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
    if ((builder = ToFudgeMsgMessageBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    for (Map.Entry<Class<?>,FudgeBuilder<?>> defaultBuilder : getGenericBuilders ().entrySet ()) {
      if (defaultBuilder.getKey ().isAssignableFrom (clazz)) return (FudgeMessageBuilder<T>)defaultBuilder.getValue ();
    }
    //return ReflectionMessageBuilder.create (clazz);
    return JavaBeanBuilder.create (clazz);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void addGenericBuilder (final Class<T> clazz, final FudgeBuilder<T> builder) {
    addGenericBuilderInternal (clazz, builder);
  }
  
  private <T> void addGenericBuilderInternal (final Class<T> clazz, final FudgeBuilder<? extends T> builder) {
    getGenericBuilders ().put (clazz, builder);
  }

}