package com.paintguesser.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.paintguesser.R;

public class ColorPickerDialog extends AlertDialog {

    public interface Listener {
        void onColorPick(int color);
    }

    private Listener listener;

    public ColorPickerDialog(@NonNull Context context) {
        super(context);
    }

    protected ColorPickerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ColorPickerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FrameLayout container = (FrameLayout) getLayoutInflater().inflate(
                R.layout.dialog_color_picker, null, false);

        final GridLayout gridLayout = container.findViewById(R.id.grid_layout);

        final Resources resources = getContext().getResources();

        final int colorWidth = resources.getDimensionPixelOffset(R.dimen.dialog_color_picker_color_width);
        final int colorHeight = resources.getDimensionPixelOffset(R.dimen.dialog_color_picker_color_height);
        final int colorMargin = resources.getDimensionPixelOffset(R.dimen.dialog_color_picker_color_margin);

        final Drawable drawable = ResourcesCompat.getDrawable(
                getContext().getResources(), R.drawable.drawable_circle, null);

        final int[] colors = resources.getIntArray(R.array.color_picker_colors);

        for (int color : colors) {
            final View view = new View(getContext());
            final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = colorWidth;
            layoutParams.height = colorHeight;

            layoutParams.leftMargin = colorMargin;
            layoutParams.topMargin = colorMargin;
            layoutParams.bottomMargin = colorMargin;
            layoutParams.rightMargin = colorMargin;

            drawable.setTint(color);
            view.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.drawable_circle, null));
            view.setLayoutParams(layoutParams);

            view.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onColorPick(color);
                    dismiss();
                }
            });

            gridLayout.addView(view);
        }

        setContentView(container);
    }
}
