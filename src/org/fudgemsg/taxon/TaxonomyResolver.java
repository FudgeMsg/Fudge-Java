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

/**
 * A taxonomy resolver allows a taxonomy to be located by ID.
 * <p>
 * A Fudge system may have multiple taxonomies in use at any one time.
 * These may be different versions of one taxonomy, or an entirely different taxonomy.
 * A Fudge message includes a two-byte ID to the taxonomy in use.
 * Implementations of this interface convert the ID to a real taxonomy.
 * The exact meaning of the ID and the translation is application dependent.
 * <p>
 * This interface should be implemented with care to ensure Fudge operates correctly.
 * Implementations should be thread-safe.
 */
public interface TaxonomyResolver {

  /**
   * Looks up the taxonomy by ID.
  * 
   * @param taxonomyId  the ID of the taxonomy to look up
   * @return the taxonomy, null if not found
   */
  FudgeTaxonomy resolveTaxonomy(short taxonomyId);

}
