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
import java.util.HashMap;

import org.fudgemsg.FudgeFieldContainer;

/**
 * Default behaviours for building and decoding Fudge messages.
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
 * Builder objects are not cached by this implementation. Requests for builders should be marshalled
 * through a {@link FudgeObjectDictionary} which will cache results. This object is publicly
 * exposed so that default builders may be used in the construction of custom builders before they
 * are registered with a dictionary. 
 * 
 * @author Andrew
 */ 
public class FudgeDefaultBuilder {
  
  // TODO 2010-01-29 Andrew -- we could have a builder builder, e.g. search for static methods that return a FudgeObjectBuilder/FudgeMessageBuilder/FudgeBuilder instance for that class
  
  private static final Map<Class<?>,FudgeBuilder<?>> s_defaultBuilders = resolveDefaultBuilders ();
  
  private static Map<Class<?>,FudgeBuilder<?>> resolveDefaultBuilders () {
    final Map<Class<?>,FudgeBuilder<?>> defaults = new HashMap<Class<?>,FudgeBuilder<?>> ();
    defaults.put (Map.class, MapBuilder.INSTANCE);
    defaults.put (List.class, ListBuilder.INSTANCE);
    defaults.put (FudgeFieldContainer.class, FudgeFieldContainerBuilder.INSTANCE);
    defaults.put (Class.class, JavaClassBuilder.INSTANCE);
    // TODO 2010-01-29 Andrew -- we could use a chain of property files or some other initial registration mechanism: this could be used to natively support MongoDB and similar things etc...
    return defaults;
  }
  
  private static Map<Class<?>,FudgeBuilder<?>> getDefaultBuilders () {
    return s_defaultBuilders;
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
  @SuppressWarnings("unchecked")
  public static <T> FudgeObjectBuilder<T> defaultObjectBuilder (final Class<T> clazz) {
    FudgeObjectBuilder<T> builder;
    if ((builder = FromFudgeMsgObjectBuilder.create (clazz)) != null) return builder;
    if ((builder = FudgeMsgConstructorObjectBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    if ((builder = (FudgeObjectBuilder<T>)getDefaultBuilders ().get (clazz)) != null) return builder;
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
  @SuppressWarnings("unchecked")
  public static <T> FudgeMessageBuilder<T> defaultMessageBuilder (final Class<T> clazz) {
    FudgeMessageBuilder<T> builder;
    if ((builder = ToFudgeMsgMessageBuilder.create (clazz)) != null) return builder;
    if (clazz.isArray ()) return new ArrayBuilder (clazz.getComponentType ());
    for (Map.Entry<Class<?>,FudgeBuilder<?>> defaultBuilder : getDefaultBuilders ().entrySet ()) {
      if (defaultBuilder.getKey ().isAssignableFrom (clazz)) return (FudgeMessageBuilder<T>)defaultBuilder.getValue ();
    }
    //return ReflectionMessageBuilder.create (clazz);
    return JavaBeanBuilder.create (clazz);
  }

  private FudgeDefaultBuilder () {
  }
  
}