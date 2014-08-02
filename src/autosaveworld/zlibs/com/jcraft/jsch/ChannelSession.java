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

class ChannelSession extends Channel {

	private static byte[] _session = Util.str2byte("session");

	protected boolean pty = false;

	protected String ttype = "vt100";
	protected int tcol = 80;
	protected int trow = 24;
	protected int twp = 640;
	protected int thp = 480;
	protected byte[] terminal_mode = null;

	ChannelSession() {
		super();
		type = _session;
		io = new IO();
	}

	/**
	 * Allocate a Pseudo-Terminal. Refer to RFC4254 6.2. Requesting a Pseudo-Terminal.
	 *
	 * @param enable
	 */
	public void setPty(boolean enable) {
		pty = enable;
	}

	/**
	 * Set the terminal mode.
	 *
	 * @param terminal_mode
	 */
	public void setTerminalMode(byte[] terminal_mode) {
		this.terminal_mode = terminal_mode;
	}

	/**
	 * Change the window dimension interactively. Refer to RFC4254 6.7. Window Dimension Change Message.
	 *
	 * @param col
	 *            terminal width, columns
	 * @param row
	 *            terminal height, rows
	 * @param wp
	 *            terminal width, pixels
	 * @param hp
	 *            terminal height, pixels
	 */
	public void setPtySize(int col, int row, int wp, int hp) {
		setPtyType(this.ttype, col, row, wp, hp);
		if (!pty || !isConnected()) {
			return;
		}
		try {
			RequestWindowChange request = new RequestWindowChange();
			request.setSize(col, row, wp, hp);
			request.request(getSession(), this);
		} catch (Exception e) {
			// System.err.println("ChannelSessio.setPtySize: "+e);
		}
	}

	/**
	 * Set the terminal type. This method is not effective after Channel#connect().
	 *
	 * @param ttype
	 *            terminal type(for example, "vt100")
	 * @see #setPtyType(String, int, int, int, int)
	 */
	public void setPtyType(String ttype) {
		setPtyType(ttype, 80, 24, 640, 480);
	}

	/**
	 * Set the terminal type. This method is not effective after Channel#connect().
	 *
	 * @param ttype
	 *            terminal type(for example, "vt100")
	 * @param col
	 *            terminal width, columns
	 * @param row
	 *            terminal height, rows
	 * @param wp
	 *            terminal width, pixels
	 * @param hp
	 *            terminal height, pixels
	 */
	public void setPtyType(String ttype, int col, int row, int wp, int hp) {
		this.ttype = ttype;
		this.tcol = col;
		this.trow = row;
		this.twp = wp;
		this.thp = hp;
	}

	protected void sendRequests() throws Exception {
		Session _session = getSession();
		Request request;

		if (pty) {
			request = new RequestPtyReq();
			((RequestPtyReq) request).setTType(ttype);
			((RequestPtyReq) request).setTSize(tcol, trow, twp, thp);
			if (terminal_mode != null) {
				((RequestPtyReq) request).setTerminalMode(terminal_mode);
			}
			request.request(_session, this);
		}
	}

	@Override
	public void run() {
		Buffer buf = new Buffer(rmpsize);
		Packet packet = new Packet(buf);
		int i = -1;
		try {
			while (isConnected() && thread != null && io != null && io.in != null) {
				i = io.in.read(buf.buffer, 14, buf.buffer.length - 14 - Session.buffer_margin);
				if (i == 0) {
					continue;
				}
				if (i == -1) {
					eof();
					break;
				}
				if (close) {
					break;
				}
				// System.out.println("write: "+i);
				packet.reset();
				buf.putByte((byte) Session.SSH_MSG_CHANNEL_DATA);
				buf.putInt(recipient);
				buf.putInt(i);
				buf.skip(i);
				getSession().write(packet, this, i);
			}
		} catch (Exception e) {
			// System.err.println("# ChannelExec.run");
			// e.printStackTrace();
		}
		Thread _thread = thread;
		if (_thread != null) {
			synchronized (_thread) {
				_thread.notifyAll();
			}
		}
		thread = null;
		// System.err.println(this+":run <");
	}
}
