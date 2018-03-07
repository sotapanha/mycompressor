package com.example.jin.mycompressor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;

public class CompressorActivity extends AppCompatActivity {

    final static int RESULT_LOAD_IMG = 1;
    final static int RESULT_LOAD_VIDEO = 2;

    ImageView imageView;
    VideoView videoView;
    TextView mediaSize;

    Uri capturedUri = null;
    Uri compressUri = null;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compressor);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        mediaSize = findViewById(R.id.media_size);
        mediaSize.setText("Size : 0 kbytes");

        progressBar = findViewById(R.id.pBar);
        progressBar.setVisibility(View.GONE);

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (capturedUri != null){
                    videoView.setVideoURI(capturedUri);
                    videoView.start();
                } else {
                    Toast.makeText(getApplicationContext(), " you need to upload video file",Toast.LENGTH_SHORT);
                }
                return false;
            }
        });



    }

    public void takeImage(View view) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    public void takeVideo(View view) {

        mediaSize.setText("Size : 0 kbytes");
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),RESULT_LOAD_VIDEO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == RESULT_LOAD_IMG){

                new ImageCompressionAsyncTask(this).execute(data.getData().toString(),
                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getPackageName()+"/media/images");

            }
            if (requestCode == RESULT_LOAD_VIDEO){

                progressBar.setVisibility(View.VISIBLE);
                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/media/videos");
                if (f.mkdirs() || f.isDirectory())
                    //compress and output new video specs
                    new VideoCompressAsyncTask(this).execute(data.getData().toString(), f.getPath());


            }
        }

    }


    class ImageCompressionAsyncTask extends AsyncTask<String, Void, String> {

        Context mContext;

        public ImageCompressionAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(params[0],params[1]+" : ++++++++++");
            String filePath = SiliCompressor.with(mContext).compress(params[0], new File(params[1]));
            return filePath;


            /*
            Bitmap compressBitMap = null;
            try {
                compressBitMap = SiliCompressor.with(mContext).getCompressBitmap(params[0], true);
                return compressBitMap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return compressBitMap;
            */
        }

        @Override
        protected void onPostExecute(String s) {
            /*
            if (null != s){
                imageView.setImageBitmap(s);
                int compressHieght = s.getHeight();
                int compressWidth = s.getWidth();
                float length = s.getByteCount() / 1024f; // Size in KB;
                String text = String.format("Name: %s\nSize: %fKB\nWidth: %d\nHeight: %d", "ff", length, compressWidth, compressHieght);
                picDescription.setVisibility(View.VISIBLE);
                picDescription.setText(text);
            }
            */

            File imageFile = new File(s);
            compressUri = Uri.fromFile(imageFile);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), compressUri);
                imageView.setImageBitmap(bitmap);

                String name = imageFile.getName();
                float length = imageFile.length() / 1024f; // Size in KB
                int compressWidth = bitmap.getWidth();
                int compressHieght = bitmap.getHeight();
                String text = String.format(Locale.US, "Name: %s\nSize: %fKB\nWidth: %d\nHeight: %d", name, length, compressWidth, compressHieght);
                mediaSize.setText(text);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String>{

        Context mContext;

        public VideoCompressAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return  filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            capturedUri = Uri.fromFile(imageFile);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;
            if(length >= 1024)
                value = length/1024f+" MB";
            else
                value = length+" KB";
            String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video compression complete!", imageFile.getName(), value);
            mediaSize.setText(text);
            progressBar.setVisibility(View.GONE);
        }
    }
}
