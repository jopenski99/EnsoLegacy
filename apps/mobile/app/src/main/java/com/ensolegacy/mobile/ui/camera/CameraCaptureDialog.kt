package com.ensolegacy.mobile.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ensolegacy.mobile.EnsoApp

/**
 * Full-screen in-app camera (CameraX). Shows a live preview and a shutter
 * button; on capture it writes the frame straight into app storage via
 * [com.ensolegacy.mobile.data.ImageStore] and returns the new relative path
 * through [onCaptured]. Handles the runtime camera permission inline.
 *
 * Rendered as a [Dialog] so it floats above whatever launched it (including a
 * bottom sheet, e.g. the add-milestone flow).
 */
@Composable
fun CameraCaptureDialog(
    onCaptured: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val context = LocalContext.current
        val imageStore = remember { (context.applicationContext as EnsoApp).imageStore }

        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED,
            )
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> hasPermission = granted }

        // Ask the first time we're shown without permission.
        if (!hasPermission) {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            if (hasPermission) {
                CameraPreview(
                    onCaptured = onCaptured,
                    imageStore = imageStore,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                PermissionPrompt(
                    onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close camera", tint = Color.White)
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onCaptured: (String) -> Unit,
    imageStore: com.ensolegacy.mobile.data.ImageStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    var capturing by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                    )
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )

        // Shutter button.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(if (capturing) Color.Gray else Color.White)
                .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .clickable(enabled = !capturing) {
                    capturing = true
                    val output = imageStore.newImageFile()
                    val options = ImageCapture.OutputFileOptions.Builder(output.file).build()
                    imageCapture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                                capturing = false
                                onCaptured(output.relativePath)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                imageStore.delete(output.relativePath)
                            }
                        },
                    )
                },
        )
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Camera access is needed to photograph your tree.",
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onGrant, modifier = Modifier.padding(top = 16.dp)) {
            Text("Allow camera")
        }
    }
}
