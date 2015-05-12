package file;

import static util.Errors.*;

import java.io.*;

public class Path implements Serializable {

    private static final long serialVersionUID = 1L;
    protected final File file;

	public Path(String fileName) {
		file = new File(bombNull(fileName, "cannot create path from null filename!"));
	}

	public Path(File file) {
		this.file = bombNull(file, "cannot create path from null file object!");
	}

	public String path() {
		return file.getAbsolutePath();
	}

	public void requireExists() {
		bombUnless(exists(), fileError("doesn't exist"));
	}

	protected String fileError(String message) {
		return "file " + file.getName() + " " + message + " in path \n" + file.getAbsolutePath();
	}

	public boolean exists() {
		return file.exists();
	}

	public void requireNotExists() {
		bombIf(exists(), fileError("already exists"));
	}

}
