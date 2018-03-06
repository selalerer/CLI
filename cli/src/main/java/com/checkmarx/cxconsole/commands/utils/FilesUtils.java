package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.parameters.CLISASTParameters;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

/**
 * Created by nirli on 01/03/2018.
 */
public class FilesUtils {

    private FilesUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static Logger log = Logger.getLogger(FilesUtils.class);

    private static byte[] zippedSourcesBytes;
    private static int numOfZippedFiles;

    public static void zipFolder(String location, CLISASTParameters cliSastParameters, long maxZipSize, FileOutputStream fileOutputStream) {
        zippedSourcesBytes = null;
        numOfZippedFiles = 0;
        if (!isProjectDirectoryValid(location)) {
            return;
        }
        try {
            log.info("Zipping files from: " + location + " Please wait");
            String[] excludePatterns = createExcludePatternsArray(cliSastParameters);
            String[] includeAllPatterns = new String[]{"**/*"};//the default is to include all files
            ZipListener zipListener = new ZipListener() {

                @Override
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                    log.trace("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
                }
            };
            if (ZipUtils.getZipOutputStream() == null) {
                ZipUtils.setZipOutputStream(new org.apache.tools.zip.ZipOutputStream(fileOutputStream));
            }
            ZipUtils.zip(new File(location), excludePatterns, includeAllPatterns, maxZipSize, zipListener);
            log.info("Zipping complete with " + numOfZippedFiles + " files.");
        } catch (Exception e) {
            log.trace(e);
            log.error("Error occurred during zipping source files. Error message: " + e.getMessage());
        }
    }

    public static void validateZippedSources(long maxZipSize) throws CLIJobException {

        // check packed sources size
        if (zippedSourcesBytes == null || zippedSourcesBytes.length == 0) {
            // if size is greater that restricted value, stop scan
            log.error("Packing sources has failed: empty packed source ");
            throw new CLIJobException("Packing sources has failed: empty packed source ");
        }

        if (zippedSourcesBytes.length > maxZipSize) {
            // if size greater that restricted value, stop scan
            log.error("Packed project size is greater than " + maxZipSize);
            throw new CLIJobException("Packed project size is greater than " + maxZipSize);
        }

    }

    private static boolean isProjectDirectoryValid(String location) {
        File projectDir = new File(location);
        if (!projectDir.exists()) {
            log.error("Project directory [" + location + "] does not exist.");
            return false;
        }

        if (!projectDir.isDirectory()) {
            log.error("Project path [" + location + "] should point to a directory.");
            return false;
        }
        return true;
    }

    private static String[] createExcludePatternsArray(CLISASTParameters cliSastParameters) {
        LinkedList<String> excludePatterns = new LinkedList<>();
        try {
            String defaultExcludedFolders = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FOLDERS);
            for (String folder : StringUtils.split(defaultExcludedFolders, ",")) {
                String trimmedPattern = folder.trim();
                if (!Objects.equals(trimmedPattern, "")) {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                }
            }

            String defaultExcludedFiles = ConfigMgr.getCfgMgr().getProperty(ConfigMgr.KEY_EXCLUDED_FILES);
            for (String file : StringUtils.split(defaultExcludedFiles, ",")) {
                String trimmedPattern = file.trim();
                if (!Objects.equals(trimmedPattern, "")) {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }

            if (cliSastParameters.isHasExcludedFoldersParam()) {
                for (String folder : cliSastParameters.getExcludedFolders()) {
                    String trimmedPattern = folder.trim();
                    if (!Objects.equals(trimmedPattern, "")) {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/') + "/**/*");
                    }
                }
            }

            if (cliSastParameters.isHasExcludedFilesParam()) {
                for (String file : cliSastParameters.getExcludedFiles()) {
                    String trimmedPattern = file.trim();
                    if (!Objects.equals(trimmedPattern, "")) {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred creation of exclude patterns");
        }

        return excludePatterns.toArray(new String[]{});
    }
}
