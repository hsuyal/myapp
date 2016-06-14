package com.example.xu.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by xu on 2015/10/28.
 */
public class SDCardImageLoader {
    private LruCache<String,Bitmap> mMemoryCache;
    private int height,width;
    private GridView mGridView;
    public static SDCardImageLoader sdCardImageLoader = new SDCardImageLoader();
    public static SDCardImageLoader getInstance(){
        return  sdCardImageLoader;
    }

    private   SDCardImageLoader(){

        //获取应用程序的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //用最大内存的1/4来存储图片
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            //获取每张图片的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public void loadNativeImage(String path,ImageView iv,GridView gridView){

        height = iv.getHeight();
        width = iv.getWidth();
        this.mGridView = gridView;

        Bitmap bitmap = getBitmapFromMemory(path);

        if(bitmap == null){
            BitmapWorkTask task = new BitmapWorkTask();
            task.execute(path);
        }else {
            if(iv !=null&&bitmap!=null){
                iv.setImageBitmap(bitmap);
            }
        }
    }

    class BitmapWorkTask extends AsyncTask<String,Void,Bitmap> {
        private String imageUrl;
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mGridView.findViewWithTag(imageUrl);
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
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, ivWidth, ivHeight);

        //设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
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
}
