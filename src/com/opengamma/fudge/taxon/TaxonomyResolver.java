/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.taxon;

/**
 * A Taxonomy Resolver can identify a {@link FudgeTaxonomy} instance that is
 * appropriate for a message with a specific taxonomy ID.
 * This ID is actually appropriate for a particular application, and possibly
 * for a particular point in time. In fact, it may be appropriate for a particular
 * stream of data from a particular source, and some applications may have
 * multiple {@code TaxonomyResolver}s loaded into a single application. 
 *
 * @author kirk
 */
public interface TaxonomyResolver {
  
  /**
   * Identify the taxonomy that should be used to resolve names with the
   * specified ID.
   * 
   * @param taxonomyId The ID of the taxonomy to load
   * @return The taxonomy, or {@code null}
   */
  FudgeTaxonomy resolveTaxonomy(short taxonomyId);

}
