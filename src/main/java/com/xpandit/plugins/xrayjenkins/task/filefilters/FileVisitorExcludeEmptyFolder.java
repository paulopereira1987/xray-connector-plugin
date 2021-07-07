package com.xpandit.plugins.xrayjenkins.task.filefilters;

import hudson.FilePath;
import hudson.util.FileVisitor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Class adapted from {@see FilterFileVisitor}.
 * This class is indented to have the same functionality as the FilterFileVisitor, but instead of calling the visitor
 * is every directory, it first checks if every given directory includes at least one file accepted by the given filter.
 */
public final class FileVisitorExcludeEmptyFolder extends FileVisitor implements Serializable {
    private final FileFilter filter;
    private final FileVisitor visitor;

    public FileVisitorExcludeEmptyFolder(FileFilter filter, FileVisitor visitor) {
        this.filter = filter;
        this.visitor = visitor;
    }

    @Override
    public void visit(File file, String relativePath) throws IOException {
        if (isNotEmptyDirectory(file) || this.filter.accept(file)) {
            this.visitor.visit(file, relativePath);
        }
    }

    private boolean isNotEmptyDirectory(File file) {
        return file.isDirectory() && directoryHasAcceptedFiles(file);
    }

    private boolean directoryHasAcceptedFiles(File directory) {
        try {
            List<FilePath> filesInFolder = new FilePath(directory).list(this.filter);
            return !filesInFolder.isEmpty();
        } catch (IOException | InterruptedException e) {
            // If something went wrong, we always include the directory to avoid losing data.
            return true;
        }
    }
}
