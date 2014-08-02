package autosaveworld.zlibs.com.dropbox.core.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class PemLoader {

	private CertificateFactory certFactory;
	private KeyStore keyStore;

	public PemLoader() throws InitException {
		this.certFactory = createX509CertificateFactory();
		this.keyStore = createEmptyKeyStore();
	}

	public KeyStore load(String[][] certs) throws IOException, LoadException {
		CertificateFactory certFactory;
		KeyStore keyStore;

		synchronized (this) {
			certFactory = this.certFactory;
			keyStore = this.keyStore;

			this.certFactory = null;
			this.keyStore = null;
		}

		if (certFactory == null) {
			try {
				certFactory = createX509CertificateFactory();
				keyStore = createEmptyKeyStore();
			} catch (InitException ex) {
				AssertionError ae = new AssertionError("impossible: first KeyStore ok, subsequent one failed");
				ae.initCause(ex);
				throw ae;
			}
		}

		load(keyStore, certFactory, certs);
		return keyStore;
	}

	public static final class InitException extends Exception {

		private static final long serialVersionUID = 1L;

		public InitException(String s) {
			super(s);
		}
	}

	private static CertificateFactory createX509CertificateFactory() throws InitException {
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException ex) {
			throw new InitException("Internal error: Unable to create X.509 certificate factory: " + ex.getMessage());
		}
		return certFactory;
	}

	private static KeyStore createEmptyKeyStore() throws InitException {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance("JKS");
		} catch (KeyStoreException ex) {
			throw new InitException("Internal error: Unable to create JKS KeyStore instance: " + ex.getMessage());
		}

		try {
			keyStore.load(null);
		} catch (IOException ex) {
			AssertionError ae = new AssertionError("Impossible");
			ae.initCause(ex);
			throw ae;
		} catch (CertificateException ex) {
			AssertionError ae = new AssertionError("Impossible");
			ae.initCause(ex);
			throw ae;
		} catch (NoSuchAlgorithmException ex) {
			AssertionError ae = new AssertionError("Impossible");
			ae.initCause(ex);
			throw ae;
		}

		return keyStore;
	}

	public static void load(KeyStore keyStore, CertificateFactory certFactory, String[][] certs) throws IOException, LoadException {
		try {
			for (String[] certlines : certs) {
				String line = certlines[0];
				if (!line.equals("-----BEGIN CERTIFICATE-----")) {
					throw new LoadException("Expecting \"-----BEGIN CERTIFICATE-----\", blank line, or comment line starting with \"#\", got \"" + line + "\"");
				}
				byte[] certData = loadCertData(certlines);
				X509Certificate certificate;
				try {
					certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
				} catch (CertificateException ex) {
					throw new LoadException("unable to load cert: " + ex.getMessage());
				}
				String alias = certificate.getSubjectX500Principal().getName();
				try {
					if (keyStore.containsAlias(alias)) {
						throw new LoadException("duplicate cert alias: \"" + alias + "\"");
					}
					keyStore.setCertificateEntry(alias, certificate);
				} catch (KeyStoreException ex) {
					throw new LoadException("unable to add cert to key store: \"" + alias + "\": " + ex.getMessage());
				}
			}
		} catch (LoadException ex) {
			throw ex;
		}
	}

	private static byte[] loadCertData(String[] certlines) throws IOException, LoadException {
		StringBuilder base64 = new StringBuilder();
		for (int i = 1; i < certlines.length; i++) {
			String line = certlines[i];
			if (line.startsWith("-")) {
				String endMarker = "-----END CERTIFICATE-----";
				if (!line.equals(endMarker)) {
					throw new LoadException("Expecting \"" + endMarker + "\" or valid base-64 data, got \"" + line + "\"");
				}
				try {
					return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64.toString());
				} catch (IllegalArgumentException ex) {
					throw new LoadException("Invalid base-64 in cert data block: " + ex.getMessage());
				}
			} else {
				base64.append(line);
			}
		}
		throw new LoadException("No end found for cert");
	}

	public static final class LoadException extends Exception {

		private static final long serialVersionUID = 1L;

		public LoadException(String msg) {
			super(msg);
		}

	}

}
