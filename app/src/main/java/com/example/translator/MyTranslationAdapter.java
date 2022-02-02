package com.example.translator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyTranslationAdapter extends ArrayAdapter<MyTranslation> {

    ArrayList<MyTranslation> questionEdits;
    Context mContext;

    public MyTranslationAdapter(Context context, ArrayList<MyTranslation> questionEdits) {
        super(context, 0, questionEdits);
        this.questionEdits = questionEdits;
        this.mContext = context;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_translation_item, parent, false);
        }
        MyTranslation currentMyTranslation = getItem(position);
        TextView dateTextView = convertView.findViewById(R.id.date);
        dateTextView.setText(currentMyTranslation.getDate());

        TextView oriTextView = convertView.findViewById(R.id.originalText);
        oriTextView.setText(currentMyTranslation.getOriginal_text());

        TextView tranTextView = convertView.findViewById(R.id.translationText);
        tranTextView.setText(currentMyTranslation.getTranslated_text());

        return convertView;
    }
}
