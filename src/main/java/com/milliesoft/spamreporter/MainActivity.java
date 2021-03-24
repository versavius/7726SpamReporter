package com.milliesoft.spamreporter;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Telephony;

import java.text.DateFormat;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int TYPE_INCOMING_MESSAGE = 2;
    private ListView messageList;
    private MessageListAdapter messageListAdapter;
    private ArrayList<Message> recordsStored;
    private ArrayList<Message> listInboxMessages;
    private ProgressDialog progressDialogInbox;
    private CustomHandler customHandler;
    String spamNumber;
    SharedPreferences sharedPref;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		spamNumber = sharedPref.getString("spam_number", "");
		listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if(key.equals("spam_number")){
				spamNumber = sharedPreferences.getString("spam_number", "");
		        TextView informationText = (TextView)findViewById(R.id.welcomeText);
		        informationText.setText("Click on a message to report spam to "+spamNumber);
				}
			}

		};
		sharedPref.registerOnSharedPreferenceChangeListener(listener);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        initViews();
        TextView informationText = (TextView)findViewById(R.id.welcomeText);
        informationText.setText("Click on a message to report spam to "+spamNumber);

	}

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
		spamNumber = sharedPref.getString("spam_number", "");
        TextView informationText = (TextView)findViewById(R.id.welcomeText);
        informationText.setText("Click on a message to report spam to "+spamNumber);

        populateMessageList();
   }
	@Override
	protected void onPause() {
	    super.onPause();
	    sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
	}
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
	}

    private void initViews() {
        customHandler = new CustomHandler(this);
        progressDialogInbox = new ProgressDialog(this);

        recordsStored = new ArrayList<Message>();

        messageList = (ListView) findViewById(R.id.messageList);
        populateMessageList();
        messageList.setClickable(true);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {


			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Message messageClicked=recordsStored.get(position);
				reportSpam(messageClicked);

		//		Toast.makeText(MainActivity.this, messageClicked.messageContent, Toast.LENGTH_SHORT).show();
				
			}
        	});
    }
    
    public void reportSpam(Message spamMessage){
		Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra("MESSAGE",spamMessage.messageContent);
        intent.putExtra("NUMBER",spamMessage.messageNumber);
        intent.setAction("REPORT_SPAM");
        startActivity(intent);
    }

    public void populateMessageList() {
        fetchInboxMessages();

        messageListAdapter = new MessageListAdapter(this,
                R.layout.message_list_item, recordsStored);
        messageList.setAdapter(messageListAdapter);
    }

    private void showProgressDialog(String message) {
        progressDialogInbox.setMessage(message);
        progressDialogInbox.setIndeterminate(true);
        progressDialogInbox.setCancelable(true);
        progressDialogInbox.show();
    }

    private void fetchInboxMessages() {
        if (listInboxMessages == null) {
            showProgressDialog("Fetching Inbox Messages...");
            startThread();
        } else {
            // messageType = TYPE_INCOMING_MESSAGE;
            recordsStored = listInboxMessages;
            messageListAdapter.setArrayList(recordsStored);
        }
    }

    public class FetchMessageThread extends Thread {

        public int tag = -1;

        public FetchMessageThread(int tag) {
            this.tag = tag;
        }

        @Override
        public void run() {

            recordsStored = fetchInboxSms(TYPE_INCOMING_MESSAGE);
            listInboxMessages = recordsStored;
            customHandler.sendEmptyMessage(0);

        }

    }
    
    public ArrayList<Message> fetchInboxSms(int type) {
        ArrayList<Message> smsInbox = new ArrayList<Message>();
        ArrayList<String[]> numbers = new ArrayList<String[]>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean lookup_names = sharedPref.getBoolean("lookup_names", false);
Boolean found=false;

        Cursor cursor = this.getContentResolver()
                .query(Telephony.Sms.Inbox.CONTENT_URI,
                        new String[] { Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.BODY,Telephony.Sms.Inbox.PERSON
                                 }, null, null,
                                 Telephony.Sms.Inbox.DATE+" COLLATE LOCALIZED ASC");
        if (cursor != null) {
            cursor.moveToLast();
            if (cursor.getCount() > 0) {

                do {
                  /*  String date =  cursor.getString(cursor.getColumnIndex("date"));
                    Long timestamp = Long.parseLong(date);    
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");*/
                    Message message = new Message();
                    message.messageNumber = cursor.getString(cursor
                            .getColumnIndex(Telephony.Sms.Inbox.ADDRESS));
                    message.messageContent = cursor.getString(cursor
                            .getColumnIndex(Telephony.Sms.Inbox.BODY));

                    if(lookup_names) {
                        try {
                            found = false;
                            for (String[] number : numbers) {
                                if (number[0].equals(message.messageNumber)) {
                                    message.messageNumber = number[1];
                                    message.isKnown = true;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                int personIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.PERSON);
                                if (!cursor.isNull(personIndex)) {
                                    //  int personId=cursor.getInt(personIndex);
                                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(message.messageNumber));
                                    Cursor contactCursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                                    if (contactCursor != null && contactCursor.moveToFirst()) {
                                        int personNameIndex = contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                                        if (!contactCursor.isNull(personNameIndex)) {
                                            String[] number = {message.messageNumber, contactCursor.getString(personNameIndex)};
                                            numbers.add(number);
                                            message.messageNumber = contactCursor.getString(personNameIndex);
                                            message.isKnown = true;
                                        }
                                    }
                                } else{
                                    int addressIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS);
                                    if(!cursor.isNull(addressIndex)){
                                      String name = getContactName(cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)));
                                      if (name!=null){
                                          String[] number = {message.messageNumber,name};
                                          numbers.add(number);
                                          message.messageNumber = name;
                                          message.isKnown = true;

                                      }
                                    }
                                }
                            }
                            }catch(Exception e1){
                            e1.printStackTrace();
                            }

                    }

                   // String personId=cursor.isNull(personIndex)?"":cursor.getString(personIndex);

                    /*Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(message.messageNumber));

                    Cursor contactCursor = getContentResolver().query(contactUri, null, null, null, null);

                    if(contactCursor.moveToFirst()){
                        int personNameIndex = contactCursor.getColumnIndex(Contacts.Phones.DISPLAY_NAME);
                        message.messageNumber = contactCursor.isNull(personNameIndex)? message.messageNumber:contactCursor.getString(personNameIndex);
                        //etc
                    }*/


               //     message.messageDate = formatter.format(calendar.getTime());
                    smsInbox.add(message);
                } while (cursor.moveToPrevious());
            }
        }

        return smsInbox;

    }

    public String getContactName( String phoneNumber) {
        ContentResolver cr = this.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }


    private FetchMessageThread fetchMessageThread;

    private int currentCount = 0;

    public synchronized void startThread() {

        if (fetchMessageThread == null) {
            fetchMessageThread = new FetchMessageThread(currentCount);
            fetchMessageThread.start();
        }
    }

    public synchronized void stopThread() {
        if (fetchMessageThread != null) {
            FetchMessageThread moribund = fetchMessageThread;
            currentCount = fetchMessageThread.tag == 0 ? 1 : 0;
            fetchMessageThread = null;
            moribund.interrupt();
        }
    }

    static class CustomHandler extends Handler {
        private final WeakReference<MainActivity> activityHolder;

        CustomHandler(MainActivity inboxListActivity) {
            activityHolder = new WeakReference<MainActivity>(inboxListActivity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {

            MainActivity inboxListActivity = activityHolder.get();
            if (inboxListActivity.fetchMessageThread != null
                    && inboxListActivity.currentCount == inboxListActivity.fetchMessageThread.tag) {
                inboxListActivity.fetchMessageThread = null;

                inboxListActivity.messageListAdapter
                        .setArrayList(inboxListActivity.recordsStored);
                inboxListActivity.progressDialogInbox.dismiss();
            }
        }
    }

    private OnCancelListener dialogCancelListener = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            stopThread();
        }

    };


    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_tps);

        if (item != null) {
            String countryISOCode="";
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
            if (telephonyManager != null){
                countryISOCode = telephonyManager.getSimCountryIso();
            }
            if(!countryISOCode.equals("gb")) {

                item.setVisible(false);
            }

        }
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        } else    if (id == R.id.action_tps) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tpsonline.org.uk/tps/number_type.html"));
            startActivity(browserIntent);
                return true;
		} else if (id == R.id.action_settings) {
		    Intent intent = new Intent(this, SettingsActivity.class);
		    startActivity(intent);
		    return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
