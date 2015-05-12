import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;


public class TSCallBacks implements RMainLoopCallbacks
{

    public void rBusy (Rengine arg0, int which)
    {
        System.out.println("rBusy("+which+")");

    }

    public String rChooseFile (Rengine arg0, int arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void rFlushConsole (Rengine arg0)
    {
        // TODO Auto-generated method stub

    }

    public void rLoadHistory (Rengine arg0, String arg1)
    {
        // TODO Auto-generated method stub

    }

    public String rReadConsole (Rengine arg0, String prompt, int arg2)
    {
        System.out.print(prompt);
        return null;
    }

    public void rSaveHistory (Rengine arg0, String arg1)
    {
        // TODO Auto-generated method stub

    }

    public void rShowMessage (Rengine arg0, String message)
    {
        System.out.println("rShowMessage \""+message+"\"");

    }

    public void rWriteConsole (Rengine arg0, String text)
    {
        System.out.print(text);

    }

}
