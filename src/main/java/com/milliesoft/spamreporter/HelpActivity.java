package com.milliesoft.spamreporter;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_layout);
		
		WebView w1 = (WebView)findViewById(R.id.helpWebView);
		w1.setWebViewClient(new WebViewClient());
		w1.loadData("<html><body><h1>7726 Spam Reporter</h1>" +
				"<p>7726 Spam Reporter helps you to report spam messages to your network provider. " +
				"It does this by sending the contents of the spam SMS message to the special number \"7726\" which has been set up by most" +
				" mobile phone networks for reporting spam texts. It then waits for a response (which is generally something like \"Thanks for reporting this " +
				"spam message, now please forward us the number it was sent from\". When it receives a response like that from 7726, it" +
				" automatically sends a message back to 7726 with the spammer's number.</p>" +
				"<h2>FAQ</h2>" +
				"<p><b>Why does it send two messages?</b><br/>" +
				"Because, unlike an e-mail, when you forward an SMS message, it doesn't say who the original sender was. The second SMS sends the details of the sender.</p>" +
				"<p><b>What does it cost to send these message?</b><br/>" +
				"It shouldn't cost anything - 7726 is a free number for reporting spam on most networks. If in doubt, contact your network operator.</p>" +
				"<p><b>Why does the app need to receive SMS messages?</b><br/>" +
				"It needs to do that to get the full list of your SMS message for you to pick from, and it needs to listen for a response from your network provider asking you to send the number of the spammer.</p>" +
				"<p><b>What is your privacy policy?</b><br/>" +
				"The app doesn't collect any data about you. It doesn't have any local storage other than the preferences, it doesn't have any connection to the internet, and doesn't transmit any information other than the spam reporting to 7726. " +
				"It doesn't even read your SMS messages when it is not open.</p>" +
				"<p><b>My operator has a different spam reporting number. Can I change it?</b><br/>" +
				"Yes, in settings you can change the number to whatever you want." +
				"<p><b>The second text with the spammer's number doesn't get sent.</b><br/>" +
				"Most operators will ask you to send the spammer's number after they receive the spam message report, and need you to wait for their message to arrive before " +
				"they understand that you are sending the spammer's number. We therefore wait to receive a reply with the word \"number\" in it before sending the spammer's number. " +
				"If your operator sends a different message for you to reply to, you can change the phrase to look for in the settings. To send the number immediatly without waiting for a " +
				"reply from your operator, enter a blank string in the settings instead." +
				"<p><b>What's the point? Can't I do this myself?</b><br/>" +
				"Yes, you certainly can send the spam to 7726 yourself. This app just makes it a bit easier.</p>" +
				"</body></html>","text/html", "UTF-8");
		
	}

}
