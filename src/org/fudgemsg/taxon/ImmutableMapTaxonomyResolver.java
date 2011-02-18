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
package org.fudgemsg.taxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An immutable taxonomy resolver implemented on top of a map.
 * <p>
 * The map of taxonomy by ID is loaded and fixed at construction.
 * This is most useful when the taxonomies are known at startup.
 */
public class ImmutableMapTaxonomyResolver implements TaxonomyResolver {

  /**
   * The taxonomies by ID.
   */
  private final Map<Short, FudgeTaxonomy> _taxonomiesById;

  /**
   * Creates a resolver that contains no taxonomies.
   */
  public ImmutableMapTaxonomyResolver() {
    this(Collections.<Short, FudgeTaxonomy> emptyMap());
  }

  /**
   * Creates a resolver that contains the given taxonomies.
   * 
   * @param taxonomiesById  a map of taxonomy IDs to taxonomy instances, null treated as an empty map
   */
  public ImmutableMapTaxonomyResolver(Map<Short, FudgeTaxonomy> taxonomiesById) {
    if (taxonomiesById == null) {
      taxonomiesById = Collections.emptyMap();
    }
    _taxonomiesById = new HashMap<Short, FudgeTaxonomy>(taxonomiesById);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeTaxonomy resolveTaxonomy(short taxonomyId) {
    return _taxonomiesById.get(taxonomyId);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "TaxonomyResolver[" + _taxonomiesById.size() + " taxonomies]";
  }

}
