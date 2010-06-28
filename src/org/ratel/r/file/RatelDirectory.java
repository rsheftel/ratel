package org.ratel.r.file;

import java.io.*;
import java.util.*;

import static org.ratel.r.Util.*;

public class RatelDirectory extends Path {

    private static final long serialVersionUID = 1L;

    public RatelDirectory(String path) {
        super(path);
    }

    public RatelDirectory(File file) {
        super(file);
    }

    public void removeAllFiles() {
        requireDirectory();
        for (File f : ioFiles())
            if (!f.isDirectory()) new RatelFile(f).delete();
    }

    public void removeAllNonHiddenFilesRecursive() {
        requireDirectory();
        for (File f : ioFiles()) {
            if (f.getName().matches("\\..*")) continue;
            if (!f.isDirectory()) new RatelFile(f).delete();
            else new RatelDirectory(f).removeAllNonHiddenFilesRecursive();
        }
    }

    public String name() {
        return file.getName();
    }

    public RatelDirectory create() {
        requireNotExists();
        bombUnless(file.mkdirs(), fileError("could not mkdirs"));
        return this;
    }

    public int size() {
        requireDirectory();
        return file.list().length;
    }

    private void requireDirectory() {
        requireExists();
        bombUnless(file.isDirectory(), fileError("is not a directory"));
    }

    public void destroy() {
        bombIf(
                list(File.listRoots()).contains(file),
                fileError("magic error #80087355 please don't try to destroy file system roots x(")
        );
        requireDirectory();
        for (File f : ioFiles()) {
            if (f.isDirectory()) new RatelDirectory(f).destroy();
            else new RatelFile(f).delete();
        }
        remove();
    }

    public void remove() {
        requireDirectory();
        if (size() != 0)
            bomb(fileError("has contents:\n" + join("\n", file.list()) + "\n"));
        file.delete();
    }

    public RatelFile file(String ... pathItems) {
        return new RatelFile(path() + "/" + join("/", pathItems));
    }

    public RatelFile file(String name) {
        return file(array(name));
    }

    public RatelDirectory directory(String ... parts) {
        return new RatelDirectory(file.getAbsolutePath() + "/" + join("/", parts));
    }

    public void destroyIfExists() {
        if (!exists()) return;
        destroy();
    }

    public void createIfMissing() {
        if (exists()) return;
        create();
    }

    public List<RatelDirectory> directories() {
        List<RatelDirectory> result = empty();
        for (File f : ioFiles())
            if (f.isDirectory()) result.add(new RatelDirectory(f));
        return result;
    }

    private File[] ioFiles() {
        requireDirectory();
        return bombNull(file.listFiles(), "listFiles returned null on " + path());
    }

    public List<RatelFile> files() {
        List<RatelFile> result = empty();
        for (File f : ioFiles())
            if (!f.isDirectory()) result.add(new RatelFile(f));
        return result;
    }

    public List<RatelFile> files(final String pattern) {
        List<RatelFile> result = empty();
        File[] files = file.listFiles(new FileFilter() {
            @Override public boolean accept(File f) {
                return !f.isDirectory() && f.getName().matches(pattern);
            }
        });
        if (files == null) return empty();
        for (File f : files)
            result.add(new RatelFile(f));
        return result;
    }


    public void copy(RatelDirectory destination) {
        requireExists();
        destination.requireNotExists();
        destination.create();
        for (RatelDirectory childDir : directories())
            if (!childDir.name().startsWith(".svn"))
                childDir.copy(destination.directory(childDir.name()));
        for(RatelFile child : files()) child.copyTo(destination);
    }

    public void clear() {
        destroyIfExists();
        create();
    }

    public boolean exists(String childPath) {
        return file(childPath).exists();
    }
}