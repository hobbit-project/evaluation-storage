package org.hobbit.evaluationstorage.resultstore;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.FileResultFuture;
import org.hobbit.evaluationstorage.FileResultPairIterator;
import org.hobbit.evaluationstorage.ResultFuture;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.SerializableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Tim Ermilov on 16.05.17.
 */
public class FileResultStoreFacade implements ResultStoreFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(RiakResultStoreFacade.class);
    private String defaultFolder = "/hobbit/storage/results/";

    public FileResultStoreFacade() throws Exception {
        LOGGER.info(String.format("Setting up file facade..."));
    }

    public void setDefaultPath(String path) {
        defaultFolder = path;
    }

    private String buildFilename(String type, String taskId) {
        return defaultFolder + type + "/" + taskId;
    }

    @Override
    public void init() throws Exception {
        // create new storage folder if not exists
        new File(defaultFolder).mkdirs();
    }

    @Override
    public void put(ResultType resultType, String taskId, SerializableResult result) {
        String fileName = buildFilename(resultType.name(), taskId);
        File targetFile = new File(fileName);
        SerializableResult data = new SerializableResult(result.getSentTimestamp(), result.getData());

        try {
            FileUtils.writeByteArrayToFile(targetFile, data.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResultFuture get(ResultType resultType, String taskId) {
        String fileName = buildFilename(resultType.name(), taskId);
        File sourceFile = new File(fileName);

        if (sourceFile.exists()) {
            try {
                byte[] data = FileUtils.readFileToByteArray(sourceFile);
                SerializableResult result = SerializableResult.deserialize(data);
                return new FileResultFuture(result);
            } catch (IOException e) {
                LOGGER.error("Exception while trying to read file. Returning a future that contains null.", e);
            }
        } else {
            LOGGER.debug("Requested file {} does not exist. Returning a future that contains null.",
                    sourceFile.getAbsolutePath());
        }
        return new FileResultFuture(null);
    }

    @Override
    public Iterator<ResultPair> createIterator() {
        String expectedFilePath = buildFilename(ResultType.EXPECTED.name(), "1");
        File expectedFolder = new File(expectedFilePath).getParentFile();
        String[] files = expectedFolder.list();
        // If there are no files or the directory has not been created
        if ((files == null) || (files.length == 0)) {
            return Collections.emptyIterator();
        }
        return new FileResultPairIterator(Arrays.asList(files).iterator(), this);
    }

    @Override
    public void run() throws Exception {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public void containerStopped(String containerName, int exitCode) throws IllegalStateException {
        // nothing to do
    }
}
