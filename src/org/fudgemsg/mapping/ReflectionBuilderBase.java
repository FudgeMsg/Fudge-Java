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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for ReflectionObjectBuilder and ReflectionMessageBuilder. Contains some helper methods common to reading class methods.
 * 
 * @param <T> class that can be serialised or deserialised by this builder
 * @author Andrew
 */
/* package */ class ReflectionBuilderBase<T> {
  
  private final Map<String, Method> _methods;
  
  private static void findMethods (final Class<?> clazz, final Map<String, Method> methods, final String prefix, final int paramLength, final Class<?> recurseLimit) {
    if (clazz == Object.class) return;
    if (clazz.getSuperclass () != recurseLimit) {
      findMethods (clazz.getSuperclass (), methods, prefix, paramLength, recurseLimit);
    }
    final int prefixLength = prefix.length ();
    for (Method method : clazz.getDeclaredMethods ()) {
      final int modifiers = method.getModifiers ();
      if (!Modifier.isPublic (modifiers) || Modifier.isStatic (modifiers) || Modifier.isTransient (modifiers)) continue;
      final Class<?> params[] = method.getParameterTypes ();
      if (params.length != paramLength) continue;
      String name = method.getName ();
      if (name.length () <= prefixLength) continue;
      if (!Character.isUpperCase (name.charAt (prefixLength))) continue;
      if (!name.startsWith (prefix)) continue;
      name = Character.toLowerCase (name.charAt (prefixLength)) + name.substring (prefixLength + 1);
      methods.put (name, method);
    }
  }
  
  protected ReflectionBuilderBase (final Class<T> clazz, final String prefix, final int paramLength, final Class<?> recurseLimit) {
    _methods = new HashMap<String, Method> ();
    findMethods (clazz, _methods, prefix, paramLength, recurseLimit);
  }
  
  protected Map<String, Method> getMethods () {
    return _methods;
  }
  
}