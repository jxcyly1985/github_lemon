package com.gionee.lockscreen.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Stack;

public class FileUtils {

    public static final String TAG = "FileUtils";

    /**
     * copy source to destination path if source path is directory, destination path should be a directory if
     * source path is file, destination path should be a file
     * 
     * @param source
     * @param des
     * @param deleteParent
     * @throws IOException
     */
    public static void copySurToDes(String source, String des, boolean deleteParent) throws IOException,
            IllegalArgumentException {

        if (source == null || des == null || "".equals(source) || "".equals(des)) {
            throw new IllegalArgumentException("bad argument");
        }
        File sourceFile = new File(source);

        if (!sourceFile.exists()) {
            return;
        }
        File desFile = new File(des);
        String parentPath = desFile.getParent();
        if (parentPath != null) {
            // only delete all the children in the parent
            // this mean only keep the destination file in the parent path
            File parentFile = new File(parentPath);
            if (deleteParent) {
                if (parentFile.exists()) {
                    // keep parent self
                    deleteFileChild(parentFile);
                }
            }
        }
        // make directory recursively and set permission
        putFileAuthorith(desFile);

        if (desFile.exists()) {
            // if destination file exists must delete it
            deleteFile(desFile);
        }

        // do copy form source to destination
        if (sourceFile.exists()) {
            if (sourceFile.isDirectory()) {
                copyDir(sourceFile, desFile);
            } else {
                copyFile(sourceFile, desFile);
            }
        }
    }

    /**
     * read from inputStream to destination file path
     * 
     * @param inputstream
     * @param desPath
     * @param deleteParent
     * @throws IOException
     */
    public static void copyInputStreamToDesFile(InputStream inputStream, String desPath) throws IOException,
            IllegalArgumentException {

        if (inputStream == null || desPath == null) {
            throw new IllegalArgumentException("bad argument");
        }

        File desFile = new File(desPath);
        if (desFile.exists()) {
            // if destination file exists must delete it
            deleteFile(desFile);
        }
        // make directory recursively and set permission
        putFileAuthorith(desFile);
        copyFile(inputStream, desFile);

    }

    /**
     * delete all the children in the file and remove self
     * 
     * @param file
     */
    public static boolean deleteFile(File file) {

        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            if (listFile != null) {
                for (File childFile : listFile) {
                    deleteFile(childFile);
                }
            }
        }
        return file.delete();

    }

    /**
     * delete all the sibling in parent of file only leave file self
     * 
     * @param file
     *            -- the file to find all the sibling to delete
     */
    public static void deleteFileSibling(File file) {

        String parentPath = file.getParent();

        if (parentPath != null) {
            File pFile = new File(parentPath);
            File[] listFile = pFile.listFiles();
            if (listFile != null) {
                for (File childFile : listFile) {
                    if (!childFile.equals(file)) {
                        deleteFile(childFile);
                    }
                }
            }
            
        }

    }

    /**
     * delete all the sibling in parent of file only leave file self
     * 
     * @param fileArray
     *            -- the file array to find all the sibling to delete except file array
     */
    public static void deleteFileSibling(File[] fileArray) {

        if (fileArray.length > 0) {

            String parentPath = fileArray[0].getParent();
            HashSet<File> fileSet = new HashSet<File>();
            for (int i = 0; i < fileArray.length; ++i) {
                fileSet.add(fileArray[i]);
            }

            if (parentPath != null) {
                File pFile = new File(parentPath);
                File[] listFile = pFile.listFiles();
                if (listFile != null) {
                    for (File childFile : listFile) {
                        boolean isDelete = true;
                        for (int i = 0; i < fileArray.length; ++i) {
                            if (childFile.equals(fileArray[i])) {
                                isDelete = false;
                                break;
                            }
                        }
                        if (isDelete) {
                            deleteFile(childFile);
                        }

                    }
                }
                
            }
        }

    }

    /**
     * delete all the children in the file retain the file self
     * 
     * @param file
     */
    public static void deleteFileChild(File file) {

        try {
            if (file.isDirectory()) {
                File[] listFile = file.listFiles();
                if (listFile != null) {
                    for (File childFile : listFile) {
                        deleteFile(childFile);
                    }
                }
                
            }

        } catch (Exception e) {

            DebugUtil.debug(TAG, "deleteFileChild Exception");

            e.printStackTrace();
        }

    }

    /**
     * copy directory when find a directory in destination path make directory do every step
     * 
     * @param sourceFile
     * @param desFile
     * @throws IOException
     */
    private static void copyDir(File sourceFile, File desFile) throws IOException {

        if (!desFile.exists()) {

            desFile.mkdir();
            desFile.setReadable(true, false);
            desFile.setWritable(true, false);
            desFile.setExecutable(true, false);
        }
        File[] listFile = sourceFile.listFiles();
        if (listFile != null) {
            for (File childFile : listFile) {
                File desChildFile = new File(desFile.getAbsolutePath() + File.separator + childFile.getName());

                if (childFile.isDirectory()) {

                    copyDir(childFile, desChildFile);
                } else {
                    copyFile(childFile, desChildFile);
                }

            }
        }
        
    }

    private static void copyFile(File fileSour, File fileDes) throws IOException {

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutinputStream = null;
        try {

            if (!fileDes.exists()) {
                fileDes.createNewFile();
                fileDes.setReadable(true, false);
                fileDes.setWritable(true, false);
                fileDes.setExecutable(true, false);
            }
            FileInputStream inputStream = new FileInputStream(fileSour);
            FileOutputStream outputSteam = new FileOutputStream(fileDes);

            byte[] buffer = new byte[8192];

            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutinputStream = new BufferedOutputStream(outputSteam);
            int readI = 0;
            while ((readI = bufferedInputStream.read(buffer, 0, 8192)) != -1) {
                bufferedOutinputStream.write(buffer, 0, readI);
            }

        } finally {

            if (bufferedInputStream != null) {

                bufferedInputStream.close();
            }
            if (bufferedOutinputStream != null) {

                bufferedOutinputStream.close();
            }
        }
    }

    private static void copyFile(InputStream inputStream, File fileDes) throws IOException {

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutinputStream = null;
        try {

            if (!fileDes.exists()) {
                fileDes.createNewFile();
                fileDes.setReadable(true, false);
                fileDes.setWritable(true, false);
                fileDes.setExecutable(true, false);
            }

            FileOutputStream outputSteam = new FileOutputStream(fileDes);

            byte[] buffer = new byte[8192];

            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutinputStream = new BufferedOutputStream(outputSteam);
            int readI = 0;
            while ((readI = bufferedInputStream.read(buffer, 0, 8192)) != -1) {
                bufferedOutinputStream.write(buffer, 0, readI);
            }

        } finally {

            if (bufferedInputStream != null) {

                bufferedInputStream.close();
            }
            if (bufferedOutinputStream != null) {

                bufferedOutinputStream.close();
            }
        }
    }

    /**
     * find hierarchy directory and make directory from root do every step should set the permission if not
     * the file in system can't be operator by other application
     * 
     * @param file
     */
    public static void putFileAuthorith(File file) {

        Stack<String> dirStack = new Stack<String>();
        String parentPath = file.getParent();
        if (parentPath != null) {
            File pFile = new File(parentPath);
            while (parentPath != null && !pFile.exists()) {
                dirStack.add(parentPath);
                pFile = new File(parentPath);
                parentPath = pFile.getParent();
            }
            File rootFile;
            while (dirStack.size() > 0) {
                String root = dirStack.pop();
                rootFile = new File(root);
                rootFile.mkdir();
                rootFile.setReadable(true, false);
                rootFile.setWritable(true, false);
                rootFile.setExecutable(true, false);
            }
        }

    }

}
