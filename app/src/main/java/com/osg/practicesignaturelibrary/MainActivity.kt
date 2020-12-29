package com.osg.practicesignaturelibrary

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
            val savePath = bitmapToFile(signature, context)
            Log.d("file path",savePath.encodedPath.toString())
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