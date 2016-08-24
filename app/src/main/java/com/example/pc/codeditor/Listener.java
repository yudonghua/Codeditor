package com.example.pc.codeditor;

/**
 * Created by PC on 2016/8/23.
 */
public interface Listener {
    void undo();
    void redo();
    void copy();
    void paste();
    void selectAll();
    void cut();
}
