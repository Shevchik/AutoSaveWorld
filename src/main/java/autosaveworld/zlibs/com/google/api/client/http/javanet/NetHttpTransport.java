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

package autosaveworld.zlibs.com.google.api.client.http.javanet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import autosaveworld.zlibs.com.google.api.client.http.HttpMethods;
import autosaveworld.zlibs.com.google.api.client.http.HttpTransport;
import autosaveworld.zlibs.com.google.api.client.util.Preconditions;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package.
 *
 * <p>
 * Users should consider modifying the keep alive property on {@link NetHttpTransport} to control
 * whether the socket should be returned to a pool of connected sockets. More information is
 * available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 * </p>
 *
 * <p>
 * We honor the default global caching behavior. To change the default behavior use
 * {@link HttpURLConnection#setDefaultUseCaches(boolean)}.
 * </p>
 *
 * <p>
 * Implementation is thread-safe. For maximum efficiency, applications should use a single
 * globally-shared instance of the HTTP transport.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends HttpTransport {

  /**
   * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
   * ascending alphabetical order.
   */
  private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE,
      HttpMethods.GET,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.POST,
      HttpMethods.PUT,
      HttpMethods.TRACE};
  static {
    Arrays.sort(SUPPORTED_METHODS);
  }

  /** Factory to produce connections from {@link URL}s */
  private final ConnectionFactory connectionFactory;

  /**
   * Constructor with the default behavior.
   *
   * <p>
   * Instead use {@link Builder} to modify behavior.
   * </p>
   */
  public NetHttpTransport() {
    this(null);
  }

  /**
   * @param connectionFactory factory to produce connections from {@link URL}s; if {@code null} then
   *        {@link DefaultConnectionFactory} is used
   * @param sslSocketFactory SSL socket factory or {@code null} for the default
   * @param hostnameVerifier host name verifier or {@code null} for the default
   * @since 1.20
   */
  public NetHttpTransport(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory == null ? new DefaultConnectionFactory() : connectionFactory;
  }

  @Override
  public boolean supportsMethod(String method) {
    return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
  }

  @Override
  protected NetHttpRequest buildRequest(String method, String url) throws IOException {
    Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
    // connection with proxy settings
    URL connUrl = new URL(url);
    HttpURLConnection connection = connectionFactory.openConnection(connUrl);
    connection.setRequestMethod(method);
    return new NetHttpRequest(connection);
  }

}
