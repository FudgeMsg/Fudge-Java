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

package org.fudgemsg.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeRuntimeIOException;
import org.fudgemsg.FudgeStreamReader;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.IndicatorType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A FudgeStreamReader implementation for decoding a stream of JSON encoded text into Fudge messages.
 * 
 * @author Andrew Griffin
 */
public class FudgeJSONStreamReader implements FudgeStreamReader {
  
  private final JSONSettings _settings;
  private final FudgeContext _fudgeContext;
  private final Reader _underlying;
  private final JSONTokener _tokener;
  
  private int _taxonomyId = 0;
  private FudgeTaxonomy _taxonomy = null;
  private int _processingDirectives = 0;
  private int _schemaVersion = 0;
  private FudgeStreamElement _currentElement = null;
  private String _fieldName = null;
  private Integer _fieldOrdinal = null;
  private Object _fieldValue = null;
  
  private final Stack<JSONObject> _objectStack = new Stack<JSONObject> ();
  private final Stack<Iterator<String>> _iteratorStack = new Stack<Iterator<String>> ();
  private final Queue<String> _fieldLookahead = new LinkedList<String> ();
  private final Queue<Object> _valueLookahead = new LinkedList<Object> ();
  
  public FudgeJSONStreamReader (final FudgeContext fudgeContext, final Reader underlying, final JSONSettings settings) {
    _fudgeContext = fudgeContext;
    _underlying = underlying;
    _settings = settings;
    _tokener = new JSONTokener (underlying);
  }
  
  public FudgeJSONStreamReader (final FudgeContext fudgeContext, final Reader reader) {
    this (fudgeContext, reader, new JSONSettings ());
  }
  
  protected Reader getUnderlying () {
    return _underlying;
  }
  
  protected JSONTokener getTokener () {
    return _tokener;
  }
  
  @Override
  public void close() {
    if (getUnderlying () != null) {
      try {
        getUnderlying ().close ();
      } catch (IOException e) {
        throw new FudgeRuntimeIOException (e);
      }
    }
  }
  
  protected RuntimeException wrapException (String message, final JSONException e) {
    message = "Error " + message + " from JSON stream";
    if (e.getCause () instanceof IOException) {
      return new FudgeRuntimeIOException (message, (IOException)e.getCause ());
    } else {
      return new FudgeRuntimeException (message, e);
    }
  }
  
  @Override
  public FudgeStreamElement getCurrentElement() {
    return _currentElement;
  }
  
  protected FudgeStreamElement setCurrentElement (final FudgeStreamElement currentElement) {
    return _currentElement = currentElement;
  }

  @Override
  public String getFieldName() {
    return _fieldName;
  }
  
  private void setNameAndOrdinal (final String name, final Integer ordinal) {
    _fieldName = name;
    _fieldOrdinal = ordinal;
  }
  
  protected void setCurrentFieldName (final String name) {
    if (name.length () == 0) {
      setNameAndOrdinal (null, null);
    } else {
      try {
        int ordinal = Integer.parseInt (name);
        setNameAndOrdinal (null, ordinal);
      } catch (NumberFormatException nfe) {
        setNameAndOrdinal (name, null);
      }
    }
  }
  
  @Override
  public Integer getFieldOrdinal() {
    return _fieldOrdinal;
  }

  @Override
  public FudgeFieldType<?> getFieldType() {
    return getFudgeContext ().getTypeDictionary ().getByJavaType (getFieldValue ().getClass ());
  }

  @Override
  public Object getFieldValue() {
    return _fieldValue;
  }
  
  protected void setFieldValue (final Object object) {
    // TODO match the object to see what we've got ...
    _fieldValue = object;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public int getProcessingDirectives() {
    return _processingDirectives;
  }

  @Override
  public int getSchemaVersion() {
    return _schemaVersion;
  }

  @Override
  public FudgeTaxonomy getTaxonomy() {
    return _taxonomy;
  }
  
  protected void setEnvelopeFields (final int processingDirectives, final int schemaVersion, final int taxonomyId) {
    _processingDirectives = processingDirectives;
    _schemaVersion = schemaVersion;
    _taxonomyId = taxonomyId;
    if (_taxonomyId != 0) {
      _taxonomy = getFudgeContext ().getTaxonomyResolver ().resolveTaxonomy ((short)_taxonomyId);
    } else {
      _taxonomy = null;
    }
  }

  @Override
  public short getTaxonomyId() {
    return (short)_taxonomyId;
  }

  @Override
  public boolean hasNext() {
    if (getCurrentElement () == null) {
      // haven't read anything yet (or have read a full message already)
      try {
        return getTokener ().more ();
      } catch (JSONException e) {
        throw wrapException ("testing for end of JSON stream", e);
      }
    }
    if (!_objectStack.isEmpty ()) {
      // More to read
      return true;
    } else {
      // Nothing more on our stack; return false to indicate end of a message field fragment
      setCurrentElement (null);
      return false;
    }
  }
  
  private int integerValue (final Object o) {
    if (o instanceof Number) {
      return ((Number)o).intValue ();
    } else {
      return 0;
    }
  }
  
  private void checkMessageEnd () {
    if ((_iteratorStack.size () == 1) && !_iteratorStack.peek ().hasNext () && _fieldLookahead.isEmpty ()) {
      _objectStack.pop ();
      _iteratorStack.pop ();
    }
  }
  
  private Object jsonArrayToPrimitiveArray (final JSONArray arr) throws JSONException {
    boolean arrInt = true, arrDouble = true, arrLong = true;
    for (int j = 0; j < arr.length (); j++) {
      Object arrValue = arr.get (j);
      if (JSONObject.NULL.equals (arrValue)) {
        arrInt = arrDouble = false;
        break;
      } else if (arrValue instanceof Number) {
        if (arrValue instanceof Integer) {
        } else if (arrValue instanceof Double) {
          arrInt = false;
          arrLong = false;
        } else if (arrValue instanceof Long) {
          arrInt = false;
        } else {
          arrInt = arrDouble = false;
        }
      } else if (arrValue instanceof JSONObject) {
        arrInt = arrDouble = false;
        break;
      } else if (arrValue instanceof JSONArray) {
        arrInt = arrDouble = false;
        break;
      }
    }
    if (arrInt) {
      final int[] data = new int[arr.length ()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number)arr.get (j)).intValue ();
      }
      return data;
    } else if (arrLong) {
      final long[] data = new long[arr.length ()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number)arr.get (j)).longValue ();
      }
      return data;
    } else if (arrDouble) {
      final double[] data = new double[arr.length ()];
      for (int j = 0; j < data.length; j++) {
        data[j] = ((Number)arr.get (j)).doubleValue ();
      }
      return data;
    } else {
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public FudgeStreamElement next() {
    try {
      JSONObject o;
      if (_objectStack.isEmpty ()) {
        _objectStack.push (o = new JSONObject (getTokener ()));
      } else {
        o = _objectStack.peek ();
      }
      Iterator<String> i;
      if (_iteratorStack.isEmpty ()) {
        _iteratorStack.push (i = (Iterator<String>)o.keys ());
        int processingDirectives = 0;
        int schemaVersion = 0;
        int taxonomyId = 0;
        while (i.hasNext ()) {
          final String fieldName = i.next ();
          if (fieldName.equals (getSettings ().getProcessingDirectivesField ())) {
            processingDirectives = integerValue (o.get (fieldName));
          } else if (fieldName.equals (getSettings ().getSchemaVersionField ())) {
            schemaVersion = integerValue (o.get (fieldName));
          } else if (fieldName.equals (getSettings ().getTaxonomyField ())) {
            taxonomyId = integerValue (o.get (fieldName));
          } else {
            _fieldLookahead.add (fieldName);
            break;
          }
        }
        setEnvelopeFields (processingDirectives, schemaVersion, taxonomyId);
        checkMessageEnd ();
        return setCurrentElement (FudgeStreamElement.MESSAGE_ENVELOPE);
      } else {
        i = _iteratorStack.peek ();
      }
      if (i.hasNext () || !_fieldLookahead.isEmpty ()) {
        final String fieldName;
        if (_fieldLookahead.isEmpty ()) {
          fieldName = i.next ();
        } else {
          fieldName = _fieldLookahead.remove ();
        }
        setCurrentFieldName (fieldName);
        final Object value;
        final boolean isValuelookahead;
        if (_valueLookahead.isEmpty ()) {
          value = o.get (fieldName);
          isValuelookahead = false;
        } else {
          value = _valueLookahead.remove ();
          isValuelookahead = true;
        }
        if (JSONObject.NULL.equals (value)) {
          setFieldValue (IndicatorType.INSTANCE);
        } else if (value instanceof JSONArray) {
          final JSONArray arr = (JSONArray)value;
          Object primArray = jsonArrayToPrimitiveArray (arr);
          if (primArray != null) {
            setFieldValue (primArray);
          } else {
            if (isValuelookahead) {
              // we're interpreting the JSON array as a repeated field; the data doesn't match a primitive type
              setFieldValue (arr.toString ());
            } else { 
              for (int j = 0; j < arr.length (); j++) {
                _fieldLookahead.add (fieldName);
                _valueLookahead.add (arr.get (j));
              }
              return next ();
            }
          }
        } else if (value instanceof JSONObject) {
          o = (JSONObject)value;
          _objectStack.push (o);
          _iteratorStack.push (o.keys ());
          return setCurrentElement (FudgeStreamElement.SUBMESSAGE_FIELD_START);
        } else {
          setFieldValue (value);
        }
        checkMessageEnd ();
        return setCurrentElement (FudgeStreamElement.SIMPLE_FIELD);
      } else {
        _iteratorStack.pop ();
        _objectStack.pop ();
        checkMessageEnd ();
        return setCurrentElement (FudgeStreamElement.SUBMESSAGE_FIELD_END);
      }
    } catch (JSONException e) {
      throw wrapException ("reading next element", e);
    }
  }
  
  public JSONSettings getSettings () {
    return _settings;
  }
  
}