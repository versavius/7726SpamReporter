package com.milliesoft.spamreporter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SendActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private BroadcastReceiver smsReceiver;
    private String spamNumber="7726";
    private String spamResponder;
    private String numberRequested="number";
    Boolean numberSent=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_layout);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		spamNumber = sharedPref.getString("spam_number", "");
		numberRequested = sharedPref.getString("number_request", "");

		if(spamNumber.startsWith("0")){
			spamResponder=spamNumber.substring(1);
		}else {
			spamResponder=spamNumber;
		}
		
		smsReceiver=new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	Bundle smsBundle=intent.getExtras();
	        	if(smsBundle!=null){
	        	  Object[] incoming=(Object[])smsBundle.get("pdus");
	        	  for (int i = 0; i < incoming.length; i++) {

	        	  SmsMessage currentMessage=SmsMessage.createFromPdu((byte[])incoming[i]);
	        	  String phoneNumber=currentMessage.getDisplayOriginatingAddress();
	        	  if(phoneNumber.contains(spamResponder)){
	        		  String currentResponse=currentMessage.getDisplayMessageBody();
	        	        TextView smsResponseText = (TextView)findViewById(R.id.smsResponseText);
	        	        smsResponseText.setText("Response:"+currentResponse);
	        		  if(currentResponse.contains(numberRequested) && numberRequested.trim().length()>0&&!numberSent){
	        		  sendSpamReportNumber();
	        		  }
	        	  }
	        	  }
	        	}

	        }
		};
	    
        final IntentFilter receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

	this.registerReceiver(smsReceiver, receiveFilter);  
	numberSent=false;
	    // Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();

	    if("REPORT_SPAM".equals(action)){
	    	handleSendFromList(intent);
	    }
	    else if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        } 
	    } 
	            		
        	TextWatcher emptyTextWatcher=	new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
		        Button sendButton = (Button)findViewById(R.id.sendButton);
		    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
		    	EditText messageTextBox = (EditText)findViewById(R.id.incomingMessage);
				if(numberTextBox.getEditableText().length()>0&&messageTextBox.getEditableText().length()>0){
                    sendButton.setEnabled(true);
				} else {
                    sendButton.setEnabled(false);					
				}
			}
        	
        };

    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
    	numberTextBox.addTextChangedListener(emptyTextWatcher);
    	EditText messageTextBox = (EditText)findViewById(R.id.incomingMessage);
    	messageTextBox.addTextChangedListener(emptyTextWatcher);

	}

	void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        TextView informationText = (TextView)findViewById(R.id.informationText);
        informationText.setText("");
        Button exitButton = (Button)findViewById(R.id.exitButton);
        exitButton.setVisibility(Button.GONE);
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setVisibility(Button.VISIBLE);
	    if (sharedText != null) {
	    	
	    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
	    	EditText messageTextBox = (EditText)findViewById(R.id.incomingMessage);
	    	String[] messageLines=sharedText.split("\n");
	    	if(messageLines.length>=2){
	    		String fromText=messageLines[0];
	    		if(fromText.startsWith("From")){
	    			fromText=fromText.substring(fromText.indexOf(" "));
	    		}
	    		int openBracket=fromText.indexOf("(");
	    		int closeBracket=fromText.indexOf(")");
	    		if( closeBracket > openBracket ){
	    			fromText=fromText.substring(openBracket+1, closeBracket) ;
	    		}
	    		numberTextBox.setText(fromText);
	    		String messageBody=messageLines[1];
	    		if(messageLines.length>3){
	    			for(int i=2;i<messageLines.length-1;i++){
	    				messageBody=messageBody+"\n"+messageLines[i];
	    			}
	    		}
	    		messageTextBox.setText(messageBody);
	            messageTextBox.setEnabled(true);
	            numberTextBox.setEnabled(true);

	    	} else {
	    		messageTextBox.setText(sharedText);

				Cursor cursor = this.getContentResolver()
						.query(Telephony.Sms.Inbox.CONTENT_URI,
								new String[] { Telephony.Sms.Inbox._ID, Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.BODY
								}, null, null,
								Telephony.Sms.Inbox.DATE+" COLLATE LOCALIZED ASC");
				Boolean found=false;
				if (cursor != null) {
					cursor.moveToLast();
					if (cursor.getCount() > 0) {

						do {

							if(cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY)).equals(sharedText)){
								numberTextBox.setText(cursor.getString(cursor
										.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)));
								found=true;
							}
						} while (!found&&cursor.moveToPrevious());
					}
				}
				if(found){
					numberTextBox.setEnabled(true);
				} else {
					informationText.setText("Could not extract sender's number. If this is an SMS, you can edit to put the spammer's number in.");
					sendButton.setEnabled(false);
				}
	    		
	    	}
	    }
	}
	void handleSendFromList(Intent intent) {
        TextView informationText = (TextView)findViewById(R.id.informationText);
        informationText.setText("");
        Button exitButton = (Button)findViewById(R.id.exitButton);
        exitButton.setVisibility(Button.GONE);
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setVisibility(Button.VISIBLE);
	    	String messageBody=intent.getStringExtra("MESSAGE");
	    	String fromText=intent.getStringExtra("NUMBER");
	    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
	    	EditText messageTextBox = (EditText)findViewById(R.id.incomingMessage);

	    		messageTextBox.setText(messageBody);
	    		numberTextBox.setText(fromText);
	            messageTextBox.setEnabled(true);
	            numberTextBox.setEnabled(true);

	    
	}
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
        this.unregisterReceiver(this.smsReceiver);
	    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

	public void sendSpamReport(View view){
        SmsManager smsManager = SmsManager.getDefault();
    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
    	EditText messageTextBox = (EditText)findViewById(R.id.incomingMessage);
        smsManager.sendTextMessage(spamNumber, null, messageTextBox.getText().toString(), null, null);
        messageTextBox.setEnabled(false);
        numberTextBox.setEnabled(false);
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setVisibility(Button.GONE);
        TextView informationText = (TextView)findViewById(R.id.informationText);
        
        if(numberRequested.trim().length()>0){
        informationText.setText("Reported spam message body. Please wait for a response before we send the spammer's number.");
        Button exitButton = (Button)findViewById(R.id.exitButton);
        exitButton.setVisibility(Button.VISIBLE);
        exitButton.setText("Cancel");
        } else {
        	sendSpamReportNumber();
        }
	}
	public void sendSpamReportNumber(){
        SmsManager smsManager = SmsManager.getDefault();
    	EditText numberTextBox = (EditText)findViewById(R.id.incomingNumber);
        smsManager.sendTextMessage(spamNumber, null, numberTextBox.getText().toString(), null, null);
        numberSent=true;
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setVisibility(Button.GONE);
        TextView informationText = (TextView)findViewById(R.id.informationText);
        informationText.setText("Reported spammer's number too. We're done.");
        Button exitButton = (Button)findViewById(R.id.exitButton);
        exitButton.setVisibility(Button.VISIBLE);
        exitButton.setText("Done");
	}
	
	public void exitApp(View view){
		onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		}else if (id == R.id.action_settings) {
		    Intent intent = new Intent(this, SettingsActivity.class);
		    startActivity(intent);
		    return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		spamNumber = sharedPref.getString("spam_number", "");
		numberRequested = sharedPref.getString("number_request", "");
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("spam_number")){
		spamNumber = sharedPreferences.getString("spam_number", "");

		if(spamNumber.startsWith("0")){
			spamResponder=spamNumber.substring(1);
		}else {
			spamResponder=spamNumber;
		}
		} else if (key.equals("number_request")){
			numberRequested = sharedPreferences.getString("number_request", "");			
		}
		
	}
}
