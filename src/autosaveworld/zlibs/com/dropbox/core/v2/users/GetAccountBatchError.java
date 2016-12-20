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
 * This class is an open tagged union.  Tagged unions instances are always
 * associated to a specific tag.  This means only one of the {@code isAbc()}
 * methods will return {@code true}. You can use {@link #tag()} to determine the
 * tag associated with this instance.
 *
 * <p> Open unions may be extended in the future with additional tags. If a new
 * tag is introduced that this SDK does not recognized, the {@link #OTHER} value
 * will be used. </p>
 */
public final class GetAccountBatchError {
    // union users.GetAccountBatchError (users.stone)

    /**
     * Discriminating tag type for {@link GetAccountBatchError}.
     */
    public enum Tag {
        /**
         * The value is an account ID specified in {@link
         * GetAccountBatchArg#getAccountIds} that does not exist.
         */
        NO_ACCOUNT, // String
        /**
         * Catch-all used for unknown tag values returned by the Dropbox
         * servers.
         *
         * <p> Receiving a catch-all value typically indicates this SDK version
         * is not up to date. Consider updating your SDK version to handle the
         * new tags. </p>
         */
        OTHER; // *catch_all
    }

    /**
     * Catch-all used for unknown tag values returned by the Dropbox servers.
     *
     * <p> Receiving a catch-all value typically indicates this SDK version is
     * not up to date. Consider updating your SDK version to handle the new
     * tags. </p>
     */
    public static final GetAccountBatchError OTHER = new GetAccountBatchError(Tag.OTHER, null);

    private final Tag _tag;
    private final String noAccountValue;

    /**
     *
     * @param noAccountValue  The value is an account ID specified in {@link
     *     GetAccountBatchArg#getAccountIds} that does not exist. Must have
     *     length of at least 40, have length of at most 40, and not be {@code
     *     null}.
     * @param _tag  Discriminating tag for this instance.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    private GetAccountBatchError(Tag _tag, String noAccountValue) {
        this._tag = _tag;
        this.noAccountValue = noAccountValue;
    }

    /**
     * Returns the tag for this instance.
     *
     * <p> This class is a tagged union.  Tagged unions instances are always
     * associated to a specific tag.  This means only one of the {@code isXyz()}
     * methods will return {@code true}. Callers are recommended to use the tag
     * value in a {@code switch} statement to properly handle the different
     * values for this {@code GetAccountBatchError}. </p>
     *
     * <p> If a tag returned by the server is unrecognized by this SDK, the
     * {@link Tag#OTHER} value will be used. </p>
     *
     * @return the tag for this instance.
     */
    public Tag tag() {
        return _tag;
    }

    /**
     * Returns {@code true} if this instance has the tag {@link Tag#NO_ACCOUNT},
     * {@code false} otherwise.
     *
     * @return {@code true} if this instance is tagged as {@link
     *     Tag#NO_ACCOUNT}, {@code false} otherwise.
     */
    public boolean isNoAccount() {
        return this._tag == Tag.NO_ACCOUNT;
    }

    /**
     * Returns an instance of {@code GetAccountBatchError} that has its tag set
     * to {@link Tag#NO_ACCOUNT}.
     *
     * <p> The value is an account ID specified in {@link
     * GetAccountBatchArg#getAccountIds} that does not exist. </p>
     *
     * @param value  value to assign to this instance.
     *
     * @return Instance of {@code GetAccountBatchError} with its tag set to
     *     {@link Tag#NO_ACCOUNT}.
     *
     * @throws IllegalArgumentException  if {@code value} is shorter than 40, is
     *     longer than 40, or is {@code null}.
     */
    public static GetAccountBatchError noAccount(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
        if (value.length() < 40) {
            throw new IllegalArgumentException("String is shorter than 40");
        }
        if (value.length() > 40) {
            throw new IllegalArgumentException("String is longer than 40");
        }
        return new GetAccountBatchError(Tag.NO_ACCOUNT, value);
    }

    /**
     * The value is an account ID specified in {@link
     * GetAccountBatchArg#getAccountIds} that does not exist.
     *
     * <p> This instance must be tagged as {@link Tag#NO_ACCOUNT}. </p>
     *
     * @return The {@link String} value associated with this instance if {@link
     *     #isNoAccount} is {@code true}.
     *
     * @throws IllegalStateException  If {@link #isNoAccount} is {@code false}.
     */
    public String getNoAccountValue() {
        if (this._tag != Tag.NO_ACCOUNT) {
            throw new IllegalStateException("Invalid tag: required Tag.NO_ACCOUNT, but was Tag." + this._tag.name());
        }
        return noAccountValue;
    }

    /**
     * Returns {@code true} if this instance has the tag {@link Tag#OTHER},
     * {@code false} otherwise.
     *
     * @return {@code true} if this instance is tagged as {@link Tag#OTHER},
     *     {@code false} otherwise.
     */
    public boolean isOther() {
        return this._tag == Tag.OTHER;
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(new Object [] {
            _tag,
            noAccountValue
        });
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof GetAccountBatchError) {
            GetAccountBatchError other = (GetAccountBatchError) obj;
            if (this._tag != other._tag) {
                return false;
            }
            switch (_tag) {
                case NO_ACCOUNT:
                    return (this.noAccountValue == other.noAccountValue) || (this.noAccountValue.equals(other.noAccountValue));
                case OTHER:
                    return true;
                default:
                    return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Serializer.INSTANCE.serialize(this, false);
    }

    /**
     * Returns a String representation of this object formatted for easier
     * readability.
     *
     * <p> The returned String may contain newlines. </p>
     *
     * @return Formatted, multiline String representation of this object
     */
    public String toStringMultiline() {
        return Serializer.INSTANCE.serialize(this, true);
    }

    /**
     * For internal use only.
     */
    static class Serializer extends UnionSerializer<GetAccountBatchError> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void serialize(GetAccountBatchError value, JsonGenerator g) throws IOException, JsonGenerationException {
            switch (value.tag()) {
                case NO_ACCOUNT: {
                    g.writeStartObject();
                    writeTag("no_account", g);
                    g.writeFieldName("no_account");
                    StoneSerializers.string().serialize(value.noAccountValue, g);
                    g.writeEndObject();
                    break;
                }
                default: {
                    g.writeString("other");
                }
            }
        }

        @Override
        public GetAccountBatchError deserialize(JsonParser p) throws IOException, JsonParseException {
            GetAccountBatchError value;
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
                String fieldValue = null;
                expectField("no_account", p);
                fieldValue = StoneSerializers.string().deserialize(p);
                value = GetAccountBatchError.noAccount(fieldValue);
            }
            else {
                value = GetAccountBatchError.OTHER;
                skipFields(p);
            }
            if (!collapsed) {
                expectEndObject(p);
            }
            return value;
        }
    }
}