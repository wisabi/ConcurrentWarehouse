package edu.uic.cs494.a3.solution;
import edu.uic.cs494.a3.Action;
import edu.uic.cs494.a3.Result;
import edu.uic.cs494.a3.Shelf;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static java.util.Collections.disjoint;

public class SolutionShelf extends Shelf<SolutionItem> {

    int size;
    Queue<Action> Q3;
    Lock l ;
    int order;
    SolutionShelf(int size, int order){
        this.size = size;
        Q3 = new LinkedList<>();
        this.order = order;
        l = new ReentrantLock();
    }

    @Override
    protected void doAction(Action a) {
        synchronized (this){
            Q3.add(a);
            this.notifyAll();
        }
    }


    @Override
    protected Action getAction() {


        Action x;
        synchronized (this){
            while ((x = Q3.poll()) == null){
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return x;
    }

    @Override
    protected void add(Set<SolutionItem> items, Result<Boolean> result) {

        Set<SolutionItem> set =  this.getContents();
        if (items.size() > (this.size - set.size())) {
            synchronized(this){
                result.setResult(false);
                this.notifyAll();
            }
            return;
        }

        if(!disjoint(set, items)){
            synchronized (this){
                result.setResult(false);
                this.notifyAll();
            }
            return;
        }

        addItems(items);
        synchronized (this){
            result.setResult(true);
            this.notifyAll();
        }
    }

    @Override
    protected void remove(Set<SolutionItem> items, Result<Boolean> result) {

            Set<SolutionItem> set = getContents();
            if (!set.containsAll(items)) {
                synchronized (this) {
                    result.setResult(false);
                    this.notifyAll();
                }
                return;
            }
            removeItems(items);
        synchronized (this) {
            result.setResult(true);
            this.notifyAll();
        }
    }

    @Override
    protected void contents(Result<Set<SolutionItem>> result) {
        Set<SolutionItem> set =  this.getContents();
        synchronized (this){
            result.setResult(set);
            this.notifyAll();
        }
    }
}
