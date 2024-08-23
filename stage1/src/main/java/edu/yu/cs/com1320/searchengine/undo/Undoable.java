package edu.yu.cs.com1320.searchengine.undo;

public interface Undoable {
    /**
     * @return true if the undo succeeds
     */
    boolean undo();
}
