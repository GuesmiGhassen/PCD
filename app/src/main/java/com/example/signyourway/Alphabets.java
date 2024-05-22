package com.example.signyourway;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Alphabets extends AppCompatActivity {
    private String alphabet;
    private int currentAlphabetIndex;
    private Handler handler = new Handler();
    private PreviewView previewView;
    private ImageView imView;
    private ImageView imageView;
    private int imageSize = 150;
    private String lastPredictedLabel = null;
    private String predictedChar = null;
    private long lastPredictionTime = 0;
    private static final long DEBOUNCE_TIME = 1000000;
    private static final float confidenceThreshold = 0.7f;// Example threshold
    private TextView result;
    private TextView sentence;
    private boolean toastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabets);
        imageView = findViewById(R.id.imageView);
        imView = findViewById(R.id.Image);
        previewView = findViewById(R.id.previewView);
        result = findViewById(R.id.combined);
        sentence=findViewById(R.id.result);
        alphabet = getIntent().getStringExtra("alphabet");
        currentAlphabetIndex = alphabet.charAt(0) - 'A';

        TextView alphabetTextView = findViewById(R.id.alphabetTextView);
        alphabetTextView.setText(alphabet);
        String imageName = alphabet.toLowerCase();
        result.setText("Predicted: " + imageName);
        int imageResId = getResources().getIdentifier(alphabet.toLowerCase(), "drawable", getPackageName());
        imView.setImageResource(imageResId);
        ImageView alphabetImage = findViewById(R.id.alphabetImage);
        alphabetImage.setImageResource(getResources().getIdentifier("alphabet_" + alphabet.toLowerCase(), "drawable", getPackageName()));

        Button prevButton = findViewById(R.id.prevButton);
        Button nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAlphabetIndex--;
                if (currentAlphabetIndex < 0) currentAlphabetIndex = 0;
                toastShown = false;
                updateAlphabet();
                sentence.setText("");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAlphabetIndex++;
                if (currentAlphabetIndex > 25) currentAlphabetIndex = 25;
                toastShown = false;
                updateAlphabet();
                sentence.setText("");
            }
        });
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }

    }
    private void updateAlphabet() {
        String newAlphabet = String.valueOf((char) ('A' + currentAlphabetIndex));
        TextView alphabetTextView = findViewById(R.id.alphabetTextView);
        alphabetTextView.setText(newAlphabet);

        ImageView alphabetImage = findViewById(R.id.alphabetImage);
        alphabetImage.setImageResource(getResources().getIdentifier("alphabet_" + newAlphabet.toLowerCase(), "drawable", getPackageName()));
        int imageResId = getResources().getIdentifier(newAlphabet.toLowerCase(), "drawable", getPackageName());
        imView.setImageResource(imageResId);

    }

    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
                    Bitmap bitmap = imageProxyToBitmap(imageProxy);
                    Bitmap resizedBitmap = resizeBitmap(bitmap, imageSize, imageSize);
                    // Rotate the bitmap before displaying it
                    Bitmap rotatedBitmap = rotateBitmap(resizedBitmap, 90); // Rotate by 90 degrees
                    imageView.setImageBitmap(rotatedBitmap);
                    classifyImage(rotatedBitmap);
                    //Debut(rotatedBitmap);
                    imageProxy.close();
                });

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                // Handle exceptions
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        int format = imageProxy.getFormat();
        // Vérifiez le format de l'ImageProxy
        if (format == ImageFormat.YUV_420_888) {
            // Utilisez la méthode de conversion pour YUV_420_888
            ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] data = new byte[ySize + uSize + vSize];
            yBuffer.get(data, 0, ySize);
            vBuffer.get(data, ySize, vSize);
            uBuffer.get(data, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            return null;
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    public void classifyImage(Bitmap image) {
        // Load the model
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
            return; // Exit the method if the model cannot be loaded
        }

        // Convert the cropped image to a ByteBuffer
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(image);

        // Run inference
        float[][] output = new float[1][29]; // Adjusted to match the model's output shape
        tflite.run(byteBuffer, output);

        // Find the index of the highest confidence
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i];
                maxPos = i;
            }
        }

        // Load labels
        String[] labels = loadLabels();
        // Debouncing logic
        long currentTime = System.currentTimeMillis();

        // Apply debouncing logic once per prediction
        if (maxConfidence > confidenceThreshold && (lastPredictedLabel == null || !lastPredictedLabel.equals(labels[maxPos]) || currentTime - lastPredictionTime >= DEBOUNCE_TIME)) {
            // The prediction hasn't changed for a while, so we can consider it stable
            lastPredictedLabel = labels[maxPos];
            lastPredictionTime = currentTime;
            result.setText("Predicted: " + lastPredictedLabel);
        }

        // Iterate over each character in the word and color it if it matches the prediction
        String newAlphabet = String.valueOf((char) ('A' + currentAlphabetIndex));
        if (lastPredictedLabel.charAt(0) == newAlphabet.charAt(0)) {
            updateAlphabet();
            colorLetter(sentence, newAlphabet, newAlphabet.indexOf(newAlphabet), Color.GREEN);
            if (!toastShown) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Toast toast = Toast.makeText(Alphabets.this, "Greaaat now move to the next alphabet", Toast.LENGTH_SHORT);
                        toast.show();

                        // Cancel the toast after 1 second
                        Handler toastHandler = new Handler();
                        toastHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        }, 2000); // 1 second delay

                        toastShown = true; // Set the flag to true after showing the Toast
                    }
                }, 500); // 1 second delay
            }
        }

    }


    private void colorLetter(TextView textView,String word, int position, int color) {
        // String text = textView.getText().toString();
        SpannableString spannableString = new SpannableString(word);
        spannableString.setSpan(new ForegroundColorSpan(color), position, position + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * bitmap.getWidth() * bitmap.getHeight() * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < bitmap.getHeight(); ++i) {
            for (int j = 0; j < bitmap.getWidth(); ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f)); // Normalisation
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f)); // Normalisation
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f)); // Normalisation
            }
        }
        return byteBuffer;
    }

    private String[] loadLabels() {
        try {
            InputStream is = getAssets().open("labels.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> labels = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
            return labels.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("Test.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
