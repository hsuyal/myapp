package com.example.xu.myapplication.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地图片加载器
 * Created by xu on 2015/10/29.
 */
public class NativeImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private static NativeImageLoader mInstance = new NativeImageLoader();
    private ExecutorService exec = Executors.newFixedThreadPool(1);

    private NativeImageLoader(){
        //获取应用程序的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        //用最大内存的1/4来存储图片
        final int cacheSize = maxMemory / 4;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

            //获取每张图片的大小
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }


    /**
     * 通过此方法来获取NativeImageLoader的实例
     * @return
     */
    public static NativeImageLoader getInstance(){
        return mInstance;
    }

    /**
     * 加载本地图片，对图片不进行裁剪
     * @param path
     * @param mCallBack
     * @return
     */
    public Bitmap loadNativeImage(final String path, final ImageView iv, final NativeImageCallBack mCallBack){

        final Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mCallBack.onImageLoader((Bitmap) msg.obj,path);
            }
        };
        if(bitmap == null){
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap mBitmap = decodeThumbBitmapForFile(path,iv.getWidth(),iv.getHeight());
                    Message msg = new Message();
                    msg.obj = mBitmap;
                    handler.sendMessage(msg);
                    addBitmapToMemoryCache(path,bitmap);
                }
            });
        }
        return bitmap;
    }

    /**
     * 往内存缓存中添加Bitmap
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 根据key来获取内存中的图片
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 根据View(主要是ImageView)的宽和高来获取图片的缩略图
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

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
        if(viewWidth == 0 || viewHeight == 0){
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }

    /**
     * 加载本地图片的回调接口
     *
     * @author xiaanming
     *
     */
    public interface NativeImageCallBack{
        /**
         * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
         * @param bitmap
         * @param path
         */
        public void onImageLoader(Bitmap bitmap, String path);
    }
}
