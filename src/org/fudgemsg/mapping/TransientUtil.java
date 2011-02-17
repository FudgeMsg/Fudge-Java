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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * Utility class for checking for a transient annotation on a Bean property. The Transient annotation from the
 * Java Persistence Framework should be used where available. Otherwise, there is a Transient annotation defined
 * within the org.fudgemsg.mapping package.
 * 
 * @author Andrew Griffin
 */
@SuppressWarnings("unchecked")
/* package */ class TransientUtil {
  
  private static final Class<? extends Annotation> s_fudgeTransient = FudgeTransient.class;
  private static final Class<? extends Annotation> s_javaxTransient;
  
  private TransientUtil () {
  }
  
  static {
    Class<? extends Annotation> javaxTransient = null;
    try {
      javaxTransient = (Class<? extends Annotation>)Class.forName ("javax.persistence.Transient");
    } catch (ClassNotFoundException e) {
      // ignore
    }
    s_javaxTransient = javaxTransient;
  }
  
  /**
   * Detects whether the {@code javax.persistence.Transient} or {@link FudgeTransient} annotation has been used on an element
   * 
   * @param element element to check
   * @return {@code true} if the annotation is present, {@code false} otherwise
   */
  public static boolean hasTransientAnnotation (final AnnotatedElement element) {
    if (s_javaxTransient != null) {
      if (element.getAnnotation (s_javaxTransient) != null) return true;
    }
    if (s_fudgeTransient != null) {
      if (element.getAnnotation (s_fudgeTransient) != null) return true;
    }
    return false;
  }
  
}