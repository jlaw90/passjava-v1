package test1;

import com.passjava.model.test.compilable.ITestable;
import com.passjava.model.test.Result;

public class Q2 implements ITestable {
    int count;

    public void run() {
        count = 0;
/*V+*/for(/*E+*/int i = 0; i < 10; i++/*E-*/) {
    // Do something here
/*V-*/            count++;
            if(count > 100)
                break;
/*V+*/}/*V-*/
    }

    @Override
    public Result test() {
        run();
        if(count == 5)
            return new Result(true, "Your code iterated 5 times, congrats!");
        else
            return new Result(false, "Your code iterated " + count + " times instead of 5!");
    }
}