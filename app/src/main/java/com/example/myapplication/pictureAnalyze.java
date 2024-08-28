package com.example.myapplication;

import static org.opencv.imgproc.Imgproc.putText;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.os.Bundle;
import android.util.Range;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageInputConfig;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;

import androidx.camera.core.internal.TargetConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.ExecutionException;

public class pictureAnalyze extends AppCompatActivity {


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OpenCVLoader.initLocal();
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_picture_analyze);


        ImageView imageView=findViewById(R.id.imageView1);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture= ProcessCameraProvider.getInstance(this);



        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()

                        // enable the following line if RGBA output is needed.
                     //   .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)

                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

                        .build();




        Preview preview = new Preview.Builder().setTargetFrameRate(new Range<>(100,120))
                .build() ;

        imageAnalysis.setAnalyzer(CameraXExecutors.mainThreadExecutor(), new ImageAnalysis.Analyzer() {


            @ExperimentalGetImage
            @Override
            public void analyze(@NonNull ImageProxy image) {


               Image image1= image.getImage();

              Mat src = ImageUtil.convertYUVtoMat(image1);

                // 转换为Bitmap
                Bitmap bitmap =Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(src, bitmap);

                imageView.setImageBitmap(bitmap  );
                image.close();


            }
        });

        try {



          Camera camera=  cameraProviderFuture.get().bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


}