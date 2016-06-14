package com.example.xu.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.List;

/**
 * Created by xu on 2015/10/28.
 */
public class ShowImageActivity extends Activity{
    private GridView gridView;
    private List<String> list;
    private ChildAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showimage_activity);
        initView();
    }
    private void initView(){
        gridView = (GridView) findViewById(R.id.child_grid);
        list = getIntent().getStringArrayListExtra("data");

        adapter = new ChildAdapter(gridView,ShowImageActivity.this,list);
        gridView.setAdapter(adapter);
    }

}
