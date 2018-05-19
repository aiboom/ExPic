package top.aiboom.lib.expic;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.maxeye.digitizer.DigitizerApplication;
import com.maxeye.digitizer.entity.local.PictureBean;
import com.maxeye.digitizer.ui.base.BaseActivity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Langsky on 2018/5/19.
 * 1. 给一个View，导出View呈现的图像
 * 2.
 */

public class ExportUtil {

    public static final String TYPE_PNG = "image/png"; //导出格式为PNG
    public static final String TYPE_JPG = "image/jpeg"; //导出格式为JPG
    public static final String TYPE_WBP = " image/webp"; //导出格式为WebP
    public static final String TYPE_PDF = "application/pdf"; //导出模式为PDF

    private ExportUtil(){}

    public static ExportUtil getInstance() {
        return Handler.instance;
    }

    private static class Handler{
        private static final ExportUtil instance = new ExportUtil();
    }

    public Builder builder(Context context){
        return new Builder(context);
    }

    //用于后台导出文件的类
    public static class ExportTask extends AsyncTask<PictureBean, Void, List<String>> {

        private final String type;

        private WeakReference<BaseActivity> reference;

        private String content;

        public ExportTask(String exportType, BaseActivity activity) {
            this.type = exportType;
            this.reference = new WeakReference<>(activity);
        }

        public ExportTask(String exportType, String content, BaseActivity activity) {
            this(exportType, activity);
            this.content = content;
        }

        @Override
        protected List<String> doInBackground(PictureBean... pictureBeans) {
            long time = System.currentTimeMillis();
            List<String> result = new ArrayList<>();
            for (PictureBean pictureBean: pictureBeans){
                result.add(ExportUtil.getInstance().builder(reference.get()).picture(pictureBean).type(type).word(content).export());
            }
            time = System.currentTimeMillis() - time;

            if (time >= 500)
                return result;

            try {
                Thread.sleep(500 - time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            if (!type.equals(TYPE_TXT))
                reference.get().dismissExportDialog();
            reference.get().progress.show();
        }

        @Override
        protected void onPostExecute(List<String> results) {
            reference.get().progress.dismiss();
            share(results);
        }

        private void share(List<String> stringList) {
            ArrayList<Uri> uris = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                for (String path: stringList) {
                    Uri u = FileProvider.getUriForFile(reference.get(), "com.maxeye.digitizer.fileprovider", new File(path));
                    uris.add(u);
                }
            }else {
                for (String path: stringList) {
                    Uri u = Uri.fromFile(new File(path));
                    uris.add(u);
                }
            }

            Intent send = new Intent(Intent.ACTION_SEND_MULTIPLE);
            send.setType(type);
            send.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            Intent view = new Intent(reference.get(), reference.get().getClass());
            view.setAction("EXPORT");
            view.setDataAndType(uris.get(0), type);
            view.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INTENT, send);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{view});
            reference.get().startActivity(chooser);
        }
    }


}

