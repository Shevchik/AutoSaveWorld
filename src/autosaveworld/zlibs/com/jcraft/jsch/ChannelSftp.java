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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

public class ChannelSftp extends ChannelSession {

	static private final int LOCAL_MAXIMUM_PACKET_SIZE = 32 * 1024;
	static private final int LOCAL_WINDOW_SIZE_MAX = (64 * LOCAL_MAXIMUM_PACKET_SIZE);

	private static final byte SSH_FXP_INIT = 1;
	private static final byte SSH_FXP_OPEN = 3;
	private static final byte SSH_FXP_CLOSE = 4;
	private static final byte SSH_FXP_WRITE = 6;
	private static final byte SSH_FXP_OPENDIR = 11;
	private static final byte SSH_FXP_READDIR = 12;
	private static final byte SSH_FXP_REMOVE = 13;
	private static final byte SSH_FXP_MKDIR = 14;
	private static final byte SSH_FXP_RMDIR = 15;
	private static final byte SSH_FXP_REALPATH = 16;
	private static final byte SSH_FXP_STAT = 17;
	private static final byte SSH_FXP_STATUS = 101;
	private static final byte SSH_FXP_HANDLE = 102;
	private static final byte SSH_FXP_NAME = 104;
	private static final byte SSH_FXP_ATTRS = 105;
	private static final byte SSH_FXP_EXTENDED = (byte) 200;

	// pflags
	private static final int SSH_FXF_WRITE = 0x00000002;
	private static final int SSH_FXF_CREAT = 0x00000008;
	private static final int SSH_FXF_TRUNC = 0x00000010;

	public static final int SSH_FX_OK = 0;
	public static final int SSH_FX_EOF = 1;
	public static final int SSH_FX_NO_SUCH_FILE = 2;
	public static final int SSH_FX_PERMISSION_DENIED = 3;
	public static final int SSH_FX_FAILURE = 4;
	public static final int SSH_FX_BAD_MESSAGE = 5;
	public static final int SSH_FX_NO_CONNECTION = 6;
	public static final int SSH_FX_CONNECTION_LOST = 7;
	public static final int SSH_FX_OP_UNSUPPORTED = 8;
	/*
	 * SSH_FX_OK Indicates successful completion of the operation. SSH_FX_EOF indicates end-of-file condition; for SSH_FX_READ it means that no more data is available in the file, and for SSH_FX_READDIR it indicates that no more files are contained in the directory. SSH_FX_NO_SUCH_FILE is returned when a reference is made to a file which should exist but doesn't. SSH_FX_PERMISSION_DENIED is returned when the authenticated user does not have sufficient permissions to perform the operation.
	 * SSH_FX_FAILURE is a generic catch-all error message; it should be returned if an error occurs for which there is no more specific error code defined. SSH_FX_BAD_MESSAGE may be returned if a badly formatted packet or protocol incompatibility is detected. SSH_FX_NO_CONNECTION is a pseudo-error which indicates that the client has no connection to the server (it can only be generated locally by the client, and MUST NOT be returned by servers). SSH_FX_CONNECTION_LOST is a pseudo-error which
	 * indicates that the connection to the server has been lost (it can only be generated locally by the client, and MUST NOT be returned by servers). SSH_FX_OP_UNSUPPORTED indicates that an attempt was made to perform an operation which is not supported for the server (it may be generated locally by the client if e.g. the version number exchange indicates that a required feature is not supported by the server, or it may be returned by the server if the server does not implement an operation).
	 */
	private static final int MAX_MSG_LENGTH = 256 * 1024;

	public static final int OVERWRITE = 0;
	public static final int RESUME = 1;
	public static final int APPEND = 2;

	private int seq = 1;
	private int[] ackid = new int[1];

	private Buffer buf;
	private Packet packet;

	// The followings will be used in file uploading.
	private Buffer obuf;
	private Packet opacket;

	private int client_version = 3;
	private int server_version = 3;
	private String version = String.valueOf(client_version);

	private java.util.Hashtable<String, String> extensions = null;
	private InputStream io_in = null;

	private String cwd;
	private String home;

	private static final String UTF8 = "UTF-8";
	private String fEncoding = UTF8;
	private boolean fEncoding_is_utf8 = true;

	private int bulkRequests = 16;

	/**
	 * Specify how many requests may be sent at any one time. Increasing this value may slightly improve file transfer speed but will increase memory usage. The default is 16 requests.
	 *
	 * @param bulk_requests
	 *            how many requests may be outstanding at any one time.
	 */
	public void setBulkRequests(int bulk_requests) throws JSchException {
		if (bulk_requests > 0) {
			bulkRequests = bulk_requests;
		} else {
			throw new JSchException("setBulkRequests: " + bulk_requests + " must be greater than 0.");
		}
	}

	/**
	 * This method will return the value how many requests may be sent at any one time.
	 *
	 * @return how many requests may be sent at any one time.
	 */
	public int getBulkRequests() {
		return bulkRequests;
	}

	public ChannelSftp() {
		super();
		setLocalWindowSizeMax(LOCAL_WINDOW_SIZE_MAX);
		setLocalWindowSize(LOCAL_WINDOW_SIZE_MAX);
		setLocalPacketSize(LOCAL_MAXIMUM_PACKET_SIZE);
	}

	@Override
	void init() {
	}

	@Override
	public void start() throws JSchException {
		try {

			PipedOutputStream pos = new PipedOutputStream();
			io.setOutputStream(pos);
			PipedInputStream pis = new MyPipedInputStream(pos, rmpsize);
			io.setInputStream(pis);

			io_in = io.in;

			if (io_in == null) {
				throw new JSchException("channel is down");
			}

			Request request = new RequestSftp();
			request.request(getSession(), this);

			/*
			 * System.err.println("lmpsize: "+lmpsize); System.err.println("lwsize: "+lwsize); System.err.println("rmpsize: "+rmpsize); System.err.println("rwsize: "+rwsize);
			 */

			buf = new Buffer(lmpsize);
			packet = new Packet(buf);

			obuf = new Buffer(rmpsize);
			opacket = new Packet(obuf);

			int length;
			// send SSH_FXP_INIT
			sendINIT();

			// receive SSH_FXP_VERSION
			Header header = new Header();
			header = header(buf, header);
			length = header.length;
			if (length > MAX_MSG_LENGTH) {
				throw new SftpException(SSH_FX_FAILURE, "Received message is too long: " + length);
			}
			server_version = header.rid;
			extensions = new java.util.Hashtable<String, String>();
			if (length > 0) {
				// extension data
				fill(buf, length);
				byte[] extension_name = null;
				byte[] extension_data = null;
				while (length > 0) {
					extension_name = buf.getString();
					length -= (4 + extension_name.length);
					extension_data = buf.getString();
					length -= (4 + extension_data.length);
					extensions.put(Util.byte2str(extension_name), Util.byte2str(extension_data));
				}
			}
		} catch (Exception e) {
			// System.err.println(e);
			if (e instanceof JSchException) {
				throw (JSchException) e;
			}
			if (e instanceof Throwable) {
				throw new JSchException(e.toString(), e);
			}
			throw new JSchException(e.toString());
		}
	}

	public void cd(String path) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);
			path = isUnique(path);

			byte[] str = _realpath(path);
			SftpATTRS attr = _stat(str);

			if ((attr.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0) {
				throw new SftpException(SSH_FX_FAILURE, "Can't change directory: " + path);
			}
			if (!attr.isDir()) {
				throw new SftpException(SSH_FX_FAILURE, "Can't change directory: " + path);
			}

			setCwd(Util.byte2str(str, fEncoding));
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public void put(InputStream src, String dst) throws SftpException {
		put(src, dst, null, OVERWRITE);
	}

	public void put(InputStream src, String dst, int mode) throws SftpException {
		put(src, dst, null, mode);
	}

	/**
	 * Sends data from the input stream <code>src</code> to <code>dst</code> file. The <code>mode</code> should be <code>OVERWRITE</code>, <code>RESUME</code> or <code>APPEND</code>.
	 *
	 * @param src
	 *            input stream
	 * @param dst
	 *            destination file
	 * @param monitor
	 *            progress monitor
	 * @param mode
	 *            how data should be added to dst
	 */
	public void put(InputStream src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			dst = remoteAbsolutePath(dst);

			Vector<String> v = glob_remote(dst);
			int vsize = v.size();
			if (vsize != 1) {
				if (vsize == 0) {
					if (isPattern(dst)) {
						throw new SftpException(SSH_FX_FAILURE, dst);
					} else {
						dst = Util.unquote(dst);
					}
				}
				throw new SftpException(SSH_FX_FAILURE, v.toString());
			} else {
				dst = (v.elementAt(0));
			}

			if (monitor != null) {
				monitor.init(SftpProgressMonitor.PUT, "-", dst, SftpProgressMonitor.UNKNOWN_SIZE);
			}

			_put(src, dst, monitor, mode);
		} catch (Exception e) {
			if (e instanceof SftpException) {
				if ((((SftpException) e).id == SSH_FX_FAILURE) && isRemoteDir(dst)) {
					throw new SftpException(SSH_FX_FAILURE, dst + " is a directory");
				}
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
			}
			throw new SftpException(SSH_FX_FAILURE, e.toString());
		}
	}

	public void _put(InputStream src, String dst, SftpProgressMonitor monitor, int mode) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			byte[] dstb = Util.str2byte(dst, fEncoding);
			long skip = 0;
			if ((mode == RESUME) || (mode == APPEND)) {
				try {
					SftpATTRS attr = _stat(dstb);
					skip = attr.getSize();
				} catch (Exception eee) {
					// System.err.println(eee);
				}
			}
			if ((mode == RESUME) && (skip > 0)) {
				long skipped = src.skip(skip);
				if (skipped < skip) {
					throw new SftpException(SSH_FX_FAILURE, "failed to resume for " + dst);
				}
			}

			if (mode == OVERWRITE) {
				sendOPENW(dstb);
			} else {
				sendOPENA(dstb);
			}

			Header header = new Header();
			header = header(buf, header);
			int length = header.length;
			int type = header.type;

			fill(buf, length);

			if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_HANDLE)) {
				throw new SftpException(SSH_FX_FAILURE, "invalid type=" + type);
			}
			if (type == SSH_FXP_STATUS) {
				int i = buf.getInt();
				throwStatusError(buf, i);
			}
			byte[] handle = buf.getString(); // handle
			byte[] data = null;

			boolean dontcopy = true;

			if (!dontcopy) { // This case will not work anymore.
				data = new byte[obuf.buffer.length - (5 + 13 + 21 + handle.length + Session.buffer_margin)];
			}

			long offset = 0;
			if ((mode == RESUME) || (mode == APPEND)) {
				offset += skip;
			}

			int startid = seq;
			int ackcount = 0;
			int _s = 0;
			int _datalen = 0;

			if (!dontcopy) { // This case will not work anymore.
				_datalen = data.length;
			} else {
				data = obuf.buffer;
				_s = 5 + 13 + 21 + handle.length;
				_datalen = obuf.buffer.length - _s - Session.buffer_margin;
			}

			int bulk_requests = bulkRequests;

			while (true) {
				int nread = 0;
				int count = 0;
				int s = _s;
				int datalen = _datalen;

				do {
					nread = src.read(data, s, datalen);
					if (nread > 0) {
						s += nread;
						datalen -= nread;
						count += nread;
					}
				} while ((datalen > 0) && (nread > 0));
				if (count <= 0) {
					break;
				}

				int foo = count;
				while (foo > 0) {
					if (((seq - 1) == startid) || (((seq - startid) - ackcount) >= bulk_requests)) {
						while (((seq - startid) - ackcount) >= bulk_requests) {
							if (checkStatus(ackid, header)) {
								int _ackid = ackid[0];
								if ((startid > _ackid) || (_ackid > (seq - 1))) {
									if (_ackid == seq) {
										System.err.println("ack error: startid=" + startid + " seq=" + seq + " _ackid=" + _ackid);
									} else {
										throw new SftpException(SSH_FX_FAILURE, "ack error: startid=" + startid + " seq=" + seq + " _ackid=" + _ackid);
									}
								}
								ackcount++;
							} else {
								break;
							}
						}
					}
					foo -= sendWRITE(handle, offset, data, 0, foo);
				}
				offset += count;
				if ((monitor != null) && !monitor.count(count)) {
					break;
				}
			}
			int _ackcount = seq - startid;
			while (_ackcount > ackcount) {
				if (!checkStatus(null, header)) {
					break;
				}
				ackcount++;
			}
			if (monitor != null) {
				monitor.end();
			}
			_sendCLOSE(handle, header);
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, e.toString(), e);
			}
			throw new SftpException(SSH_FX_FAILURE, e.toString());
		}
	}

	public java.util.Vector<LsEntry> ls(String path) throws SftpException {
		final java.util.Vector<LsEntry> v = new Vector<LsEntry>();
		LsEntrySelector selector = new LsEntrySelector() {
			@Override
			public int select(LsEntry entry) {
				v.addElement(entry);
				return CONTINUE;
			}
		};
		ls(path, selector);
		return v;
	}

	/**
	 * List files specified by the remote <code>path</code>. Each files and directories will be passed to <code>LsEntrySelector#select(LsEntry)</code> method, and if that method returns <code>LsEntrySelector#BREAK</code>, the operation will be canceled immediately.
	 *
	 * @see ChannelSftp.LsEntrySelector
	 * @since 0.1.47
	 */
	public void ls(String path, LsEntrySelector selector) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);
			byte[] pattern = null;

			int foo = path.lastIndexOf('/');
			String dir = path.substring(0, ((foo == 0) ? 1 : foo));
			String _pattern = path.substring(foo + 1);
			dir = Util.unquote(dir);

			// If pattern has included '*' or '?', we need to convert
			// to UTF-8 string before globbing.
			byte[][] _pattern_utf8 = new byte[1][];
			boolean pattern_has_wildcard = isPattern(_pattern, _pattern_utf8);

			if (pattern_has_wildcard) {
				pattern = _pattern_utf8[0];
			} else {
				String upath = Util.unquote(path);
				SftpATTRS attr = _stat(upath);
				if (attr.isDir()) {
					pattern = null;
					dir = upath;
				} else {
					/*
					 * // If we can generage longname by ourself, // we don't have to use openDIR. String filename=Util.unquote(_pattern); String longname=... v.addElement(new LsEntry(filename, longname, attr)); return v;
					 */
					if (fEncoding_is_utf8) {
						pattern = _pattern_utf8[0];
						pattern = Util.unquote(pattern);
					} else {
						_pattern = Util.unquote(_pattern);
						pattern = Util.str2byte(_pattern, fEncoding);
					}

				}
			}

			sendOPENDIR(Util.str2byte(dir, fEncoding));

			Header header = new Header();
			header = header(buf, header);
			int length = header.length;
			int type = header.type;

			fill(buf, length);

			if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_HANDLE)) {
				throw new SftpException(SSH_FX_FAILURE, "");
			}
			if (type == SSH_FXP_STATUS) {
				int i = buf.getInt();
				throwStatusError(buf, i);
			}

			int cancel = LsEntrySelector.CONTINUE;
			byte[] handle = buf.getString(); // handle

			while (cancel == LsEntrySelector.CONTINUE) {

				sendREADDIR(handle);

				header = header(buf, header);
				length = header.length;
				type = header.type;
				if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_NAME)) {
					throw new SftpException(SSH_FX_FAILURE, "");
				}
				if (type == SSH_FXP_STATUS) {
					fill(buf, length);
					int i = buf.getInt();
					if (i == SSH_FX_EOF) {
						break;
					}
					throwStatusError(buf, i);
				}

				buf.rewind();
				fill(buf.buffer, 0, 4);
				length -= 4;
				int count = buf.getInt();

				buf.reset();
				while (count > 0) {
					if (length > 0) {
						buf.shift();
						int j = (buf.buffer.length > (buf.index + length)) ? length : (buf.buffer.length - buf.index);
						int i = fill(buf.buffer, buf.index, j);
						buf.index += i;
						length -= i;
					}
					byte[] filename = buf.getString();
					byte[] longname = null;
					if (server_version <= 3) {
						longname = buf.getString();
					}
					SftpATTRS attrs = SftpATTRS.getATTR(buf);

					if (cancel == LsEntrySelector.BREAK) {
						count--;
						continue;
					}

					boolean find = false;
					String f = null;
					if (pattern == null) {
						find = true;
					} else if (!pattern_has_wildcard) {
						find = Util.array_equals(pattern, filename);
					} else {
						byte[] _filename = filename;
						if (!fEncoding_is_utf8) {
							f = Util.byte2str(_filename, fEncoding);
							_filename = Util.str2byte(f, UTF8);
						}
						find = Util.glob(pattern, _filename);
					}

					if (find) {
						if (f == null) {
							f = Util.byte2str(filename, fEncoding);
						}
						String l = null;
						if (longname == null) {
							l = attrs.toString() + " " + f;
						} else {
							l = Util.byte2str(longname, fEncoding);
						}

						cancel = selector.select(new LsEntry(f, l, attrs));
					}

					count--;
				}
			}
			_sendCLOSE(handle, header);

			/*
			 * if(v.size()==1 && pattern_has_wildcard){ LsEntry le=(LsEntry)v.elementAt(0); if(le.getAttrs().isDir()){ String f=le.getFilename(); if(isPattern(f)){ f=Util.quote(f); } if(!dir.endsWith("/")){ dir+="/"; } v=null; return ls(dir+f); } }
			 */

		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public void rm(String path) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);

			Vector<String> v = glob_remote(path);
			int vsize = v.size();

			Header header = new Header();

			for (int j = 0; j < vsize; j++) {
				path = (v.elementAt(j));
				sendREMOVE(Util.str2byte(path, fEncoding));

				header = header(buf, header);
				int length = header.length;
				int type = header.type;

				fill(buf, length);

				if (type != SSH_FXP_STATUS) {
					throw new SftpException(SSH_FX_FAILURE, "");
				}
				int i = buf.getInt();
				if (i != SSH_FX_OK) {
					throwStatusError(buf, i);
				}
			}
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public boolean isRemoteDir(String path) {
		try {
			sendSTAT(Util.str2byte(path, fEncoding));

			Header header = new Header();
			header = header(buf, header);
			int length = header.length;
			int type = header.type;

			fill(buf, length);

			if (type != SSH_FXP_ATTRS) {
				return false;
			}
			SftpATTRS attr = SftpATTRS.getATTR(buf);
			return attr.isDir();
		} catch (Exception e) {
		}
		return false;
	}

	public void rmdir(String path) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);

			Vector<String> v = glob_remote(path);
			int vsize = v.size();

			Header header = new Header();

			for (int j = 0; j < vsize; j++) {
				path = (v.elementAt(j));
				sendRMDIR(Util.str2byte(path, fEncoding));

				header = header(buf, header);
				int length = header.length;
				int type = header.type;

				fill(buf, length);

				if (type != SSH_FXP_STATUS) {
					throw new SftpException(SSH_FX_FAILURE, "");
				}

				int i = buf.getInt();
				if (i != SSH_FX_OK) {
					throwStatusError(buf, i);
				}
			}
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public void mkdir(String path) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);

			sendMKDIR(Util.str2byte(path, fEncoding), null);

			Header header = new Header();
			header = header(buf, header);
			int length = header.length;
			int type = header.type;

			fill(buf, length);

			if (type != SSH_FXP_STATUS) {
				throw new SftpException(SSH_FX_FAILURE, "");
			}

			int i = buf.getInt();
			if (i == SSH_FX_OK) {
				return;
			}
			throwStatusError(buf, i);
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public SftpATTRS stat(String path) throws SftpException {
		try {
			((MyPipedInputStream) io_in).updateReadSide();

			path = remoteAbsolutePath(path);
			path = isUnique(path);

			return _stat(path);
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
		// return null;
	}

	private SftpATTRS _stat(byte[] path) throws SftpException {
		try {

			sendSTAT(path);

			Header header = new Header();
			header = header(buf, header);
			int length = header.length;
			int type = header.type;

			fill(buf, length);

			if (type != SSH_FXP_ATTRS) {
				if (type == SSH_FXP_STATUS) {
					int i = buf.getInt();
					throwStatusError(buf, i);
				}
				throw new SftpException(SSH_FX_FAILURE, "");
			}
			SftpATTRS attr = SftpATTRS.getATTR(buf);
			return attr;
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
		// return null;
	}

	private SftpATTRS _stat(String path) throws SftpException {
		return _stat(Util.str2byte(path, fEncoding));
	}

	private byte[] _realpath(String path) throws SftpException, IOException, Exception {
		sendREALPATH(Util.str2byte(path, fEncoding));

		Header header = new Header();
		header = header(buf, header);
		int length = header.length;
		int type = header.type;

		fill(buf, length);

		if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_NAME)) {
			throw new SftpException(SSH_FX_FAILURE, "");
		}
		int i;
		if (type == SSH_FXP_STATUS) {
			i = buf.getInt();
			throwStatusError(buf, i);
		}
		i = buf.getInt(); // count

		byte[] str = null;
		while (i-- > 0) {
			str = buf.getString(); // absolute path;
			if (server_version <= 3) {
				buf.getString();
			}
			SftpATTRS.getATTR(buf);
		}
		return str;
	}

	public String pwd() throws SftpException {
		return getCwd();
	}

	public String version() {
		return version;
	}

	public String getHome() throws SftpException {
		if (home == null) {
			try {
				((MyPipedInputStream) io_in).updateReadSide();

				byte[] _home = _realpath("");
				home = Util.byte2str(_home, fEncoding);
			} catch (Exception e) {
				if (e instanceof SftpException) {
					throw (SftpException) e;
				}
				if (e instanceof Throwable) {
					throw new SftpException(SSH_FX_FAILURE, "", e);
				}
				throw new SftpException(SSH_FX_FAILURE, "");
			}
		}
		return home;
	}

	private String getCwd() throws SftpException {
		if (cwd == null) {
			cwd = getHome();
		}
		return cwd;
	}

	private void setCwd(String cwd) {
		this.cwd = cwd;
	}

	private boolean checkStatus(int[] ackid, Header header) throws IOException, SftpException {
		header = header(buf, header);
		int length = header.length;
		int type = header.type;
		if (ackid != null) {
			ackid[0] = header.rid;
		}

		fill(buf, length);

		if (type != SSH_FXP_STATUS) {
			throw new SftpException(SSH_FX_FAILURE, "");
		}
		int i = buf.getInt();
		if (i != SSH_FX_OK) {
			throwStatusError(buf, i);
		}
		return true;
	}

	private boolean _sendCLOSE(byte[] handle, Header header) throws Exception {
		sendCLOSE(handle);
		return checkStatus(null, header);
	}

	private void sendINIT() throws Exception {
		packet.reset();
		putHEAD(SSH_FXP_INIT, 5);
		buf.putInt(3); // version 3
		getSession().write(packet, this, 5 + 4);
	}

	private void sendREALPATH(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_REALPATH, path);
	}

	private void sendSTAT(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_STAT, path);
	}

	private void sendREMOVE(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_REMOVE, path);
	}

	private void sendMKDIR(byte[] path, SftpATTRS attr) throws Exception {
		packet.reset();
		putHEAD(SSH_FXP_MKDIR, 9 + path.length + (attr != null ? attr.length() : 4));
		buf.putInt(seq++);
		buf.putString(path); // path
		if (attr != null) {
			attr.dump(buf);
		} else {
			buf.putInt(0);
		}
		getSession().write(packet, this, 9 + path.length + (attr != null ? attr.length() : 4) + 4);
	}

	private void sendRMDIR(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_RMDIR, path);
	}

	private void sendOPENDIR(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_OPENDIR, path);
	}

	private void sendREADDIR(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_READDIR, path);
	}

	private void sendCLOSE(byte[] path) throws Exception {
		sendPacketPath(SSH_FXP_CLOSE, path);
	}

	private void sendOPENW(byte[] path) throws Exception {
		sendOPEN(path, SSH_FXF_WRITE | SSH_FXF_CREAT | SSH_FXF_TRUNC);
	}

	private void sendOPENA(byte[] path) throws Exception {
		sendOPEN(path, SSH_FXF_WRITE | /* SSH_FXF_APPEND| */SSH_FXF_CREAT);
	}

	private void sendOPEN(byte[] path, int mode) throws Exception {
		packet.reset();
		putHEAD(SSH_FXP_OPEN, 17 + path.length);
		buf.putInt(seq++);
		buf.putString(path);
		buf.putInt(mode);
		buf.putInt(0); // attrs
		getSession().write(packet, this, 17 + path.length + 4);
	}

	private void sendPacketPath(byte fxp, byte[] path) throws Exception {
		sendPacketPath(fxp, path, (String) null);
	}

	private void sendPacketPath(byte fxp, byte[] path, String extension) throws Exception {
		packet.reset();
		int len = 9 + path.length;
		if (extension == null) {
			putHEAD(fxp, len);
			buf.putInt(seq++);
		} else {
			len += (4 + extension.length());
			putHEAD(SSH_FXP_EXTENDED, len);
			buf.putInt(seq++);
			buf.putString(Util.str2byte(extension));
		}
		buf.putString(path); // path
		getSession().write(packet, this, len + 4);
	}

	private int sendWRITE(byte[] handle, long offset, byte[] data, int start, int length) throws Exception {
		int _length = length;
		opacket.reset();
		if (obuf.buffer.length < (obuf.index + 13 + 21 + handle.length + length + Session.buffer_margin)) {
			_length = obuf.buffer.length - (obuf.index + 13 + 21 + handle.length + Session.buffer_margin);
			// System.err.println("_length="+_length+" length="+length);
		}

		putHEAD(obuf, SSH_FXP_WRITE, 21 + handle.length + _length); // 14
		obuf.putInt(seq++); // 4
		obuf.putString(handle); // 4+handle.length
		obuf.putLong(offset); // 8
		if (obuf.buffer != data) {
			obuf.putString(data, start, _length); // 4+_length
		} else {
			obuf.putInt(_length);
			obuf.skip(_length);
		}
		getSession().write(opacket, this, 21 + handle.length + _length + 4);
		return _length;
	}

	private void putHEAD(Buffer buf, byte type, int length) throws Exception {
		buf.putByte((byte) Session.SSH_MSG_CHANNEL_DATA);
		buf.putInt(recipient);
		buf.putInt(length + 4);
		buf.putInt(length);
		buf.putByte(type);
	}

	private void putHEAD(byte type, int length) throws Exception {
		putHEAD(buf, type, length);
	}

	private Vector<String> glob_remote(String _path) throws Exception {
		Vector<String> v = new Vector<String>();
		int i = 0;

		int foo = _path.lastIndexOf('/');
		if (foo < 0) { // it is not absolute path.
			v.addElement(Util.unquote(_path));
			return v;
		}

		String dir = _path.substring(0, ((foo == 0) ? 1 : foo));
		String _pattern = _path.substring(foo + 1);

		dir = Util.unquote(dir);

		byte[] pattern = null;
		byte[][] _pattern_utf8 = new byte[1][];
		boolean pattern_has_wildcard = isPattern(_pattern, _pattern_utf8);

		if (!pattern_has_wildcard) {
			if (!dir.equals("/")) {
				dir += "/";
			}
			v.addElement(dir + Util.unquote(_pattern));
			return v;
		}

		pattern = _pattern_utf8[0];

		sendOPENDIR(Util.str2byte(dir, fEncoding));

		Header header = new Header();
		header = header(buf, header);
		int length = header.length;
		int type = header.type;

		fill(buf, length);

		if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_HANDLE)) {
			throw new SftpException(SSH_FX_FAILURE, "");
		}
		if (type == SSH_FXP_STATUS) {
			i = buf.getInt();
			throwStatusError(buf, i);
		}

		byte[] handle = buf.getString(); // filename
		String pdir = null; // parent directory

		while (true) {
			sendREADDIR(handle);
			header = header(buf, header);
			length = header.length;
			type = header.type;

			if ((type != SSH_FXP_STATUS) && (type != SSH_FXP_NAME)) {
				throw new SftpException(SSH_FX_FAILURE, "");
			}
			if (type == SSH_FXP_STATUS) {
				fill(buf, length);
				break;
			}

			buf.rewind();
			fill(buf.buffer, 0, 4);
			length -= 4;
			int count = buf.getInt();

			buf.reset();
			while (count > 0) {
				if (length > 0) {
					buf.shift();
					int j = (buf.buffer.length > (buf.index + length)) ? length : (buf.buffer.length - buf.index);
					i = io_in.read(buf.buffer, buf.index, j);
					if (i <= 0) {
						break;
					}
					buf.index += i;
					length -= i;
				}

				byte[] filename = buf.getString();
				// System.err.println("filename: "+new String(filename));
				if (server_version <= 3) {
					buf.getString();
				}
				SftpATTRS.getATTR(buf);

				byte[] _filename = filename;
				String f = null;
				boolean found = false;

				if (!fEncoding_is_utf8) {
					f = Util.byte2str(filename, fEncoding);
					_filename = Util.str2byte(f, UTF8);
				}
				found = Util.glob(pattern, _filename);

				if (found) {
					if (f == null) {
						f = Util.byte2str(filename, fEncoding);
					}
					if (pdir == null) {
						pdir = dir;
						if (!pdir.endsWith("/")) {
							pdir += "/";
						}
					}
					v.addElement(pdir + f);
				}
				count--;
			}
		}
		if (_sendCLOSE(handle, header)) {
			return v;
		}
		return null;
	}

	private boolean isPattern(byte[] path) {
		int length = path.length;
		int i = 0;
		while (i < length) {
			if ((path[i] == '*') || (path[i] == '?')) {
				return true;
			}
			if ((path[i] == '\\') && ((i + 1) < length)) {
				i++;
			}
			i++;
		}
		return false;
	}

	private void throwStatusError(Buffer buf, int i) throws SftpException {
		if ((server_version >= 3) && (buf.getLength() >= 4)) { // WindRiver's sftp will send invalid SSH_FXP_STATUS packet.
			byte[] str = buf.getString();
			throw new SftpException(i, Util.byte2str(str, UTF8));
		} else {
			throw new SftpException(i, "Failure");
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
	}

	private boolean isPattern(String path, byte[][] utf8) {
		byte[] _path = Util.str2byte(path, UTF8);
		if (utf8 != null) {
			utf8[0] = _path;
		}
		return isPattern(_path);
	}

	private boolean isPattern(String path) {
		return isPattern(path, null);
	}

	private void fill(Buffer buf, int len) throws IOException {
		buf.reset();
		fill(buf.buffer, 0, len);
		buf.skip(len);
	}

	private int fill(byte[] buf, int s, int len) throws IOException {
		int i = 0;
		int foo = s;
		while (len > 0) {
			i = io_in.read(buf, s, len);
			if (i <= 0) {
				throw new IOException("inputstream is closed");
				// return (s-foo)==0 ? i : s-foo;
			}
			s += i;
			len -= i;
		}
		return s - foo;
	}

	static class Header {
		int length;
		int type;
		int rid;
	}

	private Header header(Buffer buf, Header header) throws IOException {
		buf.rewind();
		fill(buf.buffer, 0, 9);
		header.length = buf.getInt() - 5;
		header.type = buf.getByte() & 0xff;
		header.rid = buf.getInt();
		return header;
	}

	private String remoteAbsolutePath(String path) throws SftpException {
		if (path.charAt(0) == '/') {
			return path;
		}
		String cwd = getCwd();
		// if(cwd.equals(getHome())) return path;
		if (cwd.endsWith("/")) {
			return cwd + path;
		}
		return cwd + "/" + path;
	}

	/**
	 * This method will check if the given string can be expanded to the unique string. If it can be expanded to mutiple files, SftpException will be thrown.
	 *
	 * @return the returned string is unquoted.
	 */
	private String isUnique(String path) throws SftpException, Exception {
		Vector<String> v = glob_remote(path);
		if (v.size() != 1) {
			throw new SftpException(SSH_FX_FAILURE, path + " is not unique: " + v.toString());
		}
		return (v.elementAt(0));
	}

	public int getServerVersion() throws SftpException {
		if (!isConnected()) {
			throw new SftpException(SSH_FX_FAILURE, "The channel is not connected.");
		}
		return server_version;
	}

	public void setFilenameEncoding(String encoding) throws SftpException {
		int sversion = getServerVersion();
		if ((3 <= sversion) && (sversion <= 5) && !encoding.equals(UTF8)) {
			throw new SftpException(SSH_FX_FAILURE, "The encoding can not be changed for this sftp server.");
		}
		if (encoding.equals(UTF8)) {
			encoding = UTF8;
		}
		fEncoding = encoding;
		fEncoding_is_utf8 = fEncoding.equals(UTF8);
	}

	public String getExtension(String key) {
		if (extensions == null) {
			return null;
		}
		return extensions.get(key);
	}

	public String realpath(String path) throws SftpException {
		try {
			byte[] _path = _realpath(remoteAbsolutePath(path));
			return Util.byte2str(_path, fEncoding);
		} catch (Exception e) {
			if (e instanceof SftpException) {
				throw (SftpException) e;
			}
			if (e instanceof Throwable) {
				throw new SftpException(SSH_FX_FAILURE, "", e);
			}
			throw new SftpException(SSH_FX_FAILURE, "");
		}
	}

	public static class LsEntry implements Comparable<Object> {
		private String filename;
		private String longname;
		private SftpATTRS attrs;

		LsEntry(String filename, String longname, SftpATTRS attrs) {
			setFilename(filename);
			setLongname(longname);
			setAttrs(attrs);
		}

		public String getFilename() {
			return filename;
		};

		void setFilename(String filename) {
			this.filename = filename;
		};

		public String getLongname() {
			return longname;
		};

		void setLongname(String longname) {
			this.longname = longname;
		};

		public SftpATTRS getAttrs() {
			return attrs;
		};

		void setAttrs(SftpATTRS attrs) {
			this.attrs = attrs;
		};

		@Override
		public String toString() {
			return longname;
		}

		@Override
		public int compareTo(Object o) throws ClassCastException {
			if (o instanceof LsEntry) {
				return filename.compareTo(((LsEntry) o).getFilename());
			}
			throw new ClassCastException("a decendent of LsEntry must be given.");
		}
	}

	/**
	 * This interface will be passed as an argument for <code>ls</code> method.
	 *
	 * @see ChannelSftp.LsEntry
	 * @see #ls(String, ChannelSftp.LsEntrySelector)
	 * @since 0.1.47
	 */
	public interface LsEntrySelector {
		public final int CONTINUE = 0;
		public final int BREAK = 1;

		/**
		 * <p>
		 * The <code>select</code> method will be invoked in <code>ls</code> method for each file entry. If this method returns BREAK, <code>ls</code> will be canceled.
		 *
		 * @param entry
		 *            one of entry from ls
		 * @return if BREAK is returned, the 'ls' operation will be canceled.
		 */
		public int select(LsEntry entry);
	}

}
