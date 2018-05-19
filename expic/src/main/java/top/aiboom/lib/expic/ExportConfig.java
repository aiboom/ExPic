package top.aiboom.lib.expic;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Langsky on 2018/5/19.
 * 1. 对导出功能进行默认配置
 * 2. 默认导出路径，默认导出质量，默认导出类型等
 */
public class ExportConfig {

    public static final String DEFAULT_PATH = "DEFAULT_PATH";
    public static final String DEFAULT_FORMAT = "DEFAULT_FORMAT";
    public static final String DEFAULT_QUALITY = "DEFAULT_QUALITY";

    private String defaultPath;
    private int defaultQuality;
    private String defaultFormat;

    public static class Builder {

        private Builder() { }

        private String path;
        private int quality;
        private String format;

        public Builder defaultPath(String path) {
            this.path = path;
            return this;
        }

        public Builder defaultFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder defaultQuality(int quality) {
            this.quality = quality;
            return this;
        }

        public ExportConfig build() {
            ExportConfig config = new ExportConfig();
            config.defaultPath = path;
            config.defaultFormat = format;
            config.defaultQuality = quality;
            return config;
        }
    }

    public void config(Context context){
        SharedPreferences preferences = context.getSharedPreferences("export", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DEFAULT_PATH, defaultPath);
        editor.putString(DEFAULT_FORMAT, defaultFormat);
        editor.putInt(DEFAULT_QUALITY, defaultQuality);
        editor.apply();
    }

}
