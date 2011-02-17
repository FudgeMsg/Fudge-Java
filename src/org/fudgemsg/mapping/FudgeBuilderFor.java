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
 * {@link FudgeMessageBuilder} or {@link FudgeObjectBuilder} for a particular
 * data type.
 * While {@link HasFudgeBuilder} allows the data object to specify what its builder(s) are,
 * in a case where a builder has been written external to a source data type, this annotation
 * allows {@link FudgeObjectDictionary#addAllAnnotatedBuilders()} to determine the
 * builder and automatically configure.
 *
 * @author Kirk Wylie
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FudgeBuilderFor {

  /**
   * The value for which this is a {@link FudgeObjectBuilder} or
   * {@link FudgeMessageBuilder}.
   */
  Class<?> value();
}
