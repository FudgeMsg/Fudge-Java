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
import org.fudgemsg.mapping.ImmutableFudgeObjectDictionary;
import org.fudgemsg.taxon.TaxonomyResolver;

/**
 * <p>Immutable wrapper for a {@link FudgeContext} that will be used for the global
 * "default", or for use with {@link ImmutableFudgeMsg}. It cannot be configured
 * after construction.</p>
 * 
 * @author Andrew Griffin
 */
public class ImmutableFudgeContext extends FudgeContext {
  
  /**
   * Creates an immutable version of an existing {@link FudgeContext}. Immutable copies of the type and object dictionaries
   * are taken from the source context.
   * 
   * @param context the {@code FudgeContext} to base this on
   */
  public ImmutableFudgeContext (final FudgeContext context) {
    super.setTaxonomyResolver (context.getTaxonomyResolver ());
    super.setTypeDictionary (new ImmutableFudgeTypeDictionary (context.getTypeDictionary ()));
    super.setObjectDictionary (new ImmutableFudgeObjectDictionary (context.getObjectDictionary ()));
  }
  
  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setTaxonomyResolver (TaxonomyResolver taxonomyResolver) {
    throw new UnsupportedOperationException ("setTaxonomyResolver called on an immutable Fudge context");
  }
  
  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setTypeDictionary (FudgeTypeDictionary typeDictionary) {
    throw new UnsupportedOperationException ("setTypeDictionary called on an immutable Fudge context");
  }
  
  /**
   * Always throws an exception - this is an immutable context.
   */
  @Override
  public void setObjectDictionary (FudgeObjectDictionary objectDictionary) {
    throw new UnsupportedOperationException ("setObjectDictionary called on an immutable Fudge context");
  }
  
}