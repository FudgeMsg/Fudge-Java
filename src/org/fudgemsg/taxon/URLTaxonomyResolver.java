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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A taxonomy resolver that will retrieve a taxonomy from a URL. A concrete
 * implementation must specify how the URL is constructed. This resolver will
 * cache the taxonomy objects.
 * 
 * @author Andrew Griffin
 */
public abstract class URLTaxonomyResolver implements TaxonomyResolver {
  
  private final ConcurrentMap<Short,FudgeTaxonomy> _cache = new ConcurrentHashMap<Short,FudgeTaxonomy> ();
  
  /**
   * Creates a new taxonomy resolver.
   */
  protected URLTaxonomyResolver () {
  }
  
  /**
   * {@docInherit}
   */
  @Override
  public FudgeTaxonomy resolveTaxonomy(final short taxonomyId) {
    FudgeTaxonomy taxon = getCache ().get (taxonomyId);
    if (taxon == null) {
      try {
        FudgeTaxonomy freshTaxon = new PropertyFileTaxonomy (createTaxonomyURL (taxonomyId));
        taxon = getCache ().putIfAbsent (taxonomyId, freshTaxon);
        if (taxon == null) {
          taxon = freshTaxon;
        }
      } catch (FudgeRuntimeException e) {
        if (e.getCause () instanceof FileNotFoundException) {
          return null;
        } else {
          throw e;
        }
      } catch (MalformedURLException e) {
        throw new FudgeRuntimeException ("couldn't create URL for taxonomy " + taxonomyId, e);
      }
    }
    return taxon;
  }
  
  private ConcurrentMap<Short,FudgeTaxonomy> getCache () {
    return _cache;
  }
  
  /**
   * Empties the resolver cache.
   */
  public void reset () {
    getCache ().clear ();
  }
  
  /**
   * Returns the URL that the taxonomy corresponding to the ID should be loaded from.
   * 
   * @param taxonomyId the taxonomy ID
   * @return the URL
   * @throws MalformedURLException if there is a problem creating the URL. {@link #resolveTaxonomy} will wrap this in a {@link FudgeRuntimeException}.
   */
  protected abstract URL createTaxonomyURL (final short taxonomyId) throws MalformedURLException;
  
}