package com.xpandit.plugins.xrayjenkins.task.filefilters;

import com.xpandit.plugins.xrayjenkins.Utils.FileUtils;
import com.xpandit.plugins.xrayjenkins.exceptions.XrayJenkinsGenericException;
import hudson.FilePath;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * This class is a FileFilter implementation that will only "accept" files that are both:
 * 1) In the Set of "valid" files.
 * 2) Has been modified in the last <i>"lastModified"</i> minutes. (if null, we considered all files has been modified).
 */
public class OnlyFeatureFilesInPathFilter implements FileFilter, Serializable {

    private final Set<String> validFilePaths;
    private final String lastModified;

    public OnlyFeatureFilesInPathFilter(Set<String> validFilePaths, String lastModified) {
        this.validFilePaths = validFilePaths;
        this.lastModified = lastModified;
    }

    @Override
    public boolean accept(File pathname) {
        return validFilePaths.contains(pathname.getAbsolutePath()) && isApplicableAsModifiedFile(pathname);
    }

    private boolean isApplicableAsModifiedFile(File pathname) {
        return pathname != null && FileUtils.isApplicableAsModifiedFile(new FilePath(pathname), this.lastModified);
    }
}
