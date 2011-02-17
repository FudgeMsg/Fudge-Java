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

/**
 * Factory interface for constructing builders for classes that haven't been explicitly
 * registered with a {@link FudgeObjectDictionary}. The factory should not attempt to
 * cache results - the {@code FudgeObjectDictionary} will do that.
 * 
 * @author Andrew Griffin
 */
public interface FudgeBuilderFactory {
  
  /**
   * Creates a new {@link FudgeObjectBuilder} for deserializing Fudge messages into the given class.
   * 
   * @param <T> the class the builder should create objects of 
   * @param clazz the class the builder should create objects of
   * @return the builder or {@code null} if none is available
   */
  public <T> FudgeObjectBuilder<T> createObjectBuilder (Class<T> clazz);
  
  /**
   * Creates a new {@link FudgeMessageBuilder} for encoding objects of the given class into Fudge messages.
   * 
   * @param clazz the class the builder should create messages from
   * @param <T> the class the builder should create messages from
   * @return  the builder or {@code null} if none is available
   */
  public <T> FudgeMessageBuilder<T> createMessageBuilder (Class<T> clazz);
  
  /**
   * Registers a generic builder with the factory that may be returned as a {@link FudgeObjectBuilder} for
   * the class, or as a {@link FudgeMessageBuilder} for any sub-classes of the class. After calling this, a
   * factory may choose to return an alternative builder, but may not return {@code null} for a class which
   * the generic builder has been registered for.
   * 
   * @param clazz the generic type (probably an interface) the builder is for
   * @param builder the builder to register
   * @param <T> the generic type (probably an interface) the builder is for
   */
  public <T> void addGenericBuilder (Class<T> clazz, FudgeBuilder<T> builder);
  
}