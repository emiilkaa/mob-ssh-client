package com.example.sshclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.ArrayList;

public class ShellActivity extends AppCompatActivity implements View.OnClickListener {

    EditText shell_command;
    Button send;
    RecyclerView commands_list;
    MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        shell_command = (EditText)findViewById(R.id.shell_command);
        send = (Button)findViewById(R.id.send_command);
        send.setOnClickListener(this);
        commands_list = (RecyclerView)findViewById(R.id.output);
        ArrayList<String> commands = new ArrayList<>();
        commands_list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, commands);
        commands_list.setAdapter(adapter);
        shell_command.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                    case EditorInfo.IME_ACTION_NEXT:
                    case EditorInfo.IME_ACTION_PREVIOUS:
                        String command = shell_command.getText().toString();
                        shell_command.getText().clear();
                        adapter.addItem(command);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String res = ConnectionManager.runCommand(command);
                                    if (!res.isEmpty()) {
                                        adapter.addItem(res);
                                    }
                                } catch (JSchException | IOException e) {
                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "An error occurred: " + e.getMessage(), 8000);
                                    View snackbarView = snackbar.getView();
                                    TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setMaxLines(5);
                                    snackbar.show();
                                } catch (RuntimeException runtimeException) {
                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The server is disconnected. An error occurred: " + runtimeException.getMessage(), 8000);
                                    View snackbarView = snackbar.getView();
                                    TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setMaxLines(5);
                                    snackbar.show();
                                }
                            }
                        });
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            Snackbar.make(findViewById(android.R.id.content), "Sorry, something went wrong. Try again later.", Snackbar.LENGTH_SHORT).show();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.disconnect || item.getItemId() == android.R.id.home) {
            ConnectionManager.close();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_command) {
            String command = shell_command.getText().toString();
            shell_command.getText().clear();
            adapter.addItem(command);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String res = ConnectionManager.runCommand(command);
                        if (!res.isEmpty()) {
                            adapter.addItem(res);
                        }
                    } catch (JSchException | IOException e) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "An error occurred: " + e.getMessage(), 8000);
                        View snackbarView = snackbar.getView();
                        TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setMaxLines(5);
                        snackbar.show();
                    } catch (RuntimeException runtimeException) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "The server is disconnected. An error occurred: " + runtimeException.getMessage(), 8000);
                        View snackbarView = snackbar.getView();
                        TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setMaxLines(5);
                        snackbar.show();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                Snackbar.make(findViewById(android.R.id.content), "Sorry, something went wrong. Try again later.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
