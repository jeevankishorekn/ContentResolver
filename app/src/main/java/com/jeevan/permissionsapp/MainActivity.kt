package com.jeevan.permissionsapp

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeevan.permissionsapp.Adapters.ContactsAdapter
import java.util.*

class MainActivity : AppCompatActivity() {


    private lateinit var contactsAdapter: ContactsAdapter
    lateinit var contactsRecyclerView: RecyclerView
    private var contactItemList = mutableListOf<ContactItem>()
    lateinit var floatingActionButton: FloatingActionButton
    lateinit var contactDialogName: EditText
    lateinit var contactDialogPhone: EditText
    private lateinit var cancelDialog: ImageView
    private lateinit var saveContact: Button
    private lateinit var updateContact: Button
    private lateinit var updatePhone: TextView
    private lateinit var updateDialog: Dialog
    private lateinit var cancelUpdateDialog: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        contactsRecyclerView = findViewById(R.id.contactsList)
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        registerForContextMenu(contactsRecyclerView)
        updateDialog = showUpdateDialog()
        updateContact = updateDialog.findViewById(R.id.updateContact)
        updatePhone = updateDialog.findViewById(R.id.updatePhone)
        var dialog = showAddContactDialog()
        cancelDialog = dialog.findViewById(R.id.cancelUpdateDialog)
        contactDialogName = dialog.findViewById(R.id.contactDialogName)
        contactDialogPhone = dialog.findViewById(R.id.contactDialogPhone)
        saveContact = dialog.findViewById(R.id.updateContact)
        cancelUpdateDialog = updateDialog.findViewById(R.id.cancelUpdateDialog)

        cancelUpdateDialog.setOnClickListener {
            updateDialog.dismiss()
        }

        saveContact.setOnClickListener {
            if (contactDialogName.text.isEmpty() && contactDialogPhone.text.length < 8) {
                Toast.makeText(this, "Please enter Correct values!", Toast.LENGTH_SHORT).show()
                contactDialogName.setText("")
                contactDialogPhone.setText("")
            } else {
                if (addContacts()) {
                    contactItemList = getContactList()
                    contactsAdapter.updateList(contactItemList)
                    Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Contact not added!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        floatingActionButton.setOnClickListener {
            dialog.show()
        }
        cancelDialog.setOnClickListener {
            dialog.cancel()
        }

        updateContact.setOnClickListener {
            if (updatePhone.text.isEmpty() && updatePhone.text.length < 8) {
                Toast.makeText(this, "Please enter Correct values!", Toast.LENGTH_SHORT).show()
                updatePhone.text = ""
            }
            else{
                updateContact()
                contactItemList = getContactList()
                Log.d(TAG, "updateContact: $contactItemList")
                contactsAdapter.updateList(contactItemList)
            }
        }
    }

    private fun showUpdateDialog(): Dialog {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.update_contact_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            Resources.getSystem().displayMetrics.widthPixels - 100,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    @SuppressLint("Range")
    override fun onStart() {
        super.onStart()

        var requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                Log.w(TAG, "inside activity result: ")
                it.entries.forEach { permission ->
                    when (permission.key) {
                        READ_CONTACTS -> {
                            if (!permission.value) {
                                Log.d(TAG, "onStart: User has denied permission")
                            }
                        }
                    }
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "onStart: in tiramisu")
            checkForPermissions(
                requestPermissionLauncher, arrayOf(
                    READ_CONTACTS,
                    WRITE_CONTACTS, READ_MEDIA_VIDEO, READ_MEDIA_IMAGES
                )
            )
        } else
            checkForPermissions(
                requestPermissionLauncher,
                arrayOf(READ_CONTACTS, WRITE_CONTACTS, READ_EXTERNAL_STORAGE)
            )

        contactItemList = getContactList()
        contactsAdapter = ContactsAdapter(contactItemList, this)
        contactsRecyclerView.adapter = contactsAdapter

    }

    private fun checkForPermissions(
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) {

        permissions.forEach {
            Log.d(TAG, "checkForPermissions: $it")
            if (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "checkForPermissions: Permission already granted")
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission required")
                    .setMessage("Permission is required to run this app")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Open Settings"
                    ) { dialog, which ->
                        var intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    }
                    .show()

            } else
                requestPermissionLauncher.launch(permissions)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.custom_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.gallery -> {
                Intent(this, GalleryActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> deleteContact()
            R.id.update -> {
                updateDialog.show()
                Log.d(
                    TAG,
                    "onContextItemSelected: ${contactItemList[contactsAdapter.position].contactNumber}"
                )
            }
            else -> super.onContextItemSelected(item)
        }
        Log.d(TAG, "onContextItemSelected: ${contactsAdapter.position}")
        return true
    }

    @SuppressLint("Range")
    private fun getContactList(): MutableList<ContactItem> {
        val cursorForNumber = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        var list = LinkedList<ContactItem>()
        if (cursorForNumber?.count!! > 0) {
            while (cursorForNumber.moveToNext()) {
                var contactName =
                    cursorForNumber.getString(cursorForNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var contactNumber =
                    cursorForNumber.getString(cursorForNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                list.add(ContactItem(contactName, contactNumber))
            }
        }
        return list
    }

    private fun showAddContactDialog(): Dialog {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.add_contact_dialogue)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(
            Resources.getSystem().displayMetrics.widthPixels - 100,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        return dialog
    }


    private fun addContacts(): Boolean {
        var list: ArrayList<ContentProviderOperation> = arrayListOf()
        list.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        list.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contactDialogName.text.toString()
                )
                .build()
        )

        list.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    contactDialogPhone.text.toString()
                )
                .build()
        )
        var results = contentResolver.applyBatch(ContactsContract.AUTHORITY, list)

        return results != null
    }

    private fun updateContact() {

        var contentValues = ContentValues()
        contentValues.put(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            updatePhone.text.toString()
        )

        var cursor = contentResolver.update(
            ContactsContract.Data.CONTENT_URI,
            contentValues,
            ContactsContract.CommonDataKinds.Phone.NUMBER + "=? AND " + ContactsContract.Data.DISPLAY_NAME + "=?",
            arrayOf(
                contactItemList[contactsAdapter.position].contactNumber,
                contactItemList[contactsAdapter.position].name
            )
        )
        Log.d(TAG, "updateContact: $cursor")
        if (cursor > 0) {
            Toast.makeText(this, "Contact is updated successfully", Toast.LENGTH_SHORT).show()
            contactItemList = getContactList()
            Log.d(TAG, "updateContact: $contactItemList")
            contactsAdapter.notifyItemChanged(contactsAdapter.position)
            updatePhone.text = ""
            updateDialog.dismiss()
        } else {
            Toast.makeText(this, "Contact Update Failed!", Toast.LENGTH_SHORT).show()
            updatePhone.text = ""
        }
    }

    private fun deleteContact() {
        var cursor = contentResolver.delete(
            ContactsContract.RawContacts.CONTENT_URI,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + "=?",
            arrayOf(contactItemList[contactsAdapter.position].name)
        )
        Log.d(TAG, "deleteContact: Deleted $cursor rows")
        if (cursor > 0) {
            Toast.makeText(
                this,
                "Deleted ${contactItemList[contactsAdapter.position].name} contact",
                Toast.LENGTH_SHORT
            ).show()
            contactItemList.removeAt(contactsAdapter.position)
            contactsAdapter.notifyItemRemoved(contactsAdapter.position)
        }
    }


}