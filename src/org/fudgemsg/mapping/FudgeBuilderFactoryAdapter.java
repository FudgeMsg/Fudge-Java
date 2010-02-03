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

/**
 * Implementation of a {@link FudgeBuilderFactory} that can delegate to another
 * instance for unrecognized classes. This pattern is to allow factories to be
 * chained together.
 * 
 * @author Andrew
 */
public class FudgeBuilderFactoryAdapter implements FudgeBuilderFactory {

  private final FudgeBuilderFactory _delegate;
  
  protected FudgeBuilderFactoryAdapter (final FudgeBuilderFactory delegate) {
    if (delegate == null) throw new NullPointerException ("delegate cannot be null");
    _delegate = delegate;
  }
  
  protected FudgeBuilderFactory getDelegate () {
    return _delegate;
  }
  
  @Override
  public <T> void addGenericBuilder(Class<T> clazz, FudgeBuilder<T> builder) {
    getDelegate ().addGenericBuilder (clazz, builder);
  }

  @Override
  public <T> FudgeMessageBuilder<T> createMessageBuilder(Class<T> clazz) {
    return getDelegate ().createMessageBuilder (clazz);
  }

  @Override
  public <T> FudgeObjectBuilder<T> createObjectBuilder(Class<T> clazz) {
    return getDelegate ().createObjectBuilder (clazz);
  }
  
}