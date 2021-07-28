package net.codebot.pdfviewer;

import android.graphics.Path;
import android.util.Pair;
import java.util.Stack;

public class UndoManager {
    Stack<Pair<Pair<Path, Integer>, String>> redo = new Stack<>();
    Stack<Pair<Pair<Path, Integer>, String>> undo  = new Stack<>();

    public void new_event(Pair<Pair<Path, Integer>, String> p){
        if (!redo.isEmpty()){
            redo.clear();
        }
        undo.push(p);
    }

    public Pair<Pair<Path, Integer>, String> setRedo(){
        Pair<Pair<Path, Integer>, String> action = redo.pop();
        switch (action.second) {
            case "draw":
                undo.push(new Pair<>(action.first, "erase_draw"));
                break;
            case "highlight":
                undo.push(new Pair<>(action.first, "erase_highlight"));
                break;
            case "erase_draw":
                undo.push(new Pair<>(action.first, "draw"));
                break;
            default:
                undo.push(new Pair<>(action.first, "highlight"));
                break;
        }
        return action;
    }

    public Pair<Pair<Path, Integer>, String> setUndo(){
        Pair<Pair<Path, Integer>, String> action = undo.pop();
        switch (action.second) {
            case "draw":
                redo.push(new Pair<>(action.first, "erase_draw"));
                break;
            case "highlight":
                redo.push(new Pair<>(action.first, "erase_highlight"));
                break;
            case "erase_draw":
                redo.push(new Pair<>(action.first, "draw"));
                break;
            default:
                redo.push(new Pair<>(action.first, "highlight"));
                break;
        }
        return action;
    }

    public boolean canRedo(){return !redo.isEmpty();}
    public boolean canUndo(){return !undo.isEmpty();}
}
