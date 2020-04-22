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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class DefaultSocketFactory extends SocketFactory {

	private final Proxy connProxy;

	public DefaultSocketFactory() {
		this(null);
	}

	public DefaultSocketFactory(Proxy proxy) {
		connProxy = proxy;
	}

	@Override
	public Socket createSocket() throws IOException {
		if (connProxy != null) {
			return new Socket(connProxy);
		}
		return new Socket();
	}

	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException, IOException {
		if (connProxy != null) {
			Socket s = new Socket(connProxy);
			s.connect(new InetSocketAddress(host, port));
			return s;
		}
		return new Socket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		if (connProxy != null) {
			Socket s = new Socket(connProxy);
			s.connect(new InetSocketAddress(address, port));
			return s;
		}
		return new Socket(address, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws UnknownHostException, IOException {
		if (connProxy != null) {
			Socket s = new Socket(connProxy);
			s.bind(new InetSocketAddress(localAddr, localPort));
			s.connect(new InetSocketAddress(host, port));
			return s;
		}
		return new Socket(host, port, localAddr, localPort);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
		if (connProxy != null) {
			Socket s = new Socket(connProxy);
			s.bind(new InetSocketAddress(localAddr, localPort));
			s.connect(new InetSocketAddress(address, port));
			return s;
		}
		return new Socket(address, port, localAddr, localPort);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket(port);
	}

	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return new ServerSocket(port, backlog);
	}

	public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		return new ServerSocket(port, backlog, bindAddr);
	}

}
