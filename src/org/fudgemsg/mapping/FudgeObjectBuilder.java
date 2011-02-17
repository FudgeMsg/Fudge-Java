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

import org.fudgemsg.FudgeFieldContainer;

/**
 * Defines an object capable of constructing a Java object from a Fudge message
 * 
 * @param <T> the Java type this builder deserialises Fudge messages to
 * 
 * @author Andrew Griffin
 */
public interface FudgeObjectBuilder<T> {
  
  /**
   * Decodes the message into an instance of type T.
   * 
   * @param context the {@link FudgeDeserializationContext}
   * @param message the origin Fudge message
   * @return the created object
   */
  T buildObject (FudgeDeserializationContext context, FudgeFieldContainer message);
  
}