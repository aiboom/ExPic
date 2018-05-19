package top.aiboom.lib.expic.builder;

import android.graphics.Bitmap;
import android.view.View;

public interface SourceBuilder {
    PropertyBuilder view(View view);
    PropertyBuilder bitmap(Bitmap bitmap);
}
