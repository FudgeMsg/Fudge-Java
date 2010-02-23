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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.fudgemsg.FudgeRuntimeException;

/**
 * An implementation of {@link MapFudgeTaxonomy} that is populated from an arbitrary
 * source of properties.
 * 
 * @author Andrew Griffin
 */
public class PropertyFileTaxonomy extends MapFudgeTaxonomy {
  
  /**
   * <p>Creates a taxonomy from a {@link ResourceBundle}. For example:</p>
   * 
   * <pre>
   * MyTaxonomy.properties
   * 
   * 1 = id
   * 2 = name
   * 3 = email
   * 4 = telephone
   * </pre>
   * 
   * <p>Could be loaded using:</p>
   * 
   * <pre>
   * new PropertyFileTaxonomy (ResourceBundle.getBundle ("MyTaxonomy")) 
   * </pre>
   * 
   * @param resourceBundle the taxonomy representation
   */
  public PropertyFileTaxonomy (final ResourceBundle resourceBundle) {
    super (resourceBundleToMap (resourceBundle));
  }
  
  /**
   * <p>Creates a taxonomy from a {@link Properties}. The keys correspond to ordinal values, and the values are field names</p>
   * 
   * @param properties the taxonomy representation
   */
  public PropertyFileTaxonomy (final Properties properties) {
    super (propertiesToMap (properties));
  }
  
  /**
   * <p>Creates a taxonomy from a {@link URL}. The URL must point to an XML document that
   * can be loaded with {@link Properties#loadFromXML}.</p>
   * 
   * @param url URL pointing toward an XML document that describes the taxonomy
   */
  public PropertyFileTaxonomy (final URL url) {
    super (urlToMap (url));
  }
  
  private static Map<Integer,String> resourceBundleToMap (final ResourceBundle resourceBundle) {
    final Map<Integer,String> map = new HashMap<Integer,String> ();
    for (String key : resourceBundle.keySet ()) {
      try {
        map.put (Integer.valueOf (key), resourceBundle.getString (key));
      } catch (NumberFormatException nfe) {
        throw new FudgeRuntimeException ("property file invalid - entry " + key + "/" + resourceBundle.getString (key), nfe);
      }
    }
    return map;
  }
  
  private static Map<Integer,String> propertiesToMap (final Properties properties) {
    final Map<Integer,String> map = new HashMap<Integer,String> ();
    for (Map.Entry<Object,Object> entry : properties.entrySet ()) {
      try {
        map.put (Integer.valueOf ((String)entry.getKey ()), (String)entry.getValue ());
      } catch (NumberFormatException nfe) {
        throw new FudgeRuntimeException ("property file invalid - entry " + entry.getKey () + "/" + entry.getValue (), nfe);
      }
    }
    return map;
  }
  
  private static Map<Integer,String> urlToMap (final URL url) {
    final Properties taxon = new Properties ();
    try {
      final URLConnection urlcon = url.openConnection ();
      final InputStream in = urlcon.getInputStream ();
      taxon.loadFromXML (in);
      in.close ();
    } catch (IOException e) {
      throw new FudgeRuntimeException ("Error reading from URL " + url, e);
    }
    return propertiesToMap (taxon);
  }
  
}