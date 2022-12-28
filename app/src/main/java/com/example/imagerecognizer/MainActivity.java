package com.example.imagrrecognizer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imagrrecognizer.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button select;
    Bitmap bitmap;
    ImageView img;
    TextView txt;
    BufferedReader buffReader;
    String fileName;
    FileInputStream inputStream;
    InputStreamReader inputStreamReader;
    ArrayList<String> labels;
    FileReader fileReader;
    String labelsString;
    Path filePath;
    File file;
    DataInputStream dataInput;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        select = findViewById(R.id.selectBtn);
        img = findViewById(R.id.image);
        txt = findViewById(R.id.textView);
        labels = new ArrayList<>();

        fileName = "/Users/ASUS/AndroidStudioProjects/ImageRecognizer/app/src/main/assets/labels.txt";

//        inputStream

//        try {
//            inputStream = new FileInputStream(get);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        try {
            inputStreamReader = new InputStreamReader(getAssets().open("labels.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        buffReader =new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = buffReader.readLine()) != null) {
                    labels.add(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


//        file reader
//
//        Log.d("file name", fileName);
//        System.out.println(fileName);
//        try {
//            fileReader = new FileReader(String.valueOf(getAssets().open("labels.txt")));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Log.d("file reader", String.valueOf(fileReader));
//        System.out.println(String.valueOf(fileReader));
//
//        buffReader =new BufferedReader(fileReader);
//            String line;
//            try {
//                while ((line = buffReader.readLine()) != null) {
//                    labels.add(line);
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//        filePath = FileSystems.getDefault().getPath(fileName);
//
//        System.out.println("file path "+ filePath);
//        System.out.println(filePath.getClass());
//        try {
//            buffReader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(buffReader);

//        file = new File("C:\\Users\\ASUS\\AndroidStudioProjects\\ImageRecognizer\\app\\src\\main\\assets\\labels.txt");
//        System.out.println(file);
//        try {
//            inputStream = new FileInputStream(file);
//            buffReader = new BufferedInputStream(inputStream);
//            dataInput = new DataInputStream(buffReader);
//            while (dataInput.available() != 0) {
//                System.out.println(dataInput.read());
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
        public void select(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
//                                switch (result.getResultCode()) {
//                                    case REQUEST_CODE:
                //data.getData returns the content URI for the selected Image
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    });

    @SuppressLint("SetTextI18n")
    public void predict(View v){

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        TensorImage tbuffer = TensorImage.fromBitmap(resized);
        ByteBuffer byteBuffer = tbuffer.getBuffer();


        try {
            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(this);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
            inputFeature0.loadBuffer(byteBuffer);

            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
            float[] outputFeature0 = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

            int maxInd = getMaxInd(outputFeature0);
            txt.setText(labels.get(maxInd)+ " "+maxInd);

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxInd(float[] arr){
        float max = arr[0];
        int ind = 0;

        for (int i=1; i<arr.length; i++){
            if(arr[i]>max){
                max = arr[i];
                ind = i;
            }
        }

        return ind;
    }
}



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK)
//            switch (requestCode) {
//                case REQUEST_CODE:
//                    //data.getData returns the content URI for the selected Image
//                    Uri selectedImage = data.getData();
//                    img.setImageURI(selectedImage);
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//            }
//
//    }


