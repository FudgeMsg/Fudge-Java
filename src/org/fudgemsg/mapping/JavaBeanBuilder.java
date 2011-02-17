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

import java.beans.Beans;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.beanutils.PropertyUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * A message and object builder implementation using the BeanUtils tools to work with
 * Java Beans.
 * 
 * @param <T> Bean class that can be serialized or deserialized using this builder
 * @author Andrew Griffin
 */
/* package */ class JavaBeanBuilder<T> implements FudgeBuilder<T> {
  
  private static class JBProperty {
    private final String _name;
    private final Integer _ordinal;
    private final Method _read;
    private final Method _write;
    private final Class<?> _type;
    private JBProperty (final String name, final Integer ordinal, final Method read, final Method write, final Class<?> type) {
      _read = read;
      _write = write;
      _type = type;
      _name = name;
      _ordinal = ordinal;
    }
    private String getName () {
      return _name;
    }
    private Integer getOrdinal () {
      return _ordinal;
    }
    private Method getRead () {
      return _read;
    }
    private Method getWrite () {
      return _write;
    }
    private Class<?> getType () {
      return _type;
    }
  }
  
  private final JBProperty[] _properties;
  private final String _beanName;
  private final Constructor<T> _constructor;

  /**
   * Creates a new {@link JavaBeanBuilder} for a class.
   * 
   * @param <T> class the builder should process
   * @param clazz class the builder should process
   * @return the {@code JavaBeanBuilder}
   */
  /* package */ static <T> JavaBeanBuilder<T> create (final Class<T> clazz) {
    // customise the properties
    final ArrayList<JBProperty> propList = new ArrayList<JBProperty> ();
    for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors (clazz)) {
      // ignore the class
      if (prop.getName ().equals ("class")) continue;
      // check for FudgeFieldName annotations on either accessor or mutator
      FudgeFieldName annoName;
      FudgeFieldOrdinal annoOrdinal;
      String name = prop.getName ();
      Integer ordinal = null;
      if (prop.getWriteMethod () != null) {
        // give up if it's a transient property
        if (TransientUtil.hasTransientAnnotation (prop.getWriteMethod ())) continue;
        if ((annoName = prop.getWriteMethod ().getAnnotation (FudgeFieldName.class)) != null) name = annoName.value ();
        if ((annoOrdinal = prop.getWriteMethod ().getAnnotation (FudgeFieldOrdinal.class)) != null) {
          ordinal = (int)annoOrdinal.value ();
          if (annoOrdinal.noFieldName ()) name = null;
        }
      }
      if (prop.getReadMethod () != null) {
        // give up if it's a transient property
        if (TransientUtil.hasTransientAnnotation (prop.getReadMethod ())) continue;
        if ((annoName = prop.getReadMethod ().getAnnotation (FudgeFieldName.class)) != null) name = annoName.value ();
        if ((annoOrdinal = prop.getReadMethod ().getAnnotation (FudgeFieldOrdinal.class)) != null) {
          ordinal = (int)annoOrdinal.value ();
          if (annoOrdinal.noFieldName ()) name = null;
        }
      }
      propList.add (new JBProperty (name, ordinal, prop.getReadMethod (), prop.getWriteMethod (), prop.getPropertyType ()));
    }
    // try and find a constructor
    try {
      return new JavaBeanBuilder<T> (propList.toArray (new JBProperty[propList.size ()]), clazz.getConstructor ());
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    // otherwise bean behaviour (about 5 times slower!)
    return new JavaBeanBuilder<T> (propList.toArray (new JBProperty[propList.size ()]), clazz.getName ());
  }
  
  private JavaBeanBuilder (final JBProperty[] properties, final String beanName) {
    _properties = properties;
    _beanName = beanName;
    _constructor = null;
  }
  
  private JavaBeanBuilder (final JBProperty[] properties, final Constructor<T> constructor) {
    _properties = properties;
    _beanName = null;
    _constructor = constructor;
  }
  
  private JBProperty[] getProperties () {
    return _properties;
  }
  
  private String getBeanName () {
    return _beanName;
  }
  
  private Constructor<T> getConstructor () {
    return _constructor;
  }
  
  @SuppressWarnings("unchecked")
  private T newBeanObject () throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException, ClassNotFoundException {
    if (getConstructor () != null) {
      return getConstructor ().newInstance ();
    } else {
      // Warning: the Beans.instantiate method below was about 5 times slower in the perf tests
      return (T)Beans.instantiate (getClass ().getClassLoader (), getBeanName ());
    }
  }

  /**
   *
   */
  @Override
  public MutableFudgeFieldContainer buildMessage(
      FudgeSerializationContext context, T object) {
    final MutableFudgeFieldContainer message = context.newMessage ();
    try {
      for (JBProperty prop : getProperties ()) {
        if (prop.getRead () == null) continue;
        context.objectToFudgeMsgWithClassHeaders(message, prop.getName(), prop.getOrdinal(), prop.getRead().invoke(
            object), prop.getType());
      }
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't serialise " + object, e);
    }
    return message;
  }

  /**
   *
   */
  @Override
  public T buildObject(FudgeDeserializationContext context,
      FudgeFieldContainer message) {
    final T object;
    try {
      object = newBeanObject ();
      for (JBProperty prop : getProperties ()) {
        if (prop.getWrite () == null) continue;
        final FudgeField field;
        if (prop.getOrdinal () == null) {
          field = message.getByName (prop.getName ());
        } else {
          field = message.getByOrdinal (prop.getOrdinal ());
        }
        if (field == null) continue;
        prop.getWrite ().invoke (object, context.fieldValueToObject (prop.getType (), field));
      }
    } catch (IOException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (ClassNotFoundException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (InstantiationException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    }
    return object;
  }
}