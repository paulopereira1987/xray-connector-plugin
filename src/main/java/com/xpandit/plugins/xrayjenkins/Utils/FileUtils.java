/*
 * XP.RAVEN Project
 * <p/>
 * Copyright (C) 2018 Xpand IT.
 * <p/>
 * This software is proprietary.
 */
package com.xpandit.plugins.xrayjenkins.Utils;

import com.google.common.collect.Sets;
import com.xpandit.plugins.xrayjenkins.exceptions.XrayJenkinsGenericException;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {
    }

    /**
     * Utility method that close a Closeable object
     *
     * @param closeable the Closeable object
     */
    public static void closeSilently(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch(Exception e) {
                // Don't do anything
            }
        }
    }

    /**
     * Utility method that returns all .features files from a folder, including those contained in sub folders.
     * This method works for master node and for slave (remote) nodes
     *
     * @param workspace the Jenkins project workspace
     * @param path      the folder path
     * @param listener  the TaskListener
     * @return the list of the filepath's
     */
    public static List<FilePath> getFeatureFilesFromWorkspace(
            FilePath workspace,
            String path,
            TaskListener listener
    ) throws IOException, InterruptedException {
        String errors = getErrors(workspace, path, listener);
        if (errors != null) {
            throw new XrayJenkinsGenericException(errors);
        }
        List<FilePath> paths = new ArrayList<>();
        FilePath folder = readFile(workspace, path, listener);
        if (folder.isDirectory()) {
            paths.addAll(Arrays.asList(folder.list("*.feature", "", false)));
            List<FilePath> children = folder.list();
            for (FilePath child : children) {
                if (child.isDirectory()) {
                    paths.addAll(getFeatureFilesFromWorkspace(workspace, child.toString(), listener));
                }
            }
        } else {
            throw new XrayJenkinsGenericException("The path is not a folder");
        }
        return paths;
    }

    /**
     * This method will return all files with the ".feature" extension within a given build workspace and a path.
     *
     * @param workspace the build's workspace
     * @param path      the relative or absolute path where to search for the feature files
     * @param listener  the lostener used to write some logs
     * @return Unmodifiable set with the full absolute path of the feature files found
     * @throws IOException          Exception thrown when file/directory reading the operation fails
     * @throws InterruptedException Exception thrown when file/directory reading the operation fails
     */
    public static Set<String> getFeatureFileNamesFromWorkspace(
            FilePath workspace,
            String path,
            TaskListener listener
    ) throws IOException, InterruptedException {
        final List<FilePath> filePaths = getFeatureFilesFromWorkspace(workspace, path, listener);
        final Set<String> fileNames = Sets.newHashSet();

        if (CollectionUtils.isNotEmpty(filePaths)) {
            for (FilePath fp : filePaths) {
                String remoteFileName = fp.getRemote();
                if (StringUtils.isNotBlank(remoteFileName)) {
                    fileNames.add(remoteFileName);
                }
            }
        }

        return Collections.unmodifiableSet(fileNames);
    }

    private static String getErrors(
            FilePath workspace,
            String path,
            TaskListener listener
    ) {
        List<String> errors = new LinkedList<>();
        if (workspace == null) {
            errors.add("workspace cannot be null");
        }
        if (StringUtils.isBlank(path)) {
            errors.add("The folder path cannot be null nor empty");
        }
        if (listener == null) {
            errors.add("The task listener cannot be null");
        }
        return errors.isEmpty() ? null : StringUtils.join(errors, "\n");
    }


    /**
     * Returns a list of files that matches the glob expression relatively to the workspace or that matches a file path.
     * The glob expression is relative, any attempt to match an absolute path will not work.
     *
     * @param workspace      the workspace
     * @param globExpression the glob expression. Must be relative to the workspace
     */
    public static List<FilePath> getFiles(
            FilePath workspace,
            String globExpression,
            TaskListener listener,
            VirtualChannel channel
    )
            throws IOException, InterruptedException {
        if (workspace == null) {
            throw new XrayJenkinsGenericException("workspace cannot be null");
        }
        if (StringUtils.isBlank(globExpression)) {
            throw new XrayJenkinsGenericException("The file path cannot be null nor empty");
        }

        FilePath base;
        String regexExpression = globExpression;

        String root = FilenameUtils.getPrefix(globExpression);//Get the root of the glob expression

        if (!StringUtils.isBlank(root)) {
            //If there is a root then the path is absolute, the reg exp should be the path without the root
            String path = FilenameUtils.getPath(globExpression);
            String fileName = FilenameUtils.getName(globExpression);

            regexExpression = FilenameUtils.concat(path, fileName);

            //The base will be the root. The reg exp will be evaluated here.
            base = new FilePath(channel, root);
        } else {
            base = workspace;
        }

        List<FilePath> filePaths = Stream.of(base.list(regexExpression, "", false))
                                         .filter(Objects::nonNull)
                                         .collect(Collectors.toList());

        if (filePaths.isEmpty()) {
            listener.getLogger()
                    .println("0 files found. Please make sure the path provided is valid and is not a directory");
            throw new XrayJenkinsGenericException("0 files found. Please make sure the path provided is valid and is not a directory");
        }

        filePaths.forEach(filePath -> listener.getLogger().println("File found: " + filePath.getRemote()));

        return filePaths;
    }

    /**
     * Given the Jenkins project workspace FilePath and the file path, will resolve the FilePath of the file
     *
     * @param workspace the Jenkins workspace
     * @param filePath  the file path of the file
     * @param listener  the task listener
     * @return the <code>FilePath</code>
     */
    public static FilePath readFile(FilePath workspace, String filePath, TaskListener listener) {
        FilePath f = new FilePath(workspace, filePath);
        listener.getLogger().println("File: " + f.getRemote());
        return f;
    }

}
