package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nirli on 06/03/2018.
 */
public class ZipUtils {

    private static Logger log = Logger.getLogger(ZipUtils.class);

    private static ZipOutputStream zipOutputStream;

    public static void zip(File baseDir, String[] filterExcludePatterns, String[] filterIncludePatterns, long maxZipSize, ZipListener listener) throws IOException {
        assert baseDir != null : "baseDir must not be null";

        DirectoryScanner ds = createDirectoryScanner(baseDir, null, filterIncludePatterns);
        ds.setFollowSymlinks(true);
        ds.scan();
        printDebug(ds);
        if (ds.getIncludedFiles().length == 0) {
            log.info("No files to zip");
            throw new Zipper.NoFilesToZip();
        }
        zipFile(baseDir, ds.getIncludedFiles(), maxZipSize, listener);
    }

    private static DirectoryScanner createDirectoryScanner(File baseDir, String[] filterExcludePatterns, String[] filterIncludePatterns) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(baseDir);
        ds.setCaseSensitive(false);
        ds.setFollowSymlinks(false);
        ds.setErrorOnMissingDir(false);

        if (filterIncludePatterns != null && filterIncludePatterns.length > 0) {
            ds.setIncludes(filterIncludePatterns);
        }
        if (filterExcludePatterns != null && filterExcludePatterns.length > 0) {
            ds.setExcludes(filterExcludePatterns);
        }
        return ds;
    }

    private static void printDebug(DirectoryScanner ds) {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Base Directory: " + ds.getBasedir());

        for (String file : ds.getIncludedFiles()) {
            log.debug("Included: " + file);
        }

        for (String file : ds.getExcludedFiles()) {
            log.debug("Excluded File: " + file);
        }

        for (String file : ds.getExcludedDirectories()) {
            log.debug("Excluded Dir: " + file);
        }

        for (String file : ds.getNotFollowedSymlinks()) {
            log.debug("Not followed symbolic link: " + file);
        }
    }

    private static void zipFile(File baseDir, String[] files,long maxZipSize, ZipListener listener)
            throws IOException {
        // Switched to Apache implementation due to missing support for UTF in
        // Java 6. Should be reverted after upgrading to Java 7.
        zipOutputStream.setEncoding("UTF8");

        long compressedSize = 0;
        final double AVERAGE_ZIP_COMPRESSION_RATIO = 4.0;

        for (String fileName : files) {
            log.debug("Adding file to zip: " + fileName);

            File file = new File(baseDir, fileName);
            if (!file.canRead()) {
                log.warn("Skipping unreadable file: " + file);
                continue;
            }

            if (maxZipSize > 0 && compressedSize + (file.length() / AVERAGE_ZIP_COMPRESSION_RATIO) > maxZipSize) {
                log.info("Maximum zip file size reached. Zip size: " + compressedSize + " bytes Limit: " + maxZipSize
                        + " bytes");
                zipOutputStream.close();
                throw new Zipper.MaxZipSizeReached(fileName, compressedSize, maxZipSize);
            }

            if (listener != null) {
                listener.updateProgress(fileName, compressedSize);
            }

            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);

            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
            zipOutputStream.flush();
            compressedSize += zipEntry.getCompressedSize();
        }

    }

    public static ZipOutputStream getZipOutputStream() {
        return zipOutputStream;
    }

    public static void setZipOutputStream(ZipOutputStream zipOutputStream) {
        ZipUtils.zipOutputStream = zipOutputStream;
    }
}
