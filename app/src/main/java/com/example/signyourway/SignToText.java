package com.example.signyourway;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class SignToText extends AppCompatActivity {
    private PreviewView previewView;
    private ImageView imageView;
    private TextView result;
    private TextView sentence;
    private Set<String> addedLetters = new HashSet<>();
    private int imageSize = 150;
    private String lastPredictedLabel = null;
    private long lastPredictionTime = 0;
    private static final long DEBOUNCE_TIME = 1000000;
    private static final float confidenceThreshold = 0.7f;// Example threshold
    TextToSpeech ttx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_to_text);
        previewView = findViewById(R.id.previewView);
        imageView = findViewById(R.id.imageView);
        result = findViewById(R.id.result);
        sentence=findViewById(R.id.combined);
        ImageButton btx= findViewById(R.id.song);
        btx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttx = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            ttx.setLanguage(Locale.US);
                            ttx.setSpeechRate(1.0f);
                            ttx.speak(sentence.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                });
            }
        });

        Button Btn=findViewById(R.id.btn);

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentence.setText("");
                addedLetters.clear();
            }
        });


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
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
            // Gérez d'autres formats si nécessaire
            // Par exemple, pour le format 256, vous pourriez avoir besoin d'une méthode de conversion différente
            // Vous pouvez ajouter ici le code pour gérer d'autres formats
            return null; // Retournez null ou une valeur par défaut si le format n'est pas pris en charge
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }





    public void classifyImage(Bitmap image) {
        try {
            // Load the model
            Interpreter tflite = new Interpreter(loadModelFile());

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
            if (maxConfidence > confidenceThreshold && (lastPredictedLabel == null || !lastPredictedLabel.equals(labels[maxPos]) || currentTime - lastPredictionTime >= DEBOUNCE_TIME)) {
                // The prediction hasn't changed for a while, so we can consider it stable
                lastPredictedLabel = labels[maxPos];
                lastPredictionTime = currentTime;
                result.setText("Predicted: " + lastPredictedLabel);

                // Get the text from the TextView as a String
                String sentenceText = sentence.getText().toString();

                // Check if the predicted label is "Nothing" or already added
                if (!lastPredictedLabel.equals("Nothing") && sentenceText.length() < 40) {
                    // Adjusted condition to append the predicted label to the sentence
                    // This condition now allows appending regardless of the last character
                    if (sentenceText.isEmpty() || !sentenceText.endsWith(lastPredictedLabel.charAt(0) + "")) {
                        // Add the predicted label to the sentence
                        if (lastPredictedLabel.equals("Space")) {
                            sentence.append(" "); // Assuming you want to add a space
                        } else {
                            sentence.append(lastPredictedLabel.toLowerCase());
                        }
                        // Add the label to the set of added letters
                        addedLetters.add(lastPredictedLabel);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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



