package com.application.components;

import java.io.File;

/**
 * Interface for processing a file after is uploaded
 * 
 * @author Miguel Urízar Salinas
 */
public interface FileUploaded {
    
    /**
     * Function to call when an upload is done.
     * 
     * @param tempFile
     *            File just uploaded
     * @param fileName
     *            File name in the client to rename it.
     * @return Value that can be defined as wished.
     */
    int FileUploadedWork(File tempFile, String fileName);
}
