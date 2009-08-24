/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.taxon;

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
  
  public ImmutableMapTaxonomyResolver(Map<Short, FudgeTaxonomy> taxonomiesById) {
    if(taxonomiesById == null) {
      taxonomiesById = Collections.emptyMap();
    }
    _taxonomiesById = new HashMap<Short, FudgeTaxonomy>(taxonomiesById);
  }
  
  @Override
  public FudgeTaxonomy resolveTaxonomy(short taxonomyId) {
    return _taxonomiesById.get(taxonomyId);
  }

}
