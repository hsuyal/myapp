package com.example.xu.myapplication;

/**
 * GridView�е�ÿ��item����ݶ���
 * Created by xu on 2015/10/28.
 */
public class ImageBean {

    /**
     * 文件夹中第一张图片路径
     */
    private String topImagePath;
    /**
     * 文件夹名称
     */
    private String folderName;
    /**
     * 文件夹的图片数
     */
    private int imageCounts;

    public String getTopImagePath() {
        return topImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getImageCounts() {
        return imageCounts;
    }

    public void setImageCounts(int imageCounts) {
        this.imageCounts = imageCounts;
    }


}
