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

import java.net.MalformedURLException;
import java.net.URL;

import org.fudgemsg.FudgeRuntimeException;

/**
 * A taxonomy resolver implementation that obtains taxonomies over the network.
 * <p>
 * This creates URLs by substituting the taxonomy ID into a given string.
 */
public class RESTfulTaxonomyResolver extends URLTaxonomyResolver {

  /**
   * The context URL.
   */
  private URL _context;
  /**
   * The prefix.
   */
  private String _specPrefix;
  /**
   * The suffix.
   */
  private String _specSuffix;

  /**
   * Creates a new taxonomy resolver.
   * This constructor is provided for Bean style construction for use in parameter injection frameworks.
   */
  public RESTfulTaxonomyResolver() {
    this(null, null, null);
  }

  /**
   * Creates a new taxonomy resolver.
   * The URL is constructed as {@code specPrefix} + {@code taxonomyId}.
   * 
   * @param specPrefix String fragment to precede the taxonomy ID
   */
  public RESTfulTaxonomyResolver(final String specPrefix) {
    this(null, specPrefix, null);
  }

  /**
   * Creates a new taxonomy resolver.
   * The URL is constructed as {@code specPrefix} + {@code taxonomyId} + {@code specSuffix}.
   * For example, creating the resolver as {@code RESTfulTaxonomyResolver("http://fudgemsg.org/taxonomies/", ".xml")}
   * would resolve taxonomy ID 42 to the document at {@code http://fudgemsg.org/taxonomies/42.xml}.
   * 
   * @param specPrefix String fragment to precede the taxonomy ID, or {@code null}
   * @param specSuffix String fragment to follow the taxonomy ID, or {@code null}
   */
  public RESTfulTaxonomyResolver(final String specPrefix, final String specSuffix) {
    this(null, specPrefix, specSuffix);
  }

  /**
   * Creates a new taxonomy resolver, using a string relative to an existing URL.
   * See {@link URL#URL(URL,String)} for more details.
   * 
   * @param context  the URL context if the substitution string is relative, or {@code null}
   * @param specPrefix  the fragment to precede the taxonomy ID, null if none
   * @param specSuffix  the fragment to follow the taxonomy ID, null if none
   */
  public RESTfulTaxonomyResolver(final URL context, final String specPrefix, final String specSuffix) {
    setContext(context);
    setSpecPrefix(specPrefix);
    setSpecSuffix(specSuffix);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the context URL.
   * 
   * @return the context URL, null if none
   */
  public URL getContext() {
    return _context;
  }

  /**
   * Sets or clears the context URL.
   * 
   * @param contextURL  the new URL, may be null
   */
  public void setContext(final URL contextURL) {
    _context = contextURL;
  }

  /**
   * Sets or clears the context URL.
   * 
   * @param context  the new URL, may be null
   */
  public void setContext(final String context) {
    try {
      setContext((context != null) ? new URL(context) : null);
    } catch (MalformedURLException ex) {
      throw new FudgeRuntimeException("Context URL is invalid", ex);
    }
  }

  /**
   * Returns the string prefix to precede the taxonomy ID.
   * 
   * @return the string prefix, may be null
   */
  public String getSpecPrefix() {
    return _specPrefix;
  }

  /**
   * Sets or clears the string prefix to precede the taxonomy ID.
   * 
   * @param specPrefix  the new prefix, null for none
   */
  public void setSpecPrefix(final String specPrefix) {
    _specPrefix = specPrefix;
  }

  /**
   * Returns the string suffix to follow the taxonomy ID.
   * 
   * @return the string suffix, may be null
   */
  public String getSpecSuffix() {
    return _specSuffix;
  }

  /**
   * Sets or clears the string suffix to follow the taxonomy ID.
   * 
   * @param specSuffix  the new suffix, null for none
   */
  public void setSpecSuffix(final String specSuffix) {
    _specSuffix = specSuffix;
  }

  //-------------------------------------------------------------------------
  @Override
  protected URL createTaxonomyURL(final short taxonomyId) throws MalformedURLException {
    final StringBuilder sb = new StringBuilder();
    if (getSpecPrefix() != null) {
      sb.append(getSpecPrefix());
    }
    sb.append(taxonomyId);
    if (getSpecSuffix() != null) {
      sb.append(getSpecSuffix());
    }
    if (getContext() != null) {
      return new URL(getContext(), sb.toString());
    } else {
      return new URL(sb.toString());
    }
  }

}
