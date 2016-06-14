package com.example.xu.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {


    private HashMap<String, List<String>> mGroupMap = new HashMap<>();
    private List<ImageBean> list = new ArrayList<>();
    private final static  int SCAN_OK = 1;
    private ProgressDialog mProgressDialog;
    private GroupViewAdapter adapter;
    private GridView gridView;

   private Handler handler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what){
               case SCAN_OK:
                   mProgressDialog.dismiss();

                   adapter = new GroupViewAdapter(gridView,MainActivity.this,subGroupOfImage(mGroupMap));
                   gridView.setAdapter(adapter);
                   gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                       @Override
                       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                           List<String> childList = mGroupMap.get(list.get(position).getFolderName());
                           Intent intent = new Intent(MainActivity.this,ShowImageActivity.class);
                           intent.putStringArrayListExtra("data", (ArrayList<String>) childList);
                           startActivity(intent);
                       }
                   });

                   break;
           }
       }
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        getImages();

    }
    private void initViews(){
        gridView = (GridView) findViewById(R.id.main_grid);


    }

    /**
     * 使用ContentProvider扫描手机的图片，这个方法运行在子线程中，并只扫描jpg和png的图像
     */
    private void getImages(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"暂时没有存储数据",Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this,null,"正在加载");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = MainActivity.this.getContentResolver();
                Log.i("TTT",Thread.currentThread()+"--->"+mImageUri);

                //只扫描jpg和png的图像
                Cursor cursor = contentResolver.query(mImageUri,null,MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);
                while(cursor.moveToNext()){
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获取该图片的父路径名
                    String parentName = new File(path).getParentFile().getName();
                    if(!mGroupMap.containsKey(parentName)){
                        List<String> childList = new ArrayList<String>();
                        childList.add(path);
                        mGroupMap.put(parentName,childList);
                    }else{
                        mGroupMap.get(parentName).add(path);
                    }
                }
                cursor.close();
                handler.sendEmptyMessage(SCAN_OK);
            }
        }).start();

    }

    private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGroupMap ){
        if(mGroupMap.size() == 0){
            return null;
        }
        Iterator<Map.Entry<String,List<String>>> iterator = mGroupMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,List<String>> entry = iterator.next();
            ImageBean imageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            imageBean.setFolderName(key);
            imageBean.setImageCounts(value.size());
            imageBean.setTopImagePath(value.get(0));

            list.add(imageBean);
        }
        return  list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
