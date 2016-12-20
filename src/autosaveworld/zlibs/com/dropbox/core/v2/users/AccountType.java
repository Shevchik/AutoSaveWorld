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

/**
 * What type of account this user has.
 */
public enum AccountType {
    // union users.AccountType (users.stone)
    /**
     * The basic account type.
     */
    BASIC,
    /**
     * The Dropbox Pro account type.
     */
    PRO,
    /**
     * The Dropbox Business account type.
     */
    BUSINESS;

    /**
     * For internal use only.
     */
    static class Serializer extends UnionSerializer<AccountType> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(AccountType value, JsonGenerator g) throws IOException, JsonGenerationException {
            switch (value) {
                case BASIC: {
                    g.writeString("basic");
                    break;
                }
                case PRO: {
                    g.writeString("pro");
                    break;
                }
                case BUSINESS: {
                    g.writeString("business");
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unrecognized tag: " + value);
                }
            }
        }

        @Override
        public AccountType deserialize(JsonParser p) throws IOException, JsonParseException {
            AccountType value;
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
            else if ("basic".equals(tag)) {
                value = AccountType.BASIC;
            }
            else if ("pro".equals(tag)) {
                value = AccountType.PRO;
            }
            else if ("business".equals(tag)) {
                value = AccountType.BUSINESS;
            }
            else {
                throw new JsonParseException(p, "Unknown tag: " + tag);
            }
            if (!collapsed) {
                expectEndObject(p);
            }
            return value;
        }
    }
}
