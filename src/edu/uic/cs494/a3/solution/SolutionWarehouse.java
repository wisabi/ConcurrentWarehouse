package edu.uic.cs494.a3.solution;
import edu.uic.cs494.a3.Action;
import edu.uic.cs494.a3.Result;
import edu.uic.cs494.a3.Warehouse;
import java.util.*;

public class SolutionWarehouse implements Warehouse<SolutionShelf, SolutionItem>  {


    HashSet<SolutionShelf> shelves;
    int shelfCount = 0;

    public SolutionWarehouse(){
        shelves = new HashSet<>();
    }

    @Override
    public SolutionShelf createShelf(int size) {
        shelfCount++;
        SolutionShelf shelf = new SolutionShelf(size, shelfCount);
        shelves.add(shelf);
        return shelf;
    }

    @Override
    public SolutionItem createItem(String description) {
        return new SolutionItem(description);
    }

    @Override
    public boolean addItems(SolutionShelf solutionShelf, Set<SolutionItem> items) {

        SolutionResult<Boolean> result = new SolutionResult<>();
        solutionShelf.doAction(new Action<>(Action.Operation.ADD, items, result));
        return result.getResult();

    }

    @Override
    public boolean removeItems(SolutionShelf solutionShelf, Set<SolutionItem> items) {

        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<SolutionItem, Boolean> toPerform = new Action<>(Action.Operation.REMOVE, items, result);
        solutionShelf.doAction(toPerform);
        return result.getResult();
    }

    @Override
    public boolean moveItems(SolutionShelf from, SolutionShelf to, Set<SolutionItem> items) {
        try {


            if (from.order < to.order) {
                from.l.lock();
                to.l.lock();
            } else {
                to.l.lock();
                from.l.lock();
            }

            //Calling remove and add actions. Able to work in parallel
            Result<Boolean> removeResults = removeItemsAsync(from, items);
            Result<Boolean> addResults = addItemsAsync(to, items);


            boolean _removeResults = removeResults.getResult();
            boolean _addResults = addResults.getResult();

            if (_removeResults && _addResults) {
                return true;
            }

            if (_removeResults && !_addResults) {
                while (!addItems(from, items)) ;
                return false;
            }

            if (!_removeResults && _addResults) {
                while (!removeItems(to, items)) ;
                return false;
            }
            return false;

        }finally {
            if (from.order < to.order) {
                from.l.unlock();
                to.l.unlock();
            } else {
                to.l.unlock();
                from.l.unlock();
            }
        }
    }


    @Override
    public Set<SolutionItem> getContents() {

        HashSet<SolutionResult<Set<SolutionItem>>> results = new HashSet<>();
        HashSet<SolutionItem> items = new HashSet<>();
        for(SolutionShelf shelf : shelves){
            SolutionResult<Set<SolutionItem>> result = new SolutionResult<>();
            Action<SolutionItem, Set<SolutionItem>> toPerform = new Action<>(Action.Operation.CONTENTS, new HashSet<SolutionItem>(),  result);//////////////suspect
            shelf.doAction(toPerform);
            results.add(result);
        }

        for(SolutionResult<Set<SolutionItem>> result : results){
            items.addAll(result.getResult());
        }
        return items;
    }

    @Override
    public Set<SolutionItem> getContents(SolutionShelf solutionShelf) {
        SolutionResult<Set<SolutionItem>> result = new SolutionResult<>();
        Action<SolutionItem, Set<SolutionItem>> toPerform = new Action<>(Action.Operation.CONTENTS, new HashSet<SolutionItem>(),  result );//////////////suspect
        solutionShelf.doAction(toPerform);
        return result.getResult();
    }

    @Override
    public Result<Boolean> addItemsAsync(SolutionShelf solutionShelf, Set<SolutionItem> items) {
        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<SolutionItem, Boolean> toPerform = new Action<>(Action.Operation.ADD, items, result);
        solutionShelf.doAction(toPerform);
        return result;
    }

    @Override
    public Result<Boolean> removeItemsAsync(SolutionShelf solutionShelf, Set<SolutionItem> items) {
        SolutionResult<Boolean> result = new SolutionResult<>();
        Action<SolutionItem, Boolean> toPerform = new Action<>(Action.Operation.REMOVE, items, result);
        solutionShelf.doAction(toPerform);
        return result;
    }

    @Override
    public Result<Boolean> moveItemsAsync(SolutionShelf from, SolutionShelf to, Set<SolutionItem> items) {
        Result<Boolean> removeResult = removeItemsAsync(from, items);
        Result<Boolean> addResult = addItemsAsync(to, items);

        return new SolutionResult<>(){
            @Override
            public Boolean getResult() {
                if(this.isReady()){
                    return this.get();
                }

                Boolean _removeResult = removeResult.getResult();
                Boolean _addResult = addResult.getResult();

                if(_removeResult){
                    if(_addResult){
                        this.setResult(true);
                    }
                    else{
                        Boolean addingBack = false;
                        while (!addingBack){
                            addingBack = addItems(from, items);
                        }
                        this.setResult(false);

                    }
                }
                else{
                    if(_addResult){
                        Boolean removingAdded = false;
                        while(!removingAdded){
                            removingAdded = removeItems(to, items);
                        }
                    }

                    this.setResult(false);
                }

                return super.getResult();
            }
        };



    }

    @Override
    public Result<Set<SolutionItem>> getContentsAsync() {

        Set<Result<Set<SolutionItem>>> results = new HashSet<>();
        for (SolutionShelf shelf : shelves) {

            SolutionResult<Set<SolutionItem>> result = new SolutionResult<>();
            Action<SolutionItem, Set<SolutionItem>> toPerform = new Action<>(Action.Operation.CONTENTS, new HashSet<SolutionItem>(),  result);//////////////suspect
            shelf.doAction(toPerform);
            results.add(result);

        }

        return new SolutionResult<>() {
            @Override
            public Set<SolutionItem> getResult() {
                if(this.isReady()){
                    return this.get();
                }

                HashSet<SolutionItem> ret = new HashSet<>();
                for (Result<Set<SolutionItem>> res : results) {

                    Set<SolutionItem> e = res.getResult();

                    ret.addAll(e);
                }

                this.setResult(ret);
                return ret;
            }
        };

    }

    @Override
    public Result<Set<SolutionItem>> getContentsAsync(SolutionShelf solutionShelf) {

        Result<Set<SolutionItem>> result = new SolutionResult<>();
        Action<edu.uic.cs494.a3.Item, Set<SolutionItem>> toPerform = new Action<>(Action.Operation.CONTENTS, null, result);//////////////suspect
        solutionShelf.doAction(toPerform);
//        return result;
        return new SolutionResult<>() {
            @Override
            public Set<SolutionItem> getResult() {
                if (this.isReady()) {
                    return this.get();
                }
                Set<SolutionItem> x = result.getResult();
                super.set(x);
                return x;//super.getResult();
            }
        };
    }


}
