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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.ClasspathUtilities;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * Extensible dictionary of types that Fudge can convert to and from wire format.
 * <p>
 * This class contains a cache of mappings from Java types to Fudge messages.
 * There is one instance of the dictionary per {@link FudgeContext context}.
 * <p>
 * Mappings may be added in three main ways.
 * <p>
 * The simplest way is to create an instance of {@link FudgeBuilder} and call the
 * {@code addBuilder} method. This will register an instance of the builder for a specific type.
 * Subclasses of the type will not use the builder.
 * <p>
 * The second mechanism is classpath scanning. Simply annotate the builder class with
 * {@link FudgeBuilderFor}, and call {@code addAllClasspathBuilders}.
 * The method can be slow when operating on a large classpath.
 * The system property {@code org.fudgemsg.autoscan} allows this to be done automatically.
 * <p>
 * The third method is generic builders. This class contains a single instance of
 * {@link FudgeBuilderFactory}, which is capable of creating builders on demand.
 * See {@link FudgeDefaultBuilderFactory} for the default list of handled types.
 * Further generic builders can be registered with the factory.
 * These generic builders will handle subclasses of the registered type.
 * <p>
 * All builder caching is done in this class.
 * The factory is not responsible for caching.
 * <p>
 * Registering a different factory, or registering additional/different generic builders can
 * change the default behavior for unrecognized types. As such, it is recommended to only
 * initialize the dictionary at system startup. However, the cache is concurrent, so will
 * handle later additions.
 * 
 * @author Andrew Griffin
 */
public class FudgeObjectDictionary {
  /**
   * The name of the property to be set (to any value) to automatically scan the classpath
   * for builders on startup.
   */
  public static final String AUTO_CLASSPATH_SCAN_PROPERTY = "org.fudgemsg.autoscan";
  
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
  
  private final ConcurrentMap<Class<?>, FudgeObjectBuilder<?>> _objectBuilders;
  private final ConcurrentMap<Class<?>, FudgeMessageBuilder<?>> _messageBuilders;
  private final AtomicBoolean _haveScannedClasspath = new AtomicBoolean(false);
  
  private FudgeBuilderFactory _defaultBuilderFactory;
  
  /**
   * Constructs a new (initially empty) {@link FudgeObjectDictionary}.
   */
  public FudgeObjectDictionary () {
    _objectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>> ();
    _messageBuilders = new ConcurrentHashMap<Class<?>, FudgeMessageBuilder<?>> ();
    _defaultBuilderFactory = new FudgeDefaultBuilderFactory();
    
    if (System.getProperty(AUTO_CLASSPATH_SCAN_PROPERTY) != null) {
      addAllAnnotatedBuilders();
    }
  }
  
  /**
   * Constructs a new {@link FudgeObjectDictionary} as a clone of another.
   * 
   * @param other the {@code FudgeObjectDictionary} to clone
   */
  public FudgeObjectDictionary(final FudgeObjectDictionary other) {
    _objectBuilders = new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>> (other._objectBuilders);
    _messageBuilders = new ConcurrentHashMap<Class<?>, FudgeMessageBuilder<?>> (other._messageBuilders);
    _defaultBuilderFactory = new ImmutableFudgeBuilderFactory (other._defaultBuilderFactory);
  }
  
  /**
   * Returns the current builder factory for unregistered types.
   * 
   * @return the current {@link FudgeBuilderFactory}.
   */
  public FudgeBuilderFactory getDefaultBuilderFactory () {
    return _defaultBuilderFactory;
  }
  
  /**
   * Sets the builder factory to use for types that are not explicitly registered here. It is recommended that {@link FudgeBuilderFactory}
   * implementations are made using the {@link FudgeBuilderFactoryAdapter}, constructed with the previously set factory so that the behaviours
   * can be chained. 
   * 
   * @param defaultBuilderFactory the {@code FudgeBuilderFactory} to use
   */
  public void setDefaultBuilderFactory (final FudgeBuilderFactory defaultBuilderFactory) {
    _defaultBuilderFactory = defaultBuilderFactory;
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
  public <T> void addObjectBuilder (final Class<T> clazz, final FudgeObjectBuilder<? extends T> builder) {
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
  public <T> void addMessageBuilder (final Class<T> clazz, final FudgeMessageBuilder<? super T> builder) {
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
   * it will attempt to create one using the registered {@link FudgeBuilderFactory}. If it is not possible to create a builder (e.g. for an interface) returns {@code null}.
   * 
   * @param <T> Java type of the objects to be built
   * @param clazz the Java class to look up
   * @return the builder, or {@code null} if none is available
   */
  @SuppressWarnings("unchecked")
  public <T> FudgeObjectBuilder<T> getObjectBuilder (final Class<T> clazz) {
    FudgeObjectBuilder<T> builder = (FudgeObjectBuilder<T>)_objectBuilders.get (clazz);
    if (builder == null) {
      FudgeObjectBuilder<T> freshBuilder = getDefaultBuilderFactory ().createObjectBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeObjectBuilder<T>)NULL_OBJECTBUILDER;
      builder = (FudgeObjectBuilder<T>)_objectBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) {
        // we used to store a reference at this point if it also implemented FudgeMessageBuilder , but there might be better message builders available than this one
        builder = freshBuilder;
      }
    }
    return (builder == NULL_OBJECTBUILDER) ? null : builder;
  }
  
  /**
   * Returns a {@link FudgeMessageBuilder} for the given class to convert a Fudge message to a Java object. If none is already registered for the class,
   * it will attempt to create one using the registered {@link FudgeBuilderFactory}. If it is not possible to create a builder returns {@code null}.
   * 
   * @param <T> Java type of the objects to be built
   * @param clazz the Java class to look up
   * @return the builder, or {@code null} if none is available
   */
  @SuppressWarnings("unchecked")
  public <T> FudgeMessageBuilder<T> getMessageBuilder (final Class<T> clazz) {
    FudgeMessageBuilder<T> builder = (FudgeMessageBuilder<T>)_messageBuilders.get (clazz);
    if (builder == null) {
      FudgeMessageBuilder<T> freshBuilder = getDefaultBuilderFactory ().createMessageBuilder (clazz);
      if (freshBuilder == null) freshBuilder = (FudgeMessageBuilder<T>)NULL_MESSAGEBUILDER;
      builder = (FudgeMessageBuilder<T>)_messageBuilders.putIfAbsent (clazz, freshBuilder);
      if (builder == null) {
        // we used to store a reference at this point if it also implemented FudgeObjectBuilder, but there might be better object builders available than this one
        builder = freshBuilder;
      }
    }
    return (builder == NULL_MESSAGEBUILDER) ? null : builder;
  }

  /**
   * Tests if the specification requires a default serialization, for example lists, maps, sets and arrays. Class headers
   * are never needed and must be suppressed for default objects. The objects are just written with ordinal field values
   * greater than {@code 0}.
   * 
   * @param clazz the class to test, not {@code null}
   * @return {@code true} if the object has a default serialization scheme.
   */
  public boolean isDefaultObject(final Class<?> clazz) {
    // TODO move this logic to the builder factory so that it can be overridden
    return List.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)
        || clazz.isArray();
  }

  /**
   * Returns the class indicated by a default serialization scheme.
   * 
   * @param maxOrdinal the highest ordinal used, or {@code 0} if no field ordinals were present.
   * @return the class to deserialize to, or {@code null} if the ordinal is not recognised
   */
  public Class<?> getDefaultObjectClass(final int maxOrdinal) {
    // TODO move this logic to the builder factory so that it can be overridden
    switch (maxOrdinal) {
      case 0:
        return List.class;
      case 1:
        return Set.class;
      case 2:
        return Map.class;
    }
    return null;
  }

  /**
   * Scans all files available to common classpath loading system heuristics to determine
   * which ones have the {@link FudgeBuilderFor} annotation, and registers those as appropriate
   * builders.
   * This is potentially a <em>very</em> expensive operation, and as such is optional.
   */
  public void addAllAnnotatedBuilders() {
    if (_haveScannedClasspath.getAndSet(true)) {
      return;
    }
    
    Set<String> classNamesWithAnnotation = ClasspathUtilities.getClassNamesWithAnnotation(FudgeBuilderFor.class);
    if (classNamesWithAnnotation == null) {
      return;
    }
    for (String className : classNamesWithAnnotation) {
      addAnnotatedBuilderClass(className);
    }
    classNamesWithAnnotation = ClasspathUtilities.getClassNamesWithAnnotation(GenericFudgeBuilderFor.class);
    if (classNamesWithAnnotation == null) {
      return;
    }
    for (String className : classNamesWithAnnotation) {
      addAnnotatedGenericBuilderClass(className);
    }
  }

  /**
   * Add a class which is known to have a {@link FudgeBuilderFor} annotation as an object or message
   * builder (or both). 
   * 
   * @param className The fully qualified name of the builder class.
   */
  @SuppressWarnings("unchecked")
  public void addAnnotatedBuilderClass(String className) {
    Class<?> builderClass = instantiateBuilderClass(className);
    
    if ((builderClass == null)
        || !builderClass.isAnnotationPresent(FudgeBuilderFor.class)) {
      return;
    }
    
    Object builderInstance = null;
    try {
      builderInstance = builderClass.newInstance();
    } catch (Exception e) {
      // Do nothing other than stack trace.
      e.printStackTrace();
      return;
    }
    Class<?> forClass = builderClass.getAnnotation(FudgeBuilderFor.class).value();
    
    if (builderInstance instanceof FudgeMessageBuilder) {
      addMessageBuilder(forClass, (FudgeMessageBuilder) builderInstance);
    }
    
    if (builderInstance instanceof FudgeObjectBuilder) {
      addObjectBuilder(forClass, (FudgeObjectBuilder) builderInstance);
    }
  }
  
  /**
   * Add a class which is known to have a {@link GenericFudgeBuilderFor} annotation as builder. 
   * 
   * @param className The fully qualified name of the builder class.
   */
  @SuppressWarnings("unchecked")
  public void addAnnotatedGenericBuilderClass(String className) {
    Class<?> builderClass = instantiateBuilderClass(className);
    
    if ((builderClass == null)
        || !builderClass.isAnnotationPresent(GenericFudgeBuilderFor.class)) {
      return;
    }
    
    Object builderInstance = null;
    try {
      builderInstance = builderClass.newInstance();
    } catch (Exception e) {
      // Do nothing other than stack trace.
      e.printStackTrace();
      return;
    }
    Class<?> forClass = builderClass.getAnnotation(GenericFudgeBuilderFor.class).value();
    
    if (!(builderInstance instanceof FudgeBuilder)) {
      throw new IllegalArgumentException("Annotated a generic builder " + builderClass + " but not a full FudgeBuilder<> implementation.");
    }
    getDefaultBuilderFactory().addGenericBuilder(forClass, (FudgeBuilder) builderInstance);
  }

  /**
   * @param className
   * @return
   */
  private Class<?> instantiateBuilderClass(String className) {
    Class<?> builderClass = null;
    try {
      builderClass = Class.forName(className);
    } catch (Exception e) {
      // Silently swallow. Can't actually populate it.
      // This should be rare, and you can just stop at this breakpoint
      // (which is why the stack trace is here at all).
      e.printStackTrace();
    }
    return builderClass;
  }
  
}
