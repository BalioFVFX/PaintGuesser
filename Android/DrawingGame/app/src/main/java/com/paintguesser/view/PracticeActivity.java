package com.paintguesser.view;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.paintguesser.databinding.ActivityPracticeBinding;

public class PracticeActivity extends AppCompatActivity {

    private ActivityPracticeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.drawingView.setPlayable(true);

        binding.ivColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog pickerDialog = new ColorPickerDialog(v.getContext());
                pickerDialog.setListener(color -> {
                    binding.drawingView.setPaintColor(color);
                    binding.ivColorPicker.setImageTintList(ColorStateList.valueOf(color));
                });
                pickerDialog.show();
            }
        });

        binding.btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawingView.undo();
            }
        });
    }
}
