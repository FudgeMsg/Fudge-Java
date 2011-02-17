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
 * Immutable {@link FudgeBuilderFactory} implementation.
 * 
 * @author Andrew Griffin
 */
/* package */ class ImmutableFudgeBuilderFactory extends FudgeBuilderFactoryAdapter {
  
  /**
   * {@docInherit}
   * 
   * @param delegate instance to pass non-overridden method calls to 
   */
  /* package */ ImmutableFudgeBuilderFactory(FudgeBuilderFactory delegate) {
    super(delegate);
  }

  /**
   * Always throws an exception - this is an immutable factory
   * 
   * @param clazz the generic type (probably an interface) the builder is for
   * @param builder the builder to register
   * @param <T> the generic type (probably an interface) the builder is for
   */
  public <T> void addGenericBuilder (Class<T> clazz, FudgeBuilder<T> builder) {
    throw new UnsupportedOperationException ("addGenericBuilder called on immutable instance");
  }
  
}