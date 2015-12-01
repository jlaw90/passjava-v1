package test1;

import com.passjava.model.test.compilable.ITestable;
import com.passjava.model.test.Result;

public class Q3 implements ITestable {
    String line = "";

    private void println(Object t) {
        line += t + "\r\n";
    }

    public void run() {
///*V+*/System.out./*V-*/
println(/*E+*/"Hello world!"/*E-*/);/*V-*/
    }

    @Override
    public Result test() {
        run();
        if(line.equals(""))
            return new Result(false, "You didn't print anything!");
        return new Result(true, "You printed: " + line);
    }
}