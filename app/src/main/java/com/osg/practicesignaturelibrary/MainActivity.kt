package com.osg.practicesignaturelibrary

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Dimension.Companion.value
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.UiSavedStateRegistryAmbient
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.gcacace.signaturepad.views.SignaturePad
import com.osg.practicesignaturelibrary.ui.PracticeSignatureLibraryTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.jar.Attributes

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticeSignatureLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    SignatureView()
                }
            }
        }
    }
}

@Composable
fun SignatureView() {
    val context = AmbientContext.current
    val attrs: AttributeSet? = null
    val customView = remember {
        SignaturePad(context, attrs).apply { }
    }
    Column() {
        var signaturePad = SignaturePad(context, attrs)
        var signature: Bitmap

        Column(Modifier.height(300.dp)) {
            AndroidView(viewBlock = { customView }) {
                signaturePad = it
                signaturePad.setMinWidth(2f)
                signaturePad.setMaxWidth(2f)
            }
        }
        Button(onClick = {
            signature = signaturePad.signatureBitmap
            val savePath = saveImageInAlbum(context, "signature.jpg", signature)
            Log.d("file path",savePath.toString())
        }) {
            Text("save")
        }
        Button(onClick = {
            signaturePad.clearView()
        }) {
            Text("clear")
        }
    }

}

fun bitmapToFile(bitmap:Bitmap, context: Context): Uri {
    // Get the context wrapper
    val wrapper = ContextWrapper(context)

    // Initialize a new file instance to save bitmap object
    var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
    file = File(file,"saved-signature.jpg")

    try{
        // Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        stream.flush()
        stream.close()
    }catch (e: IOException){
        e.printStackTrace()
    }

    // Return the saved bitmap uri
    return Uri.parse(file.absolutePath)
}

fun saveImageInAlbum(context: Context, fileName: String, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues()
        with(values) {
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = context.getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val fos = context.contentResolver.openOutputStream(uri!!)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos?.run {
            flush()
            close()
        }
    } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                File.separator
        val file = File(dir)
        if (!file.exists()) {
            file.mkdirs()
        }

        val imgFile = File(file, "test_capture.jpg")
        val os = FileOutputStream(imgFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()

        val values = ContentValues()
        with(values) {
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.BUCKET_ID, fileName)
            put(MediaStore.Images.Media.DATA, imgFile.absolutePath)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}