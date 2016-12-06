package edu.orangecoastcollege.cs273.smssender;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_CONTACT = 100;
    private static final int REQUEST_CODE_SEND_SMS = 10;
    private ArrayList<Contact> contactsList;
    private ContactsAdapter contactsAdapter;
    private DBHelper db;
    private ListView contactsListView;
    private EditText messageEditText;
    private Button sendTextMessageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        db = new DBHelper(this);
        contactsList = db.getAllContacts();
        contactsAdapter = new ContactsAdapter(this, R.layout.contact_list_item, contactsList);
        contactsListView = (ListView) findViewById(R.id.contactsListView);
        contactsListView.setAdapter(contactsAdapter);

        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendTextMessageButton = (Button) findViewById(R.id.sendTextMessageButton);
    }

    public void addContacts(View view) {
        Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactIntent, REQUEST_CODE_ADD_CONTACT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_CONTACT && resultCode == Activity.RESULT_OK)
        {
            Uri contactData = data.getData();
            Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
            if (cursor.moveToFirst())
            {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Contact contact = new Contact(name, number);

                db.addContact(contact);
                contactsAdapter.add(contact);
            }
            cursor.close();
        }
    }

    public void deleteContact(View view) {
        if (view instanceof LinearLayout)
        {
            Contact selectedContact = (Contact) view.getTag();
            db.deleteContact(selectedContact.getId());
            contactsAdapter.remove(selectedContact);
            Toast.makeText(this, "Contact Deleted: " + selectedContact.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendTextMessage(View view) {
        String message = messageEditText.getText().toString();
        if (message.trim().isEmpty())
        {
            Toast.makeText(this, "Message is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contactsList.size() == 0)
        {
            Toast.makeText(this, "Please add a contact.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Ask for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_SEND_SMS);
        }
        else {
            // Define a reference to the SmsManager (manages text messages)
            SmsManager manager = SmsManager.getDefault();
            for (Contact contact : contactsList) {
                manager.sendTextMessage(contact.getPhone(), null, message, null, null);
            }
            if (contactsList.size() > 1) {
                Toast.makeText(this, "Message sent to " + contactsList.size() + " contacts.", Toast.LENGTH_SHORT).show();
            }
            else if (contactsList.size() == 1)
            {
                Toast.makeText(this, "Message sent to " + contactsList.get(0).getName(), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
