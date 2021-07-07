package com.xpandit.plugins.xrayjenkins.task.filefilters;

import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * This class was based in {@see DirScanner.Filter} but excluded every empty directory from being called by the {@see FileVisitor}.
 */
public class DirScannerOnlyFeatureFilesInPath extends DirScanner.Full {

    private final OnlyFeatureFilesInPathFilter fileFilter;

    public DirScannerOnlyFeatureFilesInPath(Set<String> validFilePaths, String lastModified) {
        this.fileFilter = new OnlyFeatureFilesInPathFilter(validFilePaths, lastModified);
    }

    @Override
    public void scan(File dir, FileVisitor visitor) throws IOException {
        super.scan(dir, new FileVisitorExcludeEmptyFolder(this.fileFilter, visitor));
    }
}
