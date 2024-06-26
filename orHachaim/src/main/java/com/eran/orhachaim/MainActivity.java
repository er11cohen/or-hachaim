package com.eran.orhachaim;


import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eran.utils.Utils;

import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;

import java.lang.ref.WeakReference;
import java.util.Calendar;

//Toast.makeText(this,Integer.toString(scrollY),Toast.LENGTH_LONG).show();
public class MainActivity extends Activity {

    SharedPreferences defaultSharedPreferences;
    final String shareTextIntent = "אור החיים  - Or Hachaim https://play.google.com/store/apps/details?id=com.eran.orhachaim";
    WeakReference<Activity> weakReferenceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weakReferenceActivity = new WeakReference<Activity>(this);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences OHPreferences = getSharedPreferences("OHPreferences", MODE_PRIVATE);
        ;

        String version = OHPreferences.getString("version", "-1");
        if (!version.equals("1.2.2")) {
            String message = Utils.ReadTxtFile("files/newVersion.txt", getApplicationContext());
            ((TextView) new AlertDialog.Builder(this)
                    .setTitle("חדשות ללומדי אור החיים")
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setIcon(drawable.ic_input_add)
                    .setMessage(Html.fromHtml(message))
                    .setPositiveButton("אשריכם תזכו למצוות", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show()
                    .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());

            SharedPreferences.Editor editor = OHPreferences.edit();
            editor.putString("version", "1.2.2");
            editor.commit();
        }
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void LastLocation(View v) {
        SharedPreferences preferences = getSharedPreferences("Locations", MODE_PRIVATE);
        String preferencesLocationsJson = preferences.getString("preferencesLocationsJson", null);
        if (preferencesLocationsJson != null) {
            Intent intent = new Intent(getApplicationContext(), WebActivity.class);
            intent.putExtra("requiredFileName", "-1"/*lastLocation*/);
            startActivity(intent);
        }
    }


    public void SelectHistory(View v) {
        Intent intent = new Intent(getApplicationContext(), Gallery.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1/*from gallery*/) {
            if (data != null && data.getExtras().containsKey("fileName")) {
                String fileName = data.getStringExtra("fileName");
                //Toast.makeText(getApplicationContext(), "1 + "+fileName , Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("requiredFileName", fileName);
                startActivity(intent);
            }
        }
    }

    public void OpenSettings(View v) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void CurrentWeek(View v) {
        String file = "parashot.csv";

        Calendar c = Calendar.getInstance();
        final int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int dayToShabat = 7 - dayOfWeek;
        c.add(Calendar.DATE, dayToShabat);
        JewishCalendar jc = new JewishCalendar(c);
        boolean LiveInIsrael = defaultSharedPreferences.getBoolean("CBLiveInIsrael", true);
        jc.setInIsrael(LiveInIsrael);
        int parshaIndex = jc.getParshaIndex();
        while (parshaIndex == -1) {
            c.add(Calendar.DATE, 7);
            jc = new JewishCalendar(c);
            parshaIndex = jc.getParshaIndex();
            if (parshaIndex == 0) //Breshit
            {
                parshaIndex = 60;//Vzothabraha
            }
        }


        String parashotMapStr = Utils.ReadTxtFile("files/" + file, getApplicationContext());
        Location location = null;

        try {
            String[] items = parashotMapStr.split("\n");
            for (int i = 0; i < items.length; i++) {
                String[] parashotStr = items[i].split(",");
                int curParshaIndex = Integer.parseInt(parashotStr[0].toString());
                if (curParshaIndex == parshaIndex) {
                    location = new Location(
                            parashotStr[1].trim(),///parshHe
                            parashotStr[2].trim(),//parshEn
                            parashotStr[3].trim()//humashEn
                    );
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        if (location == null) {
            return;
        }


        if (location.getParshEn().contains("&")) {

            final Location locationFinal = location;
            (
                    (TextView) new AlertDialog.Builder(this)
                            .setTitle("פרשיות מחוברות")
                            .setIcon(android.R.drawable.ic_menu_info_details)
                            .setMessage("צדיק בחר פרשה")
                            .setPositiveButton(locationFinal.getParshHe().split("&")[0], new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    locationFinal.setParshHe(locationFinal.getParshHe().split("&")[0]);
                                    locationFinal.setParshEn(locationFinal.getParshEn().split("&")[0]);
                                    continueToOpenLimud(locationFinal);
                                }
                            })
                            .setNegativeButton(locationFinal.getParshHe().split("&")[1], new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    locationFinal.setParshHe(locationFinal.getParshHe().split("&")[1]);
                                    locationFinal.setParshEn(locationFinal.getParshEn().split("&")[1]);
                                    continueToOpenLimud(locationFinal);
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance()
                    );
        } else {
            continueToOpenLimud(location);
        }
    }

    private void continueToOpenLimud(Location location) {
        Intent intentCurrentWeek = new Intent(getApplicationContext(), WebActivity.class);
        intentCurrentWeek.putExtra("humashEn", location.getHumashEn());
        intentCurrentWeek.putExtra("parshHe", location.getParshHe());
        intentCurrentWeek.putExtra("parshEn", location.getParshEn());
        startActivity(intentCurrentWeek);
    }

    public void OpenHelp(View v) {
        Utils.alertDialogShow(weakReferenceActivity, getApplicationContext(),
                "עזרה", android.R.drawable.ic_menu_help, "files/help.txt",
                "הבנתי", "זכו את הרבים", shareTextIntent);
    }

    public void OpenAbout(View v) {
        Utils.alertDialogShow(weakReferenceActivity, getApplicationContext(),
                "אודות", android.R.drawable.ic_menu_info_details,
                "files/about.txt", "אשריכם תזכו למצוות", "זכו את הרבים",
                shareTextIntent);
    }

    public void SelectHumash(View v) {
        Intent intent = new Intent(getApplicationContext(), ParashotActivity.class);
        String humashEn = (String) ((Button) v).getTag();
        intent.putExtra("humashEn", humashEn);
        intent.putExtra("humashHe", ((Button) v).getText());
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_score:
                scoreInGooglePlay();
                break;
            case R.id.menu_item_share:
                Utils.shareApp(weakReferenceActivity, shareTextIntent);
                break;
            default:
                break;
        }

        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    private void scoreInGooglePlay() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.eran.orhachaim"));
        startActivity(browserIntent);

        String text = "צדיק דרג אותנו 5 כוכבים וטול חלק בזיכוי הרבים.";
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
