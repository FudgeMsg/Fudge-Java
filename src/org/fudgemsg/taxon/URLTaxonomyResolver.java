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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A taxonomy resolver that retrieves a taxonomy from a URL.
 * <p>
 * A concrete implementation must specify how the URL is constructed.
 * This resolver will cache the taxonomy objects.
 */
public abstract class URLTaxonomyResolver implements TaxonomyResolver {

  /**
   * The cache of taxonomies.
   */
  private final ConcurrentMap<Short, FudgeTaxonomy> _cache = new ConcurrentHashMap<Short, FudgeTaxonomy>();

  /**
   * Creates a new resolver.
   */
  protected URLTaxonomyResolver() {
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeTaxonomy resolveTaxonomy(final short taxonomyId) {
    FudgeTaxonomy taxonomy = _cache.get(taxonomyId);
    if (taxonomy == null) {
      try {
        FudgeTaxonomy freshTaxonomy = new PropertyFileTaxonomy(createTaxonomyURL(taxonomyId));
        taxonomy = _cache.putIfAbsent(taxonomyId, freshTaxonomy);
        taxonomy = _cache.get(taxonomyId);
      } catch (FudgeRuntimeException ex) {
        if (ex.getCause() instanceof FileNotFoundException) {
          return null;
        } else {
          throw ex;
        }
      } catch (MalformedURLException ex) {
        throw new FudgeRuntimeException("Unable to create URL for taxonomy: " + taxonomyId, ex);
      }
    }
    return taxonomy;
  }

  /**
   * Returns the URL that the taxonomy corresponding to the ID should be loaded from.
   * 
   * @param taxonomyId  the taxonomy ID to locate
   * @return the URL where the taxonomy is found, not null
   * @throws MalformedURLException if there is a problem creating the URL, this will be wrapped into a runtime exception
   */
  protected abstract URL createTaxonomyURL(final short taxonomyId) throws MalformedURLException;

  /**
   * Clears the resolver cache.
   */
  public void reset() {
    _cache.clear();
  }

}
