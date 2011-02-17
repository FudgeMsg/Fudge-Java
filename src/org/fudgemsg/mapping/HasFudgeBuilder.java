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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be placed on any Java object to indicate that there is an
 * instance of {@link FudgeMessageBuilder} or {@link FudgeObjectBuilder} (or a full
 * {@link FudgeBuilder}) for that type.
 * This will then be picked up at runtime and automatically configured for that type.
 * <p/>
 * All parameters are optional, and only have default values of {@link Object} for convenience.
 *
 * @author Kirk Wylie
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface HasFudgeBuilder {

  /**
   * A class that implements both {@link FudgeMessageBuilder} and {@link FudgeObjectBuilder}
   * for the annotated type.
   * Convenience method (much like the convenience type {@link FudgeBuilder}). 
   */
  Class<?> builder() default Object.class;
  /**
   * A class that implements {@link FudgeObjectBuilder} for the annotated type.
   */
  Class<?> objectBuilder() default Object.class;
  /**
   * A class that implements {@link FudgeMessageBuilder} for the annotated type.
   */
  Class<?> messageBuilder() default Object.class;
}
