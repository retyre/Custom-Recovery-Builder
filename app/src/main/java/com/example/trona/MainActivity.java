package com.example.trona;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Button btnExploit = new Button(this);
        btnExploit.setText("Run Trona Exploit");
        btnExploit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runExploitBinary();
            }
        });
        setContentView(btnExploit);
    }

    private void runExploitBinary() {
        try {
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
            }

            outFile.setExecutable(true, false);

            ProcessBuilder processBuilder = new ProcessBuilder(outFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Toast.makeText(this, "Exploit executed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Exploit exited with code: " + exitCode, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}