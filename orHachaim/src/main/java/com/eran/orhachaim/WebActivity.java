package com.eran.orhachaim;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.eran.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class WebActivity extends Activity {


    WebView wv;
    WebSettings wvSetting;
    int scrollY = 0;
    MenuItem nightModeItem = null;
    SharedPreferences OHPreferences;
    SharedPreferences defaultSharedPreferences;
    boolean fullScreen = false;
    AudioManager am;
    String phoneStatus;
    int startRingerMode = 2;//RINGER_MODE_NORMAL

    String humashEn, parshHe, parshEn;
    boolean pageReady = false;
    GestureDetector gs = null;
    ActionBar actionBar = null;
    String appName = "/OrHachaim";
    SearchView searchView;
    MenuItem PreviousMI = null;
    MenuItem NextMI = null;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        fullScreen = defaultSharedPreferences.getBoolean("CBFullScreen", false);
        if (fullScreen && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_web);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (fullScreen) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                actionBar.hide();
            }
        }

        boolean keepScreenOn = defaultSharedPreferences.getBoolean("CBKeepScreenOn", false);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        phoneStatus = defaultSharedPreferences.getString("phone_status", "-1");

        OHPreferences = getSharedPreferences("OHPreferences", MODE_PRIVATE);
        wv = (WebView) findViewById(R.id.webView);
        wvSetting = wv.getSettings();
        //registerForContextMenu(wv);


        Intent intent = getIntent();
        String requiredFileName = intent.getStringExtra("requiredFileName");
        if (requiredFileName != null) {
            ArrayList<Location> locationList;
            Gson gson = new Gson();

            SharedPreferences preferences = getSharedPreferences("Locations", MODE_PRIVATE);
            String preferencesLocationsJson = preferences.getString("preferencesLocationsJson", null);
            if (preferencesLocationsJson != null) {
                locationList = gson.fromJson(preferencesLocationsJson, new TypeToken<ArrayList<Location>>() {
                }.getType());
                Location requiredLocation = null;
                if (requiredFileName.equals("-1")/*lastLocation*/) {
                    int lastLocation = locationList.size() - 1;
                    requiredLocation = locationList.get(lastLocation);
                } else//History
                {
                    for (int i = locationList.size() - 1; i >= 0; i--) {
                        requiredLocation = locationList.get(i);
                        if (requiredLocation.getTime().equals(requiredFileName)) {
                            break;
                        }
                    }
                }

                if (requiredLocation != null) {
                    humashEn = requiredLocation.getHumashEn();
                    parshHe = requiredLocation.getParshHe();
                    parshEn = requiredLocation.getParshEn();
                    scrollY = requiredLocation.getScrollY();
                } else {
                    finish();//not need to arrive to here
                }
            } else {
                finish();//not need to arrive to here
            }
        } else {
            humashEn = intent.getStringExtra("humashEn");
            parshHe = intent.getStringExtra("parshHe");
            parshEn = intent.getStringExtra("parshEn");
        }

        wvSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        wvSetting.setJavaScriptEnabled(true);
        LoadWebView();

        WeakReference<Activity> WeakReferenceActivity = new WeakReference<Activity>(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Utils.toggleFullScreen(WeakReferenceActivity, getApplicationContext(), R.id.webView, actionBar, fullScreen);
        }

        Utils.firstDoubleClickInfo(defaultSharedPreferences, WeakReferenceActivity);

    }


    protected void onResume() {
        super.onResume();//Always call the superclass method first

        startRingerMode = am.getRingerMode();

        if (!phoneStatus.equals("-1") && startRingerMode != 0/*silent*/) {
            am.setRingerMode(Integer.parseInt(phoneStatus));
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        nightModeItem = menu.findItem(R.id.nightMode);
        NextMI = menu.findItem(R.id.next);
        PreviousMI = menu.findItem(R.id.previous);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            //Toast.makeText(getApplicationContext(),"onQueryTextChange "  ,Toast.LENGTH_LONG).show();

            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            searchItem.setVisible(true);
            searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("חיפוש בפרשה הנוכחית");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String query) {
                    //Toast.makeText(getApplicationContext(),"onQueryTextChange " +query ,Toast.LENGTH_LONG).show();

                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    scrollY = wv.getScrollY();//save the location before the search
                    find(query);
                    // TODO Auto-generated method stub
                    return true;
                }


            });

            searchView.setOnCloseListener(new OnCloseListener() {
                @Override
                public boolean onClose() {
                    closeSearch(true);
                    return false;
                }

            });

            searchView.setOnSearchClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                }
            });
        }


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.nightMode:
                Utils.NightMode(true, OHPreferences, wv, nightModeItem);
                break;
            case R.id.zoomUp:
                Utils.changeSize(true, OHPreferences, wvSetting);
                break;
            case R.id.zoomDown:
                Utils.changeSize(false, OHPreferences, wvSetting);
                break;
            case R.id.previous:
                previous();
                break;
            case R.id.next:
                next();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void LoadWebView() {
        Utils.setOpacity(wv, 0.1);//for where the wv already exist

        setTitle(parshHe);
        wv.loadUrl("file:///android_asset/" + humashEn + "/" + parshEn + ".html");
        int size = Utils.readSize(OHPreferences);
        wvSetting.setDefaultFontSize(size);

        wv.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//            	//hide loading image
//                 findViewById(R.id.progressBar).setVisibility(View.GONE);
//            	//show webview
//            	wv.setVisibility(View.VISIBLE);

                Utils.setOpacity(wv, 0.1);
                Utils.showWebView(wv, (ProgressBar) findViewById(R.id.progressBar), true);
                if (scrollY != 0) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            ChangeWebViewBySettings();
                            wv.scrollTo(0, scrollY);
                            pageReady = true;
                            Utils.setOpacity(wv, 1);
                        }
                    }, 1100);
                } else {
                    ChangeWebViewBySettings();
                    pageReady = true;
                    Utils.setOpacity(wv, 1);
                }
            }
        });
    }

    private void ChangeWebViewBySettings() {
        Utils.NightMode(false, OHPreferences, wv, nightModeItem);
    }


    @Override
    protected void onPause() {
        super.onPause();  //Always call the superclass method first

        if (!phoneStatus.equals("-1") && startRingerMode != 0/*silent*/) {
            am.setRingerMode(startRingerMode);
        }

        File folder = new File(Environment.getExternalStorageDirectory() + appName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        if (success && pageReady) {
            SharedPreferences preferences = getSharedPreferences("Locations", MODE_PRIVATE);
            String preferencesLocationsJson = preferences.getString("preferencesLocationsJson", null);

            if (preferencesLocationsJson == null)//for second install, remove the old files
            {
                if (folder.isDirectory()) {
                    String[] children = folder.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(folder, children[i]).delete();
                    }
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentTime = sdf.format(new Date());

            View content = findViewById(R.id.layout);
            content.setDrawingCacheEnabled(true);
            Bitmap bitmap = content.getDrawingCache();
            File file = new File(Environment.getExternalStorageDirectory() + appName + "/" + currentTime + ".png");
            ArrayList<Location> locationList = new ArrayList<Location>();
            Gson gson = new Gson();
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(CompressFormat.PNG, 100, ostream);
                ostream.close();

                scrollY = wv.getScrollY();
                Location location = new Location(currentTime, humashEn, parshHe, parshEn, scrollY);


                if (preferencesLocationsJson != null) {
                    locationList = gson.fromJson(preferencesLocationsJson, new TypeToken<ArrayList<Location>>() {
                    }.getType());
                    if (locationList.size() >= 10) {
                        String idFirstLocation = locationList.get(0).getTime();
                        File imageToDelete = new File(Environment.getExternalStorageDirectory() + appName + "/" + idFirstLocation + ".png");
                        if (imageToDelete.exists()) {
                            boolean deleted = imageToDelete.delete();
                        }

                        locationList.remove(0);
                    }
                }


                locationList.add(location);

                String json = gson.toJson(locationList);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("preferencesLocationsJson", json);
                editor.commit();

            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

    }


    @SuppressLint("NewApi")
    public void onBackPressed() {

        wv.clearFocus();//for close pop-up of copy, select etc.

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (!searchView.isIconified()) {
                closeSearch(false);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }

    }

    //for voice search
    @SuppressLint("NewApi")
    @Override
    protected void onNewIntent(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchView.setQuery(query, true);

                //close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void closeSearch(Boolean fromListener) {
        wv.clearMatches();//clear the finds
        PreviousMI.setVisible(false);
        NextMI.setVisible(false);

        if (!fromListener) {// the listener do this by himself
            searchView.setIconified(true);// clear the searchView
            searchView.onActionViewCollapsed();//close the searchView
        }
    }

    @SuppressLint("NewApi")
    private void find(String query) {
        PreviousMI.setVisible(true);
        NextMI.setVisible(true);
        wv.findAllAsync(query);
    }


    private void previous() {
        wv.findNext(false);
    }

    private void next() {
        wv.findNext(true);
    }

}
