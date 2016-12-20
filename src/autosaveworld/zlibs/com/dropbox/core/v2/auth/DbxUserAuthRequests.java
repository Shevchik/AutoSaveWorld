/* DO NOT EDIT */
/* This file was generated from auth.stone */

package autosaveworld.zlibs.com.dropbox.core.v2.auth;

import java.util.HashMap;
import java.util.Map;

import autosaveworld.zlibs.com.dropbox.core.DbxApiException;
import autosaveworld.zlibs.com.dropbox.core.DbxException;
import autosaveworld.zlibs.com.dropbox.core.DbxWrappedException;
import autosaveworld.zlibs.com.dropbox.core.http.HttpRequestor;
import autosaveworld.zlibs.com.dropbox.core.v2.DbxRawClientV2;

/**
 * Routes in namespace "auth".
 */
public class DbxUserAuthRequests {
    // namespace auth (auth.stone)

    private final DbxRawClientV2 client;

    public DbxUserAuthRequests(DbxRawClientV2 client) {
        this.client = client;
    }

    //
    // route 2/auth/token/revoke
    //

    /**
     * Disables the access token used to authenticate the call.
     */
    public void tokenRevoke() throws DbxApiException, DbxException {
        try {
            this.client.rpcStyle(this.client.getHost().getApi(),
                                 "2/auth/token/revoke",
                                 null,
                                 false,
                                 autosaveworld.zlibs.com.dropbox.core.stone.StoneSerializers.void_(),
                                 autosaveworld.zlibs.com.dropbox.core.stone.StoneSerializers.void_(),
                                 autosaveworld.zlibs.com.dropbox.core.stone.StoneSerializers.void_());
        }
        catch (DbxWrappedException ex) {
            throw new DbxApiException(ex.getRequestId(), ex.getUserMessage(), "Unexpected error response for \"token/revoke\":" + ex.getErrorValue());
        }
    }
}
