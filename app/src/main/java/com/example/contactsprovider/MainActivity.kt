package com.example.contactsprovider

import android.Manifest.permission.READ_CONTACTS
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class MainActivity : AppCompatActivity() {

    private lateinit var listView:ListView
    private lateinit var sharedPref: SharedPreferences
    private var appOpenTimes = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        listView=findViewById(R.id.listView)

        sharedPref = getPreferences(Context.MODE_PRIVATE)
        appOpenTimes = sharedPref.getInt("appOpenTimes", 0);

        if(appOpenTimes >= 1){
             if (checkContactAccessPermission()) {

            accessContacts()
        } else {
            requestContactAccessPermission()
        }
        }
        Log.d("appOpenTimes","$appOpenTimes")


    }

    private fun checkContactAccessPermission(): Boolean {
        // here we are checking one permission that is contacts
        // if permission is granted then we are returning
        // true otherwise false.

        val contact_permission = ContextCompat.checkSelfPermission(applicationContext, READ_CONTACTS)
        return contact_permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactAccessPermission() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.READ_CONTACTS)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Toast.makeText(this@MainActivity,"Permission Granted",Toast.LENGTH_SHORT).show()
                    accessContacts()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    showRationDialogPermissions()                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationDialogPermissions()
                }

            }).onSameThread().check()
    }
    private fun showRationDialogPermissions(){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required for this feature.It cam be enabled under Application settings")
            .setPositiveButton("Go to SETTINGS"){
                    _,_->
                try {

                    val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }.show()
    }


    private fun accessContacts(){

        val cursor : Cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)!!
        startManagingCursor(cursor)

        val data = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME , ContactsContract.CommonDataKinds.Phone.NUMBER , ContactsContract.CommonDataKinds.Phone._ID)
        val int = intArrayOf(android.R.id.text1, android.R.id.text2)
        // creation of adapter using SimpleCursorAdapter class
        // creation of adapter using SimpleCursorAdapter class
        val adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, data, int)

        // Calling setAdaptor() method to set created adapter

        // Calling setAdaptor() method to set created adapter
        listView.adapter = adapter


    }




    override fun onStop() {
        super.onStop()
        ++appOpenTimes
        with(sharedPref.edit()) {
            putInt("appOpenTimes", appOpenTimes)
            apply()
        }
    }




}