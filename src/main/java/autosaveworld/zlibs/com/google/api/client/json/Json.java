/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package autosaveworld.zlibs.com.google.api.client.json;

/**
 * JSON utilities.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class Json {

  /**
   * {@code "application/json; charset=utf-8"} media type used as a default for JSON parsing.
   *
   * <p>
   * Use {@link autosaveworld.zlibs.com.google.api.client.http.HttpMediaType#equalsIgnoreParameters} for comparing
   * media types.
   * </p>
   *
   * @since 1.10
   */
  public static final String MEDIA_TYPE = "application/json; charset=UTF-8";
}
