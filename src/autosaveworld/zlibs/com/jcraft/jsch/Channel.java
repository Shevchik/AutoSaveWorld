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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public abstract class Channel implements Runnable {

	static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91;
	static final int SSH_MSG_CHANNEL_OPEN_FAILURE = 92;
	static final int SSH_MSG_CHANNEL_WINDOW_ADJUST = 93;

	static final int SSH_OPEN_ADMINISTRATIVELY_PROHIBITED = 1;
	static final int SSH_OPEN_CONNECT_FAILED = 2;
	static final int SSH_OPEN_UNKNOWN_CHANNEL_TYPE = 3;
	static final int SSH_OPEN_RESOURCE_SHORTAGE = 4;

	static int index = 0;
	private static java.util.Vector<Channel> pool = new java.util.Vector<Channel>();

	public static enum ChannelType {
		SFTP;
	}

	static Channel getChannel(ChannelType ctype) {
		if (ctype == ChannelType.SFTP) {
			return new ChannelSftp();
		}
		return null;
	}

	static Channel getChannel(int id, Session session) {
		synchronized (pool) {
			for (int i = 0; i < pool.size(); i++) {
				Channel c = (pool.elementAt(i));
				if ((c.id == id) && (c.session == session)) {
					return c;
				}
			}
		}
		return null;
	}

	static void del(Channel c) {
		synchronized (pool) {
			pool.removeElement(c);
		}
	}

	int id;
	volatile int recipient = -1;
	protected byte[] type = Util.str2byte("foo");
	volatile int lwsize_max = 0x100000;
	volatile int lwsize = lwsize_max; // local initial window size
	volatile int lmpsize = 0x4000; // local maximum packet size

	volatile long rwsize = 0; // remote initial window size
	volatile int rmpsize = 0; // remote maximum packet size

	IO io = null;
	Thread thread = null;

	volatile boolean eof_local = false;
	volatile boolean eof_remote = false;

	volatile boolean close = false;
	volatile boolean connected = false;
	volatile boolean open_confirmation = false;

	volatile int exitstatus = -1;

	volatile int reply = 0;
	volatile int connectTimeout = 0;

	private Session session;

	int notifyme = 0;

	Channel() {
		synchronized (pool) {
			id = index++;
			pool.addElement(this);
		}
	}

	synchronized void setRecipient(int foo) {
		recipient = foo;
		if (notifyme > 0) {
			notifyAll();
		}
	}

	int getRecipient() {
		return recipient;
	}

	void init() throws JSchException {
	}

	public void connect() throws JSchException {
		connect(0);
	}

	public void connect(int connectTimeout) throws JSchException {
		this.connectTimeout = connectTimeout;
		try {
			sendChannelOpen();
			start();
		} catch (Exception e) {
			connected = false;
			disconnect();
			if (e instanceof JSchException) {
				throw (JSchException) e;
			}
			throw new JSchException(e.toString(), e);
		}
	}

	public void setXForwarding(boolean foo) {
	}

	public void start() throws JSchException {
	}

	public boolean isEOF() {
		return eof_remote;
	}

	void getData(Buffer buf) {
		setRecipient(buf.getInt());
		setRemoteWindowSize(buf.getUInt());
		setRemotePacketSize(buf.getInt());
	}

	static class MyPipedInputStream extends PipedInputStream {
		private int BUFFER_SIZE = 1024;
		private int max_buffer_size = BUFFER_SIZE;

		MyPipedInputStream() throws IOException {
			super();
		}

		MyPipedInputStream(int size) throws IOException {
			super();
			buffer = new byte[size];
			BUFFER_SIZE = size;
			max_buffer_size = size;
		}

		MyPipedInputStream(int size, int max_buffer_size) throws IOException {
			this(size);
			this.max_buffer_size = max_buffer_size;
		}

		MyPipedInputStream(PipedOutputStream out) throws IOException {
			super(out);
		}

		MyPipedInputStream(PipedOutputStream out, int size) throws IOException {
			super(out);
			buffer = new byte[size];
			BUFFER_SIZE = size;
		}

		public synchronized void updateReadSide() throws IOException {
			if (available() != 0) { // not empty
				return;
			}
			in = 0;
			out = 0;
			buffer[in++] = 0;
			read();
		}

		private int freeSpace() {
			int size = 0;
			if (out < in) {
				size = buffer.length - in;
			} else if (in < out) {
				if (in == -1) {
					size = buffer.length;
				} else {
					size = out - in;
				}
			}
			return size;
		}

		synchronized void checkSpace(int len) throws IOException {
			int size = freeSpace();
			if (size < len) {
				int datasize = buffer.length - size;
				int foo = buffer.length;
				while ((foo - datasize) < len) {
					foo *= 2;
				}

				if (foo > max_buffer_size) {
					foo = max_buffer_size;
				}
				if ((foo - datasize) < len) {
					return;
				}

				byte[] tmp = new byte[foo];
				if (out < in) {
					System.arraycopy(buffer, 0, tmp, 0, buffer.length);
				} else if (in < out) {
					if (in == -1) {
					} else {
						System.arraycopy(buffer, 0, tmp, 0, in);
						System.arraycopy(buffer, out, tmp, tmp.length - (buffer.length - out), (buffer.length - out));
						out = tmp.length - (buffer.length - out);
					}
				} else if (in == out) {
					System.arraycopy(buffer, 0, tmp, 0, buffer.length);
					in = buffer.length;
				}
				buffer = tmp;
			} else if ((buffer.length == size) && (size > BUFFER_SIZE)) {
				int i = size / 2;
				if (i < BUFFER_SIZE) {
					i = BUFFER_SIZE;
				}
				byte[] tmp = new byte[i];
				buffer = tmp;
			}
		}
	}

	void setLocalWindowSizeMax(int foo) {
		lwsize_max = foo;
	}

	void setLocalWindowSize(int foo) {
		lwsize = foo;
	}

	void setLocalPacketSize(int foo) {
		lmpsize = foo;
	}

	synchronized void setRemoteWindowSize(long foo) {
		rwsize = foo;
	}

	synchronized void addRemoteWindowSize(long foo) {
		rwsize += foo;
		if (notifyme > 0) {
			notifyAll();
		}
	}

	void setRemotePacketSize(int foo) {
		rmpsize = foo;
	}

	@Override
	public void run() {
	}

	void write(byte[] foo) throws IOException {
		write(foo, 0, foo.length);
	}

	void write(byte[] foo, int s, int l) throws IOException {
		try {
			io.put(foo, s, l);
		} catch (NullPointerException e) {
		}
	}

	void eof_remote() {
		eof_remote = true;
		try {
			io.out_close();
		} catch (NullPointerException e) {
		}
	}

	void eof() {
		if (eof_local) {
			return;
		}
		eof_local = true;

		int i = getRecipient();
		if (i == -1) {
			return;
		}

		try {
			Buffer buf = new Buffer(100);
			Packet packet = new Packet(buf);
			packet.reset();
			buf.putByte((byte) Session.SSH_MSG_CHANNEL_EOF);
			buf.putInt(i);
			synchronized (this) {
				if (!close) {
					getSession().write(packet);
				}
			}
		} catch (Exception e) {
			// System.err.println("Channel.eof");
			// e.printStackTrace();
		}
		/*
		 * if(!isConnected()){ disconnect(); }
		 */
	}

	/*
	 * http://www1.ietf.org/internet-drafts/draft-ietf-secsh-connect-24.txt
	 *
	 * 5.3 Closing a Channel When a party will no longer send more data to a channel, it SHOULD send SSH_MSG_CHANNEL_EOF.
	 *
	 * byte SSH_MSG_CHANNEL_EOF uint32 recipient_channel
	 *
	 * No explicit response is sent to this message. However, the application may send EOF to whatever is at the other end of the channel. Note that the channel remains open after this message, and more data may still be sent in the other direction. This message does not consume window space and can be sent even if no window space is available.
	 *
	 * When either party wishes to terminate the channel, it sends SSH_MSG_CHANNEL_CLOSE. Upon receiving this message, a party MUST send back a SSH_MSG_CHANNEL_CLOSE unless it has already sent this message for the channel. The channel is considered closed for a party when it has both sent and received SSH_MSG_CHANNEL_CLOSE, and the party may then reuse the channel number. A party MAY send SSH_MSG_CHANNEL_CLOSE without having sent or received SSH_MSG_CHANNEL_EOF.
	 *
	 * byte SSH_MSG_CHANNEL_CLOSE uint32 recipient_channel
	 *
	 * This message does not consume window space and can be sent even if no window space is available.
	 *
	 * It is recommended that any data sent before this message is delivered to the actual destination, if possible.
	 */

	void close() {
		if (close) {
			return;
		}
		close = true;
		eof_local = eof_remote = true;

		int i = getRecipient();
		if (i == -1) {
			return;
		}

		try {
			Buffer buf = new Buffer(100);
			Packet packet = new Packet(buf);
			packet.reset();
			buf.putByte((byte) Session.SSH_MSG_CHANNEL_CLOSE);
			buf.putInt(i);
			synchronized (this) {
				getSession().write(packet);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public boolean isClosed() {
		return close;
	}

	static void disconnect(Session session) {
		Channel[] channels = null;
		int count = 0;
		synchronized (pool) {
			channels = new Channel[pool.size()];
			for (int i = 0; i < pool.size(); i++) {
				try {
					Channel c = (pool.elementAt(i));
					if (c.session == session) {
						channels[count++] = c;
					}
				} catch (Exception e) {
				}
			}
		}
		for (int i = 0; i < count; i++) {
			channels[i].disconnect();
		}
	}

	public void disconnect() {
		// System.err.println(this+":disconnect "+io+" "+connected);
		// Thread.dumpStack();

		try {

			synchronized (this) {
				if (!connected) {
					return;
				}
				connected = false;
			}

			close();

			eof_remote = eof_local = true;

			thread = null;

			try {
				if (io != null) {
					io.close();
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
			// io=null;
		} finally {
			Channel.del(this);
		}
	}

	public boolean isConnected() {
		Session _session = session;
		if (_session != null) {
			return _session.isConnected() && connected;
		}
		return false;
	}

	void setExitStatus(int status) {
		exitstatus = status;
	}

	public int getExitStatus() {
		return exitstatus;
	}

	void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() throws JSchException {
		Session _session = session;
		if (_session == null) {
			throw new JSchException("session is not available");
		}
		return _session;
	}

	public int getId() {
		return id;
	}

	protected void sendOpenConfirmation() throws Exception {
		Buffer buf = new Buffer(100);
		Packet packet = new Packet(buf);
		packet.reset();
		buf.putByte((byte) SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
		buf.putInt(getRecipient());
		buf.putInt(id);
		buf.putInt(lwsize);
		buf.putInt(lmpsize);
		getSession().write(packet);
	}

	protected void sendOpenFailure(int reasoncode) {
		try {
			Buffer buf = new Buffer(100);
			Packet packet = new Packet(buf);
			packet.reset();
			buf.putByte((byte) SSH_MSG_CHANNEL_OPEN_FAILURE);
			buf.putInt(getRecipient());
			buf.putInt(reasoncode);
			buf.putString(Util.str2byte("open failed"));
			buf.putString(Util.empty);
			getSession().write(packet);
		} catch (Exception e) {
		}
	}

	protected Packet genChannelOpenPacket() {
		Buffer buf = new Buffer(100);
		Packet packet = new Packet(buf);
		// byte SSH_MSG_CHANNEL_OPEN(90)
		// string channel type //
		// uint32 sender channel // 0
		// uint32 initial window size // 0x100000(65536)
		// uint32 maxmum packet size // 0x4000(16384)
		packet.reset();
		buf.putByte((byte) 90);
		buf.putString(type);
		buf.putInt(id);
		buf.putInt(lwsize);
		buf.putInt(lmpsize);
		return packet;
	}

	protected void sendChannelOpen() throws Exception {
		Session _session = getSession();
		if (!_session.isConnected()) {
			throw new JSchException("session is down");
		}

		Packet packet = genChannelOpenPacket();
		_session.write(packet);

		int retry = 2000;
		long start = System.currentTimeMillis();
		long timeout = connectTimeout;
		if (timeout != 0L) {
			retry = 1;
		}
		synchronized (this) {
			while ((getRecipient() == -1) && _session.isConnected() && (retry > 0)) {
				if (timeout > 0L) {
					if ((System.currentTimeMillis() - start) > timeout) {
						retry = 0;
						continue;
					}
				}
				try {
					long t = timeout == 0L ? 10L : timeout;
					notifyme = 1;
					wait(t);
				} catch (java.lang.InterruptedException e) {
				} finally {
					notifyme = 0;
				}
				retry--;
			}
		}
		if (!_session.isConnected()) {
			throw new JSchException("session is down");
		}
		if (getRecipient() == -1) { // timeout
			throw new JSchException("channel is not opened.");
		}
		if (open_confirmation == false) { // SSH_MSG_CHANNEL_OPEN_FAILURE
			throw new JSchException("channel is not opened.");
		}
		connected = true;
	}

}
