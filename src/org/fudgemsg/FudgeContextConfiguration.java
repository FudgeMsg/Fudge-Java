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
package org.fudgemsg;

import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Abstraction of {@link FudgeContext} customization operations, such as registering user defined
 * types or a specific taxonomy resolver. This can be used with {@link FudgeContext#setConfiguration}
 * to inject configuration into a context through Bean based frameworks such as Spring, or allow
 * different components of an application to be composed to register a complete set of types.
 * 
 * @author Andrew Griffin
 */
public abstract class FudgeContextConfiguration {
  
  private static class Composite extends FudgeContextConfiguration {
    
    private final FudgeContextConfiguration[] _configurations;
    
    public Composite (final FudgeContextConfiguration ... configurations) {
      _configurations = configurations;
    }
    
    private FudgeContextConfiguration[] getConfigurations () {
      return _configurations;
    }
    
    @Override
    public void configureFudgeTypeDictionary (final FudgeTypeDictionary dictionary) {
      for (FudgeContextConfiguration configuration : getConfigurations ()) {
        configuration.configureFudgeTypeDictionary (dictionary);
      }
    }
    
    @Override
    public void configureFudgeObjectDictionary (final FudgeObjectDictionary dictionary) {
      for (FudgeContextConfiguration configuration : getConfigurations ()) {
        configuration.configureFudgeObjectDictionary (dictionary);
      }
    }
    
    @Override
    public void configureFudgeContext (final FudgeContext fudgeContext) {
      for (FudgeContextConfiguration configuration : getConfigurations ()) {
        configuration.configureFudgeContext (fudgeContext);
      }
    }
    
  }
  
  /**
   * Updates the given dictionary to customize it for an application.
   * The default implementation here does nothing.
   * 
   * @param dictionary the dictionary to customize
   */
  public void configureFudgeTypeDictionary (final FudgeTypeDictionary dictionary) {
  }
  
  /**
   * Updates the given dictionary to customize it for an application.
   * The default implementation here does nothing.
   * 
   * @param dictionary the dictionary to customize
   */
  public void configureFudgeObjectDictionary (final FudgeObjectDictionary dictionary) {
  }
  
  /**
   * Updates any aspects of the context. The default implementation just calls the {@link #configureFudgeTypeDictionary} and {@link #configureFudgeObjectDictionary} methods.
   * 
   * @param fudgeContext the context to configure
   */
  public void configureFudgeContext (final FudgeContext fudgeContext) {
    configureFudgeTypeDictionary (fudgeContext.getTypeDictionary ());
    configureFudgeObjectDictionary (fudgeContext.getObjectDictionary ());
  }
  
  /**
   * Creates a composite configuration object that calls methods from the supplied configuration
   * before those from this one. For example if three tool libraries all provide configuration
   * objects {@code a, b, c}, the composite {@code c.compose (b.compose (a))} will apply operations
   * from {@code a}, then {@code b}, and finally {@code c}. A more efficient method however would
   * be to use {@code FudgeContextConfiguration.compose (a, b, c)}. 
   * 
   * @param runBefore configuration to apply before this objects configuration
   * @return the composite configuration object
   */
  public FudgeContextConfiguration compose (final FudgeContextConfiguration runBefore) {
    return new Composite (runBefore, this);
  }
  
  /**
   * Creates a composite configuration object that calls methods from the supplied objects in the
   * order given.
   * 
   * @param configurations the configuration objects to apply, in the order to apply them
   * @return the composite configuration object 
   */
  public static FudgeContextConfiguration compose (final FudgeContextConfiguration ... configurations) {
    return new Composite (configurations);
  }
  
}