package util;

import java.io.IOException;

import org.testng.annotations.Test;

public class LocalProcessTest {

    @Test(groups = { "unittest", "local" })
    public void testLocalDir() throws IOException {
        // To execute 'dir' we need to execute a command prompt first - strange
        String[] cmd = {"cmd","/c", "dir"};
        LocalProcess process = new LocalProcess(cmd);

        String output = process.execute();
        
        assert output.contains("<DIR>") : "Failed to excute command";

    }
}
