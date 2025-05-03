package com.example.flashtorch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.flashtorch.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashlightOn = false;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Button flashTorchButton;
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flashTorchButton = binding.flashButton; // Use view binding

        // Check for permission before accessing the camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with camera initialization
            initializeCameraAndButton();
        }
    }

    private void initializeCameraAndButton() {
        Context context = requireContext();
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(context, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            flashTorchButton.setEnabled(false);
            return;
        }

        flashTorchButton.setOnClickListener(new View.OnClickListener() { // Corrected setOnClickListener
            @Override
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager.setTorchMode(cameraId, !isFlashlightOn);
                        isFlashlightOn = !isFlashlightOn;
                        flashTorchButton.setText(isFlashlightOn ? "Turn Off Flashlight" : "Turn On Flashlight");
                    } else {
                        Toast.makeText(context, "Flashlight requires Android M (API 23) or higher", Toast.LENGTH_SHORT).show();
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Camera access error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCameraAndButton();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to use the flashlight.", Toast.LENGTH_LONG).show();
                flashTorchButton.setEnabled(false);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isFlashlightOn && cameraManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, false);
                }
                isFlashlightOn = false;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
