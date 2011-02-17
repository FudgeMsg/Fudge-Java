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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.mongodb.DBObject;

/**
 * Wraps a {@link FudgeFieldContainer} and implements the {@link DBObject} interface,
 * without going through an object conversion stage (as the {@link MongoDBFudgeBuilder} will do).
 * This class is very much a work in progress. For details on why, please see
 * http://kirkwylie.blogspot.com/2010/06/performance-of-fudge-persistence-in.html and the comments
 * from the 10gen team at the bottom.
 *
 * @author Kirk Wylie
 */
public class FudgeMongoDBObject implements DBObject {
  private final FudgeMsg _underlying;
  private final Map<String, Object> _fastSingleValueCache = new HashMap<String, Object>();
  // This is used A LOT internally in MongoDB. Cache it specifically and avoid all the conversions.
  private ObjectId _objectId;
  
  /**
   * The primary constructor.
   * 
   * @param underlying underlying FudgeFieldContainer to be wrapped
   */
  public FudgeMongoDBObject(MutableFudgeFieldContainer underlying) {
    if (underlying == null) {
      throw new IllegalArgumentException("Must provide an underlying");
    }
    if (!(underlying instanceof FudgeMsg)) {
      throw new IllegalArgumentException("Underlying must extend FudgeMsgBase");
    }
    _underlying = (FudgeMsg) underlying;
    buildFastSingleValueCache();
  }

  /**
   * 
   */
  private void buildFastSingleValueCache() {
    Set<String> fieldNamesToIgnore = new HashSet<String>();
    for (FudgeField field : getUnderlying().getAllFields()) {
      if (field.getName() == null) {
        continue;
      }
      if (fieldNamesToIgnore.contains(field.getName())) {
        continue;
      }
      if (_fastSingleValueCache.containsKey(field.getName())) {
        _fastSingleValueCache.remove(field.getName());
        fieldNamesToIgnore.add(field.getName());
        continue;
      }
      _fastSingleValueCache.put(field.getName(), convertFudgeToMongoDB(field));
      if ("_id".equals(field.getName())) {
        _objectId = new ObjectId((String)field.getValue());
      }
    }
  }

  /**
   * @return the underlying
   */
  public FudgeMsg getUnderlying() {
    return _underlying;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsField(String s) {
    if (_fastSingleValueCache.containsKey(s)) {
      return true;
    }
    return getUnderlying().hasField(s);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(String s) {
    return containsField(s);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object get(String key) {
    if ("_id".equals(key)) {
      return _objectId;
    }
    Object fastField = _fastSingleValueCache.get(key);
    if (fastField != null) {
      return fastField;
    }
    
    List<FudgeField> allFields = getUnderlying().getAllByName(key);
    if ((allFields == null) || allFields.isEmpty()) {
      return null;
    }
    if (allFields.size() > 0) {
      List<Object> listResult = new ArrayList<Object>(allFields.size());
      for (FudgeField field : allFields) {
        listResult.add(convertFudgeToMongoDB(field));
      }
      return listResult;
    } else {
      return convertFudgeToMongoDB(allFields.get(0));
    }
  }
  
  private static Object convertFudgeToMongoDB(FudgeField field) {
    if (field.getType().getTypeId() == FudgeTypeDictionary.FUDGE_MSG_TYPE_ID) {
      // Sub-message.
      return new FudgeMongoDBObject((MutableFudgeFieldContainer) field.getValue());
    } else {
      return field.getValue();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPartialObject() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> keySet() {
    return getUnderlying().getAllFieldNames();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void markAsPartialObject() {
    // NOTE kirk 2010-06-14 -- Intentional no-op.
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object put(String key, Object v) {
    if (v instanceof List) {
      for (Object o : (List) v) {
        put(key, o);
      }
    } else if (v instanceof DBObject) {
      put(key, FudgeContext.GLOBAL_DEFAULT.toFudgeMsg((DBObject) v));
    } else if (v instanceof ObjectId) {
      // GROSS HACK HERE. Should be smarter in our fudge use.
      getUnderlying().add(key, ((ObjectId) v).toString());
      _objectId = (ObjectId) v;
    } else {
      getUnderlying().add(key, v);
    }
    return null;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void putAll(BSONObject o) {
    throw new UnsupportedOperationException("Put All not yet supported");
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void putAll(Map m) {
    throw new UnsupportedOperationException("Put not yet supported");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object removeField(String key) {
    throw new UnsupportedOperationException("Remove not yet supported");
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map toMap() {
    Map result = new HashMap();
    for (FudgeField field : getUnderlying().getAllFields()) {
      if (field.getName() == null) {
        continue;
      }
      result.put(field.getName(), convertFudgeToMongoDB(field));
    }
    return result;
  }

}
