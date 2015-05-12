package file;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;

import java.io.*;
import java.util.*;
public class QDirectory extends Path {

    private static final long serialVersionUID = 1L;

    public QDirectory(String path) {
		super(path);
	}

	public QDirectory(File file) {
		super(file);
	}
	
	public void removeAllFiles() {
		requireDirectory();
		for (File f : ioFiles()) 
			if (!f.isDirectory()) new QFile(f).delete();
	}
	
	public void removeAllNonHiddenFilesRecursive() {
		requireDirectory();
		for (File f : ioFiles()) {
			if (f.getName().matches("\\..*")) continue;
			if (!f.isDirectory()) new QFile(f).delete();
			else new QDirectory(f).removeAllNonHiddenFilesRecursive();
		}
	}
	
	public String name() { 
		return file.getName();
	}
	
	public QDirectory create() {
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
			if (f.isDirectory()) new QDirectory(f).destroy();
			else new QFile(f).delete();
		}
		remove();
	}

	public void remove() {
		requireDirectory();
		if (size() != 0) 
			bomb(fileError("has contents:\n" + join("\n", file.list()) + "\n"));
		file.delete();
	}

	public QFile file(String ... pathItems) {
		return new QFile(path() + "/" + join("/", pathItems));
	}
	
	public QFile file(String name) {
	    return file(array(name));
	}

	public QDirectory directory(String ... parts) {
		return new QDirectory(file.getAbsolutePath() + "/" + join("/", parts));
	}

	public void destroyIfExists() {
		if (!exists()) return;
		destroy();
	}

	public void createIfMissing() {
		if (exists()) return;
		create();
	}

	public List<QDirectory> directories() {
		List<QDirectory> result = empty();
		for (File f : ioFiles()) 
			if (f.isDirectory()) result.add(new QDirectory(f));
		return result;
	}

	private File[] ioFiles() {
		requireDirectory();
		return bombNull(file.listFiles(), "listFiles returned null on " + path());
	}

	public List<QFile> files() {
		List<QFile> result = empty();
		for (File f : ioFiles()) 
			if (!f.isDirectory()) result.add(new QFile(f));
		return result;
	}

	public List<QFile> files(final String pattern) {
		List<QFile> result = empty();
		File[] files = file.listFiles(new FileFilter() {
			@Override public boolean accept(File f) {
				return !f.isDirectory() && f.getName().matches(pattern); 
			}
		});
		if (files == null) return empty();
		for (File f : files)
			result.add(new QFile(f));
		return result;
	}

	public Csv csv(String suffix, boolean hasHeader) {
	    return new Csv(file(suffix), hasHeader);
	}

    public void copy(QDirectory destination) {
        requireExists();
        destination.requireNotExists();
        destination.create();
        for (QDirectory childDir : directories()) 
            if (!childDir.name().startsWith(".svn"))
                childDir.copy(destination.directory(childDir.name()));
        for(QFile child : files()) child.copyTo(destination);
    }

    public void clear() {
        destroyIfExists();
        create();
    }

	public boolean exists(String childPath) {
		return file(childPath).exists();
	}
}
