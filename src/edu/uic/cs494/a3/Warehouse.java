package edu.uic.cs494.a3;

import edu.uic.cs494.a3.solution.SolutionWarehouse;

import java.util.Set;

public interface Warehouse <S extends Shelf, I extends Item> {

    static Warehouse createWarehouse() {
        //throw new Error("Not implemented");
        return new SolutionWarehouse();
    }

    S createShelf(int size);

    I createItem(String description);

    boolean addItems(S s, Set<I> items);

    boolean removeItems(S s, Set<I> items);

    boolean moveItems(S from, S to, Set<I> items);

    Set<I> getContents();

    Set<I> getContents(S s);

    Result<Boolean> addItemsAsync(S s, Set<I> items);

    Result<Boolean> removeItemsAsync(S s, Set<I> items);

    Result<Boolean> moveItemsAsync(S from, S to, Set<I> items);

    Result<Set<I>> getContentsAsync();

    Result<Set<I>> getContentsAsync(S s);
}
