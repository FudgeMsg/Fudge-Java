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

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import java.beans.PropertyDescriptor;
import java.beans.Beans;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * A message and object builder implementation using the BeanUtils tools to work with
 * Java Beans.
 * 
 * @param <T> Bean class that can be serialised or deserialised using this builder
 * @author Andrew
 */
/* package */ class JavaBeanBuilder<T> implements FudgeBuilder<T> {
  
  private final PropertyDescriptor[] _properties;
  private final String _beanName;
  
  /* package */ static <T> JavaBeanBuilder<T> create (final Class<T> clazz) {
    // make sure that getClass is ignored - we don't really want that in the resulting message
    final ArrayList<PropertyDescriptor> propList = new ArrayList<PropertyDescriptor> ();
    for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors (clazz)) {
      if (!prop.getName ().equals ("class")) propList.add (prop);
    }
    return new JavaBeanBuilder<T> (propList.toArray (new PropertyDescriptor[propList.size ()]), clazz.getName ());
  }
  
  private JavaBeanBuilder (final PropertyDescriptor[] properties, final String beanName) {
    _properties = properties;
    _beanName = beanName;
  }
  
  private PropertyDescriptor[] getProperties () {
    return _properties;
  }
  
  private String getBeanName () {
    return _beanName;
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(
      FudgeSerialisationContext context, T object) {
    final MutableFudgeFieldContainer message = context.newMessage ();
    try {
      for (PropertyDescriptor property : getProperties ()) {
        final Method getter = property.getReadMethod ();
        if (getter != null) {
          //System.out.println (property.getName () + "; " + getter);
          context.objectToFudgeMsg (message, property.getName (), null, getter.invoke (object));
        }
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

  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(FudgeDeserialisationContext context,
      FudgeFieldContainer message) {
    final T object;
    try {
      object = (T)Beans.instantiate (getClass ().getClassLoader (), getBeanName ());
      for (PropertyDescriptor property : getProperties ()) {
        final FudgeField field = message.getByName (property.getName ());
        if (field != null) {
          final Method setter = property.getWriteMethod ();
          if (setter != null) {
            setter.invoke (object, context.fieldValueToObject (property.getPropertyType (), field));
          }
        }
      }
    } catch (IOException e) {
      throw new FudgeRuntimeException ("Couldn't deserialise " + getBeanName (), e);
    } catch (ClassNotFoundException e) {
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