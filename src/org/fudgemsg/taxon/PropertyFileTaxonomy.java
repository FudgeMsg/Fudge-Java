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
 * An immutable taxonomy implementation based a properties file.
 */
public class PropertyFileTaxonomy extends MapFudgeTaxonomy {

  /**
   * Creates a taxonomy from a {@link ResourceBundle}.
   * <p>
   * For example:
   * <pre>
   * MyTaxonomy.properties
   * 
   * 1 = id
   * 2 = name
   * 3 = email
   * 4 = telephone
   * </pre>
   * 
   * Could be loaded using:
   * <pre>
   * new PropertyFileTaxonomy(ResourceBundle.getBundle("MyTaxonomy")) 
   * </pre>
   * 
   * @param resourceBundle  the taxonomy representation to load, not null
   */
  public PropertyFileTaxonomy(final ResourceBundle resourceBundle) {
    super(resourceBundleToMap(resourceBundle));
  }

  /**
   * Creates a taxonomy from a {@code Properties} instance.
   * <p>
   * The keys are the ordinals and the values are the field names.
   * 
   * @param properties the taxonomy representation
   */
  public PropertyFileTaxonomy(final Properties properties) {
    super(propertiesToMap(properties));
  }

  /**
   * Creates a taxonomy from an XML document at a {@code URL}.
   * <p>
   * The URL must point to an XML document that can be loaded with
   * {@link Properties#loadFromXML}.
   * 
   * @param url URL pointing toward an XML document that describes the taxonomy
   */
  public PropertyFileTaxonomy(final URL url) {
    super(urlToMap(url));
  }

  private static Map<Integer, String> resourceBundleToMap(final ResourceBundle resourceBundle) {
    final Map<Integer, String> map = new HashMap<Integer, String>();
    for (String key : resourceBundle.keySet()) {
      try {
        map.put(Integer.valueOf(key), resourceBundle.getString(key));
      } catch (NumberFormatException ex) {
        throw new FudgeRuntimeException("property file invalid - entry " + key + "/" + resourceBundle.getString(key), ex);
      }
    }
    return map;
  }

  private static Map<Integer, String> propertiesToMap(final Properties properties) {
    final Map<Integer, String> map = new HashMap<Integer, String>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      try {
        map.put(Integer.valueOf((String) entry.getKey()), (String) entry.getValue());
      } catch (NumberFormatException ex) {
        throw new FudgeRuntimeException("property file invalid - entry " + entry.getKey() + "/" + entry.getValue(), ex);
      }
    }
    return map;
  }

  private static Map<Integer, String> urlToMap(final URL url) {
    final Properties properties = new Properties();
    InputStream in = null;
    try {
      final URLConnection urlcon = url.openConnection();
      in = urlcon.getInputStream();
      properties.loadFromXML(in);
    } catch (IOException ex) {
      throw new FudgeRuntimeException("Error reading from URL " + url, ex);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }
    return propertiesToMap(properties);
  }

}
