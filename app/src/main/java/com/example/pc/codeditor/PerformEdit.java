
package com.example.pc.codeditor;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Stack;


public class PerformEdit {
    private int index;
    private Watcher watcher;
    private Stack<Action> history = new Stack<>();
    private Stack<Action> historyBack = new Stack<>();
    private boolean ignore;
    private Editable editable;
    public EditText editText;
    private boolean flag = false;
    private boolean paste =false;
    private Indent indent;

    public void setIndent(Indent indent){
        this.indent = indent;
    }
    public PerformEdit(@NonNull EditText editText) {
        this.editable = editText.getText();
        this.editText = editText;

    }
    public void setUndoWatcher(){
        watcher = new Watcher();
        editText.addTextChangedListener(watcher);
    }
    public void setPaste(){
        this.paste = true;
    }
    public  void  setIgnore(boolean ignore){
        this.ignore = ignore;
    }
    public final void clearHistory() {
        history.clear();
        historyBack.clear();
    }

    public final void undo() {
        if (history.empty()) return;
        flag = true;
        Action action = history.pop();
        historyBack.push(action);
        if (action.isAdd) {
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length());
            editText.setSelection(action.startCursor, action.startCursor);
        } else {
            editable.insert(action.startCursor, action.actionTarget);
            if(action.startCursor+1==action.endCursor)action.startCursor=action.endCursor;
            editText.setSelection(action.startCursor,action.endCursor);
        }
        flag = false;
        if (!history.empty() && history.peek().index == action.index) {
            undo();
        }
    }

    public final void redo() {
        if (historyBack.empty()) return;
        flag = true;
        Action action = historyBack.pop();
        history.push(action);
        if (action.isAdd) {
            editable.insert(action.startCursor, action.actionTarget);
        } else {
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length());
        }
        flag = false;
        while (!historyBack.empty() && historyBack.peek().index == action.index){
            if(indent!=null)indent.setFlag(false);
            redo();
          if(indent!=null)indent.setFlag(true);
        }
    }
    private class Watcher implements TextWatcher {

        /**
         * Before text changed.
         *
         * @param s     the s
         * @param start the start 起始光标
         * @param count the endCursor 选择数量
         * @param after the after 替换增加的文字数
         */
        boolean del = false;
        int relativePos = -3;

        @Override
        public final void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (flag||ignore ) return;
            int end = start + count;
            if (end > start && end <= s.length()) {
                CharSequence charSequence = s.subSequence(start, end);
                if (charSequence.length() > 0) {
                    Action action = new Action(charSequence, start, false);action.setSelectCount(count);
                    history.push(action);
                    historyBack.clear();
                    char c = charSequence.charAt(charSequence.length()-1);
                    int relativePos_ = s.length() - editText.getSelectionStart();
                    boolean update = c=='\n'|| relativePos_!= relativePos;
                    if(update || !del) {
                        action.setIndex(++index);
                    }
                    else action.setIndex(index);
                    if( relativePos_!= relativePos){
                       relativePos = relativePos_;
                    }
                }
                del = true;
            }
            else del = false;
        }

        @Override
        public final void onTextChanged(CharSequence s, int start, int before, int count) {
            if (flag||ignore ) return;
            int end = start + count;
            if (end > start) {
                CharSequence charSequence = s.subSequence(start, end);
                if (charSequence.length() > 0) {
                    Action action = new Action(charSequence, start, true);
                    boolean isAdd=false;
                    if(!history.empty())isAdd= history.peek().isAdd;
                    history.push(action);
                    historyBack.clear();
                    int relativePos_ = s.length() - editText.getSelectionStart();
                    char c = charSequence.charAt(charSequence.length()-1);
                    boolean update = (c=='\n'|| relativePos != relativePos_||paste);
                    if(before>0){
                        action.setIndex(index);
                    }
                    else {
                        if(!update&&isAdd){
                            action.setIndex(index);
                        }
                        else {
                            action.setIndex(++index);
                        }
                    }
                    if(relativePos != relativePos_){
                        relativePos = relativePos_;
                    }
                }
            }
        }

        @Override
        public final void afterTextChanged(Editable s) {
            paste = false;
        }

    }

    private class Action {
        /** 改变字符. */
        CharSequence actionTarget;
        /** 光标位置. */
        int startCursor;
        int endCursor;
        /** 标志增加操作. */
        boolean isAdd;
        /** 操作序号. */
        int index;

        public Action(CharSequence actionTar, int startCursor, boolean add) {
            this.actionTarget = actionTar;
            this.startCursor = startCursor;
            this.endCursor = startCursor;
            this.isAdd = add;
        }

        public void setSelectCount(int count) {
            this.endCursor = endCursor + count;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
    public void destroy(){
        editText.removeTextChangedListener(watcher);
    }
}
