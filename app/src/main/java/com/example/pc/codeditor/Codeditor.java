package com.example.pc.codeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Codeditor extends AppCompatActivity implements Listener{
    ListView listView,lv;
    ArrayList<String> word;
    KeyWatch keyWatch;
    ArrayAdapter<String> adapter,adapter2;
    SharedPreferences content;
    Vector<String> vector;
    String sd="";
    int t;
    MyEditText editText;
    String ddd;
    TextView textView;
    String language;
    PerformEdit mPerformEdit;
    ClipboardManager myClipboard;
    HighLight highLight;
    Indent indent;
    int lang=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codeditor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText=(MyEditText)findViewById(R.id.text);
        editText.setListener(this);
        content = getSharedPreferences("content",
                Activity.MODE_PRIVATE);
        String contentstring = content.getString("content", "");
        editText.setText(contentstring);
        textView=(TextView)findViewById(R.id.line);
        int lang = 0;
        //mEditText = ((EditText) findViewById(R.id.editText));
        mPerformEdit = new PerformEdit(editText);
        mPerformEdit.setUndoWatcher();
        indent = new Indent(mPerformEdit,'\t',true,lang);
        indent.setIndent();
        highLight = new HighLight(mPerformEdit, new ColorPlan(Color.RED, Color.GRAY,0xFF1C00CF, Color.GREEN, Color.BLACK),lang);
        highLight.setHighLight();
        highLight.reloadInRange();
        keyWatch = new KeyWatch(mPerformEdit);
        myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int line = 1;
                String inner=editText.getText().toString();
                for (int i=0;i<inner.length();i++){
                    if(inner.charAt(i)=='\n')line++;
                }
                textView.setText("1");
                for(int i=2;i<=line;i++){
                    textView.append("\n"+i);
                }
            }
        });
        adapter2 = new ArrayAdapter<String>(this, R.layout.word, getData());
        listView = (ListView)findViewById(R.id.xxxxxxx);
        listView.setAdapter(adapter2);
        try {
            sd= Environment.getExternalStorageDirectory().getCanonicalPath()+"/CDOJ/";
        } catch (IOException e) {
            e.printStackTrace();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }


    @Override
    public  void  onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Codeditor.this);
        builder.setMessage("是否保存？");
        builder.setTitle("提示");
        builder.setPositiveButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                save();
                finish();
            }
        });
        builder.create().show();
    }
    public void save(){
        SharedPreferences content = getSharedPreferences("content", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = content.edit();
        editor.putString("content", editText.getText().toString());
        editor.commit();
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d("keyCode",""+keyCode);
//        return super.onKeyUp(keyCode, event);
//    }


    public static Vector<String> GetFileName(String fileAbsolutePath) {
        Vector<String> vecFile = new Vector<String>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        if(subFile==null)return vecFile;
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                vecFile.add(filename);
            }
        }
        return vecFile;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            try {
                FileInputStream fis = new FileInputStream(uri.toString().substring("file://".length()));
                if(uri.toString().endsWith(".c")||uri.toString().endsWith(".cpp")||uri.toString().endsWith(".h")){
                    indent.setLang(0);
                    highLight.setLang(0);
                }
                if(uri.toString().endsWith(".java")){
                    indent.setLang(1);
                    highLight.setLang(1);
                }
                if(uri.toString().endsWith(".pascal")||uri.toString().endsWith(".p")||uri.toString().endsWith(".pas")){
                    indent.setLang(2);
                    highLight.setLang(2);
                }
                highLight.reloadInRange();
                Log.d("language:",""+lang);
                byte[] data0 = new byte[102400];
                int length = fis.read(data0);
                if(length!=-1){
                    editText.setText(new String(data0, 0, length));
                    editText.setSelection(length);
                }
                else{
                    editText.setText("");
                    editText.setSelection(0);
                }
                mPerformEdit.clearHistory();
                replay();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_codeditor, menu);
        return true;
    }

    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        String strFilePath = filePath + fileName;
        String strContent = strcontent;
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream io = new FileOutputStream(file);
            io.write(strContent.getBytes());
            io.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            save();
            dialog(null);
            return true;
        }
        if(id == R.id.chance_language){
            dialoglanguage(null);
        }
        if (id == R.id.action_search) {
            vector = GetFileName(sd);
            dialog2(editText);
        }
        if (id == R.id.action_up) {
            mPerformEdit.undo();
            return true;
        }
        if (id == R.id.action_down) {
            mPerformEdit.redo();
            return true;
        }
        if (id == R.id.action_run) {
            if(listView.getVisibility()== View.INVISIBLE)
                listView.setVisibility(View.VISIBLE);
            else listView.setVisibility(View.INVISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public AlertDialog language0;
    public void dialoglanguage(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Codeditor.this);
        LinearLayout chooselanguage = (LinearLayout)findViewById(R.id.choose_language);
        builder.setView(getLayoutInflater().inflate(R.layout.choose_language,null));
        builder.setTitle("请选择语言");
        language0=builder.show();
    }
    public void c(View view){
        indent.setLang(0);
        highLight.setLang(0);
        language0.dismiss();
    }
    public void java(View view){
        indent.setLang(1);
        highLight.setLang(1);
        language0.dismiss();
    }
    public void pascal(View view){
        indent.setLang(2);
        highLight.setLang(2);
        language0.dismiss();
    }
    public AlertDialog dialog;
    public void dialog2(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Codeditor.this);
        adapter = new ArrayAdapter<String>(this, R.layout.word, getData2());
        listView = new ListView(this);
        if (adapter != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    t=i;
                    String filePath = sd;
                    String fileName = vector.get(t);
                    if(fileName.endsWith(".c")||fileName.endsWith(".cpp")||fileName.endsWith(".h")){
                        indent.setLang(0);
                        highLight.setLang(0);
                    }
                    if(fileName.endsWith(".java")){
                        indent.setLang(1);
                        highLight.setLang(1);
                    }
                    if(fileName.endsWith(".pascal")||fileName.endsWith(".p")||fileName.endsWith(".pas")){
                        indent.setLang(2);
                        highLight.setLang(2);
                    }
                    highLight.reloadInRange();
                    try {
                        FileInputStream fis=new FileInputStream(filePath+fileName);
                        byte[] data0=new byte[102400];
                        int length= 0;
                        length = fis.read(data0);
                        if(length!=-1){
                            editText.setText(new String(data0,0,length));
                            editText.setSelection(length);
                        }
                        else{
                            editText.setText("");
                            editText.setSelection(0);
                        }
                        mPerformEdit.clearHistory();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    replay();
                }
            });
            builder.setView(listView);
        }
        builder.setTitle("请选择文件名");
        dialog=builder.show();
    }

    List<String> data;
    ArrayList<String> data2;
    public List<String> getData2() {
        data = new ArrayList<String>();
        Log.d("v:",vector.size()+"");
        for (int i = 0; i < vector.size(); i++) {
            data.add(vector.get(i));
        }
        return data;
    }
    public List<String> getData() {
        data2 = new ArrayList<String>();
        return data2;
    }
    public void add(){
        data2.clear();
        for(int i=0;i<word.size();i++){
            data2.add(word.get(i));
        }
        adapter2.notifyDataSetChanged();
    }

    public void dialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Codeditor.this);
        final EditText editText0 = new EditText(this);
        editText0.setHint("文件名");
        if(ddd!=null)editText0.setText(ddd);
        TextView text = new TextView(this);
        text.setText("文件保存在CDOJ目录下");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.addView(editText0);
        linearLayout.addView(text);
        builder.setView(linearLayout);
        builder.setTitle("请输入文件名");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String filePath = sd;
                String fileName = editText0.getText().toString();
                ddd=fileName;
                writeTxtToFile(editText.getText().toString(), filePath, fileName);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void undo() {
        keyWatch.undo();
    }

    @Override
    public void redo() {
        keyWatch.redo();
    }

    @Override
    public void copy() {
        keyWatch.copyText(editText,myClipboard);
    }

    @Override
    public void paste() {
        keyWatch.pasteText(editText,myClipboard,Codeditor.this);
    }

    @Override
    public void selectAll() {
        keyWatch.SelectAll(editText);
    }

    @Override
    public void cut() {
        keyWatch.cutText(editText,myClipboard);
    }
    public void replay(){
        mPerformEdit.destroy();
        highLight.destroy();
        indent.destroy();
        mPerformEdit = new PerformEdit(editText);
        mPerformEdit.setUndoWatcher();
        indent = new Indent(mPerformEdit,'\t',true,lang);
        indent.setIndent();
        highLight = new HighLight(mPerformEdit, new ColorPlan(Color.RED, Color.GRAY,0xFF1C00CF, Color.GREEN, Color.BLACK),lang);
        highLight.setHighLight();
        highLight.reloadInRange();
    }
}
