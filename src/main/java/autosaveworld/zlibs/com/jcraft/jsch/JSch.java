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

public class JSch {
	/**
	 * The version number.
	 */
	public static final String VERSION = "0.1.51";

	static java.util.Hashtable<String, String> config = new java.util.Hashtable<String, String>();
	static {
		config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1");
		config.put("server_host_key", "ssh-rsa,ssh-dss");

		config.put("cipher.s2c", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-cbc,aes256-cbc");
		config.put("cipher.c2s", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-cbc,aes256-cbc");

		config.put("mac.s2c", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
		config.put("mac.c2s", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
		config.put("compression.s2c", "none");
		config.put("compression.c2s", "none");

		config.put("lang.s2c", "");
		config.put("lang.c2s", "");

		config.put("compression_level", "6");

		config.put("diffie-hellman-group-exchange-sha1", DHGEX.class.getName());
		config.put("diffie-hellman-group1-sha1", DHG1.class.getName());
		config.put("diffie-hellman-group14-sha1", DHG14.class.getName());
		config.put("diffie-hellman-group-exchange-sha256", DHGEX256.class.getName());

		config.put("dh", autosaveworld.zlibs.com.jcraft.jsch.jce.DH.class.getName());
		config.put("3des-cbc", autosaveworld.zlibs.com.jcraft.jsch.jce.TripleDESCBC.class.getName());
		config.put("blowfish-cbc", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.BlowfishCBC.class.getName());
		config.put("hmac-sha1", autosaveworld.zlibs.com.jcraft.jsch.jce.mac.HMACSHA1.class.getName());
		config.put("hmac-sha1-96", autosaveworld.zlibs.com.jcraft.jsch.jce.mac.HMACSHA196.class.getName());
		config.put("hmac-sha2-256", autosaveworld.zlibs.com.jcraft.jsch.jce.mac.HMACSHA256.class.getName());

		config.put("hmac-md5", autosaveworld.zlibs.com.jcraft.jsch.jce.mac.HMACMD5.class.getName());
		config.put("hmac-md5-96", autosaveworld.zlibs.com.jcraft.jsch.jce.mac.HMACMD596.class.getName());
		config.put("sha-1", autosaveworld.zlibs.com.jcraft.jsch.jce.hash.SHA1.class.getName());
		config.put("sha-256", autosaveworld.zlibs.com.jcraft.jsch.jce.hash.SHA256.class.getName());
		config.put("md5", autosaveworld.zlibs.com.jcraft.jsch.jce.hash.MD5.class.getName());
		config.put("signature.dss", autosaveworld.zlibs.com.jcraft.jsch.jce.SignatureDSA.class.getName());
		config.put("signature.rsa", autosaveworld.zlibs.com.jcraft.jsch.jce.SignatureRSA.class.getName());
		config.put("random", autosaveworld.zlibs.com.jcraft.jsch.jce.Random.class.getName());

		config.put("none", autosaveworld.zlibs.com.jcraft.jsch.CipherNone.class.getName());

		config.put("aes128-cbc", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES128CBC.class.getName());
		config.put("aes192-cbc", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES192CBC.class.getName());
		config.put("aes256-cbc", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES256CBC.class.getName());

		config.put("aes128-ctr", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES128CTR.class.getName());
		config.put("aes192-ctr", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES192CTR.class.getName());
		config.put("aes256-ctr", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.AES256CTR.class.getName());
		config.put("3des-ctr", autosaveworld.zlibs.com.jcraft.jsch.jce.TripleDESCTR.class.getName());
		config.put("arcfour", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.ARCFOUR.class.getName());
		config.put("arcfour128", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.ARCFOUR128.class.getName());
		config.put("arcfour256", autosaveworld.zlibs.com.jcraft.jsch.jce.cipher.ARCFOUR256.class.getName());

		config.put("userauth.none", autosaveworld.zlibs.com.jcraft.jsch.UserAuthNone.class.getName());
		config.put("userauth.password", autosaveworld.zlibs.com.jcraft.jsch.UserAuthPassword.class.getName());

		config.put("pbkdf", autosaveworld.zlibs.com.jcraft.jsch.jce.PBKDF.class.getName());

		config.put("StrictHostKeyChecking", "no");
		config.put("HashKnownHosts", "no");

		config.put("PreferredAuthentications", "password");

		config.put("CheckCiphers", "aes256-ctr,aes192-ctr,aes128-ctr,aes256-cbc,aes192-cbc,aes128-cbc,3des-ctr,arcfour,arcfour128,arcfour256");
		config.put("CheckKexes", "diffie-hellman-group14-sha1");

		config.put("MaxAuthTries", "6");
		config.put("ClearAllForwardings", "no");
	}

	private java.util.Vector<Session> sessionPool = new java.util.Vector<Session>();

	private static final Logger DEVNULL = new Logger() {
		@Override
		public boolean isEnabled(int level) {
			return false;
		}

		@Override
		public void log(int level, String message) {
		}
	};
	static Logger logger = DEVNULL;

	/**
	 * Instantiates the <code>Session</code> object with <code>host</code>. The user name and port number will be retrieved from ConfigRepository. If user name is not given, the system property "user.name" will be referred.
	 *
	 * @param host
	 *            hostname
	 *
	 * @throws JSchException
	 *             if <code>username</code> or <code>host</code> are invalid.
	 *
	 * @return the instance of <code>Session</code> class.
	 *
	 * @see #getSession(String username, String host, int port)
	 * @see autosaveworld.zlibs.com.jcraft.jsch.Session
	 * @see autosaveworld.zlibs.com.jcraft.jsch.ConfigRepository
	 */
	public Session getSession(String host) throws JSchException {
		return getSession(null, host, 22);
	}

	/**
	 * Instantiates the <code>Session</code> object with <code>username</code> and <code>host</code>. The TCP port 22 will be used in making the connection. Note that the TCP connection must not be established until Session#connect().
	 *
	 * @param username
	 *            user name
	 * @param host
	 *            hostname
	 *
	 * @throws JSchException
	 *             if <code>username</code> or <code>host</code> are invalid.
	 *
	 * @return the instance of <code>Session</code> class.
	 *
	 * @see #getSession(String username, String host, int port)
	 * @see autosaveworld.zlibs.com.jcraft.jsch.Session
	 */
	public Session getSession(String username, String host) throws JSchException {
		return getSession(username, host, 22);
	}

	/**
	 * Instantiates the <code>Session</code> object with given <code>username</code>, <code>host</code> and <code>port</code>. Note that the TCP connection must not be established until Session#connect().
	 *
	 * @param username
	 *            user name
	 * @param host
	 *            hostname
	 * @param port
	 *            port number
	 *
	 * @throws JSchException
	 *             if <code>username</code> or <code>host</code> are invalid.
	 *
	 * @return the instance of <code>Session</code> class.
	 *
	 * @see #getSession(String username, String host, int port)
	 * @see autosaveworld.zlibs.com.jcraft.jsch.Session
	 */
	public Session getSession(String username, String host, int port) throws JSchException {
		if (host == null) {
			throw new JSchException("host must not be null.");
		}
		Session s = new Session(this, username, host, port);
		return s;
	}

	protected void addSession(Session session) {
		synchronized (sessionPool) {
			sessionPool.addElement(session);
		}
	}

	protected boolean removeSession(Session session) {
		synchronized (sessionPool) {
			return sessionPool.remove(session);
		}
	}

	/**
	 * Returns the config value for the specified key.
	 *
	 * @param key
	 *            key for the configuration.
	 * @return config value
	 */
	public static String getConfig(String key) {
		synchronized (config) {
			return (config.get(key));
		}
	}

	/**
	 * Sets or Overrides the configuration.
	 *
	 * @param key
	 *            key for the configuration
	 * @param value
	 *            value for the configuration
	 */
	public static void setConfig(String key, String value) {
		config.put(key, value);
	}

	/**
	 * Sets the logger
	 *
	 * @param logger
	 *            logger
	 *
	 * @see autosaveworld.zlibs.com.jcraft.jsch.Logger
	 */
	public static void setLogger(Logger logger) {
		if (logger == null) {
			logger = DEVNULL;
		}
		JSch.logger = logger;
	}

	static Logger getLogger() {
		return logger;
	}

}
