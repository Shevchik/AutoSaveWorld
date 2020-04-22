/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
 Copyright (c) 2002-2014 ymnk, JCraft,Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in
 the documentation and/or other materials provided with the distribution.

 3. The names of the authors may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package autosaveworld.zlibs.com.jcraft.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import autosaveworld.zlibs.com.jcraft.jsch.Channel.ChannelType;

public class Session implements Runnable {

	// http://ietf.org/internet-drafts/draft-ietf-secsh-assignednumbers-01.txt
	static final int SSH_MSG_DISCONNECT = 1;
	static final int SSH_MSG_IGNORE = 2;
	static final int SSH_MSG_UNIMPLEMENTED = 3;
	static final int SSH_MSG_DEBUG = 4;
	static final int SSH_MSG_SERVICE_REQUEST = 5;
	static final int SSH_MSG_SERVICE_ACCEPT = 6;
	static final int SSH_MSG_KEXINIT = 20;
	static final int SSH_MSG_NEWKEYS = 21;
	static final int SSH_MSG_KEXDH_INIT = 30;
	static final int SSH_MSG_KEXDH_REPLY = 31;
	static final int SSH_MSG_KEX_DH_GEX_GROUP = 31;
	static final int SSH_MSG_KEX_DH_GEX_INIT = 32;
	static final int SSH_MSG_KEX_DH_GEX_REPLY = 33;
	static final int SSH_MSG_KEX_DH_GEX_REQUEST = 34;
	static final int SSH_MSG_GLOBAL_REQUEST = 80;
	static final int SSH_MSG_REQUEST_SUCCESS = 81;
	static final int SSH_MSG_REQUEST_FAILURE = 82;
	static final int SSH_MSG_CHANNEL_OPEN = 90;
	static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
	static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
	static final int SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;
	static final int SSH_MSG_CHANNEL_DATA = 94;
	static final int SSH_MSG_CHANNEL_EXTENDED_DATA = 95;
	static final int SSH_MSG_CHANNEL_EOF = 96;
	static final int SSH_MSG_CHANNEL_CLOSE = 97;
	static final int SSH_MSG_CHANNEL_REQUEST = 98;
	static final int SSH_MSG_CHANNEL_SUCCESS = 99;
	static final int SSH_MSG_CHANNEL_FAILURE = 100;

	private static final int PACKET_MAX_SIZE = 256 * 1024;

	private byte[] V_S; // server version
	private byte[] V_C = Util.str2byte("SSH-2.0-JSCH-" + JSch.VERSION); // client
	// version

	private byte[] I_C; // the payload of the client's SSH_MSG_KEXINIT
	private byte[] I_S; // the payload of the server's SSH_MSG_KEXINIT

	private byte[] session_id;

	private byte[] IVc2s;
	private byte[] IVs2c;
	private byte[] Ec2s;
	private byte[] Es2c;
	private byte[] MACc2s;
	private byte[] MACs2c;

	private int seqi = 0;
	private int seqo = 0;

	String[] guess = null;
	private Cipher s2ccipher;
	private Cipher c2scipher;
	private MAC s2cmac;
	private MAC c2smac;
	// private byte[] mac_buf;
	private byte[] s2cmac_result1;
	private byte[] s2cmac_result2;

	private IO io;
	private Socket socket;
	private int timeout = 0;

	private volatile boolean isConnected = false;

	private boolean isAuthed = false;

	private Thread connectThread = null;
	private Object lock = new Object();

	static Random random;

	Buffer buf;
	Packet packet;

	SocketFactory socket_factory = null;

	static final int buffer_margin = 32 + // maximum padding length
			20 + // maximum mac length
			32; // margin for deflater; deflater may inflate data

	private java.util.Hashtable<String, String> config = null;

	private UserInfo userinfo;

	private int serverAliveInterval = 0;
	private int serverAliveCountMax = 1;

	protected boolean daemon_thread = false;

	private long kex_start_time = 0L;

	int max_auth_tries = 6;
	int auth_failures = 0;

	String host = "127.0.0.1";
	int port = 22;

	String username = null;
	byte[] password = null;

	JSch jsch;

	Session(JSch jsch, String username, String host, int port) throws JSchException {
		super();
		this.jsch = jsch;
		buf = new Buffer();
		packet = new Packet(buf);
		this.username = username;
		this.host = host;
		this.port = port;

		if (this.username == null) {
			try {
				this.username = (String) (System.getProperties().get("user.name"));
			} catch (SecurityException e) {
				// ignore e
			}
		}

		if (this.username == null) {
			throw new JSchException("username is not given.");
		}
	}

	public void connect() throws JSchException {
		connect(timeout);
	}

	@SuppressWarnings("resource")
	public void connect(int connectTimeout) throws JSchException {
		if (isConnected) {
			throw new JSchException("session is already connected");
		}

		io = new IO();
		if (random == null) {
			try {
				Class<?> c = Class.forName(getConfig("random"));
				random = (Random) (c.newInstance());
			} catch (Exception e) {
				throw new JSchException(e.toString(), e);
			}
		}
		Packet.setRandom(random);

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "Connecting to " + host + " port " + port);
		}

		try {
			int i, j;

			InputStream in;
			OutputStream out;
			if (socket_factory == null) {
				socket = Util.createSocket(host, port, connectTimeout);
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} else {
				socket = socket_factory.createSocket(host, port);
				in = socket_factory.getInputStream(socket);
				out = socket_factory.getOutputStream(socket);
			}
			// if(timeout>0){ socket.setSoTimeout(timeout); }
			socket.setTcpNoDelay(true);
			io.setInputStream(in);
			io.setOutputStream(out);

			if ((connectTimeout > 0) && (socket != null)) {
				socket.setSoTimeout(connectTimeout);
			}

			isConnected = true;

			if (JSch.getLogger().isEnabled(Logger.INFO)) {
				JSch.getLogger().log(Logger.INFO, "Connection established");
			}

			jsch.addSession(this);

			{
				// Some Cisco devices will miss to read '\n' if it is sent
				// separately.
				byte[] foo = new byte[V_C.length + 1];
				System.arraycopy(V_C, 0, foo, 0, V_C.length);
				foo[foo.length - 1] = (byte) '\n';
				io.put(foo, 0, foo.length);
			}

			while (true) {
				i = 0;
				j = 0;
				while (i < buf.buffer.length) {
					j = io.getByte();
					if (j < 0) {
						break;
					}
					buf.buffer[i] = (byte) j;
					i++;
					if (j == 10) {
						break;
					}
				}
				if (j < 0) {
					throw new JSchException("connection is closed by foreign host");
				}

				if (buf.buffer[i - 1] == 10) { // 0x0a
					i--;
					if ((i > 0) && (buf.buffer[i - 1] == 13)) { // 0x0d
						i--;
					}
				}

				if ((i <= 3) || ((i != buf.buffer.length) && ((buf.buffer[0] != 'S') || (buf.buffer[1] != 'S') || (buf.buffer[2] != 'H') || (buf.buffer[3] != '-')))) {
					// It must not start with 'SSH-'
					// System.err.println(new String(buf.buffer, 0, i);
					continue;
				}

				if ((i == buf.buffer.length) || (i < 7) || // SSH-1.99 or SSH-2.0
						((buf.buffer[4] == '1') && (buf.buffer[6] != '9')) // SSH-1.5
						) {
					throw new JSchException("invalid server's version string");
				}
				break;
			}

			V_S = new byte[i];
			System.arraycopy(buf.buffer, 0, V_S, 0, i);
			// System.err.println("V_S: ("+i+") ["+new String(V_S)+"]");

			if (JSch.getLogger().isEnabled(Logger.INFO)) {
				JSch.getLogger().log(Logger.INFO, "Remote version string: " + Util.byte2str(V_S));
				JSch.getLogger().log(Logger.INFO, "Local version string: " + Util.byte2str(V_C));
			}

			send_kexinit();

			buf = read(buf);
			if (buf.getCommand() != SSH_MSG_KEXINIT) {
				in_kex = false;
				throw new JSchException("invalid protocol: " + buf.getCommand());
			}

			if (JSch.getLogger().isEnabled(Logger.INFO)) {
				JSch.getLogger().log(Logger.INFO, "SSH_MSG_KEXINIT received");
			}

			KeyExchange kex = receive_kexinit(buf);

			while (true) {
				buf = read(buf);
				if (kex.getState() == buf.getCommand()) {
					kex_start_time = System.currentTimeMillis();
					boolean result = kex.next(buf);
					if (!result) {
						// System.err.println("verify: "+result);
						in_kex = false;
						throw new JSchException("verify: " + result);
					}
				} else {
					in_kex = false;
					throw new JSchException("invalid protocol(kex): " + buf.getCommand());
				}
				if (kex.getState() == KeyExchange.STATE_END) {
					break;
				}
			}

			send_newkeys();

			// receive SSH_MSG_NEWKEYS(21)
			buf = read(buf);
			// System.err.println("read: 21 ? "+buf.getCommand());
			if (buf.getCommand() == SSH_MSG_NEWKEYS) {

				if (JSch.getLogger().isEnabled(Logger.INFO)) {
					JSch.getLogger().log(Logger.INFO, "SSH_MSG_NEWKEYS received");
				}

				receive_newkeys(buf, kex);
			} else {
				in_kex = false;
				throw new JSchException("invalid protocol(newkyes): " + buf.getCommand());
			}

			try {
				String s = getConfig("MaxAuthTries");
				if (s != null) {
					max_auth_tries = Integer.parseInt(s);
				}
			} catch (NumberFormatException e) {
				throw new JSchException("MaxAuthTries: " + getConfig("MaxAuthTries"), e);
			}

			boolean auth = false;
			boolean auth_cancel = false;

			UserAuth ua = null;
			try {
				Class<?> c = Class.forName(getConfig("userauth.none"));
				ua = (UserAuth) (c.newInstance());
			} catch (Exception e) {
				throw new JSchException(e.toString(), e);
			}

			auth = ua.start(this);

			String cmethods = getConfig("PreferredAuthentications");

			String[] cmethoda = Util.split(cmethods, ",");

			String smethods = null;
			if (!auth) {
				smethods = ((UserAuthNone) ua).getMethods();
				if (smethods != null) {
					smethods = smethods.toLowerCase();
				} else {
					// methods: publickey,password,keyboard-interactive
					// smethods="publickey,password,keyboard-interactive";
					smethods = cmethods;
				}
			}

			String[] smethoda = Util.split(smethods, ",");

			int methodi = 0;

			loop: while (true) {

				while (!auth && (cmethoda != null) && (methodi < cmethoda.length)) {

					String method = cmethoda[methodi++];
					boolean acceptable = false;
					for (int k = 0; k < smethoda.length; k++) {
						if (smethoda[k].equals(method)) {
							acceptable = true;
							break;
						}
					}
					if (!acceptable) {
						continue;
					}

					// System.err.println("  method: "+method);

					if (JSch.getLogger().isEnabled(Logger.INFO)) {
						String str = "Authentications that can continue: ";
						for (int k = methodi - 1; k < cmethoda.length; k++) {
							str += cmethoda[k];
							if ((k + 1) < cmethoda.length) {
								str += ",";
							}
						}
						JSch.getLogger().log(Logger.INFO, str);
						JSch.getLogger().log(Logger.INFO, "Next authentication method: " + method);
					}

					ua = null;
					try {
						Class<?> c = null;
						if (getConfig("userauth." + method) != null) {
							c = Class.forName(getConfig("userauth." + method));
							ua = (UserAuth) (c.newInstance());
						}
					} catch (Exception e) {
						if (JSch.getLogger().isEnabled(Logger.WARN)) {
							JSch.getLogger().log(Logger.WARN, "failed to load " + method + " method");
						}
					}

					if (ua != null) {
						auth_cancel = false;
						try {
							auth = ua.start(this);
							if (auth && JSch.getLogger().isEnabled(Logger.INFO)) {
								JSch.getLogger().log(Logger.INFO, "Authentication succeeded (" + method + ").");
							}
						} catch (JSchAuthCancelException ee) {
							auth_cancel = true;
						} catch (JSchPartialAuthException ee) {
							String tmp = smethods;
							smethods = ee.getMethods();
							smethoda = Util.split(smethods, ",");
							if (!tmp.equals(smethods)) {
								methodi = 0;
							}
							// System.err.println("PartialAuth: "+methods);
							auth_cancel = false;
							continue loop;
						} catch (RuntimeException ee) {
							throw ee;
						} catch (JSchException ee) {
							throw ee;
						} catch (Exception ee) {
							// System.err.println("ee: "+ee); //
							// SSH_MSG_DISCONNECT: 2 Too many authentication
							// failures
							if (JSch.getLogger().isEnabled(Logger.WARN)) {
								JSch.getLogger().log(Logger.WARN, "an exception during authentication\n" + ee.toString());
							}
							break loop;
						}
					}
				}
				break;
			}

			if (!auth) {
				if (auth_failures >= max_auth_tries) {
					if (JSch.getLogger().isEnabled(Logger.INFO)) {
						JSch.getLogger().log(Logger.INFO, "Login trials exceeds " + max_auth_tries);
					}
				}
				if (auth_cancel) {
					throw new JSchException("Auth cancel");
				}
				throw new JSchException("Auth fail");
			}

			if ((socket != null) && ((connectTimeout > 0) || (timeout > 0))) {
				socket.setSoTimeout(timeout);
			}

			isAuthed = true;

			synchronized (lock) {
				if (isConnected) {
					connectThread = new Thread(this);
					connectThread.setName("Connect thread " + host + " session");
					if (daemon_thread) {
						connectThread.setDaemon(daemon_thread);
					}
					connectThread.start();
				} else {
					// The session has been already down and
					// we don't have to start new thread.
				}
			}
		} catch (Exception e) {
			in_kex = false;
			try {
				if (isConnected) {
					String message = e.toString();
					packet.reset();
					buf.checkFreeSize(1 + (4 * 3) + message.length() + 2 + buffer_margin);
					buf.putByte((byte) SSH_MSG_DISCONNECT);
					buf.putInt(3);
					buf.putString(Util.str2byte(message));
					buf.putString(Util.str2byte("en"));
					write(packet);
				}
			} catch (Exception ee) {
			}
			try {
				disconnect();
			} catch (Exception ee) {
			}
			isConnected = false;
			// e.printStackTrace();
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			if (e instanceof JSchException) {
				throw (JSchException) e;
			}
			throw new JSchException("Session.connect: " + e);
		} finally {
			Util.bzero(password);
			password = null;
		}
	}

	private KeyExchange receive_kexinit(Buffer buf) throws Exception {
		int j = buf.getInt();
		if (j != buf.getLength()) { // packet was compressed and
			buf.getByte(); // j is the size of deflated packet.
			I_S = new byte[buf.index - 5];
		} else {
			I_S = new byte[j - 1 - buf.getByte()];
		}
		System.arraycopy(buf.buffer, buf.s, I_S, 0, I_S.length);

		if (!in_kex) { // We are in rekeying activated by the remote!
			send_kexinit();
		}

		guess = KeyExchange.guess(I_S, I_C);
		if (guess == null) {
			throw new JSchException("Algorithm negotiation fail");
		}

		if (!isAuthed && (guess[KeyExchange.PROPOSAL_ENC_ALGS_CTOS].equals("none") || (guess[KeyExchange.PROPOSAL_ENC_ALGS_STOC].equals("none")))) {
			throw new JSchException("NONE Cipher should not be chosen before authentification is successed.");
		}

		KeyExchange kex = null;
		try {
			Class<?> c = Class.forName(getConfig(guess[KeyExchange.PROPOSAL_KEX_ALGS]));
			kex = (KeyExchange) (c.newInstance());
		} catch (Exception e) {
			throw new JSchException(e.toString(), e);
		}

		kex.init(this, V_S, V_C, I_S, I_C);
		return kex;
	}

	private boolean in_kex = false;

	public void rekey() throws Exception {
		send_kexinit();
	}

	private void send_kexinit() throws Exception {
		if (in_kex) {
			return;
		}

		String cipherc2s = getConfig("cipher.c2s");
		String ciphers2c = getConfig("cipher.s2c");

		String[] not_available_ciphers = checkCiphers(getConfig("CheckCiphers"));
		if ((not_available_ciphers != null) && (not_available_ciphers.length > 0)) {
			cipherc2s = Util.diffString(cipherc2s, not_available_ciphers);
			ciphers2c = Util.diffString(ciphers2c, not_available_ciphers);
			if ((cipherc2s == null) || (ciphers2c == null)) {
				throw new JSchException("There are not any available ciphers.");
			}
		}

		String kex = getConfig("kex");
		String[] not_available_kexes = checkKexes(getConfig("CheckKexes"));
		if ((not_available_kexes != null) && (not_available_kexes.length > 0)) {
			kex = Util.diffString(kex, not_available_kexes);
			if (kex == null) {
				throw new JSchException("There are not any available kexes.");
			}
		}

		in_kex = true;
		kex_start_time = System.currentTimeMillis();

		// byte SSH_MSG_KEXINIT(20)
		// byte[16] cookie (random bytes)
		// string kex_algorithms
		// string server_host_key_algorithms
		// string encryption_algorithms_client_to_server
		// string encryption_algorithms_server_to_client
		// string mac_algorithms_client_to_server
		// string mac_algorithms_server_to_client
		// string compression_algorithms_client_to_server
		// string compression_algorithms_server_to_client
		// string languages_client_to_server
		// string languages_server_to_client
		Buffer buf = new Buffer(); // send_kexinit may be invoked
		Packet packet = new Packet(buf); // by user thread.
		packet.reset();
		buf.putByte((byte) SSH_MSG_KEXINIT);
		synchronized (random) {
			random.fill(buf.buffer, buf.index, 16);
			buf.skip(16);
		}
		buf.putString(Util.str2byte(kex));
		buf.putString(Util.str2byte(getConfig("server_host_key")));
		buf.putString(Util.str2byte(cipherc2s));
		buf.putString(Util.str2byte(ciphers2c));
		buf.putString(Util.str2byte(getConfig("mac.c2s")));
		buf.putString(Util.str2byte(getConfig("mac.s2c")));
		buf.putString(Util.str2byte(getConfig("compression.c2s")));
		buf.putString(Util.str2byte(getConfig("compression.s2c")));
		buf.putString(Util.str2byte(getConfig("lang.c2s")));
		buf.putString(Util.str2byte(getConfig("lang.s2c")));
		buf.putByte((byte) 0);
		buf.putInt(0);

		buf.setOffSet(5);
		I_C = new byte[buf.getLength()];
		buf.getByte(I_C);

		write(packet);

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "SSH_MSG_KEXINIT sent");
		}
	}

	private void send_newkeys() throws Exception {
		// send SSH_MSG_NEWKEYS(21)
		packet.reset();
		buf.putByte((byte) SSH_MSG_NEWKEYS);
		write(packet);

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "SSH_MSG_NEWKEYS sent");
		}
	}

	public Channel openChannel(ChannelType ctype) throws JSchException {
		if (!isConnected) {
			throw new JSchException("session is down");
		}
		try {
			Channel channel = Channel.getChannel(ctype);
			addChannel(channel);
			channel.init();
			return channel;
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	// encode will bin invoked in write with synchronization.
	public void encode(Packet packet) throws Exception {
		// System.err.println("encode: "+packet.buffer.getCommand());
		// System.err.println("        "+packet.buffer.index);
		// if(packet.buffer.getCommand()==96){
		// Thread.dumpStack();
		// }
		if (c2scipher != null) {
			// packet.padding(c2scipher.getIVSize());
			packet.padding(c2scipher_size);
			int pad = packet.buffer.buffer[4];
			synchronized (random) {
				random.fill(packet.buffer.buffer, packet.buffer.index - pad, pad);
			}
		} else {
			packet.padding(8);
		}

		if (c2smac != null) {
			c2smac.update(seqo);
			c2smac.update(packet.buffer.buffer, 0, packet.buffer.index);
			c2smac.doFinal(packet.buffer.buffer, packet.buffer.index);
		}
		if (c2scipher != null) {
			byte[] buf = packet.buffer.buffer;
			c2scipher.update(buf, 0, packet.buffer.index, buf, 0);
		}
		if (c2smac != null) {
			packet.buffer.skip(c2smac.getBlockSize());
		}
	}

	private int s2ccipher_size = 8;
	private int c2scipher_size = 8;

	public Buffer read(Buffer buf) throws Exception {
		int j = 0;
		while (true) {
			buf.reset();
			io.getByte(buf.buffer, buf.index, s2ccipher_size);
			buf.index += s2ccipher_size;
			if (s2ccipher != null) {
				s2ccipher.update(buf.buffer, 0, s2ccipher_size, buf.buffer, 0);
			}
			j = ((buf.buffer[0] << 24) & 0xff000000) | ((buf.buffer[1] << 16) & 0x00ff0000) | ((buf.buffer[2] << 8) & 0x0000ff00) | ((buf.buffer[3]) & 0x000000ff);
			// RFC 4253 6.1. Maximum Packet Length
			if ((j < 5) || (j > PACKET_MAX_SIZE)) {
				start_discard(buf, s2ccipher, s2cmac, j, PACKET_MAX_SIZE);
			}
			int need = (j + 4) - s2ccipher_size;
			// if(need<0){
			// throw new IOException("invalid data");
			// }
			if ((buf.index + need) > buf.buffer.length) {
				byte[] foo = new byte[buf.index + need];
				System.arraycopy(buf.buffer, 0, foo, 0, buf.index);
				buf.buffer = foo;
			}

			if ((need % s2ccipher_size) != 0) {
				String message = "Bad packet length " + need;
				if (JSch.getLogger().isEnabled(Logger.FATAL)) {
					JSch.getLogger().log(Logger.FATAL, message);
				}
				start_discard(buf, s2ccipher, s2cmac, j, PACKET_MAX_SIZE - s2ccipher_size);
			}

			if (need > 0) {
				io.getByte(buf.buffer, buf.index, need);
				buf.index += (need);
				if (s2ccipher != null) {
					s2ccipher.update(buf.buffer, s2ccipher_size, need, buf.buffer, s2ccipher_size);
				}
			}

			if (s2cmac != null) {
				s2cmac.update(seqi);
				s2cmac.update(buf.buffer, 0, buf.index);

				s2cmac.doFinal(s2cmac_result1, 0);
				io.getByte(s2cmac_result2, 0, s2cmac_result2.length);
				if (!java.util.Arrays.equals(s2cmac_result1, s2cmac_result2)) {
					if (need > PACKET_MAX_SIZE) {
						throw new IOException("MAC Error");
					}
					start_discard(buf, s2ccipher, s2cmac, j, PACKET_MAX_SIZE - need);
					continue;
				}
			}

			seqi++;

			int type = buf.getCommand() & 0xff;
			// System.err.println("read: "+type);
			if (type == SSH_MSG_DISCONNECT) {
				buf.rewind();
				buf.getInt();
				buf.getShort();
				int reason_code = buf.getInt();
				byte[] description = buf.getString();
				byte[] language_tag = buf.getString();
				throw new JSchException("SSH_MSG_DISCONNECT: " + reason_code + " " + Util.byte2str(description) + " " + Util.byte2str(language_tag));
				// break;
			} else if (type == SSH_MSG_IGNORE) {
			} else if (type == SSH_MSG_UNIMPLEMENTED) {
				buf.rewind();
				buf.getInt();
				buf.getShort();
				int reason_id = buf.getInt();
				if (JSch.getLogger().isEnabled(Logger.INFO)) {
					JSch.getLogger().log(Logger.INFO, "Received SSH_MSG_UNIMPLEMENTED for " + reason_id);
				}
			} else if (type == SSH_MSG_DEBUG) {
				buf.rewind();
				buf.getInt();
				buf.getShort();
				/*
				 * byte always_display=(byte)buf.getByte(); byte[] message=buf.getString(); byte[] language_tag=buf.getString(); System.err.println("SSH_MSG_DEBUG:"+ " "+Util.byte2str(message)+ " "+Util.byte2str(language_tag));
				 */
			} else if (type == SSH_MSG_CHANNEL_WINDOW_ADJUST) {
				buf.rewind();
				buf.getInt();
				buf.getShort();
				Channel c = Channel.getChannel(buf.getInt(), this);
				if (c == null) {
				} else {
					c.addRemoteWindowSize(buf.getUInt());
				}
			} else if (type == UserAuth.SSH_MSG_USERAUTH_SUCCESS) {
				isAuthed = true;
				break;
			} else {
				break;
			}
		}
		buf.rewind();
		return buf;
	}

	private void start_discard(Buffer buf, Cipher cipher, MAC mac, int packet_length, int discard) throws JSchException, IOException {
		MAC discard_mac = null;

		if (!cipher.isCBC()) {
			throw new JSchException("Packet corrupt");
		}

		if ((packet_length != PACKET_MAX_SIZE) && (mac != null)) {
			discard_mac = mac;
		}

		discard -= buf.index;

		while (discard > 0) {
			buf.reset();
			int len = discard > buf.buffer.length ? buf.buffer.length : discard;
			io.getByte(buf.buffer, 0, len);
			if (discard_mac != null) {
				discard_mac.update(buf.buffer, 0, len);
			}
			discard -= len;
		}

		if (discard_mac != null) {
			discard_mac.doFinal(buf.buffer, 0);
		}

		throw new JSchException("Packet corrupt");
	}

	byte[] getSessionId() {
		return session_id;
	}

	private void receive_newkeys(Buffer buf, KeyExchange kex) throws Exception {
		updateKeys(kex);
		in_kex = false;
	}

	private void updateKeys(KeyExchange kex) throws Exception {
		byte[] K = kex.getK();
		byte[] H = kex.getH();
		HASH hash = kex.getHash();

		if (session_id == null) {
			session_id = new byte[H.length];
			System.arraycopy(H, 0, session_id, 0, H.length);
		}

		/*
		 * Initial IV client to server: HASH (K || H || "A" || session_id) Initial IV server to client: HASH (K || H || "B" || session_id) Encryption key client to server: HASH (K || H || "C" || session_id) Encryption key server to client: HASH (K || H || "D" || session_id) Integrity key client to server: HASH (K || H || "E" || session_id) Integrity key server to client: HASH (K || H || "F" || session_id)
		 */

		buf.reset();
		buf.putMPInt(K);
		buf.putByte(H);
		buf.putByte((byte) 0x41);
		buf.putByte(session_id);
		hash.update(buf.buffer, 0, buf.index);
		IVc2s = hash.digest();

		int j = buf.index - session_id.length - 1;

		buf.buffer[j]++;
		hash.update(buf.buffer, 0, buf.index);
		IVs2c = hash.digest();

		buf.buffer[j]++;
		hash.update(buf.buffer, 0, buf.index);
		Ec2s = hash.digest();

		buf.buffer[j]++;
		hash.update(buf.buffer, 0, buf.index);
		Es2c = hash.digest();

		buf.buffer[j]++;
		hash.update(buf.buffer, 0, buf.index);
		MACc2s = hash.digest();

		buf.buffer[j]++;
		hash.update(buf.buffer, 0, buf.index);
		MACs2c = hash.digest();

		try {
			Class<?> c;
			String method;

			method = guess[KeyExchange.PROPOSAL_ENC_ALGS_STOC];
			c = Class.forName(getConfig(method));
			s2ccipher = (Cipher) (c.newInstance());
			while (s2ccipher.getBlockSize() > Es2c.length) {
				buf.reset();
				buf.putMPInt(K);
				buf.putByte(H);
				buf.putByte(Es2c);
				hash.update(buf.buffer, 0, buf.index);
				byte[] foo = hash.digest();
				byte[] bar = new byte[Es2c.length + foo.length];
				System.arraycopy(Es2c, 0, bar, 0, Es2c.length);
				System.arraycopy(foo, 0, bar, Es2c.length, foo.length);
				Es2c = bar;
			}
			s2ccipher.init(Cipher.DECRYPT_MODE, Es2c, IVs2c);
			s2ccipher_size = s2ccipher.getIVSize();

			method = guess[KeyExchange.PROPOSAL_MAC_ALGS_STOC];
			c = Class.forName(getConfig(method));
			s2cmac = (MAC) (c.newInstance());
			MACs2c = expandKey(buf, K, H, MACs2c, hash, s2cmac.getBlockSize());
			s2cmac.init(MACs2c);
			// mac_buf=new byte[s2cmac.getBlockSize()];
			s2cmac_result1 = new byte[s2cmac.getBlockSize()];
			s2cmac_result2 = new byte[s2cmac.getBlockSize()];

			method = guess[KeyExchange.PROPOSAL_ENC_ALGS_CTOS];
			c = Class.forName(getConfig(method));
			c2scipher = (Cipher) (c.newInstance());
			while (c2scipher.getBlockSize() > Ec2s.length) {
				buf.reset();
				buf.putMPInt(K);
				buf.putByte(H);
				buf.putByte(Ec2s);
				hash.update(buf.buffer, 0, buf.index);
				byte[] foo = hash.digest();
				byte[] bar = new byte[Ec2s.length + foo.length];
				System.arraycopy(Ec2s, 0, bar, 0, Ec2s.length);
				System.arraycopy(foo, 0, bar, Ec2s.length, foo.length);
				Ec2s = bar;
			}
			c2scipher.init(Cipher.ENCRYPT_MODE, Ec2s, IVc2s);
			c2scipher_size = c2scipher.getIVSize();

			method = guess[KeyExchange.PROPOSAL_MAC_ALGS_CTOS];
			c = Class.forName(getConfig(method));
			c2smac = (MAC) (c.newInstance());
			MACc2s = expandKey(buf, K, H, MACc2s, hash, c2smac.getBlockSize());
			c2smac.init(MACc2s);
		} catch (Exception e) {
			if (e instanceof JSchException) {
				throw e;
			}
			throw new JSchException(e.toString(), e);
			// System.err.println("updatekeys: "+e);
		}
	}

	/*
	 * RFC 4253 7.2. Output from Key Exchange If the key length needed is longer than the output of the HASH, the key is extended by computing HASH of the concatenation of K and H and the entire key so far, and appending the resulting bytes (as many as HASH generates) to the key. This process is repeated until enough key material is available; the key is taken from the beginning of this value. In other words: K1 = HASH(K || H || X || session_id) (X is e.g., "A") K2 = HASH(K || H || K1) K3 =
	 * HASH(K || H || K1 || K2) ... key = K1 || K2 || K3 || ...
	 */
	private byte[] expandKey(Buffer buf, byte[] K, byte[] H, byte[] key, HASH hash, int required_length) throws Exception {
		byte[] result = key;
		int size = hash.getBlockSize();
		while (result.length < required_length) {
			buf.reset();
			buf.putMPInt(K);
			buf.putByte(H);
			buf.putByte(result);
			hash.update(buf.buffer, 0, buf.index);
			byte[] tmp = new byte[result.length + size];
			System.arraycopy(result, 0, tmp, 0, result.length);
			System.arraycopy(hash.digest(), 0, tmp, result.length, size);
			Util.bzero(result);
			result = tmp;
		}
		return result;
	}

	/* public *//* synchronized */void write(Packet packet, Channel c, int length) throws Exception {
		long t = getTimeout();
		while (true) {
			if (in_kex) {
				if ((t > 0L) && ((System.currentTimeMillis() - kex_start_time) > t)) {
					throw new JSchException("timeout in wating for rekeying process.");
				}
				try {
					Thread.sleep(10);
				} catch (java.lang.InterruptedException e) {
				}
				;
				continue;
			}
			synchronized (c) {

				if (c.rwsize < length) {
					try {
						c.notifyme++;
						c.wait(100);
					} catch (java.lang.InterruptedException e) {
					} finally {
						c.notifyme--;
					}
				}

				if (c.rwsize >= length) {
					c.rwsize -= length;
					break;
				}

			}
			if (c.close || !c.isConnected()) {
				throw new IOException("channel is broken");
			}

			boolean sendit = false;
			int s = 0;
			byte command = 0;
			int recipient = -1;
			synchronized (c) {
				if (c.rwsize > 0) {
					long len = c.rwsize;
					if (len > length) {
						len = length;
					}
					if (len != length) {
						s = packet.shift((int) len, (c2scipher != null ? c2scipher_size : 8), (c2smac != null ? c2smac.getBlockSize() : 0));
					}
					command = packet.buffer.getCommand();
					recipient = c.getRecipient();
					length -= len;
					c.rwsize -= len;
					sendit = true;
				}
			}
			if (sendit) {
				_write(packet);
				if (length == 0) {
					return;
				}
				packet.unshift(command, recipient, s, length);
			}

			synchronized (c) {
				if (in_kex) {
					continue;
				}
				if (c.rwsize >= length) {
					c.rwsize -= length;
					break;
				}

				// try{
				// System.out.println("1wait: "+c.rwsize);
				// c.notifyme++;
				// c.wait(100);
				// }
				// catch(java.lang.InterruptedException e){
				// }
				// finally{
				// c.notifyme--;
				// }
			}
		}
		_write(packet);
	}

	public void write(Packet packet) throws Exception {
		// System.err.println("in_kex="+in_kex+" "+(packet.buffer.getCommand()));
		long t = getTimeout();
		while (in_kex) {
			if ((t > 0L) && ((System.currentTimeMillis() - kex_start_time) > t)) {
				throw new JSchException("timeout in wating for rekeying process.");
			}
			byte command = packet.buffer.getCommand();
			// System.err.println("command: "+command);
			if ((command == SSH_MSG_KEXINIT) || (command == SSH_MSG_NEWKEYS) || (command == SSH_MSG_KEXDH_INIT) || (command == SSH_MSG_KEXDH_REPLY) ||

					(// this two commands have the same number, and it is not a mistake, so find bugs warning can be ignored
							command == SSH_MSG_KEX_DH_GEX_GROUP) || (command == SSH_MSG_KEX_DH_GEX_INIT) ||

							(command == SSH_MSG_KEX_DH_GEX_REPLY) || (command == SSH_MSG_KEX_DH_GEX_REQUEST) || (command == SSH_MSG_DISCONNECT)) {
				break;
			}
			try {
				Thread.sleep(10);
			} catch (java.lang.InterruptedException e) {
			}
			;
		}
		_write(packet);
	}

	private void _write(Packet packet) throws Exception {
		synchronized (lock) {
			encode(packet);
			if (io != null) {
				io.put(packet);
				seqo++;
			}
		}
	}

	Runnable thread;

	@Override
	public void run() {
		thread = this;

		byte[] foo;
		Buffer buf = new Buffer();
		Packet packet = new Packet(buf);
		int i = 0;
		Channel channel;
		int[] start = new int[1];
		int[] length = new int[1];
		KeyExchange kex = null;

		int stimeout = 0;
		try {
			while (isConnected && (thread != null)) {
				try {
					buf = read(buf);
					stimeout = 0;
				} catch (InterruptedIOException/* SocketTimeoutException */ee) {
					if (!in_kex && (stimeout < serverAliveCountMax)) {
						sendKeepAliveMsg();
						stimeout++;
						continue;
					} else if (in_kex && (stimeout < serverAliveCountMax)) {
						stimeout++;
						continue;
					}
					throw ee;
				}

				int msgType = buf.getCommand() & 0xff;

				if ((kex != null) && (kex.getState() == msgType)) {
					kex_start_time = System.currentTimeMillis();
					boolean result = kex.next(buf);
					if (!result) {
						throw new JSchException("verify: " + result);
					}
					continue;
				}

				switch (msgType) {
					case SSH_MSG_KEXINIT:
						// System.err.println("KEXINIT");
						kex = receive_kexinit(buf);
						break;

					case SSH_MSG_NEWKEYS:
						// System.err.println("NEWKEYS");
						send_newkeys();
						receive_newkeys(buf, kex);
						kex = null;
						break;

					case SSH_MSG_CHANNEL_DATA:
						buf.getInt();
						buf.getByte();
						buf.getByte();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						foo = buf.getString(start, length);
						if (channel == null) {
							break;
						}

						if (length[0] == 0) {
							break;
						}

						try {
							channel.write(foo, start[0], length[0]);
						} catch (Exception e) {
							// System.err.println(e);
							try {
								channel.disconnect();
							} catch (Exception ee) {
							}
							break;
						}
						int len = length[0];
						channel.setLocalWindowSize(channel.lwsize - len);
						if (channel.lwsize < (channel.lwsize_max / 2)) {
							packet.reset();
							buf.putByte((byte) SSH_MSG_CHANNEL_WINDOW_ADJUST);
							buf.putInt(channel.getRecipient());
							buf.putInt(channel.lwsize_max - channel.lwsize);
							synchronized (channel) {
								if (!channel.close) {
									write(packet);
								}
							}
							channel.setLocalWindowSize(channel.lwsize_max);
						}
						break;

					case SSH_MSG_CHANNEL_EXTENDED_DATA:
						throw new IllegalStateException("Can't operate ext data");

					case SSH_MSG_CHANNEL_WINDOW_ADJUST:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel == null) {
							break;
						}
						channel.addRemoteWindowSize(buf.getUInt());
						break;

					case SSH_MSG_CHANNEL_EOF:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel != null) {
							// channel.eof_remote=true;
							// channel.eof();
							channel.eof_remote();
						}
						/*
						 * packet.reset(); buf.putByte((byte)SSH_MSG_CHANNEL_EOF); buf.putInt(channel.getRecipient()); write(packet);
						 */
						break;
					case SSH_MSG_CHANNEL_CLOSE:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel != null) {
							// channel.close();
							channel.disconnect();
						}
						/*
						 * if(Channel.pool.size()==0){ thread=null; }
						 */
						break;
					case SSH_MSG_CHANNEL_OPEN_CONFIRMATION:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						int r = buf.getInt();
						long rws = buf.getUInt();
						int rps = buf.getInt();
						if (channel != null) {
							channel.setRemoteWindowSize(rws);
							channel.setRemotePacketSize(rps);
							channel.open_confirmation = true;
							channel.setRecipient(r);
						}
						break;
					case SSH_MSG_CHANNEL_OPEN_FAILURE:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel != null) {
							int reason_code = buf.getInt();
							// foo=buf.getString(); // additional textual
							// information
							// foo=buf.getString(); // language tag
							channel.setExitStatus(reason_code);
							channel.close = true;
							channel.eof_remote = true;
							channel.setRecipient(0);
						}
						break;
					case SSH_MSG_CHANNEL_REQUEST:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						foo = buf.getString();
						boolean reply = (buf.getByte() != 0);
						channel = Channel.getChannel(i, this);
						if (channel != null) {
							byte reply_type = (byte) SSH_MSG_CHANNEL_FAILURE;
							if ((Util.byte2str(foo)).equals("exit-status")) {
								i = buf.getInt(); // exit-status
								channel.setExitStatus(i);
								reply_type = (byte) SSH_MSG_CHANNEL_SUCCESS;
							}
							if (reply) {
								packet.reset();
								buf.putByte(reply_type);
								buf.putInt(channel.getRecipient());
								write(packet);
							}
						} else {
						}
						break;
					case SSH_MSG_CHANNEL_OPEN:
						buf.getInt();
						buf.getShort();
						foo = buf.getString();
						packet.reset();
						buf.putByte((byte) SSH_MSG_CHANNEL_OPEN_FAILURE);
						buf.putInt(buf.getInt());
						buf.putInt(Channel.SSH_OPEN_ADMINISTRATIVELY_PROHIBITED);
						buf.putString(Util.empty);
						buf.putString(Util.empty);
						write(packet);
						break;
					case SSH_MSG_CHANNEL_SUCCESS:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel == null) {
							break;
						}
						channel.reply = 1;
						break;
					case SSH_MSG_CHANNEL_FAILURE:
						buf.getInt();
						buf.getShort();
						i = buf.getInt();
						channel = Channel.getChannel(i, this);
						if (channel == null) {
							break;
						}
						channel.reply = 0;
						break;
					case SSH_MSG_GLOBAL_REQUEST:
						buf.getInt();
						buf.getShort();
						foo = buf.getString(); // request name
						reply = (buf.getByte() != 0);
						if (reply) {
							packet.reset();
							buf.putByte((byte) SSH_MSG_REQUEST_FAILURE);
							write(packet);
						}
						break;
					case SSH_MSG_REQUEST_FAILURE:
					case SSH_MSG_REQUEST_SUCCESS:
						break;
					default:
						// System.err.println("Session.run: unsupported type "+msgType);
						throw new IOException("Unknown SSH message type " + msgType);
				}
			}
		} catch (Exception e) {
			in_kex = false;
			if (JSch.getLogger().isEnabled(Logger.INFO)) {
				JSch.getLogger().log(Logger.INFO, "Caught an exception, leaving main loop due to " + e.getMessage());
			}
			// System.err.println("# Session.run");
			// e.printStackTrace();
		}
		try {
			disconnect();
		} catch (NullPointerException e) {
			// System.err.println("@1");
			// e.printStackTrace();
		} catch (Exception e) {
			// System.err.println("@2");
			// e.printStackTrace();
		}
		isConnected = false;
	}

	public void disconnect() {
		if (!isConnected) {
			return;
		}
		// System.err.println(this+": disconnect");
		// Thread.dumpStack();
		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "Disconnecting from " + host + " port " + port);
		}
		/*
		 * for(int i=0; i<Channel.pool.size(); i++){ try{ Channel c=((Channel)(Channel.pool.elementAt(i))); if(c.session==this) c.eof(); } catch(Exception e){ } }
		 */

		Channel.disconnect(this);

		isConnected = false;

		synchronized (lock) {
			if (connectThread != null) {
				Thread.yield();
				connectThread.interrupt();
				connectThread = null;
			}
		}
		thread = null;
		try {
			if (io != null) {
				if (io.in != null) {
					io.in.close();
				}
				if (io.out != null) {
					io.out.close();
				}
			}
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		io = null;
		socket = null;
		// synchronized(jsch.pool){
		// jsch.pool.removeElement(this);
		// }

		jsch.removeSession(this);

		// System.gc();
	}

	void addChannel(Channel channel) {
		channel.setSession(this);
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	void setUserName(String username) {
		this.username = username;
	}

	public void setUserInfo(UserInfo userinfo) {
		this.userinfo = userinfo;
	}

	public UserInfo getUserInfo() {
		return userinfo;
	}

	public void setPassword(String password) {
		if (password != null) {
			this.password = Util.str2byte(password);
		}
	}

	public void setPassword(byte[] password) {
		if (password != null) {
			this.password = new byte[password.length];
			System.arraycopy(password, 0, this.password, 0, password.length);
		}
	}

	public void setConfig(String key, String value) {
		synchronized (lock) {
			if (config == null) {
				config = new java.util.Hashtable<String, String>();
			}
			config.put(key, value);
		}
	}

	public String getConfig(String key) {
		Object foo = null;
		if (config != null) {
			foo = config.get(key);
			if (foo instanceof String) {
				return (String) foo;
			}
		}
		foo = JSch.getConfig(key);
		if (foo instanceof String) {
			return (String) foo;
		}
		return null;
	}

	public void setSocketFactory(SocketFactory sfactory) {
		socket_factory = sfactory;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) throws JSchException {
		if (socket == null) {
			if (timeout < 0) {
				throw new JSchException("invalid timeout value");
			}
			this.timeout = timeout;
			return;
		}
		try {
			socket.setSoTimeout(timeout);
			this.timeout = timeout;
		} catch (Exception e) {
			if (e instanceof Throwable) {
				throw new JSchException(e.toString(), e);
			}
			throw new JSchException(e.toString());
		}
	}

	public String getServerVersion() {
		return Util.byte2str(V_S);
	}

	public String getClientVersion() {
		return Util.byte2str(V_C);
	}

	public void setClientVersion(String cv) {
		V_C = Util.str2byte(cv);
	}

	public void sendIgnore() throws Exception {
		Buffer buf = new Buffer();
		Packet packet = new Packet(buf);
		packet.reset();
		buf.putByte((byte) SSH_MSG_IGNORE);
		write(packet);
	}

	private static final byte[] keepalivemsg = Util.str2byte("keepalive@jcraft.com");

	public void sendKeepAliveMsg() throws Exception {
		Buffer buf = new Buffer();
		Packet packet = new Packet(buf);
		packet.reset();
		buf.putByte((byte) SSH_MSG_GLOBAL_REQUEST);
		buf.putString(keepalivemsg);
		buf.putByte((byte) 1);
		write(packet);
	}

	private static final byte[] nomoresessions = Util.str2byte("no-more-sessions@openssh.com");

	public void noMoreSessionChannels() throws Exception {
		Buffer buf = new Buffer();
		Packet packet = new Packet(buf);
		packet.reset();
		buf.putByte((byte) SSH_MSG_GLOBAL_REQUEST);
		buf.putString(nomoresessions);
		buf.putByte((byte) 0);
		write(packet);
	}

	public String getHost() {
		return host;
	}

	public String getUserName() {
		return username;
	}

	public int getPort() {
		return port;
	}

	/**
	 * Sets the interval to send a keep-alive message. If zero is specified, any keep-alive message must not be sent. The default interval is zero.
	 *
	 * @param interval
	 *            the specified interval, in milliseconds.
	 * @see #getServerAliveInterval()
	 */
	public void setServerAliveInterval(int interval) throws JSchException {
		setTimeout(interval);
		serverAliveInterval = interval;
	}

	/**
	 * Returns setting for the interval to send a keep-alive message.
	 *
	 * @see #setServerAliveInterval(int)
	 */
	public int getServerAliveInterval() {
		return serverAliveInterval;
	}

	/**
	 * Sets the number of keep-alive messages which may be sent without receiving any messages back from the server. If this threshold is reached while keep-alive messages are being sent, the connection will be disconnected. The default value is one.
	 *
	 * @param count
	 *            the specified count
	 * @see #getServerAliveCountMax()
	 */
	public void setServerAliveCountMax(int count) {
		serverAliveCountMax = count;
	}

	/**
	 * Returns setting for the threshold to send keep-alive messages.
	 *
	 * @see #setServerAliveCountMax(int)
	 */
	public int getServerAliveCountMax() {
		return serverAliveCountMax;
	}

	public void setDaemonThread(boolean enable) {
		daemon_thread = enable;
	}

	private String[] checkCiphers(String ciphers) {
		if ((ciphers == null) || (ciphers.length() == 0)) {
			return null;
		}

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "CheckCiphers: " + ciphers);
		}

		String cipherc2s = getConfig("cipher.c2s");
		String ciphers2c = getConfig("cipher.s2c");

		Vector<String> result = new Vector<String>();
		String[] _ciphers = Util.split(ciphers, ",");
		for (int i = 0; i < _ciphers.length; i++) {
			String cipher = _ciphers[i];
			if ((ciphers2c.indexOf(cipher) == -1) && (cipherc2s.indexOf(cipher) == -1)) {
				continue;
			}
			if (!checkCipher(getConfig(cipher))) {
				result.addElement(cipher);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		String[] foo = new String[result.size()];
		System.arraycopy(result.toArray(), 0, foo, 0, result.size());

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			for (int i = 0; i < foo.length; i++) {
				JSch.getLogger().log(Logger.INFO, foo[i] + " is not available.");
			}
		}

		return foo;
	}

	static boolean checkCipher(String cipher) {
		try {
			Class<?> c = Class.forName(cipher);
			Cipher _c = (Cipher) (c.newInstance());
			_c.init(Cipher.ENCRYPT_MODE, new byte[_c.getBlockSize()], new byte[_c.getIVSize()]);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String[] checkKexes(String kexes) {
		if ((kexes == null) || (kexes.length() == 0)) {
			return null;
		}

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			JSch.getLogger().log(Logger.INFO, "CheckKexes: " + kexes);
		}

		java.util.Vector<String> result = new java.util.Vector<String>();
		String[] _kexes = Util.split(kexes, ",");
		for (int i = 0; i < _kexes.length; i++) {
			if (!checkKex(this, getConfig(_kexes[i]))) {
				result.addElement(_kexes[i]);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		String[] foo = new String[result.size()];
		System.arraycopy(result.toArray(), 0, foo, 0, result.size());

		if (JSch.getLogger().isEnabled(Logger.INFO)) {
			for (int i = 0; i < foo.length; i++) {
				JSch.getLogger().log(Logger.INFO, foo[i] + " is not available.");
			}
		}

		return foo;
	}

	static boolean checkKex(Session s, String kex) {
		try {
			Class<?> c = Class.forName(kex);
			KeyExchange _c = (KeyExchange) (c.newInstance());
			_c.init(s, null, null, null, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
