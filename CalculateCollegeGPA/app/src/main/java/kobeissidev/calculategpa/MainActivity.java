package kobeissidev.calculategpa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


@SuppressWarnings("ALL")
public class MainActivity extends Activity {
    private int numberOfClasses = 1;
    static double[] scales;
    private File path;
    private File file;
    private String[] inputs;
    private SharedPreferences prefs;
    private boolean isFirstTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("isFirstTime", Context.MODE_PRIVATE);
        isFirstTime = prefs.getBoolean("isFirstTime",true);

        if(isFirstTime && BuildConfig.FLAVOR.equals("free")){
            Dialog();
            SharedPreferences.Editor themeEditor = prefs.edit();
            themeEditor.putBoolean("isFirstTime", false);
            themeEditor.apply();
        }

        Button collegeGPA = (Button) findViewById(R.id.collegeButton);
        Button hsGPA = (Button) findViewById(R.id.hsButton);

        path = this.getFilesDir();
        file = new File(path, "scales.txt");

        if (file.length() == 0) {
            scales = new double[]{4.0, 4.0, 3.7, 3.4, 3.0, 2.7, 2.4, 2.0, 1.7, 1.4, 1.0, 0.7, 0.0};
            inputs = new String[13];
            saveAll();
        } else {
            inputs = Load(file);
            scales = new double[13];

            for (int index = 0; index < scales.length; index++) {
                scales[index] = Double.parseDouble(inputs[index]);
            }
        }

        navigateToCollege(collegeGPA);
        navigateToHighScool(hsGPA);
    }

    private void Dialog(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("CalcGPA is now depreciated. Please download CalcGPA+.")
                .setMessage("Go to the CalcGPA+ link?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = "kobeissidev.calculategpaplus";
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void navigateToCollege(Button collegeGPA) {
        collegeGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText classesText = (EditText) findViewById(R.id.classes);

                if (classesText.getText().length() == 0) {
                    classesText.setText("1");
                    classesText.setSelection(1);
                }

                numberOfClasses = Integer.parseInt(classesText.getText().toString());

                if (numberOfClasses > 6 || numberOfClasses < 1) {
                    numberOfClasses = 1;
                }

                Intent intent = new Intent(getBaseContext(), CollegeGPA.class);
                intent.putExtra("NumberOfClasses", numberOfClasses);
                startActivity(intent);
                saveAll();
            }
        });
    }

    private void navigateToHighScool(Button hsGPA) {
        hsGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText classesText = (EditText) findViewById(R.id.classes);

                if (classesText.getText().length() == 0) {
                    classesText.setText("1");
                }

                numberOfClasses = Integer.parseInt(classesText.getText().toString());

                if (numberOfClasses > 6 || numberOfClasses < 1) {
                    numberOfClasses = 1;
                }

                Intent intent = new Intent(getBaseContext(), HighSchoolGPA.class);
                intent.putExtra("NumberOfClasses", numberOfClasses);
                startActivity(intent);
                saveAll();
            }
        });
    }

    private void navigateToSettings() {
        Intent intent = new Intent(getBaseContext(), ChangeScale.class);
        startActivity(intent);
        saveAll();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                navigateToSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static void Save(File file, String[] data) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            try {
                for (int index = 0; index < data.length; index++) {
                    fileOutputStream.write(data[index].getBytes());
                    if (index < data.length - 1) {
                        fileOutputStream.write("\n".getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] Load(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String test;
        int value = 0;
        try {
            while ((test = bufferedReader.readLine()) != null) {
                value++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileInputStream.getChannel().position(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] array = new String[value];

        String line;
        int index = 0;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                array[index] = line;
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    private void saveAll() {
        for (int index = 0; index < scales.length; index++) {
            inputs[index] = String.valueOf(scales[index]);
        }
        Save(file, inputs);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveAll();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveAll();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
