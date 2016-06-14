package com.example.xu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xu.myapplication.utils.NativeImageLoader;

import java.util.List;

/**
 * Created by xu on 2015/10/28.
 */
public class ChildAdapter extends BaseAdapter{
    private GridView gridView;
    private Context context;
    private List<String> list;
    private LayoutInflater mInflater;
   private  SDCardImageLoader sdCardImageLoader;

    public ChildAdapter(GridView mGridView, Context context, List<String> list){
        this.gridView = mGridView;
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);

    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View_Holder holder;
        String path = list.get(position);

        if(convertView ==null){
            holder = new View_Holder();
            convertView = mInflater.inflate(R.layout.grid_child,null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.child_image);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.child_checkbox);

            convertView.setTag(holder);
        }else{
            holder = (View_Holder) convertView.getTag();
        }
        holder.checkBox.setChecked(true);
        holder.imageView.setTag(path);
        Bitmap bmp = NativeImageLoader.getInstance().loadNativeImage(path, holder.imageView, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) gridView.findViewWithTag(path);
                if(bitmap != null && mImageView != null){
                    mImageView.setImageBitmap(bitmap);
                }
            }
        });
        if(bmp !=null){
            holder.imageView.setImageBitmap(bmp);
        }else{
            holder.imageView.setImageResource(R.drawable.friends_sends_pictures_no);
        }
        return convertView;
    }
    class View_Holder{
        public ImageView imageView;
        public CheckBox checkBox;
    }
}
