package com.example.pc.codeditor;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Indent {
    private String tabType;
    private boolean flag=true;
    private boolean autoMatch;
    private EditText editText;
    IndentWatcher watcher;
    private PerformEdit undo;
    private int lang;
    public  Indent(EditText editText, char tabType, boolean autoMatch, int lang ){
        this.editText =  editText;
        if(tabType=='\t')this.tabType = "\t\t";
        else this.tabType = "    ";
        this.autoMatch = autoMatch;
        this.lang = lang;
    }
    public void setLang(int lang){
        this.lang = lang;
    }
    public void setAutoMatch(boolean autoMatch){
       this. autoMatch = autoMatch;
    }
    public Indent(PerformEdit edit,char tabType,boolean autoMatch,int lang ){
        this.editText =  edit.editText;
        if(tabType=='\t')this.tabType = "\t\t";
        else this.tabType = "    ";
        this.autoMatch = autoMatch;
        this.lang = lang;
        undo = edit;
        undo.setIndent(this);
    }
    public void setFlag(boolean flag){
        this.flag = flag;
    }
    public void setIndent(){
         watcher = new IndentWatcher();
        editText.addTextChangedListener(watcher);
    }
     private  class IndentWatcher implements TextWatcher {
        int pos;
        Editable editable = editText.getText();
        boolean ignore = false;
        int  preLen,postLen;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            preLen = s.length();

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            postLen = s.length();
            pos = editText.getSelectionStart();
            if (!ignore&&flag) {
                ignore = true;
                editable = editText.getText();
                if (postLen > preLen) {
                    if (pos > 0 && isToMatch(pos)) {
                        editable.insert(pos, getMatch(editable.charAt(pos - 1)) + "");
                        editText.setSelection(pos - 1);
                    }
                    if (pos > 0 && pos <= editable.length() && editable.charAt(pos - 1) == '\n') {
                       if(lang<2) autoTab(editable, pos);
                        else {
                           autoTab_pascal(editable,pos);
                       }
                    }
                }
                ignore = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
       private boolean isToMatch(int pos){
            if (!autoMatch )return false;
            try
            {
                char self = editable.charAt(pos-1);
                if(getMatch(self)>0){
                    if(pos==editable.length())return true;
                    else {
                        char next = editable.charAt(pos);
                        if(next !=getMatch(self)&& isSpace(next)){return true;}
                    }
                }
            }catch (Exception e){
                return  false;
            }
            return  false;
        }
        private char getMatch(char c){
            switch (c){
                case '(':return ')';
                case '[':return ']';
                case '{':return '}';
                case '\"':return '\"';
                case '\'':return '\'';
            }
            return 0;
        }
       private boolean isSpace(char c){
            return c==' '||c=='\t'||c=='\n'|| c==']'||c== '}'||c==')'||c=='\''||c=='\"';
        }
    }
    private void autoTab_pascal(Editable editable, int pos){
        int cntTab = 0;
        String s = " "+editable.subSequence(0,pos)+" ";
        String regex = "\\W(begin)\\s";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        int start = 0;
        while (matcher.find(start)){
            cntTab++;
            start = matcher.end()-1;
        }
        regex = "\\W(end)\\s";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(s);
        start = 0;
        while (matcher.find(start)){
            cntTab--;
            start = matcher.end()-1;
        }
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<cntTab; i++) {
            builder.append(tabType);
        }
        editable.insert(pos,builder.toString());
    }

   private void  autoTab(Editable editable, int pos){
        int cntTab = 0;
        int st=0;
        for(int i=pos-2;i>=0;i--){
            if(editable.charAt(i)=='\n'){
                st = i;
                break;
            }
        }
        if(st==0)cntTab = 0;
        else {
           while (editable.toString().startsWith(tabType,cntTab*tabType.length()+st+1)){
               cntTab++;
           }
        }
        if(pos>1&&editable.charAt(pos-2)=='{'){
            cntTab++;
        }
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<cntTab; i++) {
            builder.append(tabType);
        }
        editable.insert(pos,builder.toString());
        pos = editText.getSelectionStart();
        if(pos<editable.length()&&editable.charAt(pos)=='}'){
            builder.delete(0,builder.length());
            for(int i=0;i<cntTab-1;i++)builder.append(tabType);
            editable.insert(pos,"\n"+ builder.toString());
            pos = editText.getSelectionStart();
            while (pos>0&&editable.charAt(pos--)!='\n');
            editText.setSelection(pos+1);
        }
    }
    public void destroy(){
        editText.removeTextChangedListener(watcher);
    }

}
