package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    String stringZaDeljenje = "Bio sam kod doktora, pa kod tate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


    }

    public void metoda(View view) {
        String[] arr = stringZaDeljenje.split("\\W+");
        int brojLoopova = arr.length;

        int numberOfTextViews = 1;
        TextView[] textViewArray = new TextView[brojLoopova+1];
        TextView textViewToSeeFirst = (TextView) findViewById(R.id.textView1);
        textViewArray[numberOfTextViews - 1] = textViewToSeeFirst;

        RelativeLayout rLayout1 = findViewById(R.id.relLay);

        for(int i=0;i<brojLoopova;i++) {
            RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lparams.addRule(RelativeLayout.RIGHT_OF, textViewArray[numberOfTextViews - 1].getId());
            lparams.addRule(RelativeLayout.ALIGN_TOP, textViewArray[numberOfTextViews - 1].getId());
            TextView text = new TextView(this);
            text.setText(arr[i] + " " + numberOfTextViews);
            text.setId(numberOfTextViews);
            text.setOnClickListener(myListener);
            textViewArray[numberOfTextViews] = text;
            rLayout1.addView(textViewArray[numberOfTextViews], lparams);
            numberOfTextViews = numberOfTextViews + 1;
        }

        //LinearLayout m_ll = (LinearLayout) findViewById(R.id.linLay);
        /*int idGenerator = 1000;
        for(int i=0;i<brojLoopova;i++) {
            System.out.println("U PETLJIII SMOOOOO");
            TextView text = new TextView(this);
            text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            text.setText(arr[i]);
            text.setId(idGenerator);
            idGenerator++;
            text.setOnClickListener(myListener);
            m_ll.addView(text);
        }*/
    }

    public View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int idTexta = view.getId();
            System.out.println("ID TEXTAAAA" + idTexta);
            TextView tv = findViewById(idTexta);
            String message = tv.getText().toString();
            Toast.makeText(TestActivity.this,
                    message, Toast.LENGTH_LONG).show();
        }
    };
}