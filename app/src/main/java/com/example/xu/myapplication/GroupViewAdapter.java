package com.example.xu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xu.myapplication.utils.NativeImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xu on 2015/10/28.
 */
public class GroupViewAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private List<ImageBean> list;
    private GridView mGridView;
    private LayoutInflater mInflater;
    private Context context;
    private int height,width;
    SDCardImageLoader sdCardImageLoader;
    /**
     * 缓存技术核心类，用于缓存所有解析好的图片，在程序达到设定值时会最近最少使用的图片移除
     */
    private LruCache<String,Bitmap> mMemoryCache;

    //使用线程解析图片，否则会出现OOM，而且在getView中使用BitmapFactory去解析图片，擦偶走过多会卡
    private Set<BitmapWorkTask> sets = new HashSet<>();

    public GroupViewAdapter(GridView mGridView, Context context, List<ImageBean> list) {
        this.mGridView = mGridView;
        this.list = list;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        //获得程序可应用的最大内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024;
        //设置图片缓存大小为应用程序了缓存的1/8
        int cacheSize =maxMemory/8;
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };
        mGridView.setOnScrollListener(this);
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
        ImageBean imageBean = list.get(position);
        String path = imageBean.getTopImagePath();
        Log.i("TTT", "----->" + imageBean.getImageCounts());

        if(convertView ==null){
            holder = new View_Holder();
            convertView = mInflater.inflate(R.layout.grid_group,null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.group_img);
            holder.textViewCounts = (TextView) convertView.findViewById(R.id.group_count);
            holder.textViewTitles = (TextView) convertView.findViewById(R.id.group_title);

            convertView.setTag(holder);
        }else{
            holder = (View_Holder) convertView.getTag();
        }
        holder.textViewTitles.setText(imageBean.getFolderName());
        holder.textViewCounts.setText(imageBean.getImageCounts() + "");

         holder.imageView.setTag(path);
       Bitmap bmp =  NativeImageLoader.getInstance().loadNativeImage(path, holder.imageView, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoader(Bitmap bitmap, String path) {
                ImageView mImageView = (ImageView) mGridView.findViewWithTag(path);
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
//        SDCardImageLoader.getInstance().loadNativeImage(path,holder.imageView,mGridView);
//        sdCardImageLoader = new SDCardImageLoader(path,holder.imageView,mGridView);
//        sdCardImageLoader.loadNativeImage(path,holder.imageView);
         //loadBitmaps(holder.imageView, path);
        return convertView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    class View_Holder{
        public ImageView imageView;
        public TextView textViewTitles;
        public TextView textViewCounts;
    }


    /**
     * 根据View(主要是ImageView)的宽和高来计算Bitmap缩放比例。默认不缩放
     * @param options
     * @param viewWidth
     * @param viewHeight
     */
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight){
        int inSampleSize = 1;
        if(viewWidth == 0 || viewWidth == 0){
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }


    /**
     * 根据imageView的长宽获取缩略图
     * @param path 路径
     * @param ivWidth
     * @param ivHeight
     * @return
     */
    private Bitmap decodeThumbBitmapForFile(String path,int ivWidth,int ivHeight){

        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true，表示解析BItmap对象，但是该对象不占用内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = computeScale(options,ivWidth,ivHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    /**
     * 从getView中加载图片的话
     * @param iv
     * @param path
     */
    private void loadBitmaps(ImageView iv,String path){
        Bitmap bitmap = getBitmapFromMemory(path);
        height = iv.getHeight();
        width = iv.getWidth();
        if(bitmap == null){
            BitmapWorkTask task = new BitmapWorkTask();
            sets.add(task);
            task.execute(path);
        }else {
            if(iv !=null&&bitmap!=null){
                iv.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * 从LRuCache获取一张图片
     * @param path
     * @return
     */
    private Bitmap getBitmapFromMemory(String path){
        return mMemoryCache.get(path);
    }

    /**
     * 将解析好的图片添加到缓存当中
     * @param path LruCache的键，这里传入地址
     * @param bitmap 值。这里是BitmapFactory解析好的图片
     */
    private void addBitmapToMemoryCache(String path,Bitmap bitmap){
        if(getBitmapFromMemory(path) == null){
            mMemoryCache.put(path, bitmap);
        }
    }



    class BitmapWorkTask extends AsyncTask<String,Void,Bitmap>{
        private String imageUrl;
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mGridView.findViewWithTag(imageUrl);
            notifyDataSetChanged();
            if(imageView != null){
                imageView.setImageBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            if(imageUrl !=null) {
                Bitmap bitmap = decodeThumbBitmapForFile(imageUrl,width,height);
                if(bitmap !=null){
                    addBitmapToMemoryCache(imageUrl,bitmap);
                }
                return bitmap;
            }
            return null;
        }

        public void cancelAllTasks(){
            if(sets != null){
                for (BitmapWorkTask task:sets){
                    task.cancel(false);
                }
            }
        }
    }
}
