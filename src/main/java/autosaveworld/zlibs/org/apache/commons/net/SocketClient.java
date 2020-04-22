/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package autosaveworld.zlibs.org.apache.commons.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * The SocketClient provides the basic operations that are required of client objects accessing sockets. It is meant to be subclassed to avoid having to rewrite the same code over and over again to open a socket, close a socket, set timeouts, etc. Of special note is the {@link #setSocketFactory setSocketFactory } method, which allows you to control the type of Socket the SocketClient creates for initiating network connections. This is especially useful for adding SSL or proxy support as well as
 * better support for applets. For example, you could create a {@link javax.net.SocketFactory} that requests browser security capabilities before creating a socket. All classes derived from SocketClient should use the {@link #_socketFactory_ _socketFactory_ } member variable to create Socket and ServerSocket instances rather than instantiating them by directly invoking a constructor. By honoring this contract you guarantee that a user will always be able to provide his own Socket implementations
 * by substituting his own SocketFactory.
 *
 * @see SocketFactory
 */
public abstract class SocketClient {

	public static final String NETASCII_EOL = "\r\n";

	private static final SocketFactory __DEFAULT_SOCKET_FACTORY = SocketFactory.getDefault();

	private static final ServerSocketFactory __DEFAULT_SERVER_SOCKET_FACTORY = ServerSocketFactory.getDefault();

	protected int _timeout_;

	protected Socket _socket_;

	protected int _defaultPort_;

	protected InputStream _input_;

	protected OutputStream _output_;

	protected SocketFactory _socketFactory_;

	protected ServerSocketFactory _serverSocketFactory_;

	private static final int DEFAULT_CONNECT_TIMEOUT = 0;
	protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

	private int receiveBufferSize = -1;

	private int sendBufferSize = -1;

	private Proxy connProxy;

	private Charset charset = Charset.defaultCharset();

	public SocketClient() {
		_socket_ = null;
		_input_ = null;
		_output_ = null;
		_timeout_ = 0;
		_defaultPort_ = 0;
		_socketFactory_ = __DEFAULT_SOCKET_FACTORY;
		_serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
	}

	protected void _connectAction_() throws IOException {
		_socket_.setSoTimeout(_timeout_);
		_input_ = _socket_.getInputStream();
		_output_ = _socket_.getOutputStream();
	}

	public void connect(InetAddress host, int port) throws SocketException, IOException {
		_socket_ = _socketFactory_.createSocket();
		if (receiveBufferSize != -1) {
			_socket_.setReceiveBufferSize(receiveBufferSize);
		}
		if (sendBufferSize != -1) {
			_socket_.setSendBufferSize(sendBufferSize);
		}
		_socket_.connect(new InetSocketAddress(host, port), connectTimeout);
		_connectAction_();
	}

	public void connect(String hostname, int port) throws SocketException, IOException {
		connect(InetAddress.getByName(hostname), port);
	}

	public void connect(InetAddress host, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
		_socket_ = _socketFactory_.createSocket();
		if (receiveBufferSize != -1) {
			_socket_.setReceiveBufferSize(receiveBufferSize);
		}
		if (sendBufferSize != -1) {
			_socket_.setSendBufferSize(sendBufferSize);
		}
		_socket_.bind(new InetSocketAddress(localAddr, localPort));
		_socket_.connect(new InetSocketAddress(host, port), connectTimeout);
		_connectAction_();
	}

	public void connect(String hostname, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
		connect(InetAddress.getByName(hostname), port, localAddr, localPort);
	}

	public void connect(InetAddress host) throws SocketException, IOException {
		connect(host, _defaultPort_);
	}

	public void connect(String hostname) throws SocketException, IOException {
		connect(hostname, _defaultPort_);
	}

	public void disconnect() throws IOException {
		closeQuietly(_socket_);
		closeQuietly(_input_);
		closeQuietly(_output_);
		_socket_ = null;
		_input_ = null;
		_output_ = null;
	}

	private void closeQuietly(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	private void closeQuietly(Closeable close) {
		if (close != null) {
			try {
				close.close();
			} catch (IOException e) {
			}
		}
	}

	public boolean isConnected() {
		if (_socket_ == null) {
			return false;
		}

		return _socket_.isConnected();
	}

	public boolean isAvailable() {
		if (isConnected()) {
			try {
				if (_socket_.getInetAddress() == null) {
					return false;
				}
				if (_socket_.getPort() == 0) {
					return false;
				}
				if (_socket_.getRemoteSocketAddress() == null) {
					return false;
				}
				if (_socket_.isClosed()) {
					return false;
				}
				if (_socket_.isInputShutdown()) {
					return false;
				}
				if (_socket_.isOutputShutdown()) {
					return false;
				}
				_socket_.getInputStream();
				_socket_.getOutputStream();
			} catch (IOException ioex) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public void setDefaultPort(int port) {
		_defaultPort_ = port;
	}

	public int getDefaultPort() {
		return _defaultPort_;
	}

	public void setDefaultTimeout(int timeout) {
		_timeout_ = timeout;
	}

	public int getDefaultTimeout() {
		return _timeout_;
	}

	public void setSoTimeout(int timeout) throws SocketException {
		_socket_.setSoTimeout(timeout);
	}

	public void setSendBufferSize(int size) throws SocketException {
		sendBufferSize = size;
	}

	protected int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setReceiveBufferSize(int size) throws SocketException {
		receiveBufferSize = size;
	}

	protected int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public int getSoTimeout() throws SocketException {
		return _socket_.getSoTimeout();
	}

	public void setTcpNoDelay(boolean on) throws SocketException {
		_socket_.setTcpNoDelay(on);
	}

	public boolean getTcpNoDelay() throws SocketException {
		return _socket_.getTcpNoDelay();
	}

	public void setKeepAlive(boolean keepAlive) throws SocketException {
		_socket_.setKeepAlive(keepAlive);
	}

	public boolean getKeepAlive() throws SocketException {
		return _socket_.getKeepAlive();
	}

	public void setSoLinger(boolean on, int val) throws SocketException {
		_socket_.setSoLinger(on, val);
	}

	public int getSoLinger() throws SocketException {
		return _socket_.getSoLinger();
	}

	public int getLocalPort() {
		return _socket_.getLocalPort();
	}

	public InetAddress getLocalAddress() {
		return _socket_.getLocalAddress();
	}

	public int getRemotePort() {
		return _socket_.getPort();
	}

	public InetAddress getRemoteAddress() {
		return _socket_.getInetAddress();
	}

	public boolean verifyRemote(Socket socket) {
		InetAddress host1, host2;

		host1 = socket.getInetAddress();
		host2 = getRemoteAddress();

		return host1.equals(host2);
	}

	public void setSocketFactory(SocketFactory factory) {
		if (factory == null) {
			_socketFactory_ = __DEFAULT_SOCKET_FACTORY;
		} else {
			_socketFactory_ = factory;
		}
		connProxy = null;
	}

	public void setServerSocketFactory(ServerSocketFactory factory) {
		if (factory == null) {
			_serverSocketFactory_ = __DEFAULT_SERVER_SOCKET_FACTORY;
		} else {
			_serverSocketFactory_ = factory;
		}
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public ServerSocketFactory getServerSocketFactory() {
		return _serverSocketFactory_;
	}

	public void setProxy(Proxy proxy) {
		setSocketFactory(new DefaultSocketFactory(proxy));
		connProxy = proxy;
	}

	public Proxy getProxy() {
		return connProxy;
	}

	public String getCharsetName() {
		return charset.name();
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

}
