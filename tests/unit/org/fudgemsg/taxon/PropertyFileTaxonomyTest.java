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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Test the PropertyFileTaxonomy implementation
 * 
 * @author Andrew Griffin
 */
public class PropertyFileTaxonomyTest {
  
  private static final String XML_PATH = System.getProperty ("user.name") + "/PropertyFileTaxonomyTest.xml";
  private static final String WRITE_TAXONOMY_XML = "/var/www/html/" + XML_PATH;
  private static final String TAXONOMY_URL = "http://localhost/" + XML_PATH;
  
  private static void verifyTaxon (final FudgeTaxonomy taxon) {
    assertEquals (null, taxon.getFieldName ((short)0));
    assertEquals ("id", taxon.getFieldName ((short)1));
    assertEquals ("name", taxon.getFieldName ((short)2));
    assertEquals ("email", taxon.getFieldName ((short)3));
    assertEquals ("telephone", taxon.getFieldName ((short)4));
    assertEquals (null, taxon.getFieldName ((short)5));
    assertEquals (null, taxon.getFieldOrdinal ("class"));
    assertEquals (1, (short)taxon.getFieldOrdinal ("id"));
    assertEquals (2, (short)taxon.getFieldOrdinal ("name"));
    assertEquals (3, (short)taxon.getFieldOrdinal ("email"));
    assertEquals (4, (short)taxon.getFieldOrdinal ("telephone"));
    assertEquals (null, taxon.getFieldOrdinal (null));
  }
  
  private static Properties makeProperties () {
    final Properties props = new Properties ();
    props.setProperty ("1", "id");
    props.setProperty ("2", "name");
    props.setProperty ("3", "email");
    props.setProperty ("4", "telephone");
    return props;
  }
  
  /**
   * 
   * @param f [documentation not available]
   * @throws IOException [documentation not available]
   */
  /* package */ static void writeTaxonomyXML (final File f) throws IOException {
    final Properties props = makeProperties ();
    final FileOutputStream fos = new FileOutputStream (f);
    props.storeToXML (fos, "PropertyFileTaxonomyTest");
    fos.close ();
  }
  
  /**
   * 
   */
  @Test
  public void testResourceBundle () {
    final FudgeTaxonomy taxon = new PropertyFileTaxonomy (ResourceBundle.getBundle ("org.fudgemsg.taxon.MyTaxonomy"));
    verifyTaxon (taxon);
  }
  
  /**
   * 
   */
  @Test
  public void testProperties () {
    final FudgeTaxonomy taxon = new PropertyFileTaxonomy (makeProperties ());
    verifyTaxon (taxon);
  }
  
  /**
   * @throws IOException if there's a problem with the webserver or filesystem
   */
  @Test
  public void testURL () throws IOException {
    final File f = new File (WRITE_TAXONOMY_XML);
    if ((f.exists () && !f.canWrite ()) || !f.getParentFile ().canWrite ()) {
      System.out.println ("skipping PropertyFileTaxonomyTest::testURL test - no local webserver");
      return;
    }
    writeTaxonomyXML (f);
    final FudgeTaxonomy taxon = new PropertyFileTaxonomy (new URL (TAXONOMY_URL));
    verifyTaxon (taxon);
  }
  
}
