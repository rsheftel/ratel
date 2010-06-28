package org.ratel.r.file;


import java.io.*;
import java.util.*;

import static org.ratel.r.Util.*;

public class RatelFile extends Path {

    private static final long serialVersionUID = 1L;

    public RatelFile(String fileName) {
        super(fileName);
    }

    public RatelFile(File file) {
        super(file);
    }

    public void create(String text) {
        create(text.getBytes());
    }
    public void create(byte[] bytes) {
        requireNotExists();
        FileOutputStream w = null;
        try {
            w = new FileOutputStream(file);
            w.write(bytes);
        } catch (IOException e) {
            throw bomb(fileError("couldn't write"), e);
        } finally {
            maybeClose(w);
        }
    }

    static void maybeClose(Reader r) {
        try {
            if (r != null) r.close();
        } catch (IOException uncatchable) {
            uncatchable.printStackTrace();
        }
    }

    private static void maybeClose(InputStream i) {
        try {
            if (i != null) i.close();
        } catch (IOException uncatchable) {
            uncatchable.printStackTrace();
        }
    }

    private static void maybeClose(OutputStream o) {
        try {
            if (o != null) o.close();
        } catch (IOException uncatchable) {
            uncatchable.printStackTrace();
        }
    }

    public String text() {
        return new String(bytes());
    }

    public static String text(InputStream in) {
        return text(new InputStreamReader(in));
    }

    public static String text(Reader reader) {
        try {
            StringBuilder buf = new StringBuilder();
            int ch = reader.read();
            while (ch != -1) {
                buf.append((char)ch);
                ch = reader.read();
            }
            return buf.toString();
        } catch (IOException e) {
            throw bomb("failed read", e);
        } finally {
            maybeClose(reader);
        }
    }

    public void delete() {
        requireExists();
        file.delete();
    }

    public DataInputStream dataIn() {
        return new DataInputStream(in());
    }


    public InputStream in() {
        requireExists();
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw bomb(fileError("can't create data input stream"), e);
        }
    }

    public long size() {
        requireExists();
        return file.length();
    }

    public DataOutputStream dataOut() {
        try {
            return new DataOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw bomb(fileError("can't create data output stream"), e);
        }
    }

    public List<String> lines() {
        requireExists();
        FileReader r = null;
        List<String> result = empty();
        try {
            r = new FileReader(file);
            BufferedReader in = new BufferedReader(r);
            for (String s = in.readLine(); s != null; s = in.readLine())
                result.add(s);
            return result;
        } catch (IOException e) {
            throw bomb(fileError("couldn't read from file"), e);
        } finally {
            maybeClose(r);
        }
    }

    public void ensurePath() {
        new RatelDirectory(file.getParentFile()).createIfMissing();
    }

    public void deleteIfExists() {
        if (!exists()) return;
        delete();
    }

    public String name() {
        return file.getName();
    }

    public File file() {
        return file;
    }

    public PrintStream printAppender() {
        try {
            return new PrintStream(new FileOutputStream(file, true));
        } catch (IOException e) {
            throw bomb(fileError("couldn't open file writer"), e);
        }
    }

    public Writer appender() {
        try {
            return new FileWriter(file, true);
        } catch (IOException e) {
            throw bomb(fileError("couldn't open file writer"), e);
        }
    }

    public void append(String string) {
        Writer appender = null;
        try {
            appender = appender();
            appender.append(string);
        } catch (IOException e) {
            throw bomb(fileError("couldn't append"), e);
        } finally {
            try {
                if (appender != null) appender.close();
            } catch (IOException uncatchableInFinally) {
                uncatchableInFinally.printStackTrace();
            }
        }
    }

    @Override public String toString() {
        return "RatelFile@" + path();
    }

    public void copyTo(String destination) {
        copyTo(new RatelFile(destination));
    }

    public void overwrite(String text) {
        overwrite(text.getBytes());
    }

    public void overwrite(byte[] bytes) {
        deleteIfExists();
        create(bytes);
    }

    public RatelDirectory parent() {
        return new RatelDirectory(file.getParent());
    }

    public Date lastModified() {
        return new Date(file.lastModified());
    }

    public void copyTo(RatelDirectory dir) {
        copyTo(dir.file(name()));
    }

    public void copyTo(RatelFile dest) {
        try {
            copyTo(dest.out());
        } catch (RuntimeException e) {
            throw bomb(fileError("failed copying to " + dest.path()), e);
        }
    }

    public void copyFrom(InputStream in) {
        try {
            copy(in, out());
        } catch (RuntimeException e) {
            throw bomb(fileError("failed copying from " + in), e);
        }
    }

    public void copyTo(OutputStream out) {
        requireExists();
        try {
            copy(in(), out);
        } catch (RuntimeException e) {
            throw bomb(fileError("failed copying to " + out), e);
        }
    }

    public static void copy(InputStream in, OutputStream out) {
        try {
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        } catch (IOException e) {
            throw bomb("error copying " + in + " to " + out, e);
        } finally {
            maybeClose(in);
            maybeClose(out);
        }
    }

    public byte[] bytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        copyTo(bytes);
        return bytes.toByteArray();
    }


    private FileOutputStream out() {
        requireNotExists();
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw bomb("cannot create FileOutputStream", e);
        }
    }

    public String basename() {
        return name().replaceAll(".*[/\\\\]", "");
    }

    public RatelFile withSuffix(String suffix) {
        return parent().file(name() + suffix);
    }


    public void moveTo(RatelFile newFile) {
        file.renameTo(newFile.file);
    }

    public void moveTo(RatelDirectory newDir) {
        file.renameTo(newDir.file(name()).file);
    }

    public boolean missing() {
        return !exists();
    }

    public void deleteFrom(RatelDirectory other) {
        other.file(name()).delete();
    }

    public Reader reader() {
        return new InputStreamReader(in());
    }
}