package com.example.chat;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.Message;
import com.example.chat.chatServer;
import com.example.chat.fileServer;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

public class chatClient extends AppCompatActivity implements PickiTCallbacks {
    String TAG = "CLIENT ACTIVITY";

    EditText smessage;
    ImageButton sent;
    String serverIpAddress = "";
    int myport;
    int sendPort;
    ArrayList<Message> messageArray;
    ImageButton fileUp;
    TextView textView;
    chatServer s;
    fileServer f;
    String ownIp;
    Toolbar toolbar;
    ProgressBar progressBar;
    PickiT pickiT;
    private Boolean exit = false;
    private RecyclerView mMessageRecycler;
    private ChatAdapterRecycler mMessageAdapter;
    private int REQUEST_CODE = 200;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        pickiT = new PickiT(this, this);
        smessage = findViewById(R.id.edittext_chatbox);
        toolbar = findViewById(R.id.toolbar);
        sent = findViewById(R.id.button_chatbox_send);
        fileUp = findViewById(R.id.file_send);
        textView = findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        setSupportActionBar(toolbar);

        messageArray = new ArrayList<>();
        mMessageRecycler = findViewById(R.id.message_list);
        mMessageAdapter = new ChatAdapterRecycler(this, messageArray);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(true);

        mMessageRecycler.setLayoutManager(layoutManager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String info = bundle.getString("ip&port");
            assert info != null;
            String[] infos = info.split(" ");
            serverIpAddress = infos[0];
//            serverIpAddress = "192.168.49.1";
            sendPort = Integer.parseInt(infos[1]);
            myport = Integer.parseInt(infos[2]);
        }
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ownIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        getSupportActionBar().setTitle("Connection to " + serverIpAddress);
        if(serverIpAddress.equals("0.0.0.0")){
            initializeAIChat();
            sent.setOnClickListener(v -> {
                if (!smessage.getText().toString().isEmpty()) {
                    String userInput = smessage.getText().toString();
                    // Send message to AI
                    sendToAI(userInput);
                } else {
                    Toast.makeText(getApplicationContext(), "Please write something", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (!serverIpAddress.equals("")) {
            s = new chatServer(ownIp, this, getApplicationContext(), mMessageAdapter, mMessageRecycler, messageArray, myport, serverIpAddress);
            s.start();
            f = new fileServer(getApplicationContext(), mMessageAdapter, mMessageRecycler, messageArray, myport, serverIpAddress);
            f.start();

            sent.setOnClickListener(v -> {
                if (!smessage.getText().toString().isEmpty()) {
                    User user = new User("1:" + smessage.getText().toString());
                    user.execute();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please write something", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        if (permissionAlreadyGranted()) {
            Toast.makeText(chatClient.this, "Permission is already granted!", Toast.LENGTH_SHORT).show();

        }

        requestPermission();


        fileUp.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select file"), 1);
        });


    }
    private void initializeAIChat() {
        getSupportActionBar().setTitle("Chat with Gemini");

    }
    @SuppressLint("StaticFieldLeak")
    private void sendToAI(String userInput) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String aiResponse = "";
                try {
                    // Make API call to AI service using userInput
                    // Example: aiResponse = callAIChatAPI(userInput);
                    // Specify a Gemini model appropriate for your use case
                    GenerativeModel gm =
                            new GenerativeModel(
                                    /* modelName */ "gemini-1.5-flash",
                                    // Access your API key as a Build Configuration variable (see "Set up your API key"
                                    // above)
                                    /* apiKey */ "AIzaSyCeEkC0uUV9IdyqXDL0xeRLTlpD0I93HF8");
                    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                    Content content =
                            new Content.Builder().addText(userInput).build();

                    // For illustrative purposes only. You should use an executor that fits your needs.
                    Executor executor = Executors.newSingleThreadExecutor();

                    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                    Futures.addCallback(
                            response,
                            new FutureCallback<GenerateContentResponse>() {
                                @Override
                                public void onSuccess(GenerateContentResponse result) {
                                    String resultText = result.getText();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            messageArray.add(new Message(resultText, 1, Calendar.getInstance().getTime()));
                                            mMessageRecycler.setAdapter(mMessageAdapter); // Or notify the adapter of data change
                                            smessage.setText("");
                                        }
                                    });
                                    Log.d("ChatApp", "Generated content: " + resultText);
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                    Log.e("ChatApp", "Failed to generate content", t);
                                }
                            },
                            executor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return aiResponse;
            }

            @Override
            protected void onPostExecute(String aiResponse) {
                messageArray.add(new Message(userInput, 0, Calendar.getInstance().getTime()));
//                messageArray.add(new Message(aiResponse, 1, Calendar.getInstance().getTime()));
                mMessageRecycler.setAdapter(mMessageAdapter);
                smessage.setText("");
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            final Context context = chatClient.this;
            ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .initialColor(0xffffffff)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(12)
                    .setOnColorSelectedListener(selectedColor -> {
                    })
                    .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                        changeBackgroundColor(selectedColor);
                        User user = new User("2:" + Integer.toHexString(selectedColor));
                        user.execute();
                        Log.d("ColorPicker", "onColorChanged: 0x" + Integer.toHexString(selectedColor));
                    })
                    .setNegativeButton("cancel", (dialog, which) -> {
                    })
                    .build()
                    .show();
        }
        if (item.getItemId() == R.id.action_history) {

            File path = Environment.getExternalStorageDirectory();
            Log.i(TAG, "FilesDir =>" + path + "\n");
            String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + serverIpAddress + ".txt";
            File file = new File(path + "/Download/", fileName);

            for (int i = 0; i < messageArray.size(); i++) {
                String s = messageArray.get(i).getMessage();
                if (messageArray.get(i).isSent()) {
                    s = "Client:" + s + "\n";
                    System.out.println(s);
                } else {
                    s = "Serer : " + s + "\n";
                    System.out.println(s);
                }

                try {
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(s.getBytes());
                    Toast.makeText(chatClient.this, "Chat history has been saved in " + path +"/Download/  folder", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean permissionAlreadyGranted() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission is denied!", Toast.LENGTH_SHORT).show();
                boolean showRationale = shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE);
                if (!showRationale) {
                    openSettingsDialog();
                }


            }
        }
    }


    private void openSettingsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(chatClient.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", (dialog, which) -> {
            dialog.cancel();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
        }
    }

    public final void changeBackgroundColor(Integer selectedColor) {
        LayerDrawable layerDrawable = (LayerDrawable) mMessageRecycler.getBackground();
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.shapeColor);
        gradientDrawable.setColor(selectedColor);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Confirmation")
                .setMessage("Are you sure you want to exit? All chat history will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clean up resources
                    pickiT.deleteTemporaryFile();
                    s.stopServer();  // Ensure you handle stopping properly
                    s.interrupt();
                    f.interrupt();
                    finish();  // Close the activity
                    super.onBackPressed();  // Handle the back press
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();  // Dismiss the dialog and keep the user in the activity
                })
                .show();
//        if (exit) {
//            pickiT.deleteTemporaryFile();
//            s.stopServer();
//            s.interrupt();
//            f.interrupt();
//            finish();
//            super.onBackPressed();
//        } else {
//            Toast.makeText(this, "Press Back again to Exit.",
//                    Toast.LENGTH_SHORT).show();
//            exit = true;
//            new Handler().postDelayed(() -> exit = false, 3 * 1000);
//        }
    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        Log.d(TAG, "PickiTonCompleteListener: Directory was" + path);
        new fileTransfer(path).execute();

    }

    @SuppressLint("StaticFieldLeak")

    public class User extends AsyncTask<Void, Void, String> {
        String msg;

        User(String message) {
            msg = message;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String ipadd = serverIpAddress;
                int portr = sendPort;
                Socket clientSocket = new Socket(ipadd, portr);
                OutputStream outToServer = clientSocket.getOutputStream();
                PrintWriter output = new PrintWriter(outToServer);
                output.println(msg);
                output.flush();
                clientSocket.close();
                runOnUiThread(() -> sent.setEnabled(false)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(() -> sent.setEnabled(true));
            Log.i(TAG, "on post execution result => " + result);
            StringBuilder stringBuilder = new StringBuilder(result);
            if (stringBuilder.charAt(0) == '1' && stringBuilder.charAt(1) == ':') {
                stringBuilder.deleteCharAt(0);
                stringBuilder.deleteCharAt(0);
                result = stringBuilder.toString();

                messageArray.add(new Message(result, 0, Calendar.getInstance().getTime()));
                mMessageRecycler.setAdapter(mMessageAdapter);
                smessage.setText("");
            }
        }


    }

    @SuppressLint("StaticFieldLeak")
    class fileTransfer extends AsyncTask<Void, Integer, String> {
        String path;

        fileTransfer(String path) {
            this.path = path;
        }

        @Override
        protected String doInBackground(Void... voids) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.VISIBLE);
            });
            String filenameX = "";
            String ipadd = serverIpAddress;
            int portr = sendPort + 1;
            try {
                Socket clientSocket = new Socket(ipadd, portr);
                if (path.charAt(0) != '/') {
                    path = "/storage/emulated/0/" + path;
                }
                Log.d(TAG, "doInBackground: Storage Here " + path);
                File file = new File(path);
                if (path.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Path is empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                Log.d(TAG, "doInBackground: " + path);

                FileInputStream fileInputStream = new FileInputStream(file);

                long fileSize = file.length();
                byte[] byteArray = new byte[(int) fileSize];

                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                dataInputStream.readFully(byteArray, 0, byteArray.length);

                OutputStream outputStream = clientSocket.getOutputStream();

                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.writeLong(byteArray.length);

                filenameX = file.getName();


                dataOutputStream.write(byteArray, 0, byteArray.length);
                dataOutputStream.flush();

                outputStream.write(byteArray, 0, byteArray.length);
                outputStream.flush();

                outputStream.close();
                dataOutputStream.close();

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return filenameX;
        }

        @Override
        protected void onPostExecute(String name) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
            });
            Log.d(TAG, "onPostExecute: " + name);

            if (!name.isEmpty()) {
                messageArray.add(new Message("New File Sent: " + name + ":" + path, 0, Calendar.getInstance().getTime()));
                mMessageRecycler.setAdapter(mMessageAdapter);
                smessage.setText("");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "File Sending Error.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


    }
}