/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc. and other contributors.
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

package org.fudgemsg;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.scannotation.AnnotationDB;

/**
 * A collection of utilities for automatically populating Fudge contexts based on classpath scanning.
 *
 * @author Kirk Wylie
 */
public final class ClasspathUtilities {
  private static volatile URL[] _classPathElements;
  private static volatile AnnotationDB _annotationDB;

  private ClasspathUtilities() {
  }
  
  /**
   * Obtain a database of annotations suitable for autoconfiguring Fudge based on
   * the current classpath.
   * 
   * @return A database of all annotations.
   */
  public static AnnotationDB getAnnotationDB() {
    // Don't bother locking. Double-scanning is acceptable, but not desirable,
    // and the synchronization is worse.
    if (_annotationDB == null) {
      URL[] classpathElements = getClassPathElements();
      AnnotationDB annotationDB = new AnnotationDB();
      annotationDB.setScanClassAnnotations(true);
      annotationDB.setScanFieldAnnotations(true);
      annotationDB.setScanMethodAnnotations(false);
      annotationDB.setScanParameterAnnotations(false);
      try {
        annotationDB.scanArchives(classpathElements);
      } catch (IOException e) {
        throw new FudgeRuntimeException("Unable to scan classpath elements for @FudgeBuilderFor annotations", e);
      }
      _annotationDB = annotationDB;
    }
    return _annotationDB;
  }
  
  /**
   * Determine URLs for all elements in the classpath.
   * @return URLs for all elements in the classpath.
   */
  public static URL[] getClassPathElements() {
    // Don't bother locking. Double-scanning is acceptable, but not desirable,
    // and the synchronization is worse.
    if (_classPathElements == null) {
      _classPathElements = findClassPathElements();
    }
    
    return _classPathElements;
  }

  /**
   * The version in Scannotation doesn't work properly with Eclipse, which will put in
   * project references that don't actually exist to help itself.
   */
  private static URL[] findClassPathElements() {
    List<URL> results = new LinkedList<URL>();
    String javaClassPath = System.getProperty("java.class.path");
    String[] paths = javaClassPath.split(Pattern.quote(File.pathSeparator));
    for (String path : paths) {
      File f = new File(path);
      if (!f.exists()) {
        continue;
      }
      URL url;
      try {
        url = f.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new FudgeRuntimeException("Could not convert file " + f + " to URL", e);
      }
      results.add(url);
    }
    return results.toArray(new URL[0]);
  }

}
