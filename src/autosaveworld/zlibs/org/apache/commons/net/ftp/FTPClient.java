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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import autosaveworld.zlibs.org.apache.commons.net.MalformedServerReplyException;
import autosaveworld.zlibs.org.apache.commons.net.io.CRLFLineReader;
import autosaveworld.zlibs.org.apache.commons.net.io.Util;

public class FTPClient extends FTP {

	public static final int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0;

	public static final int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1;

	public static final int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2;

	public static final int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3;

	private int __dataConnectionMode;
	private int __dataTimeout;
	private int __passivePort;
	private String __passiveHost;
	private final Random __random;
	private int __activeMinPort;
	private int __activeMaxPort;
	private InetAddress __activeExternalHost;
	private InetAddress __reportActiveExternalHost;
	private InetAddress __passiveLocalHost;

	private boolean __remoteVerificationEnabled;
	private long __restartOffset;
	private int __bufferSize;
	private int __sendDataSocketBufferSize;
	private int __receiveDataSocketBufferSize;
	private boolean __listHiddenFiles;
	private boolean __useEPSVwithIPv4;

	private boolean __passiveNatWorkaround = true;

	private static final java.util.regex.Pattern __PARMS_PAT;
	static {
		__PARMS_PAT = java.util.regex.Pattern.compile("(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})");
	}

	private boolean __autodetectEncoding = false;

	private HashMap<String, Set<String>> __featuresMap;

	public FTPClient() {
		__initDefaults();
		__dataTimeout = -1;
		__remoteVerificationEnabled = true;
		__listHiddenFiles = false;
		__useEPSVwithIPv4 = false;
		__random = new Random();
		__passiveLocalHost = null;
	}

	private void __initDefaults() {
		__dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
		__passiveHost = null;
		__passivePort = -1;
		__activeExternalHost = null;
		__reportActiveExternalHost = null;
		__activeMinPort = 0;
		__activeMaxPort = 0;
		__restartOffset = 0;
		__featuresMap = null;
	}

	protected void _parsePassiveModeReply(String reply) throws MalformedServerReplyException {
		java.util.regex.Matcher m = __PARMS_PAT.matcher(reply);
		if (!m.find()) {
			throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
		}

		__passiveHost = m.group(1).replace(',', '.');

		try {
			int oct1 = Integer.parseInt(m.group(2));
			int oct2 = Integer.parseInt(m.group(3));
			__passivePort = (oct1 << 8) | oct2;
		} catch (NumberFormatException e) {
			throw new MalformedServerReplyException("Could not parse passive port information.\nServer Reply: " + reply);
		}

		if (__passiveNatWorkaround) {
			try {
				InetAddress host = InetAddress.getByName(__passiveHost);
				if (host.isSiteLocalAddress()) {
					InetAddress remote = getRemoteAddress();
					if (!remote.isSiteLocalAddress()) {
						String hostAddress = remote.getHostAddress();
						__passiveHost = hostAddress;
					}
				}
			} catch (UnknownHostException e) {
				throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
			}
		}
	}

	protected void _parseExtendedPassiveModeReply(String reply) throws MalformedServerReplyException {
		reply = reply.substring(reply.indexOf('(') + 1, reply.indexOf(')')).trim();

		char delim1, delim2, delim3, delim4;
		delim1 = reply.charAt(0);
		delim2 = reply.charAt(1);
		delim3 = reply.charAt(2);
		delim4 = reply.charAt(reply.length() - 1);

		if (!(delim1 == delim2) || !(delim2 == delim3) || !(delim3 == delim4)) {
			throw new MalformedServerReplyException("Could not parse extended passive host information.\nServer Reply: " + reply);
		}

		int port;
		try {
			port = Integer.parseInt(reply.substring(3, reply.length() - 1));
		} catch (NumberFormatException e) {
			throw new MalformedServerReplyException("Could not parse extended passive host information.\nServer Reply: " + reply);
		}

		__passiveHost = getRemoteAddress().getHostAddress();
		__passivePort = port;
	}

	private boolean __storeFile(FTPCmd command, String remote, InputStream local) throws IOException {
		return _storeFile(command.getCommand(), remote, local);
	}

	protected boolean _storeFile(String command, String remote, InputStream local) throws IOException {
		Socket socket = _openDataConnection_(command, remote);

		if (socket == null) {
			return false;
		}

		OutputStream output = getBufferedOutputStream(socket.getOutputStream());

		try {
			Util.copyStream(local, output);
		} catch (IOException e) {
			Util.closeQuietly(socket);
			throw e;
		}

		output.close();
		socket.close();
		boolean ok = completePendingCommand();
		return ok;
	}

	private OutputStream __storeFileStream(FTPCmd command, String remote) throws IOException {
		return _storeFileStream(command.getCommand(), remote);
	}

	protected OutputStream _storeFileStream(String command, String remote) throws IOException {
		Socket socket = _openDataConnection_(command, remote);

		if (socket == null) {
			return null;
		}

		OutputStream output = socket.getOutputStream();

		return new autosaveworld.zlibs.org.apache.commons.net.io.SocketOutputStream(socket, output);
	}

	protected Socket _openDataConnection_(FTPCmd command, String arg) throws IOException {
		return _openDataConnection_(command.getCommand(), arg);
	}

	protected Socket _openDataConnection_(String command, String arg) throws IOException {
		if ((__dataConnectionMode != ACTIVE_LOCAL_DATA_CONNECTION_MODE) && (__dataConnectionMode != PASSIVE_LOCAL_DATA_CONNECTION_MODE)) {
			return null;
		}

		final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;

		Socket socket;

		if (__dataConnectionMode == ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
			ServerSocket server = _serverSocketFactory_.createServerSocket(getActivePort(), 1, getHostAddress());

			try {
				if (isInet6Address) {
					if (!FTPReply.isPositiveCompletion(eprt(getReportHostAddress(), server.getLocalPort()))) {
						return null;
					}
				} else {
					if (!FTPReply.isPositiveCompletion(port(getReportHostAddress(), server.getLocalPort()))) {
						return null;
					}
				}

				if ((__restartOffset > 0) && !restart(__restartOffset)) {
					return null;
				}

				if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
					return null;
				}

				if (__dataTimeout >= 0) {
					server.setSoTimeout(__dataTimeout);
				}
				socket = server.accept();

				if (__dataTimeout >= 0) {
					socket.setSoTimeout(__dataTimeout);
				}
				if (__receiveDataSocketBufferSize > 0) {
					socket.setReceiveBufferSize(__receiveDataSocketBufferSize);
				}
				if (__sendDataSocketBufferSize > 0) {
					socket.setSendBufferSize(__sendDataSocketBufferSize);
				}
			} finally {
				server.close();
			}
		} else {
			boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
			if (attemptEPSV && (epsv() == FTPReply.ENTERING_EPSV_MODE)) {
				_parseExtendedPassiveModeReply(_replyLines.get(0));
			} else {
				if (isInet6Address) {
					return null;
				}
				if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
					return null;
				}
				_parsePassiveModeReply(_replyLines.get(0));
			}

			socket = _socketFactory_.createSocket();
			if (__receiveDataSocketBufferSize > 0) {
				socket.setReceiveBufferSize(__receiveDataSocketBufferSize);
			}
			if (__sendDataSocketBufferSize > 0) {
				socket.setSendBufferSize(__sendDataSocketBufferSize);
			}
			if (__passiveLocalHost != null) {
				socket.bind(new InetSocketAddress(__passiveLocalHost, 0));
			}

			if (__dataTimeout >= 0) {
				socket.setSoTimeout(__dataTimeout);
			}

			socket.connect(new InetSocketAddress(__passiveHost, __passivePort), connectTimeout);
			if ((__restartOffset > 0) && !restart(__restartOffset)) {
				socket.close();
				return null;
			}

			if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
				socket.close();
				return null;
			}
		}

		if (__remoteVerificationEnabled && !verifyRemote(socket)) {
			socket.close();

			throw new IOException("Host attempting data connection " + socket.getInetAddress().getHostAddress() + " is not same as server " + getRemoteAddress().getHostAddress());
		}

		return socket;
	}

	@Override
	protected void _connectAction_() throws IOException {
		super._connectAction_();
		__initDefaults();
		if (__autodetectEncoding) {
			ArrayList<String> oldReplyLines = new ArrayList<String>(_replyLines);
			int oldReplyCode = _replyCode;
			if (hasFeature("UTF8") || hasFeature("UTF-8")) {
				setControlEncoding("UTF-8");
				_controlInput_ = new CRLFLineReader(new InputStreamReader(_input_, getControlEncoding()));
				_controlOutput_ = new BufferedWriter(new OutputStreamWriter(_output_, getControlEncoding()));
			}
			_replyLines.clear();
			_replyLines.addAll(oldReplyLines);
			_replyCode = oldReplyCode;
		}
	}

	public void setDataTimeout(int timeout) {
		__dataTimeout = timeout;
	}

	@Override
	public void disconnect() throws IOException {
		super.disconnect();
		__initDefaults();
	}

	public void setRemoteVerificationEnabled(boolean enable) {
		__remoteVerificationEnabled = enable;
	}

	public boolean isRemoteVerificationEnabled() {
		return __remoteVerificationEnabled;
	}

	public boolean login(String username, String password) throws IOException {

		user(username);

		if (FTPReply.isPositiveCompletion(_replyCode)) {
			if (setFileType(FTP.BINARY_FILE_TYPE)) {
				return true;
			}
		}

		if (!FTPReply.isPositiveIntermediate(_replyCode)) {
			return false;
		}

		if (!FTPReply.isPositiveCompletion(pass(password))) {
			return false;
		}

		return setFileType(FTP.BINARY_FILE_TYPE);
	}

	public boolean login(String username, String password, String account) throws IOException {
		user(username);

		if (FTPReply.isPositiveCompletion(_replyCode)) {
			return true;
		}

		if (!FTPReply.isPositiveIntermediate(_replyCode)) {
			return false;
		}

		pass(password);

		if (FTPReply.isPositiveCompletion(_replyCode)) {
			return true;
		}

		if (!FTPReply.isPositiveIntermediate(_replyCode)) {
			return false;
		}

		return FTPReply.isPositiveCompletion(acct(account));
	}

	public boolean logout() throws IOException {
		return FTPReply.isPositiveCompletion(quit());
	}

	public boolean changeWorkingDirectory(String pathname) throws IOException {
		return FTPReply.isPositiveCompletion(cwd(pathname));
	}

	public boolean changeToParentDirectory() throws IOException {
		return FTPReply.isPositiveCompletion(cdup());
	}

	public boolean structureMount(String pathname) throws IOException {
		return FTPReply.isPositiveCompletion(smnt(pathname));
	}

	boolean reinitialize() throws IOException {
		rein();

		if (FTPReply.isPositiveCompletion(_replyCode) || (FTPReply.isPositivePreliminary(_replyCode) && FTPReply.isPositiveCompletion(getReply()))) {

			__initDefaults();

			return true;
		}

		return false;
	}

	public void enterLocalActiveMode() {
		__dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
		__passiveHost = null;
		__passivePort = -1;
	}

	public void enterLocalPassiveMode() {
		__dataConnectionMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
		__passiveHost = null;
		__passivePort = -1;
	}

	public boolean enterRemoteActiveMode(InetAddress host, int port) throws IOException {
		if (FTPReply.isPositiveCompletion(port(host, port))) {
			__dataConnectionMode = ACTIVE_REMOTE_DATA_CONNECTION_MODE;
			__passiveHost = null;
			__passivePort = -1;
			return true;
		}
		return false;
	}

	public boolean enterRemotePassiveMode() throws IOException {
		if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
			return false;
		}

		__dataConnectionMode = PASSIVE_REMOTE_DATA_CONNECTION_MODE;
		_parsePassiveModeReply(_replyLines.get(0));

		return true;
	}

	public String getPassiveHost() {
		return __passiveHost;
	}

	public int getPassivePort() {
		return __passivePort;
	}

	public int getDataConnectionMode() {
		return __dataConnectionMode;
	}

	private int getActivePort() {
		if ((__activeMinPort > 0) && (__activeMaxPort >= __activeMinPort)) {
			if (__activeMaxPort == __activeMinPort) {
				return __activeMaxPort;
			}
			return __random.nextInt((__activeMaxPort - __activeMinPort) + 1) + __activeMinPort;
		} else {
			return 0;
		}
	}

	private InetAddress getHostAddress() {
		if (__activeExternalHost != null) {
			return __activeExternalHost;
		} else {
			// default local address
			return getLocalAddress();
		}
	}

	private InetAddress getReportHostAddress() {
		if (__reportActiveExternalHost != null) {
			return __reportActiveExternalHost;
		} else {
			return getHostAddress();
		}
	}

	public void setActivePortRange(int minPort, int maxPort) {
		__activeMinPort = minPort;
		__activeMaxPort = maxPort;
	}

	public void setActiveExternalIPAddress(String ipAddress) throws UnknownHostException {
		__activeExternalHost = InetAddress.getByName(ipAddress);
	}

	public void setPassiveLocalIPAddress(String ipAddress) throws UnknownHostException {
		__passiveLocalHost = InetAddress.getByName(ipAddress);
	}

	public void setPassiveLocalIPAddress(InetAddress inetAddress) {
		__passiveLocalHost = inetAddress;
	}

	public InetAddress getPassiveLocalIPAddress() {
		return __passiveLocalHost;
	}

	public void setReportActiveExternalIPAddress(String ipAddress) throws UnknownHostException {
		__reportActiveExternalHost = InetAddress.getByName(ipAddress);
	}

	public boolean setFileType(int fileType) throws IOException {
		if (FTPReply.isPositiveCompletion(type(fileType))) {
			return true;
		}
		return false;
	}

	public boolean setFileType(int fileType, int formatOrByteSize) throws IOException {
		if (FTPReply.isPositiveCompletion(type(fileType, formatOrByteSize))) {
			return true;
		}
		return false;
	}

	public boolean setFileStructure(int structure) throws IOException {
		if (FTPReply.isPositiveCompletion(stru(structure))) {
			return true;
		}
		return false;
	}

	public boolean setFileTransferMode(int mode) throws IOException {
		if (FTPReply.isPositiveCompletion(mode(mode))) {
			return true;
		}
		return false;
	}

	public boolean remoteRetrieve(String filename) throws IOException {
		if ((__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE) || (__dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)) {
			return FTPReply.isPositivePreliminary(retr(filename));
		}
		return false;
	}

	public boolean remoteStore(String filename) throws IOException {
		if ((__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE) || (__dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)) {
			return FTPReply.isPositivePreliminary(stor(filename));
		}
		return false;
	}

	public boolean remoteStoreUnique(String filename) throws IOException {
		if ((__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE) || (__dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)) {
			return FTPReply.isPositivePreliminary(stou(filename));
		}
		return false;
	}

	public boolean remoteStoreUnique() throws IOException {
		if ((__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE) || (__dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)) {
			return FTPReply.isPositivePreliminary(stou());
		}
		return false;
	}

	public boolean remoteAppend(String filename) throws IOException {
		if ((__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE) || (__dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)) {
			return FTPReply.isPositivePreliminary(appe(filename));
		}
		return false;
	}

	public boolean completePendingCommand() throws IOException {
		return FTPReply.isPositiveCompletion(getReply());
	}

	public boolean storeFile(String remote, InputStream local) throws IOException {
		return __storeFile(FTPCmd.STOR, remote, local);
	}

	public OutputStream storeFileStream(String remote) throws IOException {
		return __storeFileStream(FTPCmd.STOR, remote);
	}

	public boolean appendFile(String remote, InputStream local) throws IOException {
		return __storeFile(FTPCmd.APPE, remote, local);
	}

	public OutputStream appendFileStream(String remote) throws IOException {
		return __storeFileStream(FTPCmd.APPE, remote);
	}

	public boolean storeUniqueFile(String remote, InputStream local) throws IOException {
		return __storeFile(FTPCmd.STOU, remote, local);
	}

	public OutputStream storeUniqueFileStream(String remote) throws IOException {
		return __storeFileStream(FTPCmd.STOU, remote);
	}

	public OutputStream storeUniqueFileStream() throws IOException {
		return __storeFileStream(FTPCmd.STOU, null);
	}

	public boolean allocate(int bytes) throws IOException {
		return FTPReply.isPositiveCompletion(allo(bytes));
	}

	public boolean features() throws IOException {
		return FTPReply.isPositiveCompletion(feat());
	}

	public String[] featureValues(String feature) throws IOException {
		if (!initFeatureMap()) {
			return null;
		}
		Set<String> entries = __featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
		if (entries != null) {
			return entries.toArray(new String[entries.size()]);
		}
		return null;
	}

	public String featureValue(String feature) throws IOException {
		String[] values = featureValues(feature);
		if (values != null) {
			return values[0];
		}
		return null;
	}

	public boolean hasFeature(String feature) throws IOException {
		if (!initFeatureMap()) {
			return false;
		}
		return __featuresMap.containsKey(feature.toUpperCase(Locale.ENGLISH));
	}

	public boolean hasFeature(String feature, String value) throws IOException {
		if (!initFeatureMap()) {
			return false;
		}
		Set<String> entries = __featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
		if (entries != null) {
			return entries.contains(value);
		}
		return false;
	}

	private boolean initFeatureMap() throws IOException {
		if (__featuresMap == null) {
			boolean success = FTPReply.isPositiveCompletion(feat());
			__featuresMap = new HashMap<String, Set<String>>();
			if (!success) {
				return false;
			}
			for (String l : getReplyStrings()) {
				if (l.startsWith(" ")) {
					String key;
					String value = "";
					int varsep = l.indexOf(' ', 1);
					if (varsep > 0) {
						key = l.substring(1, varsep);
						value = l.substring(varsep + 1);
					} else {
						key = l.substring(1);
					}
					key = key.toUpperCase(Locale.ENGLISH);
					Set<String> entries = __featuresMap.get(key);
					if (entries == null) {
						entries = new HashSet<String>();
						__featuresMap.put(key, entries);
					}
					entries.add(value);
				}
			}
		}
		return true;
	}

	public boolean allocate(int bytes, int recordSize) throws IOException {
		return FTPReply.isPositiveCompletion(allo(bytes, recordSize));
	}

	public boolean doCommand(String command, String params) throws IOException {
		return FTPReply.isPositiveCompletion(sendCommand(command, params));
	}

	public String[] doCommandAsStrings(String command, String params) throws IOException {
		boolean success = FTPReply.isPositiveCompletion(sendCommand(command, params));
		if (success) {
			return getReplyStrings();
		} else {
			return null;
		}
	}

	protected boolean restart(long offset) throws IOException {
		__restartOffset = 0;
		return FTPReply.isPositiveIntermediate(rest(Long.toString(offset)));
	}

	public void setRestartOffset(long offset) {
		if (offset >= 0) {
			__restartOffset = offset;
		}
	}

	public long getRestartOffset() {
		return __restartOffset;
	}

	public boolean rename(String from, String to) throws IOException {
		if (!FTPReply.isPositiveIntermediate(rnfr(from))) {
			return false;
		}

		return FTPReply.isPositiveCompletion(rnto(to));
	}

	public boolean abort() throws IOException {
		return FTPReply.isPositiveCompletion(abor());
	}

	public boolean deleteFile(String pathname) throws IOException {
		return FTPReply.isPositiveCompletion(dele(pathname));
	}

	public boolean removeDirectory(String pathname) throws IOException {
		return FTPReply.isPositiveCompletion(rmd(pathname));
	}

	public boolean makeDirectory(String pathname) throws IOException {
		return FTPReply.isPositiveCompletion(mkd(pathname));
	}

	public boolean sendSiteCommand(String arguments) throws IOException {
		return FTPReply.isPositiveCompletion(site(arguments));
	}

	public String listHelp() throws IOException {
		if (FTPReply.isPositiveCompletion(help())) {
			return getReplyString();
		}
		return null;
	}

	public String listHelp(String command) throws IOException {
		if (FTPReply.isPositiveCompletion(help(command))) {
			return getReplyString();
		}
		return null;
	}

	public boolean sendNoOp() throws IOException {
		return FTPReply.isPositiveCompletion(noop());
	}

	public String[] listNames(String pathname) throws IOException {
		Socket socket = _openDataConnection_(FTPCmd.NLST, getListArguments(pathname));

		if (socket == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), getControlEncoding()));

		ArrayList<String> results = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			results.add(line);
		}

		reader.close();
		socket.close();

		if (completePendingCommand()) {
			String[] names = new String[results.size()];
			return results.toArray(names);
		}

		return null;
	}

	public String[] listNames() throws IOException {
		return listNames(null);
	}

	protected String getListArguments(String pathname) {
		if (getListHiddenFiles()) {
			if (pathname != null) {
				StringBuilder sb = new StringBuilder(pathname.length() + 3);
				sb.append("-a ");
				sb.append(pathname);
				return sb.toString();
			} else {
				return "-a";
			}
		}

		return pathname;
	}

	public String getStatus() throws IOException {
		if (FTPReply.isPositiveCompletion(stat())) {
			return getReplyString();
		}
		return null;
	}

	public String getStatus(String pathname) throws IOException {
		if (FTPReply.isPositiveCompletion(stat(pathname))) {
			return getReplyString();
		}
		return null;
	}

	public String getModificationTime(String pathname) throws IOException {
		if (FTPReply.isPositiveCompletion(mdtm(pathname))) {
			return getReplyString();
		}
		return null;
	}

	public boolean setModificationTime(String pathname, String timeval) throws IOException {
		return (FTPReply.isPositiveCompletion(mfmt(pathname, timeval)));
	}

	public void setBufferSize(int bufSize) {
		__bufferSize = bufSize;
	}

	public int getBufferSize() {
		return __bufferSize;
	}

	public void setSendDataSocketBufferSize(int bufSize) {
		__sendDataSocketBufferSize = bufSize;
	}

	public int getSendDataSocketBufferSize() {
		return __sendDataSocketBufferSize;
	}

	public void setReceieveDataSocketBufferSize(int bufSize) {
		__receiveDataSocketBufferSize = bufSize;
	}

	public int getReceiveDataSocketBufferSize() {
		return __receiveDataSocketBufferSize;
	}

	public void setListHiddenFiles(boolean listHiddenFiles) {
		__listHiddenFiles = listHiddenFiles;
	}

	public boolean getListHiddenFiles() {
		return __listHiddenFiles;
	}

	public boolean isUseEPSVwithIPv4() {
		return __useEPSVwithIPv4;
	}

	public void setUseEPSVwithIPv4(boolean selected) {
		__useEPSVwithIPv4 = selected;
	}

	public void setPassiveNatWorkaround(boolean enabled) {
		__passiveNatWorkaround = enabled;
	}

	private OutputStream getBufferedOutputStream(OutputStream outputStream) {
		if (__bufferSize > 0) {
			return new BufferedOutputStream(outputStream, __bufferSize);
		}
		return new BufferedOutputStream(outputStream);
	}

	public void setAutodetectUTF8(boolean autodetect) {
		__autodetectEncoding = autodetect;
	}

	public boolean getAutodetectUTF8() {
		return __autodetectEncoding;
	}

}