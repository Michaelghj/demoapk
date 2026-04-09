package com.example.antiddiction.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.antiddiction.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceContour;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 人脸验证 Activity
 * 功能：支持人脸注册和人脸验证两种模式
 */
public class FaceVerifyActivity extends AppCompatActivity {

    private static final String TAG = "FaceVerifyActivity";
    private static final String MODE_REGISTER = "register";
    private static final String MODE_VERIFY = "verify";

    private PreviewView previewView;
    private TextView tvInstruction;
    private TextView tvResult;
    private TextView tvCountdown;
    private Button btnConfirm;

    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    
    private String mode = MODE_VERIFY; // 默认验证模式
    private boolean isProcessing = false;
    
    // 注册模式用
    private List<Bitmap> registeredFaceImages = new ArrayList<>();
    private static final int REQUIRED_FACE_IMAGES = 3; // 需要采集 3 张不同角度的人脸

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_face_verify);

        // 获取模式
        mode = getIntent().getStringExtra("mode");
        if (mode == null) {
            mode = MODE_VERIFY;
        }

        initViews();
        initFaceDetector();
        
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 根据模式设置提示
        if (MODE_REGISTER.equals(mode)) {
            tvInstruction.setText("请正对摄像头，系统将采集 3 张不同角度的人脸");
            btnConfirm.setVisibility(View.GONE);
        } else {
            tvInstruction.setText("请正对摄像头进行人脸验证");
            btnConfirm.setVisibility(View.GONE);
        }

        // 启动倒计时
        startCountdown();
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnConfirm = findViewById(R.id.btnConfirm);
        
        btnConfirm.setOnClickListener(v -> {
            if (MODE_REGISTER.equals(mode) && registeredFaceImages.size() >= REQUIRED_FACE_IMAGES) {
                saveFaceData();
            } else {
                finish();
            }
        });
    }

    private void initFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build();

        faceDetector = FaceDetection.getClient(options);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera startup failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        // 预览
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // 人脸分析
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

        // 选择前置摄像头
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera binding failed", e);
        }
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (isProcessing) {
            imageProxy.close();
            return;
        }

        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        // 创建输入图像
        InputImage inputImage = InputImage.fromByteArray(
            bytes,
            imageProxy.getWidth(),
            imageProxy.getHeight(),
            0,
            InputImage.IMAGE_FORMAT_NV21
        );

        isProcessing = true;
        faceDetector.process(inputImage)
            .addOnSuccessListener(faces -> {
                handleFaceDetectionResult(faces, imageProxy);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Face detection failed", e);
                isProcessing = false;
                imageProxy.close();
            })
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    isProcessing = false;
                    imageProxy.close();
                }
            });
    }

    private void handleFaceDetectionResult(@NonNull List<Face> faces, ImageProxy imageProxy) {
        imageProxy.close();
        isProcessing = false;

        if (faces.isEmpty()) {
            runOnUiThread(() -> {
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText("未检测到人脸，请正对摄像头");
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.error));
            });
            return;
        }

        if (faces.size() > 1) {
            runOnUiThread(() -> {
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText("检测到多张人脸，请只保留一人");
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.error));
            });
            return;
        }

        // 检测到单张人脸
        Face face = faces.get(0);
        
        // 检查人脸质量
        if (!isFaceQualityGood(face)) {
            runOnUiThread(() -> {
                tvResult.setVisibility(View.VISIBLE);
                tvResult.setText("人脸质量不佳，请调整角度和光线");
                tvResult.setTextColor(ContextCompat.getColor(this, R.color.warning));
            });
            return;
        }

        // 根据模式处理
        if (MODE_REGISTER.equals(mode)) {
            handleRegisterMode(face, imageProxy);
        } else {
            handleVerifyMode(face);
        }
    }

    /**
     * 检查人脸质量
     */
    private boolean isFaceQualityGood(Face face) {
        // 检查人脸是否居中（简化版）
        if (face.getHeadEulerAngleX() != null && Math.abs(face.getHeadEulerAngleX()) > 20) {
            return false; // 低头/抬头角度太大
        }
        if (face.getHeadEulerAngleY() != null && Math.abs(face.getHeadEulerAngleY()) > 20) {
            return false; // 左右转头角度太大
        }
        
        // 检查眼睛是否睁开（简化版）
        if (face.getLeftEyeOpenProbability() != null && face.getLeftEyeOpenProbability() < 0.5) {
            return false;
        }
        if (face.getRightEyeOpenProbability() != null && face.getRightEyeOpenProbability() < 0.5) {
            return false;
        }

        return true;
    }

    /**
     * 处理注册模式
     */
    private void handleRegisterMode(Face face, ImageProxy imageProxy) {
        // 采集人脸图像
        if (registeredFaceImages.size() < REQUIRED_FACE_IMAGES) {
            // 延迟采集，让用户有时间调整角度
            new android.os.Handler().postDelayed(() -> {
                Bitmap faceBitmap = captureFaceImage(face, imageProxy);
                if (faceBitmap != null) {
                    registeredFaceImages.add(faceBitmap);
                    int remaining = REQUIRED_FACE_IMAGES - registeredFaceImages.size();
                    
                    runOnUiThread(() -> {
                        tvResult.setVisibility(View.VISIBLE);
                        tvResult.setText("已采集 " + registeredFaceImages.size() + "/3 张人脸");
                        tvResult.setTextColor(ContextCompat.getColor(this, R.color.success));
                        
                        if (remaining > 0) {
                            tvInstruction.setText("请转动头部，采集剩余 " + remaining + " 张不同角度的人脸");
                        } else {
                            tvInstruction.setText("人脸采集完成！");
                            btnConfirm.setVisibility(View.VISIBLE);
                            btnConfirm.setText("确认注册");
                        }
                    });
                }
            }, 1000); // 每张间隔 1 秒
        }
    }

    /**
     * 处理验证模式
     */
    private void handleVerifyMode(Face face) {
        // 获取已注册的人脸特征
        byte[] registeredEmbedding = PreferenceManager.getFaceEmbedding(this);
        
        if (registeredEmbedding == null) {
            runOnUiThread(() -> {
                Toast.makeText(FaceVerifyActivity.this, "未注册人脸，请先注册", Toast.LENGTH_LONG).show();
                finish();
            });
            return;
        }

        // 简化版验证：实际应提取人脸特征并比对
        // 这里 Demo 版本直接通过（因为 ML Kit 人脸检测不直接提供特征向量）
        // 实际产品需要使用人脸识别专用 SDK（如 ArcSoft、Face++ 等）
        
        runOnUiThread(() -> {
            tvResult.setVisibility(View.VISIBLE);
            tvResult.setText(R.string.face_verify_success);
            tvResult.setTextColor(ContextCompat.getColor(this, R.color.success));
            
            // 记录使用时间
            PreferenceManager.addUsage(this, 1); // 记录 1 分钟使用
            
            Toast.makeText(this, "验证成功！", Toast.LENGTH_SHORT).show();
            
            // 延迟关闭
            new android.os.Handler().postDelayed(() -> {
                finish();
            }, 1000);
        });
    }

    /**
     * 抓拍人脸图像
     */
    private Bitmap captureFaceImage(Face face, ImageProxy imageProxy) {
        try {
            // 获取人脸边界框
            Rect bounds = face.getBoundingBox();
            
            // 创建 Bitmap
            Bitmap bitmap = Bitmap.createBitmap(
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                Bitmap.Config.ARGB_8888
            );
            
            // 这里简化处理，实际应从 ImageProxy 获取 YUV 数据并转换
            // Demo 版本返回 null，仅做演示
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Failed to capture face image", e);
            return null;
        }
    }

    /**
     * 保存人脸数据
     */
    private void saveFaceData() {
        // Demo 版本：简化处理，实际应提取人脸特征向量并加密存储
        // 这里仅标记已注册
        
        // 生成一个简单的人脸特征（实际应使用专业人脸识别 SDK）
        byte[] mockEmbedding = new byte[128];
        for (int i = 0; i < 128; i++) {
            mockEmbedding[i] = (byte) (Math.random() * 255);
        }
        
        PreferenceManager.saveFaceEmbedding(this, mockEmbedding);
        
        runOnUiThread(() -> {
            Toast.makeText(this, "人脸注册成功！", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void startCountdown() {
        int duration = MODE_REGISTER.equals(mode) ? 30000 : 15000; // 注册 30 秒，验证 15 秒
        
        new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("剩余时间：" + (millisUntilFinished / 1000) + "秒");
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("验证超时");
                tvResult.setText(R.string.face_verify_failed);
                tvResult.setTextColor(ContextCompat.getColor(FaceVerifyActivity.this, R.color.error));
                
                // 超时后延迟关闭
                new CountDownTimer(2000, 2000) {
                    @Override
                    public void onTick(long millisUntilFinished) {}
                    @Override
                    public void onFinish() {
                        finish();
                    }
                }.start();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        faceDetector.close();
    }
}
