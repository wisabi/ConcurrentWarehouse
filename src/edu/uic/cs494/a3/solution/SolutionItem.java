package edu.uic.cs494.a3.solution;

import edu.uic.cs494.a3.Item;

public class SolutionItem implements Item {
    String description;
    SolutionItem(String description){
        this.description = description;
    }

    @Override
    public String toString() {
        return "SolutionItem{" + description + "}";
    }
}