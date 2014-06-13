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

package autosaveworld.libs.org.apache.commons.net;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

public abstract class DatagramSocketClient {

	private static final DatagramSocketFactory __DEFAULT_SOCKET_FACTORY = new DefaultDatagramSocketFactory();

	private Charset charset = Charset.defaultCharset();

	protected int _timeout_;

	protected DatagramSocket _socket_;

	protected boolean _isOpen_;

	protected DatagramSocketFactory _socketFactory_;

	public DatagramSocketClient() {
		_socket_ = null;
		_timeout_ = 0;
		_isOpen_ = false;
		_socketFactory_ = __DEFAULT_SOCKET_FACTORY;
	}

	public void open() throws SocketException {
		_socket_ = _socketFactory_.createDatagramSocket();
		_socket_.setSoTimeout(_timeout_);
		_isOpen_ = true;
	}

	public void open(int port) throws SocketException {
		_socket_ = _socketFactory_.createDatagramSocket(port);
		_socket_.setSoTimeout(_timeout_);
		_isOpen_ = true;
	}

	public void open(int port, InetAddress laddr) throws SocketException {
		_socket_ = _socketFactory_.createDatagramSocket(port, laddr);
		_socket_.setSoTimeout(_timeout_);
		_isOpen_ = true;
	}

	public void close() {
		if (_socket_ != null) {
			_socket_.close();
		}
		_socket_ = null;
		_isOpen_ = false;
	}

	public boolean isOpen() {
		return _isOpen_;
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

	public int getSoTimeout() throws SocketException {
		return _socket_.getSoTimeout();
	}

	public int getLocalPort() {
		return _socket_.getLocalPort();
	}

	public InetAddress getLocalAddress() {
		return _socket_.getLocalAddress();
	}

	public void setDatagramSocketFactory(DatagramSocketFactory factory) {
		if (factory == null) {
			_socketFactory_ = __DEFAULT_SOCKET_FACTORY;
		} else {
			_socketFactory_ = factory;
		}
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
