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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which at runtime specifies that a particular class is a
 * {@link FudgeBuilder} or {@link FudgeObjectBuilder} for a particular
 * abstract or interface data type.
 * Where the intention is to invoke {@link FudgeBuilderFactory#addGenericBuilder(Class, FudgeBuilder)} rather
 * than {@link FudgeObjectDictionary#addBuilder(Class, FudgeBuilder)}, this annotation should be used instead.
 *
 * @author Kirk Wylie
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenericFudgeBuilderFor {
  
  /**
   * The generic class for which the annotated type is a {@link FudgeBuilder}.
   */
  Class<?> value();

}
