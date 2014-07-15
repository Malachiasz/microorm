/*
 * Copyright (C) 2013 Jerzy Chalupski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chalup.microorm;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Collection;
import java.util.Set;

class ReflectiveDaoAdapter<T> implements DaoAdapter<T> {

  private final Class<T> mKlass;
  private final ImmutableList<FieldAdapter> mFieldAdapters;
  private final Collection<EmbeddedFieldInitializer> mFieldInitializers;
  private final ImmutableList<String> mProjection;
  private final ImmutableList<String> mWritableColumns;
  private final ImmutableSet<String> mWritableDuplicates;

  ReflectiveDaoAdapter(Class<T> klass, Collection<FieldAdapter> fieldAdapters, Collection<EmbeddedFieldInitializer> fieldInitializers) {
    mKlass = klass;
    mFieldAdapters = ImmutableList.copyOf(fieldAdapters);
    mFieldInitializers = fieldInitializers;

    ImmutableList.Builder<String> projectionBuilder = ImmutableList.builder();
    ImmutableList.Builder<String> writableColumnsBuilder = ImmutableList.builder();

    for (FieldAdapter fieldAdapter : fieldAdapters) {
      projectionBuilder.add(fieldAdapter.getColumnNames());
      writableColumnsBuilder.add(fieldAdapter.getWritableColumnNames());
    }
    mProjection = projectionBuilder.build();
    mWritableColumns = writableColumnsBuilder.build();
    mWritableDuplicates = ImmutableSet.copyOf(findDuplicates(mWritableColumns));
  }

  private static <T> Set<T> findDuplicates(Iterable<T> iterable) {
    final Set<T> result = Sets.newHashSet();
    final Set<T> uniques = Sets.newHashSet();

    for (T element : iterable) {
      if (!uniques.add(element)) {
        result.add(element);
      }
    }

    return result;
  }

  @Override
  public T createInstance() {
    return createInstance(mKlass);
  }

  private T createInstance(Class<T> klass) {
    try {
      T instance = klass.newInstance();
      for (EmbeddedFieldInitializer fieldInitializer : mFieldInitializers) {
        fieldInitializer.initEmbeddedField(instance);
      }
      return instance;
    } catch (InstantiationException e) {
      throw new AssertionError(e);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public T fromCursor(Cursor c, T object) {
    try {
      for (FieldAdapter fieldAdapter : mFieldAdapters) {
        fieldAdapter.setValueFromCursor(c, object);
      }
      return object;
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public ContentValues toContentValues(ContentValues values, T object) {
    if (!mWritableDuplicates.isEmpty()) {
      throw new IllegalArgumentException("Duplicate columns definitions: " + Joiner.on(", ").join(mWritableDuplicates));
    }
    for (FieldAdapter fieldAdapter : mFieldAdapters) {
      try {
        fieldAdapter.putToContentValues(object, values);
      } catch (IllegalArgumentException e) {
        throw new AssertionError(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    return values;
  }

  @Override
  public String[] getProjection() {
    return mProjection.toArray(new String[mProjection.size()]);
  }

  @Override
  public String[] getWritableColumns() {
    return mWritableColumns.toArray(new String[mWritableColumns.size()]);
  }
}
