/* DO NOT EDIT */
/* This file was generated from users.stone */

package autosaveworld.zlibs.com.dropbox.core.v2.users;

import java.io.IOException;
import java.util.Arrays;

import autosaveworld.zlibs.com.dropbox.core.stone.StoneSerializers;
import autosaveworld.zlibs.com.dropbox.core.stone.UnionSerializer;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonGenerationException;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonGenerator;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonParseException;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonParser;
import autosaveworld.zlibs.com.fasterxml.jackson.core.JsonToken;

public enum GetAccountError {
    // union users.GetAccountError (users.stone)
    /**
     * The specified {@link GetAccountArg#getAccountId} does not exist.
     */
    NO_ACCOUNT,
    /**
     * Catch-all used for unknown tag values returned by the Dropbox servers.
     *
     * <p> Receiving a catch-all value typically indicates this SDK version is
     * not up to date. Consider updating your SDK version to handle the new
     * tags. </p>
     */
    OTHER; // *catch_all

    /**
     * For internal use only.
     */
    static class Serializer extends UnionSerializer<GetAccountError> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(GetAccountError value, JsonGenerator g) throws IOException, JsonGenerationException {
            switch (value) {
                case NO_ACCOUNT: {
                    g.writeString("no_account");
                    break;
                }
                default: {
                    g.writeString("other");
                }
            }
        }

        @Override
        public GetAccountError deserialize(JsonParser p) throws IOException, JsonParseException {
            GetAccountError value;
            boolean collapsed;
            String tag;
            if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                collapsed = true;
                tag = getStringValue(p);
                p.nextToken();
            }
            else {
                collapsed = false;
                expectStartObject(p);
                tag = readTag(p);
            }
            if (tag == null) {
                throw new JsonParseException(p, "Required field missing: " + TAG_FIELD);
            }
            else if ("no_account".equals(tag)) {
                value = GetAccountError.NO_ACCOUNT;
            }
            else {
                value = GetAccountError.OTHER;
                skipFields(p);
            }
            if (!collapsed) {
                expectEndObject(p);
            }
            return value;
        }
    }
}