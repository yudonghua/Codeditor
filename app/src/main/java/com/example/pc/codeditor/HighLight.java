package com.example.pc.codeditor;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @ method : new HighLight(EditText editText,int colorOfKeywords,int Language ).setHighLight()
 * language: 0-c/c++ 1-java 2-pascal
 *
  public void  test(){
 EditText editText =(EditText)findViewById(R.id.editText);
 new Indent(editText,'\t',true).setIndent();
 new HighLight(editText,Color.RED,0).setHighLight();
 }
 */
public class HighLight {
    private final String[] keys_c ={"do","if","return","typedef","auto","double","inline","short","typeid","bool","dynamic_cast","int","signed","typename","break","else","long","sizeof","union","case","enum","mutable","static","unsigned","catch","explicit","namespace","static_cast","using","char","export","new","struct","virtual","class","extern","operator","switch","void","const","false","private","template","volatile","const_cast","float","protected","this","wchar_t","continue","for","public","throw","while","default","friend","register","true","delete","goto","reinterpret_cast","try"

    };
    private final String[] keys_p ={
            "absolute","abstract","and","array","as","asm","assembler","at","automated","begin","case","cdecl","class","const","constructor","contains","default","destructor","dispid","dispinterface","div","do","downto","dynamic","else","end","except","export","exports","external","far","file","finalization","finally","for","forward","function","goto","if","implementation","implements","in","index","inherited","initialization","inline","interface","is","label","library","message","mod","name","near","nil","nodefault","not","object","of","on","or","out","overload","packed","pascal","private","procedure","program","property","public","published","raise","read","readonly","record","register","reintroduce","repeat","requires","resident","resourcestring","safecall","set","shl","shr","stdcall","stored","string","then","to","try","type","unit","until","uses","var","virtual","while","with","write","writeonly","xor"
    };
    private final String[] keys_j={"abstract","boolean","break","byte","case","catch","char","class","continue","default","do","double","else","extends","false","final","finally","float","for","if","implements","import","instanceof","int","interface","long","native","new","null","package","private","protected","public","return","short","static","super","switch","synchronized","this","throw","throws","transient","try","true","void","volatile","while"};
    private EditText editText;
    private PerformEdit edit;
    private ColorPlan color;
    private int normalColor;
    private String[]  keys;
    private int lang;
    private SpannableStringBuilder ssb;

    final String regex_key = "\\W(\\w+)\\W";
    final String number0="\\b[0-9]+\\b";
    final String char0="'(.*?)('|$)";
    final String string0="\\\"(.*?)(\\\"|$)";
    private boolean refreshAll = false;
    public HighLight(EditText editText, ColorPlan color, int language, PerformEdit edit){
        this.editText = editText;
        this.color = color;
        this.edit = edit;
        this.normalColor = editText.getCurrentTextColor();
        setLang(language);
    }
    public HighLight(PerformEdit edit,ColorPlan color, int language){
        this.editText = edit.editText;
        this.edit = edit;
        this.color = color;
        this.normalColor = editText.getCurrentTextColor();
        setLang(language);
    }
    public void setRefreshAll(){
        this.refreshAll = true;
    }
    private HighLightWatcher watcher;
    public  void setHighLight(){
        watcher = new HighLightWatcher();
        editText.addTextChangedListener(watcher);
    }
    public void setLang(int language){
        if(language == 0){keys = keys_c; }
        else if(language==1){keys = keys_j; ;}
        else if(language==2){keys = keys_p; ;}
        this.lang = language;
    }

    public class  HighLightWatcher implements TextWatcher {
        boolean ignoreChange = false;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void afterTextChanged(Editable s){

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)  {
            if(!ignoreChange&&s.length()>0){
                ignoreChange = true;
                edit.setIgnore(true);
                if(refreshAll||s.length()>1)
                    reloadInRange();
                else {
                    int st=-1,end=-1;
                    int pos = editText.getSelectionStart();
                    for(int i=pos;i<s.length();i++){
                        if(s.charAt(i)=='\n'){
                            end = i+1;break;
                        }
                    }
                    if(end<0)end = s.length();
                    for(int i=pos-1;i>=0;i--){
                        if(s.charAt(i)=='\n'){
                            st = i+1;
                            break;
                        }
                    }
                    if(st<0)st = 0;
                    for (int i=st-2;i>=0;i--){
                        if(s.charAt(i)=='\n'||i==0){
                            st = i;
                            break;
                        }
                    }
                    reloadInRange(st,end);
                }
                ignoreChange = false;
                edit.setIgnore(false);
                refreshAll = false;
            }
        }
    }
    void lightKeys(String string, int left, int right, int color){
        boolean high = false;
        for(int i=0;i<keys.length;i++){
            if(keys[i].equals(string)){
                ssb.setSpan(new ForegroundColorSpan(color),left,right, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                high = true;
                break;
            }
        }
        if(!high) {
            ssb.setSpan(new ForegroundColorSpan(normalColor),left,right, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    public void dealKeys(String str){
        Pattern pattern = Pattern.compile(regex_key);
        Matcher matcher = pattern.matcher(str);
        int start = 0;
        while (matcher.find(start)){
            lightKeys(matcher.group(1),matcher.start(1)-1,matcher.end(1)-1,color.grammarColor);
            start = matcher.end()-1;
        }
    }
    private void darkenComment(String str){
        int index,endIndex = 0;
        while ((index =str.indexOf("//",endIndex))!=-1){
            endIndex = str.indexOf("\n",index+2)+1;
            if(endIndex == 0){endIndex = str.length();}
            if(index<endIndex&&endIndex<=str.length())
                 ssb.setSpan(new ForegroundColorSpan(color.commentColor),index,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    private void darkenMultiComment(int lang){
        String str = editText.getText().toString();
        int index,endIndex = 0;
        if(lang<2)
            while ((index = str.indexOf("/*",endIndex))!=-1){
                endIndex = str.indexOf("*/",index+2)+2;
                if(endIndex == 1){endIndex = str.length();}
                if(!refreshAll)ssb = new SpannableStringBuilder(str);
                ssb .setSpan(new ForegroundColorSpan(color.commentColor),index,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        else
            while ((index = str.indexOf("{",endIndex))!=-1){
                endIndex = str.indexOf("}",index+1)+1;
                if(endIndex == 0){endIndex = str.length();}
                if(!refreshAll)ssb = new SpannableStringBuilder(str);
                ssb .setSpan(new ForegroundColorSpan(color.commentColor),index,endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
    }
    private void light(int color, String regex, String str ){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            ssb.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    public void reloadInRange(){
        String str0 = editText.getText().toString();
         String str =" "+str0+"\n";
        ssb = new SpannableStringBuilder(str0);
        dealKeys(str);
        light(color.numberColor,number0,str0);
        light(color.charColor,char0 ,str0);
        light(color.strColor,string0,str0);
        darkenComment(str0);
        darkenMultiComment(lang);
        editText.getText().replace(0,editText.length(),ssb);
    }
    public void reloadInRange(int st,int end){
        String str0 = editText.getText().subSequence(st,end).toString();
        String str =" "+str0+"\n";
        ssb = new SpannableStringBuilder(str0);
        dealKeys(str);
        light(color.numberColor,number0,str0);
        light(color.charColor,char0,str0);
        light(color.strColor,string0,str0);
        darkenComment(str0);
        editText.getText().replace(st,end,ssb);
        darkenMultiComment(lang);
        editText.getText().replace(0,editText.length(),ssb);
    }
    public void destroy(){
        editText.removeTextChangedListener(watcher);
    }
}
