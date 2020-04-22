/*
 * Copyright (c) 2014 Google Inc.
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

package autosaveworld.zlibs.com.google.api.client.googleapis.auth.oauth2;

import java.nio.charset.Charset;

import autosaveworld.zlibs.com.google.api.client.util.Beta;

/**
 * Utilities used by the com.google.api.client.googleapis.auth.oauth2 namespace.
 */
@Beta
public class OAuth2Utils {

  static final Charset UTF_8 = Charset.forName("UTF-8");

  static <T extends Throwable> T exceptionWithCause(T exception, Throwable cause) {
    exception.initCause(cause);
    return exception;
  }

}
