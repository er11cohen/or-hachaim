package com.eran.orhachaim;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;

public class ParashotActivity extends Activity {

    String BreshitEnglish = "Breshit,Noah,Lekhlkha,Vayera,HayeSara,Toldot,Vayetse,Vayishlah,Vayeshev,Mikets,Vayigash,Vayhi";
    String ShmotEnglish = "Shmot,Vaera,Bo,Bshalah,Yitro,Mishpatim,Truma,Ttsave,Kitisa,Vayakhel,Pkude";
    String VayikraEnglish = "Vayikra,Tsav,Shmini,Tazria,Mtsora,Aharemot,Kdoshim,Emor,Bhar,Bhukotay";
    String BamidbarEnglish = "Bamidbar,Naso,Bhaalotkha,Shlahlkha,Korah,Hukat,Balak,Pinhas,Matot,Mase";
    String DvarimEnglish = "Dvarim,Vaethanan,Ekev,Ree,Shoftim,Kitetse,Kitavo,Nitsavim,Vayelekh,Haazinu,Vzothabraha";

    String BreshitHebrew = "בראשית,נח,לך לך,וירא,חיי שרה,תולדות,ויצא,וישלח,וישב,מקץ,ויגש,ויחי";
    String ShmotHebrew = "שמות,וארא,בא,בשלח,יתרו,משפטים,תרומה,תצוה,כי תשא,ויקהל,פקודי";
    String VayikraHebrew = "ויקרא,צו,שמיני,תזריע,מצרע,אחרי מות,קדושים,אמור,בהר,בחקתי";
    String BamidbarHebrew = "במדבר,נשא,בהעלתך,שלח לך,קרח,חוקת,בלק,פינחס,מטות,מסעי";
    String DvarimHebrew = "דברים,ואתחנן,עקב,ראה,שופטים,כי תצא,כי תבוא,ניצבים,וילך,האזינו,וזאת הברכה";

    String humashEn, humashHe;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parashot);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        String[] EngliseArr = null;
        String[] HebrewArr = null;
        Intent intent = getIntent();

        humashHe = intent.getStringExtra("humashHe");
        setTitle(humashHe);
        humashEn = intent.getStringExtra("humashEn");

        if (humashEn.equals("Breshit")) {
            EngliseArr = BreshitEnglish.split(",");
            HebrewArr = BreshitHebrew.split(",");
        } else if (humashEn.equals("Shmot")) {
            EngliseArr = ShmotEnglish.split(",");
            HebrewArr = ShmotHebrew.split(",");
        } else if (humashEn.equals("Vayikra")) {
            EngliseArr = VayikraEnglish.split(",");
            HebrewArr = VayikraHebrew.split(",");
        } else if (humashEn.equals("Bamidbar")) {
            EngliseArr = BamidbarEnglish.split(",");
            HebrewArr = BamidbarHebrew.split(",");
        } else if (humashEn.equals("Dvarim")) {
            EngliseArr = DvarimEnglish.split(",");
            HebrewArr = DvarimHebrew.split(",");
        }

        LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutParashot);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 50, getResources().getDisplayMetrics());
        //LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        Button btnParash;
        View view;
        int height2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, (float) 1.5, getResources().getDisplayMetrics());
        LayoutParams lpView = new LayoutParams(LayoutParams.MATCH_PARENT, height2);

        for (int i = 0; i < EngliseArr.length; i++) {
            String hebrewParash = HebrewArr[i];
            String engliseParash = EngliseArr[i];

            btnParash = new Button(this);
            btnParash.setText(hebrewParash);
            btnParash.setTag(engliseParash);
            btnParash.setOnClickListener(SelectParash);
            btnParash.setBackgroundResource(R.drawable.background_button);
            //btnParash.setTextSize(20);
            btnParash.setTextAppearance(this, R.style.ButtonFontStyle);//font size
            ll.addView(btnParash, lp);

            view = new View(this);
            view.setBackgroundColor(-16777216/*black*/);
            ll.addView(view, lpView);
        }
    }

    View.OnClickListener SelectParash = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), WebActivity.class);
            intent.putExtra("parshHe", ((Button) v).getText());
            intent.putExtra("parshEn", v.getTag().toString());
            intent.putExtra("humashEn", humashEn);
            startActivity(intent);
        }
    };


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
