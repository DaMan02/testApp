package com.innovvscript.visitormanagement;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class CustomDialog {

    private Context context;
    private AlertDialog dialog;
    private TextView title, message, positiveBtn, negativeBtn;
    private View view;

    public CustomDialog(@NonNull Context context) {
        this.context = context;

        buildCustomDialog();
    }

    private void buildCustomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.custom_dialog,null);
            title = view.findViewById(R.id.alert_title);
            message = view.findViewById(R.id.alert_message);
            positiveBtn = view.findViewById(R.id.alert_positive_btn);
            negativeBtn = view.findViewById(R.id.alert_negative_btn);
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        negativeBtn.setVisibility(View.INVISIBLE);
    }

    public void show() {
        dialog.show();
    }

    public void hide(){
        dialog.dismiss();
    }

    public void setTitle(@Nullable CharSequence titleText){

        title.setText(titleText);
    }



    public void setNegativeBtnNull(CharSequence text){

        negativeBtn.setVisibility(View.VISIBLE);
        negativeBtn.setText(text.toString().toUpperCase());
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

    }


    public void setPositiveBtnNull(CharSequence text){

        positiveBtn.setText(text.toString().toUpperCase());
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

    }

    public void setMessage(CharSequence msg){

        message.setText(msg);
    }

}
