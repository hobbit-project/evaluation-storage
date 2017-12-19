package org.hobbit.evaluationstorage.resultstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.hobbit.core.data.Result;
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
     * Maximum size of a result stored in the decorated store (in byte).
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
    protected SerializableResult handleResult(ResultType resultType, Result result) {
        if ((result != null) && (result instanceof SerializableResult)) {
            SerializableResult sResult = (SerializableResult) result;
            if (ResultValueType.FILE_REF == sResult.getValueType()) {
                String fileName = new String(sResult.getData(), StandardCharsets.UTF_8);
                byte[] data = null;
                File sourceFile = new File(fileName);
                if (sourceFile.exists()) {
                    try {
                        data = FileUtils.readFileToByteArray(sourceFile);
                    } catch (IOException e) {
                        LOGGER.error("Exception while trying to read file. Returning a future that contains null.", e);
                    }
                } else {
                    LOGGER.error("Requested file {} does not exist. Returning a future that contains null.",
                            sourceFile.getAbsolutePath());
                }
                sResult.setData(data);
            }
            return sResult;
        }
        return new SerializableResult(result);
    }
}
