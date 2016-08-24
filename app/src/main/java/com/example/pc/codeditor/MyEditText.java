package com.example.pc.codeditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by PC on 2016/8/23.
 */
public class MyEditText extends EditText {

    Listener listener;
    public MyEditText(Context context) {
        this(context,null);
    }
    public MyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MyEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void setListener(Listener l){
        this.listener = l;
    }
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(event.getAction()== KeyEvent.ACTION_UP){
            myonKeyUp(event.getKeyCode(),event);
            return true;
        }
        if(event.getAction()== KeyEvent.ACTION_DOWN){
            myonKeyDown(event.getKeyCode(),event);
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }
    Boolean ctrldown=false;
    Boolean shiftdown=false;
    public boolean myonKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_CTRL_LEFT||keyCode== KeyEvent.KEYCODE_CTRL_RIGHT){
            ctrldown=true;
        }
        if(keyCode== KeyEvent.KEYCODE_SHIFT_LEFT||keyCode== KeyEvent.KEYCODE_SHIFT_RIGHT){
            shiftdown=true;
        }
        if(keyCode== KeyEvent.KEYCODE_Z&&ctrldown&&!shiftdown){
            listener.undo();
        }
        if(keyCode== KeyEvent.KEYCODE_C&&ctrldown&&!shiftdown){
            listener.copy();
        }
        if(keyCode== KeyEvent.KEYCODE_V&&ctrldown&&!shiftdown){
            listener.paste();
        }
        if(keyCode== KeyEvent.KEYCODE_X&&ctrldown&&!shiftdown){
            listener.cut();
        }
        if(keyCode== KeyEvent.KEYCODE_A&&ctrldown&&!shiftdown){
            listener.selectAll();
        }
        if(keyCode== KeyEvent.KEYCODE_Z&&ctrldown&&shiftdown){
            listener.redo();
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean myonKeyUp(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_CTRL_LEFT||keyCode== KeyEvent.KEYCODE_CTRL_RIGHT){

            ctrldown=false;
        }
        if(keyCode== KeyEvent.KEYCODE_SHIFT_LEFT||keyCode== KeyEvent.KEYCODE_SHIFT_RIGHT){
            shiftdown=false;
        }
        return super.onKeyUp(keyCode, event);
    }
}
