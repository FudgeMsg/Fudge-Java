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
package org.fudgemsg.taxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link TaxonomyResolver} which is backed by a {@link Map}.
 * This is mostly useful where the entire set of taxonomies is known at module
 * initialization (or compilation) time. As for performance reasons the
 * {@link Map} is fixed at instantiation time, it is not appropriate for
 * situations where the set of taxonomies will change at runtime.
 *
 * @author kirk
 */
public class ImmutableMapTaxonomyResolver implements TaxonomyResolver {
  private final Map<Short, FudgeTaxonomy> _taxonomiesById;
  
  /**
   * The default constructor will result in a resolver that never
   * resolves any taxonomies.
   */
  public ImmutableMapTaxonomyResolver() {
    this(Collections.<Short,FudgeTaxonomy>emptyMap());
  }
  
  /**
   * Creates a taxonomy resolver from a map of taxonomy identifier to {@link FudgeTaxonomy} objects.
   * 
   * @param taxonomiesById a map of taxonomy identifiers to taxonomy instances
   */
  public ImmutableMapTaxonomyResolver(Map<Short, FudgeTaxonomy> taxonomiesById) {
    if(taxonomiesById == null) {
      taxonomiesById = Collections.emptyMap();
    }
    _taxonomiesById = new HashMap<Short, FudgeTaxonomy>(taxonomiesById);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public FudgeTaxonomy resolveTaxonomy(short taxonomyId) {
    return _taxonomiesById.get(taxonomyId);
  }

}
