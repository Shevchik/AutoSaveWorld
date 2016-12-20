package autosaveworld.zlibs.com.dropbox.core;

import java.io.IOException;

import autosaveworld.zlibs.com.dropbox.core.json.JsonReadException;
import autosaveworld.zlibs.com.dropbox.core.json.JsonReader;
import autosaveworld.zlibs.com.dropbox.core.json.JsonWriter;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonGenerator;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonLocation;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonParser;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonToken;

/**
 * Used by the example code to remember auth information.
 */
public final class DbxAuthInfo {
    private final String accessToken;
    private final DbxHost host;

    /**
     * Creates a new instance with the given parameters.
     *
     * @param accessToken OAuth access token for authorization with Dropbox servers
     * @param host Dropbox host configuration used to select Dropbox servers
     */
    public DbxAuthInfo(String accessToken, DbxHost host) {
        if (accessToken == null) throw new IllegalArgumentException("'accessToken' can't be null");
        if (host == null) throw new IllegalArgumentException("'host' can't be null");

        this.accessToken = accessToken;
        this.host = host;
    }

    /**
     * Returns the OAuth access token to use for authorization with Dropbox servers.
     *
     * @return OAuth access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Returns Dropbox host configuration used to map requests to the appropriate Dropbox servers.
     *
     * @return Dropbox host configuration
     */
    public DbxHost getHost() {
        return host;
    }

    public static final JsonReader<DbxAuthInfo> Reader = new JsonReader<DbxAuthInfo>()
    {
        @Override
        public final DbxAuthInfo read(JsonParser parser)
            throws IOException, JsonReadException
        {
            JsonLocation top = JsonReader.expectObjectStart(parser);

            DbxHost host = null;
            String accessToken = null;

            while (parser.getCurrentToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();

                try {
                    if (fieldName.equals("host")) {
                        host = DbxHost.Reader.readField(parser, fieldName, host);
                    }
                    else if (fieldName.equals("access_token")) {
                        accessToken = StringReader.readField(parser, fieldName, accessToken);
                    }
                    else {
                        // Unknown field.  Skip over it.
                        JsonReader.skipValue(parser);
                    }
                }
                catch (JsonReadException ex) {
                    throw ex.addFieldContext(fieldName);
                }
            }

            JsonReader.expectObjectEnd(parser);

            if (accessToken == null) throw new JsonReadException("missing field \"access_token\"", top);
            if (host == null) host = DbxHost.DEFAULT;

            return new DbxAuthInfo(accessToken, host);
        }
    };

    public static final JsonWriter<DbxAuthInfo> Writer = new JsonWriter<DbxAuthInfo>()
    {
        @Override
        public void write(DbxAuthInfo authInfo, JsonGenerator g) throws IOException
        {
            g.writeStartObject();
            g.writeStringField("access_token", authInfo.accessToken);
            if (!authInfo.host.equals(DbxHost.DEFAULT)) {
                g.writeFieldName("host");
                DbxHost.Writer.write(authInfo.host, g);
            }
            g.writeEndObject();
        }
    };
}
