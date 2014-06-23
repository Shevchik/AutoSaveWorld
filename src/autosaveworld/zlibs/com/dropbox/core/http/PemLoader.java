package autosaveworld.zlibs.com.dropbox.core.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
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

	public KeyStore load(Reader in) throws IOException, LoadException {
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

		load(keyStore, certFactory, in);
		return keyStore;
	}

	public static final class InitException extends Exception {

		private static final long serialVersionUID = 1L;

		public InitException(String s) {
			super(s);
		}
	}

	private static CertificateFactory createX509CertificateFactory()
			throws InitException {
		CertificateFactory certFactory;
		try {
			certFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException ex) {
			throw new InitException(
					"Internal error: Unable to create X.509 certificate factory: "
							+ ex.getMessage());
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

	public static KeyStore createAndLoad(Reader in) throws IOException, LoadException, InitException {
		return new PemLoader().load(in);
	}

	public static void load(KeyStore keyStore, CertificateFactory certFactory, Reader in) throws IOException, LoadException {
		LineReader lin = new LineReader(in);
		try {
			while (true) {
				String line = lin.readLine();
				if (line == null) {
					break;
				}
				String trimmed = line.trim();
				if (trimmed.startsWith("#"))
				 {
					continue; // Skip comment lines.
				}
				if (trimmed.length() == 0)
				 {
					continue; // Skip empty lines.
				}
				if (!line.equals("-----BEGIN CERTIFICATE-----")) {
					throw new LoadException(
							"Expecting \"-----BEGIN CERTIFICATE-----\", blank line, or comment line starting with \"#\", got \""
									+ line + "\"");
				}
				int certStartLine = lin.getLastLineNumber();
				byte[] certData = loadCertData(lin, certStartLine);
				X509Certificate certificate;
				try {
					certificate = (X509Certificate) certFactory
							.generateCertificate(new ByteArrayInputStream(
									certData));
				} catch (CertificateException ex) {
					throw new LoadException(certStartLine,
							"unable to load cert: " + ex.getMessage());
				}
				String alias = certificate.getSubjectX500Principal().getName();
				try {
					if (keyStore.containsAlias(alias)) {
						throw new LoadException(certStartLine,
								"duplicate cert alias: \"" + alias + "\"");
					}
					keyStore.setCertificateEntry(alias, certificate);
				} catch (KeyStoreException ex) {
					throw new LoadException(certStartLine,
							"unable to add cert to key store: \"" + alias
									+ "\": " + ex.getMessage());
				}
			}
		} catch (LoadException ex) {
			// If there's no line number in the exception, this will add one in.
			ex.addLineNumber(lin.getLastLineNumber());
			throw ex;
		}
	}

	private static byte[] loadCertData(LineReader in, int certStartLine) throws IOException, LoadException {
		StringBuilder base64 = new StringBuilder();
		while (true) {
			String line = in.readLine();
			if (line == null) {
				throw new LoadException("No end found for cert beginning on line " + certStartLine);
			}
			if (line.startsWith("-")) {
				String endMarker = "-----END CERTIFICATE-----";
				if (!line.equals(endMarker)) {
					throw new LoadException("Expecting \"" + endMarker + "\" or valid base-64 data, got \"" + line + "\"");
				}
				try {
					return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64.toString());
				} catch (IllegalArgumentException ex) {
					throw new LoadException(certStartLine,"Invalid base-64 in cert data block: " + ex.getMessage());
				}
			} else {
				base64.append(line);
			}
		}
	}

	public static final class LoadException extends Exception {

		private static final long serialVersionUID = 1L;

		private final int lineNumber;

		public LoadException(String msg) {
			super(msg);
			this.lineNumber = -1;
		}

		public LoadException(int lineNumber, String msg) {
			super("line " + lineNumber + ": " + msg);
			this.lineNumber = lineNumber;
		}

		public LoadException addLineNumber(int lineNumber) {
			if (this.lineNumber >= 0) {
				return this;
			}
			return new LoadException(lineNumber, this.getMessage());
		}
	}

	public static final class LineReader {
		private final BufferedReader in;
		private int lastLineNumber;

		public LineReader(Reader in) {
			this.in = new BufferedReader(in);
			this.lastLineNumber = 0;
		}

		public String readLine() throws IOException {
			String line = in.readLine();
			if (line == null) {
				return null;
			}
			lastLineNumber++;
			return line;
		}

		private int getLastLineNumber() {
			return lastLineNumber;
		}
	}

}
