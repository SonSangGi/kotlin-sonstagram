package dev.sanggi.sonstagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dev.sanggi.sonstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    val TAG = AddPhotoActivity::class.simpleName

    val PICK_IMAGE_FROM_ABMUM = 0

    var photoUri: Uri? = null

    var firestore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // Firebase Storage
        storage = FirebaseStorage.getInstance()
        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Firebase database
        firestore = FirebaseFirestore.getInstance()

        // open album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ABMUM)

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ABMUM) {
            photoUri = data?.data
            addphoto_image.setImageURI(photoUri)

        } else finish()
    }

    fun contentUpload() {
        //Make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_$timestamp.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->

            var contentDTO = ContentDTO()

            contentDTO.imageUrl = uri.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
                ?.addOnSuccessListener { Log.i(TAG, "O") }
                ?.addOnFailureListener { e -> Log.i(TAG, "Error adding document", e) }

            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}
