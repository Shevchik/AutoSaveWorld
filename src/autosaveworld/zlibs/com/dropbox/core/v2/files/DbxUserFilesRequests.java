/* DO NOT EDIT */
/* This file was generated from files_properties.stone, files.stone */

package autosaveworld.zlibs.com.dropbox.core.v2.files;

import java.util.List;

import autosaveworld.zlibs.com.dropbox.core.DbxApiException;
import autosaveworld.zlibs.com.dropbox.core.DbxException;
import autosaveworld.zlibs.com.dropbox.core.DbxWrappedException;
import autosaveworld.zlibs.com.dropbox.core.http.HttpRequestor;
import autosaveworld.zlibs.com.dropbox.core.v2.DbxRawClientV2;
import autosaveworld.zlibs.com.dropbox.core.v2.async.LaunchEmptyResult;
import autosaveworld.zlibs.com.dropbox.core.v2.async.PollArg;
import autosaveworld.zlibs.com.dropbox.core.v2.async.PollError;
import autosaveworld.zlibs.com.dropbox.core.v2.async.PollErrorException;

/**
 * Routes in namespace "files".
 */
public class DbxUserFilesRequests {
    // namespace files (files_properties.stone, files.stone)

    private final DbxRawClientV2 client;

    public DbxUserFilesRequests(DbxRawClientV2 client) {
        this.client = client;
    }

    //
    // route 2/files/create_folder
    //

    /**
     * Create a folder at a given path.
     *
     */
    FolderMetadata createFolder(CreateFolderArg arg) throws CreateFolderErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/create_folder",
                                        arg,
                                        false,
                                        CreateFolderArg.Serializer.INSTANCE,
                                        FolderMetadata.Serializer.INSTANCE,
                                        CreateFolderError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new CreateFolderErrorException("2/files/create_folder", ex.getRequestId(), ex.getUserMessage(), (CreateFolderError) ex.getErrorValue());
        }
    }

    /**
     * Create a folder at a given path.
     *
     * <p> The {@code autorename} request parameter will default to {@code
     * false} (see {@link #createFolder(String,boolean)}). </p>
     *
     * @param path  Path in the user's Dropbox to create. Must match pattern
     *     "{@code (/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)}" and not be {@code null}.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public FolderMetadata createFolder(String path) throws CreateFolderErrorException, DbxException {
        CreateFolderArg _arg = new CreateFolderArg(path);
        return createFolder(_arg);
    }

    /**
     * Create a folder at a given path.
     *
     * @param path  Path in the user's Dropbox to create. Must match pattern
     *     "{@code (/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)}" and not be {@code null}.
     * @param autorename  If there's a conflict, have the Dropbox server try to
     *     autorename the folder to avoid the conflict.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public FolderMetadata createFolder(String path, boolean autorename) throws CreateFolderErrorException, DbxException {
        CreateFolderArg _arg = new CreateFolderArg(path, autorename);
        return createFolder(_arg);
    }

    //
    // route 2/files/delete
    //

    /**
     * Delete the file or folder at a given path. If the path is a folder, all
     * its contents will be deleted too. A successful response indicates that
     * the file or folder was deleted. The returned metadata will be the
     * corresponding {@link FileMetadata} or {@link FolderMetadata} for the item
     * at time of deletion, and not a {@link DeletedMetadata} object.
     *
     *
     * @return Metadata for a file or folder.
     */
    Metadata delete(DeleteArg arg) throws DeleteErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/delete",
                                        arg,
                                        false,
                                        DeleteArg.Serializer.INSTANCE,
                                        Metadata.Serializer.INSTANCE,
                                        DeleteError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new DeleteErrorException("2/files/delete", ex.getRequestId(), ex.getUserMessage(), (DeleteError) ex.getErrorValue());
        }
    }

    /**
     * Delete the file or folder at a given path. If the path is a folder, all
     * its contents will be deleted too. A successful response indicates that
     * the file or folder was deleted. The returned metadata will be the
     * corresponding {@link FileMetadata} or {@link FolderMetadata} for the item
     * at time of deletion, and not a {@link DeletedMetadata} object.
     *
     * @param path  Path in the user's Dropbox to delete. Must match pattern
     *     "{@code (/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)}" and not be {@code null}.
     *
     * @return Metadata for a file or folder.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public Metadata delete(String path) throws DeleteErrorException, DbxException {
        DeleteArg _arg = new DeleteArg(path);
        return delete(_arg);
    }

    //
    // route 2/files/delete_batch
    //

    /**
     * Delete multiple files/folders at once. This route is asynchronous, which
     * returns a job ID immediately and runs the delete batch asynchronously.
     * Use {@link DbxUserFilesRequests#deleteBatchCheck(String)} to check the
     * job status.
     *
     *
     * @return Result returned by methods that may either launch an asynchronous
     *     job or complete synchronously. Upon synchronous completion of the
     *     job, no additional information is returned.
     */
    LaunchEmptyResult deleteBatch(DeleteBatchArg arg) throws DbxApiException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/delete_batch",
                                        arg,
                                        false,
                                        DeleteBatchArg.Serializer.INSTANCE,
                                        LaunchEmptyResult.Serializer.INSTANCE,
                                        autosaveworld.zlibs.com.dropbox.core.stone.StoneSerializers.void_());
        }
        catch (DbxWrappedException ex) {
            throw new DbxApiException(ex.getRequestId(), ex.getUserMessage(), "Unexpected error response for \"delete_batch\":" + ex.getErrorValue());
        }
    }

    /**
     * Delete multiple files/folders at once. This route is asynchronous, which
     * returns a job ID immediately and runs the delete batch asynchronously.
     * Use {@link DbxUserFilesRequests#deleteBatchCheck(String)} to check the
     * job status.
     *
     * @param entries  Must not contain a {@code null} item and not be {@code
     *     null}.
     *
     * @return Result returned by methods that may either launch an asynchronous
     *     job or complete synchronously. Upon synchronous completion of the
     *     job, no additional information is returned.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public LaunchEmptyResult deleteBatch(List<DeleteArg> entries) throws DbxApiException, DbxException {
        DeleteBatchArg _arg = new DeleteBatchArg(entries);
        return deleteBatch(_arg);
    }

    //
    // route 2/files/delete_batch/check
    //

    /**
     * Returns the status of an asynchronous job for {@link
     * DbxUserFilesRequests#deleteBatch(List)}. If success, it returns list of
     * result for each entry.
     *
     * @param arg  Arguments for methods that poll the status of an asynchronous
     *     job.
     */
    DeleteBatchJobStatus deleteBatchCheck(PollArg arg) throws PollErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/delete_batch/check",
                                        arg,
                                        false,
                                        PollArg.Serializer.INSTANCE,
                                        DeleteBatchJobStatus.Serializer.INSTANCE,
                                        PollError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new PollErrorException("2/files/delete_batch/check", ex.getRequestId(), ex.getUserMessage(), (PollError) ex.getErrorValue());
        }
    }

    /**
     * Returns the status of an asynchronous job for {@link
     * DbxUserFilesRequests#deleteBatch(List)}. If success, it returns list of
     * result for each entry.
     *
     * @param asyncJobId  Id of the asynchronous job. This is the value of a
     *     response returned from the method that launched the job. Must have
     *     length of at least 1 and not be {@code null}.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public DeleteBatchJobStatus deleteBatchCheck(String asyncJobId) throws PollErrorException, DbxException {
        PollArg _arg = new PollArg(asyncJobId);
        return deleteBatchCheck(_arg);
    }

    //
    // route 2/files/get_metadata
    //

    /**
     * Returns the metadata for a file or folder. Note: Metadata for the root
     * folder is unsupported.
     *
     *
     * @return Metadata for a file or folder.
     */
    Metadata getMetadata(GetMetadataArg arg) throws GetMetadataErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/get_metadata",
                                        arg,
                                        false,
                                        GetMetadataArg.Serializer.INSTANCE,
                                        Metadata.Serializer.INSTANCE,
                                        GetMetadataError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new GetMetadataErrorException("2/files/get_metadata", ex.getRequestId(), ex.getUserMessage(), (GetMetadataError) ex.getErrorValue());
        }
    }

    /**
     * Returns the metadata for a file or folder. Note: Metadata for the root
     * folder is unsupported.
     *
     * <p> The default values for the optional request parameters will be used.
     * See {@link GetMetadataBuilder} for more details. </p>
     *
     * @param path  The path of a file or folder on Dropbox. Must match pattern
     *     "{@code (/(.|[\\r\\n])*|id:.*)|(rev:[0-9a-f]{9,})|(ns:[0-9]+(/.*)?)}"
     *     and not be {@code null}.
     *
     * @return Metadata for a file or folder.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public Metadata getMetadata(String path) throws GetMetadataErrorException, DbxException {
        GetMetadataArg _arg = new GetMetadataArg(path);
        return getMetadata(_arg);
    }

    /**
     * Returns the metadata for a file or folder. Note: Metadata for the root
     * folder is unsupported.
     *
     * @param path  The path of a file or folder on Dropbox. Must match pattern
     *     "{@code (/(.|[\\r\\n])*|id:.*)|(rev:[0-9a-f]{9,})|(ns:[0-9]+(/.*)?)}"
     *     and not be {@code null}.
     *
     * @return Request builder for configuring request parameters and completing
     *     the request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public GetMetadataBuilder getMetadataBuilder(String path) {
        GetMetadataArg.Builder argBuilder_ = GetMetadataArg.newBuilder(path);
        return new GetMetadataBuilder(this, argBuilder_);
    }

    //
    // route 2/files/list_folder
    //

    /**
     * Starts returning the contents of a folder. If the result's {@link
     * ListFolderResult#getHasMore} field is {@code true}, call {@link
     * DbxUserFilesRequests#listFolderContinue(String)} with the returned {@link
     * ListFolderResult#getCursor} to retrieve more entries. If you're using
     * {@link ListFolderArg#getRecursive} set to {@code true} to keep a local
     * cache of the contents of a Dropbox account, iterate through each entry in
     * order and process them as follows to keep your local state in sync: For
     * each {@link FileMetadata}, store the new entry at the given path in your
     * local state. If the required parent folders don't exist yet, create them.
     * If there's already something else at the given path, replace it and
     * remove all its children. For each {@link FolderMetadata}, store the new
     * entry at the given path in your local state. If the required parent
     * folders don't exist yet, create them. If there's already something else
     * at the given path, replace it but leave the children as they are. Check
     * the new entry's {@link FolderSharingInfo#getReadOnly} and set all its
     * children's read-only statuses to match. For each {@link DeletedMetadata},
     * if your local state has something at the given path, remove it and all
     * its children. If there's nothing at the given path, ignore this entry.
     *
     */
    ListFolderResult listFolder(ListFolderArg arg) throws ListFolderErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/list_folder",
                                        arg,
                                        false,
                                        ListFolderArg.Serializer.INSTANCE,
                                        ListFolderResult.Serializer.INSTANCE,
                                        ListFolderError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new ListFolderErrorException("2/files/list_folder", ex.getRequestId(), ex.getUserMessage(), (ListFolderError) ex.getErrorValue());
        }
    }

    /**
     * Starts returning the contents of a folder. If the result's {@link
     * ListFolderResult#getHasMore} field is {@code true}, call {@link
     * DbxUserFilesRequests#listFolderContinue(String)} with the returned {@link
     * ListFolderResult#getCursor} to retrieve more entries. If you're using
     * {@link ListFolderArg#getRecursive} set to {@code true} to keep a local
     * cache of the contents of a Dropbox account, iterate through each entry in
     * order and process them as follows to keep your local state in sync: For
     * each {@link FileMetadata}, store the new entry at the given path in your
     * local state. If the required parent folders don't exist yet, create them.
     * If there's already something else at the given path, replace it and
     * remove all its children. For each {@link FolderMetadata}, store the new
     * entry at the given path in your local state. If the required parent
     * folders don't exist yet, create them. If there's already something else
     * at the given path, replace it but leave the children as they are. Check
     * the new entry's {@link FolderSharingInfo#getReadOnly} and set all its
     * children's read-only statuses to match. For each {@link DeletedMetadata},
     * if your local state has something at the given path, remove it and all
     * its children. If there's nothing at the given path, ignore this entry.
     *
     * <p> The default values for the optional request parameters will be used.
     * See {@link ListFolderBuilder} for more details. </p>
     *
     * @param path  The path to the folder you want to see the contents of. Must
     *     match pattern "{@code (/(.|[\\r\\n])*)?|(ns:[0-9]+(/.*)?)}" and not
     *     be {@code null}.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public ListFolderResult listFolder(String path) throws ListFolderErrorException, DbxException {
        ListFolderArg _arg = new ListFolderArg(path);
        return listFolder(_arg);
    }

    /**
     * Starts returning the contents of a folder. If the result's {@link
     * ListFolderResult#getHasMore} field is {@code true}, call {@link
     * DbxUserFilesRequests#listFolderContinue(String)} with the returned {@link
     * ListFolderResult#getCursor} to retrieve more entries. If you're using
     * {@link ListFolderArg#getRecursive} set to {@code true} to keep a local
     * cache of the contents of a Dropbox account, iterate through each entry in
     * order and process them as follows to keep your local state in sync: For
     * each {@link FileMetadata}, store the new entry at the given path in your
     * local state. If the required parent folders don't exist yet, create them.
     * If there's already something else at the given path, replace it and
     * remove all its children. For each {@link FolderMetadata}, store the new
     * entry at the given path in your local state. If the required parent
     * folders don't exist yet, create them. If there's already something else
     * at the given path, replace it but leave the children as they are. Check
     * the new entry's {@link FolderSharingInfo#getReadOnly} and set all its
     * children's read-only statuses to match. For each {@link DeletedMetadata},
     * if your local state has something at the given path, remove it and all
     * its children. If there's nothing at the given path, ignore this entry.
     *
     * @param path  The path to the folder you want to see the contents of. Must
     *     match pattern "{@code (/(.|[\\r\\n])*)?|(ns:[0-9]+(/.*)?)}" and not
     *     be {@code null}.
     *
     * @return Request builder for configuring request parameters and completing
     *     the request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public ListFolderBuilder listFolderBuilder(String path) {
        ListFolderArg.Builder argBuilder_ = ListFolderArg.newBuilder(path);
        return new ListFolderBuilder(this, argBuilder_);
    }

    //
    // route 2/files/list_folder/continue
    //

    /**
     * Once a cursor has been retrieved from {@link
     * DbxUserFilesRequests#listFolder(String)}, use this to paginate through
     * all files and retrieve updates to the folder, following the same rules as
     * documented for {@link DbxUserFilesRequests#listFolder(String)}.
     *
     */
    ListFolderResult listFolderContinue(ListFolderContinueArg arg) throws ListFolderContinueErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/list_folder/continue",
                                        arg,
                                        false,
                                        ListFolderContinueArg.Serializer.INSTANCE,
                                        ListFolderResult.Serializer.INSTANCE,
                                        ListFolderContinueError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new ListFolderContinueErrorException("2/files/list_folder/continue", ex.getRequestId(), ex.getUserMessage(), (ListFolderContinueError) ex.getErrorValue());
        }
    }

    /**
     * Once a cursor has been retrieved from {@link
     * DbxUserFilesRequests#listFolder(String)}, use this to paginate through
     * all files and retrieve updates to the folder, following the same rules as
     * documented for {@link DbxUserFilesRequests#listFolder(String)}.
     *
     * @param cursor  The cursor returned by your last call to {@link
     *     DbxUserFilesRequests#listFolder(String)} or {@link
     *     DbxUserFilesRequests#listFolderContinue(String)}. Must have length of
     *     at least 1 and not be {@code null}.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public ListFolderResult listFolderContinue(String cursor) throws ListFolderContinueErrorException, DbxException {
        ListFolderContinueArg _arg = new ListFolderContinueArg(cursor);
        return listFolderContinue(_arg);
    }

    //
    // route 2/files/list_folder/get_latest_cursor
    //

    /**
     * A way to quickly get a cursor for the folder's state. Unlike {@link
     * DbxUserFilesRequests#listFolder(String)}, {@link
     * DbxUserFilesRequests#listFolderGetLatestCursor(String)} doesn't return
     * any entries. This endpoint is for app which only needs to know about new
     * files and modifications and doesn't need to know about files that already
     * exist in Dropbox.
     *
     */
    ListFolderGetLatestCursorResult listFolderGetLatestCursor(ListFolderArg arg) throws ListFolderErrorException, DbxException {
        try {
            return this.client.rpcStyle(this.client.getHost().getApi(),
                                        "2/files/list_folder/get_latest_cursor",
                                        arg,
                                        false,
                                        ListFolderArg.Serializer.INSTANCE,
                                        ListFolderGetLatestCursorResult.Serializer.INSTANCE,
                                        ListFolderError.Serializer.INSTANCE);
        }
        catch (DbxWrappedException ex) {
            throw new ListFolderErrorException("2/files/list_folder/get_latest_cursor", ex.getRequestId(), ex.getUserMessage(), (ListFolderError) ex.getErrorValue());
        }
    }

    /**
     * A way to quickly get a cursor for the folder's state. Unlike {@link
     * DbxUserFilesRequests#listFolder(String)}, {@link
     * DbxUserFilesRequests#listFolderGetLatestCursor(String)} doesn't return
     * any entries. This endpoint is for app which only needs to know about new
     * files and modifications and doesn't need to know about files that already
     * exist in Dropbox.
     *
     * <p> The default values for the optional request parameters will be used.
     * See {@link ListFolderGetLatestCursorBuilder} for more details. </p>
     *
     * @param path  The path to the folder you want to see the contents of. Must
     *     match pattern "{@code (/(.|[\\r\\n])*)?|(ns:[0-9]+(/.*)?)}" and not
     *     be {@code null}.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public ListFolderGetLatestCursorResult listFolderGetLatestCursor(String path) throws ListFolderErrorException, DbxException {
        ListFolderArg _arg = new ListFolderArg(path);
        return listFolderGetLatestCursor(_arg);
    }

    /**
     * A way to quickly get a cursor for the folder's state. Unlike {@link
     * DbxUserFilesRequests#listFolder(String)}, {@link
     * DbxUserFilesRequests#listFolderGetLatestCursor(String)} doesn't return
     * any entries. This endpoint is for app which only needs to know about new
     * files and modifications and doesn't need to know about files that already
     * exist in Dropbox.
     *
     * @param path  The path to the folder you want to see the contents of. Must
     *     match pattern "{@code (/(.|[\\r\\n])*)?|(ns:[0-9]+(/.*)?)}" and not
     *     be {@code null}.
     *
     * @return Request builder for configuring request parameters and completing
     *     the request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public ListFolderGetLatestCursorBuilder listFolderGetLatestCursorBuilder(String path) {
        ListFolderArg.Builder argBuilder_ = ListFolderArg.newBuilder(path);
        return new ListFolderGetLatestCursorBuilder(this, argBuilder_);
    }

    //
    // route 2/files/upload
    //

    /**
     * Create a new file with the contents provided in the request. Do not use
     * this to upload a file larger than 150 MB. Instead, create an upload
     * session with {@link DbxUserFilesRequests#uploadSessionStart(boolean)}.
     *
     *
     * @return Uploader used to upload the request body and finish request.
     */
    UploadUploader upload(CommitInfo arg) throws DbxException {
        HttpRequestor.Uploader _uploader = this.client.uploadStyle(this.client.getHost().getContent(),
                                                                   "2/files/upload",
                                                                   arg,
                                                                   false,
                                                                   CommitInfo.Serializer.INSTANCE);
        return new UploadUploader(_uploader);
    }

    /**
     * Create a new file with the contents provided in the request. Do not use
     * this to upload a file larger than 150 MB. Instead, create an upload
     * session with {@link DbxUserFilesRequests#uploadSessionStart(boolean)}.
     *
     * <p> The default values for the optional request parameters will be used.
     * See {@link UploadBuilder} for more details. </p>
     *
     * @param path  Path in the user's Dropbox to save the file. Must match
     *     pattern "{@code (/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)}" and not be
     *     {@code null}.
     *
     * @return Uploader used to upload the request body and finish request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public UploadUploader upload(String path) throws DbxException {
        CommitInfo _arg = new CommitInfo(path);
        return upload(_arg);
    }

    /**
     * Create a new file with the contents provided in the request. Do not use
     * this to upload a file larger than 150 MB. Instead, create an upload
     * session with {@link DbxUserFilesRequests#uploadSessionStart(boolean)}.
     *
     * @param path  Path in the user's Dropbox to save the file. Must match
     *     pattern "{@code (/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)}" and not be
     *     {@code null}.
     *
     * @return Uploader builder for configuring request parameters and
     *     instantiating an uploader.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public UploadBuilder uploadBuilder(String path) {
        CommitInfo.Builder argBuilder_ = CommitInfo.newBuilder(path);
        return new UploadBuilder(this, argBuilder_);
    }

    //
    // route 2/files/upload_session/append
    //

    /**
     * Append more data to an upload session. A single request should not upload
     * more than 150 MB of file contents.
     *
     *
     * @return Uploader used to upload the request body and finish request.
     */
    UploadSessionAppendUploader uploadSessionAppend(UploadSessionCursor arg) throws DbxException {
        HttpRequestor.Uploader _uploader = this.client.uploadStyle(this.client.getHost().getContent(),
                                                                   "2/files/upload_session/append",
                                                                   arg,
                                                                   false,
                                                                   UploadSessionCursor.Serializer.INSTANCE);
        return new UploadSessionAppendUploader(_uploader);
    }

    /**
     * Append more data to an upload session. A single request should not upload
     * more than 150 MB of file contents.
     *
     * @param sessionId  The upload session ID (returned by {@link
     *     DbxUserFilesRequests#uploadSessionStart(boolean)}). Must not be
     *     {@code null}.
     * @param offset  The amount of data that has been uploaded so far. We use
     *     this to make sure upload data isn't lost or duplicated in the event
     *     of a network error.
     *
     * @return Uploader used to upload the request body and finish request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     *
     * @deprecated use {@link
     *     DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     *     instead.
     */
    @Deprecated
    public UploadSessionAppendUploader uploadSessionAppend(String sessionId, long offset) throws DbxException {
        UploadSessionCursor _arg = new UploadSessionCursor(sessionId, offset);
        return uploadSessionAppend(_arg);
    }

    //
    // route 2/files/upload_session/append_v2
    //

    /**
     * Append more data to an upload session. When the parameter close is set,
     * this call will close the session. A single request should not upload more
     * than 150 MB of file contents.
     *
     *
     * @return Uploader used to upload the request body and finish request.
     */
    UploadSessionAppendV2Uploader uploadSessionAppendV2(UploadSessionAppendArg arg) throws DbxException {
        HttpRequestor.Uploader _uploader = this.client.uploadStyle(this.client.getHost().getContent(),
                                                                   "2/files/upload_session/append_v2",
                                                                   arg,
                                                                   false,
                                                                   UploadSessionAppendArg.Serializer.INSTANCE);
        return new UploadSessionAppendV2Uploader(_uploader);
    }

    /**
     * Append more data to an upload session. When the parameter close is set,
     * this call will close the session. A single request should not upload more
     * than 150 MB of file contents.
     *
     * <p> The {@code close} request parameter will default to {@code false}
     * (see {@link #uploadSessionAppendV2(UploadSessionCursor,boolean)}). </p>
     *
     * @param cursor  Contains the upload session ID and the offset. Must not be
     *     {@code null}.
     *
     * @return Uploader used to upload the request body and finish request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public UploadSessionAppendV2Uploader uploadSessionAppendV2(UploadSessionCursor cursor) throws DbxException {
        UploadSessionAppendArg _arg = new UploadSessionAppendArg(cursor);
        return uploadSessionAppendV2(_arg);
    }

    /**
     * Append more data to an upload session. When the parameter close is set,
     * this call will close the session. A single request should not upload more
     * than 150 MB of file contents.
     *
     * @param cursor  Contains the upload session ID and the offset. Must not be
     *     {@code null}.
     * @param close  If true, the current session will be closed, at which point
     *     you won't be able to call {@link
     *     DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     *     anymore with the current session.
     *
     * @return Uploader used to upload the request body and finish request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public UploadSessionAppendV2Uploader uploadSessionAppendV2(UploadSessionCursor cursor, boolean close) throws DbxException {
        UploadSessionAppendArg _arg = new UploadSessionAppendArg(cursor, close);
        return uploadSessionAppendV2(_arg);
    }

    //
    // route 2/files/upload_session/finish
    //

    /**
     * Finish an upload session and save the uploaded data to the given file
     * path. A single request should not upload more than 150 MB of file
     * contents.
     *
     *
     * @return Uploader used to upload the request body and finish request.
     */
    UploadSessionFinishUploader uploadSessionFinish(UploadSessionFinishArg arg) throws DbxException {
        HttpRequestor.Uploader _uploader = this.client.uploadStyle(this.client.getHost().getContent(),
                                                                   "2/files/upload_session/finish",
                                                                   arg,
                                                                   false,
                                                                   UploadSessionFinishArg.Serializer.INSTANCE);
        return new UploadSessionFinishUploader(_uploader);
    }

    /**
     * Finish an upload session and save the uploaded data to the given file
     * path. A single request should not upload more than 150 MB of file
     * contents.
     *
     * @param cursor  Contains the upload session ID and the offset. Must not be
     *     {@code null}.
     * @param commit  Contains the path and other optional modifiers for the
     *     commit. Must not be {@code null}.
     *
     * @return Uploader used to upload the request body and finish request.
     *
     * @throws IllegalArgumentException  If any argument does not meet its
     *     preconditions.
     */
    public UploadSessionFinishUploader uploadSessionFinish(UploadSessionCursor cursor, CommitInfo commit) throws DbxException {
        UploadSessionFinishArg _arg = new UploadSessionFinishArg(cursor, commit);
        return uploadSessionFinish(_arg);
    }

    //
    // route 2/files/upload_session/start
    //

    /**
     * Upload sessions allow you to upload a single file in one or more
     * requests, for example where the size of the file is greater than 150 MB.
     * This call starts a new upload session with the given data. You can then
     * use {@link
     * DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     * to add more data and {@link
     * DbxUserFilesRequests#uploadSessionFinish(UploadSessionCursor,CommitInfo)}
     * to save all the data to a file in Dropbox. A single request should not
     * upload more than 150 MB of file contents.
     *
     *
     * @return Uploader used to upload the request body and finish request.
     */
    UploadSessionStartUploader uploadSessionStart(UploadSessionStartArg arg) throws DbxException {
        HttpRequestor.Uploader _uploader = this.client.uploadStyle(this.client.getHost().getContent(),
                                                                   "2/files/upload_session/start",
                                                                   arg,
                                                                   false,
                                                                   UploadSessionStartArg.Serializer.INSTANCE);
        return new UploadSessionStartUploader(_uploader);
    }

    /**
     * Upload sessions allow you to upload a single file in one or more
     * requests, for example where the size of the file is greater than 150 MB.
     * This call starts a new upload session with the given data. You can then
     * use {@link
     * DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     * to add more data and {@link
     * DbxUserFilesRequests#uploadSessionFinish(UploadSessionCursor,CommitInfo)}
     * to save all the data to a file in Dropbox. A single request should not
     * upload more than 150 MB of file contents.
     *
     * <p> The {@code close} request parameter will default to {@code false}
     * (see {@link #uploadSessionStart(boolean)}). </p>
     *
     * @return Uploader used to upload the request body and finish request.
     */
    public UploadSessionStartUploader uploadSessionStart() throws DbxException {
        UploadSessionStartArg _arg = new UploadSessionStartArg();
        return uploadSessionStart(_arg);
    }

    /**
     * Upload sessions allow you to upload a single file in one or more
     * requests, for example where the size of the file is greater than 150 MB.
     * This call starts a new upload session with the given data. You can then
     * use {@link
     * DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     * to add more data and {@link
     * DbxUserFilesRequests#uploadSessionFinish(UploadSessionCursor,CommitInfo)}
     * to save all the data to a file in Dropbox. A single request should not
     * upload more than 150 MB of file contents.
     *
     * @param close  If true, the current session will be closed, at which point
     *     you won't be able to call {@link
     *     DbxUserFilesRequests#uploadSessionAppendV2(UploadSessionCursor,boolean)}
     *     anymore with the current session.
     *
     * @return Uploader used to upload the request body and finish request.
     */
    public UploadSessionStartUploader uploadSessionStart(boolean close) throws DbxException {
        UploadSessionStartArg _arg = new UploadSessionStartArg(close);
        return uploadSessionStart(_arg);
    }
}
