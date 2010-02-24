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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A concrete implementation of a {@link URLTaxonomyResolver} that constructs URLs by
 * substituting the taxonomy ID into a given string.
 * 
 * @author Andrew Griffin
 */
public class RESTfulTaxonomyResolver extends URLTaxonomyResolver {
  
  private final URL _context;
  private final String _specPrefix;
  private final String _specSuffix;
  
  /**
   * Creates a new taxonomy resolver, using a string relative to an existing URL. See {@link URL#URL(URL,String)}
   * for more details.
   * 
   * @param context URL context if the substitution string is relative, or {@code null}
   * @param specPrefix String fragment to precede the taxonomy ID, or {@code null}
   * @param specSuffix String fragment to follow the taxonomy ID, or {@code null}
   */
  public RESTfulTaxonomyResolver (final URL context, final String specPrefix, final String specSuffix) {
    _context = context;
    _specPrefix = specPrefix;
    _specSuffix = specSuffix;
  }
  
  /**
   * Creates a new taxonomy resolver. The URL is constructed as {@code specPrefix} + {@code taxonomyId} + {@code specSuffix}.
   * E.g. Creating the resolver as {@code RESTfulTaxonomyResolver("http://fudgemsg.org/taxonomies/", ".xml")} would resolve
   * taxonomy ID 42 to the document at {@code http://fudgemsg.org/taxonomies/42.xml}.
   * 
   * @param specPrefix String fragment to precede the taxonomy ID, or {@code null}
   * @param specSuffix String fragment to follow the taxonomy ID, or {@code null}
   */
  public RESTfulTaxonomyResolver (final String specPrefix, final String specSuffix) {
    this (null, specPrefix, specSuffix);
  }
  
  /**
   * Creates a new taxonomy resolver. The URL is constructed as {@code specPrefix} + {@code taxonomyId}.
   * 
   * @param specPrefix String fragment to precede the taxonomy ID
   */
  public RESTfulTaxonomyResolver (final String specPrefix) {
    this (null, specPrefix, null);
  }
  
  /**
   * Returns the context URL, or {@code null} if there isn't one.
   * 
   * @return the {@link URL}
   */
  protected URL getContext () {
    return _context;
  }
  
  /**
   * Returns the string fragment to precede the taxonomy ID, or {@code null} for none.
   * 
   * @return the string fragment
   */
  protected String getSpecPrefix () {
    return _specPrefix;
  }
  
  /**
   * Returns the string fragment to follow the taxonomy ID, or {@code null} for none.
   * 
   * @return the string fragment
   */
  protected String getSpecSuffix () {
    return _specSuffix;
  }

  /**
   * {@docInherit}
   */
  @Override
  protected URL createTaxonomyURL(final short taxonomyId) throws MalformedURLException {
    final StringBuilder sb = new StringBuilder ();
    if (getSpecPrefix () != null) sb.append (getSpecPrefix ());
    sb.append (taxonomyId);
    if (getSpecSuffix () != null) sb.append (getSpecSuffix ());
    return new URL (getContext (), sb.toString ());
  }
  
}