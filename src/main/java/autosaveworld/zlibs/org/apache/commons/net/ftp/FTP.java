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

package autosaveworld.zlibs.org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import autosaveworld.zlibs.org.apache.commons.net.MalformedServerReplyException;
import autosaveworld.zlibs.org.apache.commons.net.SocketClient;
import autosaveworld.zlibs.org.apache.commons.net.io.CRLFLineReader;

public class FTP extends SocketClient {

	public static final int DEFAULT_DATA_PORT = 20;

	public static final int DEFAULT_PORT = 21;

	public static final int EBCDIC_FILE_TYPE = 1;

	public static final int BINARY_FILE_TYPE = 2;

	public static final int LOCAL_FILE_TYPE = 3;

	public static final int NON_PRINT_TEXT_FORMAT = 4;

	public static final int TELNET_TEXT_FORMAT = 5;

	public static final int CARRIAGE_CONTROL_TEXT_FORMAT = 6;

	public static final int FILE_STRUCTURE = 7;

	public static final int RECORD_STRUCTURE = 8;

	public static final int PAGE_STRUCTURE = 9;

	public static final int STREAM_TRANSFER_MODE = 10;

	public static final int BLOCK_TRANSFER_MODE = 11;

	public static final int COMPRESSED_TRANSFER_MODE = 12;

	public static final String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

	public static final int REPLY_CODE_LEN = 3;

	private static final String __modes = "AEILNTCFRPSBC";

	protected int _replyCode;
	protected ArrayList<String> _replyLines;
	protected boolean _newReplyString;
	protected String _replyString;
	protected String _controlEncoding;

	protected boolean strictMultilineParsing = false;

	protected BufferedReader _controlInput_;

	protected BufferedWriter _controlOutput_;

	public FTP() {
		super();
		setDefaultPort(DEFAULT_PORT);
		_replyLines = new ArrayList<String>();
		_newReplyString = false;
		_replyString = null;
		_controlEncoding = DEFAULT_CONTROL_ENCODING;
	}

	private boolean __strictCheck(String line, String code) {
		return (!(line.startsWith(code) && (line.charAt(REPLY_CODE_LEN) == ' ')));
	}

	private boolean __lenientCheck(String line) {
		return (!((line.length() > REPLY_CODE_LEN) && (line.charAt(REPLY_CODE_LEN) != '-') && Character.isDigit(line.charAt(0))));
	}

	private void __getReply() throws IOException {
		__getReply(true);
	}

	protected void __getReplyNoReport() throws IOException {
		__getReply(false);
	}

	private void __getReply(boolean reportReply) throws IOException {
		int length;

		_newReplyString = true;
		_replyLines.clear();

		String line = _controlInput_.readLine();

		if (line == null) {
			throw new FTPConnectionClosedException("Connection closed without indication.");
		}

		length = line.length();
		if (length < REPLY_CODE_LEN) {
			throw new MalformedServerReplyException("Truncated server reply: " + line);
		}

		String code = null;
		try {
			code = line.substring(0, REPLY_CODE_LEN);
			_replyCode = Integer.parseInt(code);
		} catch (NumberFormatException e) {
			throw new MalformedServerReplyException("Could not parse response code.\nServer Reply: " + line);
		}

		_replyLines.add(line);

		if ((length > REPLY_CODE_LEN) && (line.charAt(REPLY_CODE_LEN) == '-')) {
			do {
				line = _controlInput_.readLine();

				if (line == null) {
					throw new FTPConnectionClosedException("Connection closed without indication.");
				}

				_replyLines.add(line);

			} while (isStrictMultilineParsing() ? __strictCheck(line, code) : __lenientCheck(line));
		}

		if (_replyCode == FTPReply.SERVICE_NOT_AVAILABLE) {
			throw new FTPConnectionClosedException("FTP response 421 received.  Server closed connection.");
		}
	}

	@Override
	protected void _connectAction_() throws IOException {
		super._connectAction_();
		_controlInput_ = new CRLFLineReader(new InputStreamReader(_input_, getControlEncoding()));
		_controlOutput_ = new BufferedWriter(new OutputStreamWriter(_output_, getControlEncoding()));
		if (connectTimeout > 0) {
			int original = _socket_.getSoTimeout();
			_socket_.setSoTimeout(connectTimeout);
			try {
				__getReply();
				if (FTPReply.isPositivePreliminary(_replyCode)) {
					__getReply();
				}
			} catch (SocketTimeoutException e) {
				IOException ioe = new IOException("Timed out waiting for initial connect reply");
				ioe.initCause(e);
				throw ioe;
			} finally {
				_socket_.setSoTimeout(original);
			}
		} else {
			__getReply();
			if (FTPReply.isPositivePreliminary(_replyCode)) {
				__getReply();
			}
		}
	}

	public void setControlEncoding(String encoding) {
		_controlEncoding = encoding;
	}

	public String getControlEncoding() {
		return _controlEncoding;
	}

	@Override
	public void disconnect() throws IOException {
		super.disconnect();
		_controlInput_ = null;
		_controlOutput_ = null;
		_newReplyString = false;
		_replyString = null;
	}

	public int sendCommand(String command, String args) throws IOException {
		if (_controlOutput_ == null) {
			throw new IOException("Connection is not open");
		}

		final String message = __buildMessage(command, args);

		__send(message);

		__getReply();
		return _replyCode;
	}

	private String __buildMessage(String command, String args) {
		final StringBuilder __commandBuffer = new StringBuilder();

		__commandBuffer.append(command);

		if (args != null) {
			__commandBuffer.append(' ');
			__commandBuffer.append(args);
		}
		__commandBuffer.append(SocketClient.NETASCII_EOL);
		return __commandBuffer.toString();
	}

	private void __send(String message) throws IOException, FTPConnectionClosedException, SocketException {
		try {
			_controlOutput_.write(message);
			_controlOutput_.flush();
		} catch (SocketException e) {
			if (!isConnected()) {
				throw new FTPConnectionClosedException("Connection unexpectedly closed.");
			} else {
				throw e;
			}
		}
	}

	protected void __noop() throws IOException {
		String msg = __buildMessage(FTPCmd.NOOP.getCommand(), null);
		__send(msg);
		__getReplyNoReport(); // This may timeout
	}

	public int sendCommand(FTPCmd command) throws IOException {
		return sendCommand(command, null);
	}

	public int sendCommand(FTPCmd command, String args) throws IOException {
		return sendCommand(command.getCommand(), args);
	}

	public int sendCommand(String command) throws IOException {
		return sendCommand(command, null);
	}

	public int getReplyCode() {
		return _replyCode;
	}

	public int getReply() throws IOException {
		__getReply();
		return _replyCode;
	}

	public String[] getReplyStrings() {
		return _replyLines.toArray(new String[_replyLines.size()]);
	}

	public String getReplyString() {
		StringBuilder buffer;

		if (!_newReplyString) {
			return _replyString;
		}

		buffer = new StringBuilder(256);

		for (String line : _replyLines) {
			buffer.append(line);
			buffer.append(SocketClient.NETASCII_EOL);
		}

		_newReplyString = false;

		return (_replyString = buffer.toString());
	}

	public int user(String username) throws IOException {
		return sendCommand(FTPCmd.USER, username);
	}

	public int pass(String password) throws IOException {
		return sendCommand(FTPCmd.PASS, password);
	}

	public int acct(String account) throws IOException {
		return sendCommand(FTPCmd.ACCT, account);
	}

	public int abor() throws IOException {
		return sendCommand(FTPCmd.ABOR);
	}

	public int cwd(String directory) throws IOException {
		return sendCommand(FTPCmd.CWD, directory);
	}

	public int cdup() throws IOException {
		return sendCommand(FTPCmd.CDUP);
	}

	public int quit() throws IOException {
		return sendCommand(FTPCmd.QUIT);
	}

	public int rein() throws IOException {
		return sendCommand(FTPCmd.REIN);
	}

	public int smnt(String dir) throws IOException {
		return sendCommand(FTPCmd.SMNT, dir);
	}

	public int port(InetAddress host, int port) throws IOException {
		int num;
		StringBuilder info = new StringBuilder(24);

		info.append(host.getHostAddress().replace('.', ','));
		num = port >>> 8;
		info.append(',');
		info.append(num);
		info.append(',');
		num = port & 0xff;
		info.append(num);

		return sendCommand(FTPCmd.PORT, info.toString());
	}

	public int eprt(InetAddress host, int port) throws IOException {
		int num;
		StringBuilder info = new StringBuilder();
		String h;

		// If IPv6, trim the zone index
		h = host.getHostAddress();
		num = h.indexOf("%");
		if (num > 0) {
			h = h.substring(0, num);
		}

		info.append("|");

		if (host instanceof Inet4Address) {
			info.append("1");
		} else if (host instanceof Inet6Address) {
			info.append("2");
		}
		info.append("|");
		info.append(h);
		info.append("|");
		info.append(port);
		info.append("|");

		return sendCommand(FTPCmd.EPRT, info.toString());
	}

	public int pasv() throws IOException {
		return sendCommand(FTPCmd.PASV);
	}

	public int epsv() throws IOException {
		return sendCommand(FTPCmd.EPSV);
	}

	public int type(int fileType, int formatOrByteSize) throws IOException {
		StringBuilder arg = new StringBuilder();

		arg.append(__modes.charAt(fileType));
		arg.append(' ');
		if (fileType == LOCAL_FILE_TYPE) {
			arg.append(formatOrByteSize);
		} else {
			arg.append(__modes.charAt(formatOrByteSize));
		}

		return sendCommand(FTPCmd.TYPE, arg.toString());
	}

	public int type(int fileType) throws IOException {
		return sendCommand(FTPCmd.TYPE, __modes.substring(fileType, fileType + 1));
	}

	public int stru(int structure) throws IOException {
		return sendCommand(FTPCmd.STRU, __modes.substring(structure, structure + 1));
	}

	public int mode(int mode) throws IOException {
		return sendCommand(FTPCmd.MODE, __modes.substring(mode, mode + 1));
	}

	public int retr(String pathname) throws IOException {
		return sendCommand(FTPCmd.RETR, pathname);
	}

	public int stor(String pathname) throws IOException {
		return sendCommand(FTPCmd.STOR, pathname);
	}

	public int stou() throws IOException {
		return sendCommand(FTPCmd.STOU);
	}

	public int stou(String pathname) throws IOException {
		return sendCommand(FTPCmd.STOU, pathname);
	}

	public int appe(String pathname) throws IOException {
		return sendCommand(FTPCmd.APPE, pathname);
	}

	public int allo(int bytes) throws IOException {
		return sendCommand(FTPCmd.ALLO, Integer.toString(bytes));
	}

	public int feat() throws IOException {
		return sendCommand(FTPCmd.FEAT);
	}

	public int allo(int bytes, int recordSize) throws IOException {
		return sendCommand(FTPCmd.ALLO, Integer.toString(bytes) + " R " + Integer.toString(recordSize));
	}

	public int rest(String marker) throws IOException {
		return sendCommand(FTPCmd.REST, marker);
	}

	public int mdtm(String file) throws IOException {
		return sendCommand(FTPCmd.MDTM, file);
	}

	public int mfmt(String pathname, String timeval) throws IOException {
		return sendCommand(FTPCmd.MFMT, timeval + " " + pathname);
	}

	public int rnfr(String pathname) throws IOException {
		return sendCommand(FTPCmd.RNFR, pathname);
	}

	public int rnto(String pathname) throws IOException {
		return sendCommand(FTPCmd.RNTO, pathname);
	}

	public int dele(String pathname) throws IOException {
		return sendCommand(FTPCmd.DELE, pathname);
	}

	public int rmd(String pathname) throws IOException {
		return sendCommand(FTPCmd.RMD, pathname);
	}

	public int mkd(String pathname) throws IOException {
		return sendCommand(FTPCmd.MKD, pathname);
	}

	public int pwd() throws IOException {
		return sendCommand(FTPCmd.PWD);
	}

	public int list() throws IOException {
		return sendCommand(FTPCmd.LIST);
	}

	public int list(String pathname) throws IOException {
		return sendCommand(FTPCmd.LIST, pathname);
	}

	public int mlsd() throws IOException {
		return sendCommand(FTPCmd.MLSD);
	}

	public int mlsd(String path) throws IOException {
		return sendCommand(FTPCmd.MLSD, path);
	}

	public int mlst() throws IOException {
		return sendCommand(FTPCmd.MLST);
	}

	public int mlst(String path) throws IOException {
		return sendCommand(FTPCmd.MLST, path);
	}

	public int nlst() throws IOException {
		return sendCommand(FTPCmd.NLST);
	}

	public int nlst(String pathname) throws IOException {
		return sendCommand(FTPCmd.NLST, pathname);
	}

	public int site(String parameters) throws IOException {
		return sendCommand(FTPCmd.SITE, parameters);
	}

	public int syst() throws IOException {
		return sendCommand(FTPCmd.SYST);
	}

	public int stat() throws IOException {
		return sendCommand(FTPCmd.STAT);
	}

	public int stat(String pathname) throws IOException {
		return sendCommand(FTPCmd.STAT, pathname);
	}

	public int help() throws IOException {
		return sendCommand(FTPCmd.HELP);
	}

	public int help(String command) throws IOException {
		return sendCommand(FTPCmd.HELP, command);
	}

	public int noop() throws IOException {
		return sendCommand(FTPCmd.NOOP);
	}

	public boolean isStrictMultilineParsing() {
		return strictMultilineParsing;
	}

	public void setStrictMultilineParsing(boolean strictMultilineParsing) {
		this.strictMultilineParsing = strictMultilineParsing;
	}

}