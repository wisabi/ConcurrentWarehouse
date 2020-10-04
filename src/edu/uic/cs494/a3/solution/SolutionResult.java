package edu.uic.cs494.a3.solution;
import edu.uic.cs494.a3.Result;


public class SolutionResult<T> extends Result<T> {

    @Override
    public void setResult(T result) {
        synchronized (this){
            super.set(result);
            this.notifyAll();
        }
    }

    @Override
    public T getResult() {
        synchronized (this) {
            try {
                while (!isReady()) {
                    this.wait(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return super.get();
    }
}

