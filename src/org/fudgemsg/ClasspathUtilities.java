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

package org.fudgemsg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.scannotation.AnnotationDB;

/**
 * A collection of utilities for automatically populating Fudge contexts based on classpath scanning.
 *
 * @author Kirk Wylie
 */
public final class ClasspathUtilities {

  /**
   * The property used to specify where the path annotations should be cached to avoid costly scanning.
   */
  public static final String ANNOTATION_CACHE_PATH_PROPERTY = "fudgemsg.annotationCachePath";

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
    Set<URL> results = new LinkedHashSet<URL>();
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

  private static File getCacheFile(final String cachePath, final Class<? extends Annotation> annotationClass) {
    return new File(cachePath + File.separatorChar + "." + annotationClass.getSimpleName());
  }

  /**
   * Returns a set of classes with the declared annotation, using a cache file on disk if one is
   * available.
   * 
   * @param annotationClass annotation to search for
   * @return the set of class names
   */
  public static Set<String> getClassNamesWithAnnotation(final Class<? extends Annotation> annotationClass) {
    final String cachePath = System.getProperty(ANNOTATION_CACHE_PATH_PROPERTY);
    Set<String> classes;
    if (cachePath != null) {
      final File cacheFile = getCacheFile(cachePath, annotationClass);
      if (cacheFile.exists()) {
        classes = new HashSet<String>();
        try {
          final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(cacheFile)));
          String line = lnr.readLine();
          while (line != null) {
            line = line.trim();
            if (line.length() > 0) {
              classes.add(line);
            }
            line = lnr.readLine();
          }
          lnr.close();
          return classes.isEmpty() ? null : classes;
        } catch (IOException e) {
          // Error reading the file
          classes = null;
        }
      }
    }
    classes = getAnnotationDB().getAnnotationIndex().get(annotationClass.getName());
    classes = (classes != null ? classes : Collections.<String>emptySet());
    if (cachePath != null) {
      final File cacheFile = getCacheFile(cachePath, annotationClass);
      try {
        final PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(cacheFile)));
        for (String className : classes) {
          pw.println(className);
        }
        pw.close();
      } catch (IOException e) {
        // Error writing the file
      }
    }
    return classes;
  }

}
