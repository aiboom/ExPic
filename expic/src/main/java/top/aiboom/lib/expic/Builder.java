package top.aiboom.lib.expic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import top.aiboom.lib.expic.builder.ExportBuilder;
import top.aiboom.lib.expic.builder.PropertyBuilder;
import top.aiboom.lib.expic.builder.SourceBuilder;

//导出逻辑交给Builder实现
public class Builder implements PropertyBuilder, SourceBuilder {

    private Operator operator; //过滤操作

    private String exportType; //导出的类型
    private Context context; //上下文

    private int width; //宽
    private int height; //高

    private int quality; //导出质量
    private Bitmap bitmap; //需要操作的bitmap对象
    private String path; //导出文件路径
    private String name;

    Builder(Context context){
        this.context = context;
    }

    //指定一个View
    @Override
    public PropertyBuilder view(View view) {
        if (!view.isDrawingCacheEnabled()) {
            view.setDrawingCacheEnabled(true);
            this.bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
        } else {
            this.bitmap = Bitmap.createBitmap(view.getDrawingCache());
        }
        if (operator == null)
            operator = new Operator();
        return operator;
    }

    //直接指定一个Bitmap
    @Override
    public PropertyBuilder bitmap(Bitmap bitmap) {
        this.bitmap = Bitmap.createBitmap(bitmap);
        if (operator == null)
            operator = new Operator();
        return operator;
    }

    //指定导出类型
    @Override
    public PropertyBuilder type(String exportType) {
        this.exportType = exportType;
        return this;
    }

    @Override
    public PropertyBuilder path(String path) {
        this.path = path;
        return this;
    }

    //指定导出文件的宽和高
    @Override
    public PropertyBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public PropertyBuilder quality(int quality) {
        this.quality = quality;
        return this;
    }

    @Override
    public PropertyBuilder name(String name) {
        this.name = name;
        return this;
    }

    //用来隔离操作类型的类
    private class Operator implements PropertyBuilder, ExportBuilder{

        //指定导出类型
        public Operator type(String exportType) {
            Builder.this.type(exportType);
            return this;
        }

        @Override
        public PropertyBuilder path(String path) {
            Builder.this.path(path);
            return this;
        }

        //指定导出文件的宽和高
        public Operator size(int width, int height) {
            Builder.this.size(width, height);
            return this;
        }

        @Override
        public PropertyBuilder quality(int quality) {
            Builder.this.quality(quality);
            return this;
        }

        @Override
        public PropertyBuilder name(String name) {
            Builder.this.name(name);
            return this;
        }

        @Override
        public String export() {
            return saveBitmap(bitmap);
        }
    }


    //提供的Bitmap是pictureBean的Bitmap，也就是不带背景的笔记Bitmap
    private String saveBitmap(Bitmap bitmap){

        File parent = new File(path);
        if (parent.exists()||parent.mkdir())
            Log.e("HGL", "saveBitmap: "+parent.getAbsolutePath());


        StringBuilder buffer = new StringBuilder(parent.getAbsolutePath() +File.separator + name);
        String file = null;

        if (exportType.equals(ExportUtil.TYPE_JPG)) {
            file = buffer.append(".jpg").toString();
            savePicFile(drawWithBackground(bitmap), file, Bitmap.CompressFormat.JPEG);
        }

        if (exportType.equals(ExportUtil.TYPE_PNG)) {
            file = buffer.append(".png").toString();
            savePicFile(bitmap, file, Bitmap.CompressFormat.PNG);
        }

        if (exportType.equals(ExportUtil.TYPE_PDF)) {
            file = buffer.append(".pdf").toString();
            savePdfFile(bitmap, file);
        }

        if (bitmap != null)
            bitmap.recycle();

        return file;
    }

    private String saveText(String content) {

        File parent = new File(Environment.getExternalStorageDirectory() + File.separator +"maxeye");
        if (parent.exists()||parent.mkdir())
            Log.e("HGL", "saveBitmap: "+parent.getAbsolutePath());


        StringBuilder buffer = new StringBuilder(parent.getAbsolutePath() +File.separator + pictureBean.getPictureTime());
        String file = null;

        if (exportType.equals(TYPE_TXT)) {
            file = buffer.append(".txt").toString();
            saveTxtFile(content, file);
        }

        return file;

    }

    //获得背景Bitmap
    private Bitmap drawWithBackground(Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.outWidth = bitmap.getWidth();
        options.outHeight = bitmap.getHeight();
        options.inMutable = true;

        if (DigitizerApplication.getInstance().isUseWallpaper()) {
            Bitmap background = BitmapFactory.decodeResource(context.getResources(), WallpaperUtil.Wallpaper.bigWallpaper[pictureBean.getWallpaperId()], options);
            Bitmap bits = Bitmap.createScaledBitmap(background, bitmap.getWidth(), bitmap.getHeight(), false);
            Canvas canvas = new Canvas(bits);
            canvas.drawBitmap(bitmap, 0, 0, null);
            canvas.save();
            return bits;
        } else {

            Bitmap background = bitmap.copy(Bitmap.Config.RGB_565, true);
            Canvas canvas = new Canvas(background);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            canvas.save();
            return background;
        }
    }

    //保存txt文本格式文件
    private void saveTxtFile(String content, String path) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //保存图片格式文件
    private void savePicFile(Bitmap bitmap, String path, Bitmap.CompressFormat format){
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(new File(path));
            if (bitmap.compress(format, 100, outputStream))
                outputStream.flush();
                outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap!=null && !bitmap.isRecycled())
                bitmap.recycle();
        }
    }

    //保存PDF格式文件
    private void savePdfFile(Bitmap bitmap, String path) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(drawWithBackground(bitmap), 0, 0, null); //保存背景
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.save();
        document.finishPage(page);

        try {
            document.writeTo(new FileOutputStream(path, false));
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.close();
    }

}
