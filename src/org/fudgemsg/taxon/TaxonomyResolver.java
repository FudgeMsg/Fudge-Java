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

/**
 * A Taxonomy Resolver can identify a {@link FudgeTaxonomy} instance that is
 * appropriate for a message with a specific taxonomy ID.
 * This ID is actually appropriate for a particular application, and possibly
 * for a particular point in time. In fact, it may be appropriate for a particular
 * stream of data from a particular source, and some applications may have
 * multiple {@code TaxonomyResolver}s loaded into a single application. 
 *
 * @author Kirk Wylie
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
