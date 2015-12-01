package com.passjava.model.test.compilable;

import com.passjava.model.test.IQuestionEvaluator;
import com.passjava.model.test.Question;
import com.passjava.model.test.Result;
import com.passjava.model.compiler.CompilationResult;
import com.passjava.model.compiler.PCompiler;
import com.passjava.model.test.Test;

public final class CompilableQuestionEvaluator implements IQuestionEvaluator {
    @Override
    public Result evaluate(Test test, Question q, Object answer) {

        String pack = "test" + test.id;
        String clazz = "Q" + q.id;
        String qualifiedName = pack + "." + clazz;
        String source = (String) answer;
        source = "package " + pack +
                ";import com.passjava.model.test.compilable.ITestable;import com.passjava.model.test.Result;" + source;
        // append package and import statements...

        CompilationResult res = PCompiler.compile(qualifiedName, source);

        // compilation error
        if(!res.succeeded) {
            System.err.println(res.diagnostics);
            return new Result(false, "Your code did not compile properly");
        }

        try {
            ITestable it = (ITestable) res.creation.newInstance();
            return it.test();
        } catch(Exception e) {
            return new Result(false, "Your code encountered a problem during runtime");
        }
    }
}