package org.hobbit.evaluationstorage.resultstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.FileResultFuture;
import org.hobbit.evaluationstorage.FileResultPairIterator;
import org.hobbit.evaluationstorage.ResultFuture;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.data.ResultValueType;
import org.hobbit.evaluationstorage.data.SerializableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Tim Ermilov on 16.05.17.
 */
public class FileResultStoreBasedFacadeDecorator extends AbstractResultStoreFacadeDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiakResultStoreFacade.class);

    private String storageFolder = "/hobbit/storage/results/";
    /**
     * Maximum size of a result stored in the decorated store (in kB).
     */
    private int maxDecoratedStoreSize;

    public FileResultStoreBasedFacadeDecorator(ResultStoreFacade decorated, int maxDecoratedStoreSize) {
        super(decorated);
        this.maxDecoratedStoreSize = maxDecoratedStoreSize;
    }

    public FileResultStoreBasedFacadeDecorator(ResultStoreFacade decorated, int maxDecoratedStoreSize,
            String storageFolder) {
        super(decorated);
        this.maxDecoratedStoreSize = maxDecoratedStoreSize;
        setStorageFolder(storageFolder);
    }

    public void setStorageFolder(String storageFolder) {
        if (!storageFolder.endsWith(File.separator)) {
            storageFolder += File.separator;
        }
        this.storageFolder = storageFolder;
    }

    private String buildFilename(String type, String taskId) {
        return storageFolder + type + "/" + taskId;
    }

    @Override
    public void init() throws Exception {
        super.init();
        // create new storage folder if not exists
        new File(storageFolder).mkdirs();
    }

    @Override
    public void put(ResultType resultType, String taskId, SerializableResult result) {
        int actualSize = (result.getData() != null) ? result.getData().length : 0;
        // transform into kB
        actualSize /= 1024;
        if (actualSize <= maxDecoratedStoreSize) {
            decorated.put(resultType, taskId, result);
        } else {
            // Store the data in a file
            String fileName = buildFilename(resultType.name(), taskId);
            File targetFile = new File(fileName);
            try {
                FileUtils.writeByteArrayToFile(targetFile, result.getData());
            } catch (IOException e) {
                LOGGER.error("Error while writing result to file. Result will be lost.", e);
                e.printStackTrace();
            }
            // Put the reference and the time stamp in the decorated store
            decorated.put(resultType, taskId, new SerializableResult(result.getSentTimestamp(),
                    ResultValueType.FILE_REF, targetFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8)));
        }

    }

    @Override
    public ResultFuture get(ResultType resultType, String taskId) {
        // FIXME This method is not aligned to the changes made in put!!!
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

        // FIXME This method needs to create a wrapper for the iterator of the decorated
        // store! The wrapper has to decide whether it has to forward the result of the
        // decorated store or whether it has to load the data from a file.

        String expectedFilePath = buildFilename(ResultType.EXPECTED.name(), "1");
        File expectedFolder = new File(expectedFilePath).getParentFile();
        String[] files = expectedFolder.list();
        // If there are no files or the directory has not been created
        if ((files == null) || (files.length == 0)) {
            return Collections.emptyIterator();
        }
        return new FileResultPairIterator(Arrays.asList(files).iterator(), this);
    }

}
