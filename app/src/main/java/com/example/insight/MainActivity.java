package com.example.insight;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ibm.watson.visual_recognition.v3.VisualRecognition;
import com.ibm.cloud.sdk.core.security.*;
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Button b;
    File photoFile = null;
    String objName = null;
    public static final String EXTRA_MESSAGE = "com.example.insight.MESSAGE";
    private static final int PICK_IMAGE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b = findViewById(R.id.button2);
        b.setClickable(false);
        b.setVisibility(View.GONE);
        ImageView i = findViewById(R.id.imageView2);
        i.setVisibility(View.VISIBLE);
        i = findViewById(R.id.imageView);
        i.setVisibility(View.GONE);
    }

    public void onClick(View view) {
        // Do something in response to button
        dispatchTakePictureIntent();

    }


    //@@@@ New method to save file that we capture
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Bundle extras = data.getExtras();
        //Bitmap imageBitmap = (Bitmap) extras.get("data");
        //imageView.setImageBitmap(imageBitmap);


        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            ImageView image = (ImageView)  findViewById(R.id.imageView);
            image.setImageBitmap(myBitmap);


            ImageButton b1 = findViewById(R.id.button);
            b1.setImageResource(R.drawable.retake);
            b.setClickable(true);
            b.setVisibility(View.VISIBLE);
            ImageView i = findViewById(R.id.imageView2);
            i.setVisibility(View.GONE);
            i = findViewById(R.id.imageView);
            i.setVisibility(View.VISIBLE);

        }
        else if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri imageUri = data.getData();
            ImageView image = (ImageView)  findViewById(R.id.imageView);
            image.setImageURI(imageUri);

            try {
                // Creating file
                photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                }

                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                FileOutputStream fileOutputStream = new FileOutputStream(photoFile);
                // Copying
                copyStream(inputStream, fileOutputStream);
                fileOutputStream.close();
                inputStream.close();

            } catch (Exception e) {
            }

            if (photoFile!= null){
                b.setClickable(true);
                b.setVisibility(View.VISIBLE);
            }
            ImageView i = findViewById(R.id.imageView2);
            i.setVisibility(View.GONE);
            i = findViewById(R.id.imageView);
            i.setVisibility(View.VISIBLE);
        }
        else
        {
            displayMessage(getBaseContext(),"Request cancelled or something went wrong.");
            ImageView i = findViewById(R.id.imageView2);
            i.setVisibility(View.VISIBLE);
            i = findViewById(R.id.imageView);
            i.setVisibility(View.GONE);
            b.setVisibility(View.GONE);
        }


    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void displayMessage(Context context, String message)
    {
        Toast.makeText(context,message, Toast.LENGTH_LONG).show();
    }
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalCacheDir();
        File image = File.createTempFile(

                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onClick2(View view) throws ExecutionException, InterruptedException {
        String out = new DataAsync().execute().get();
        String finalOP= JsonHandler.parseVisual(out);
//        createAlert(finalOP);
//        String toSend = "This is "+finalOP+".";
        objName = finalOP;
//        createAlert(toSend);

        Intent intent = new Intent(this, TranslateActivity.class);
        intent.putExtra(EXTRA_MESSAGE, objName);
        startActivity(intent);
        photoFile.delete();
        File storageDir = getExternalCacheDir();
        deleteDir(storageDir);
    }

    //empty cache files.
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void createAlert(String out) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(out);
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Toast.makeText(MainActivity.this,"You clicked yesbutton",Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Need an async method to call API
    class DataAsync extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String res=null;
            IamAuthenticator authenticator = new IamAuthenticator("SmuaWIuvBKt1Pa7lDJ5mpveepuelhNcITbAaek9BLTZr");
            VisualRecognition visualRecognition = new VisualRecognition("2018-03-19", authenticator);
            visualRecognition.setServiceUrl("https://gateway.watsonplatform.net/visual-recognition/api");
            try {
                ClassifyOptions classifyOptions = new ClassifyOptions.Builder().imagesFile(photoFile).build();
                ClassifiedImages result = visualRecognition.classify(classifyOptions).execute().getResult();
                res = result.toString();
            } catch (FileNotFoundException e) {
                System.out.println(e);
            }
            return res;
        }


    }
    public void onTranslateClick(View view) {
        Intent intent = new Intent(this, TranslateActivity.class);
        intent.putExtra(EXTRA_MESSAGE, objName);
        startActivity(intent);
        photoFile.delete();
    }

    // handling gallery button
    public void onGalleryButtonClicked(View view){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
}
