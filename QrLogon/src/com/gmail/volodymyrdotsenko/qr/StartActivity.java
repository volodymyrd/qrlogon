package com.gmail.volodymyrdotsenko.qr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class StartActivity extends Activity {

	private String userName;
	private String token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		userName = intent.getStringExtra("userName");

		setContentView(R.layout.activity_start);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	public void scan(View view) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		// intent.setPackage("com.google.zxing.client.android");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		System.out.println("onActivityResult: ");

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				System.out.println("contents: " + contents);
				System.out.println("format: " + format);
				token = contents;
				new TokenSendTask().execute();
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	private class TokenSendTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			final String url = getString(R.string.token_uri) + "/" + userName
					+ "/" + token;

			System.out.println("doInBackground: " + url);
			
			String responseString = "";

			// UsernamePasswordCredentials creds = new
			// UsernamePasswordCredentials(
			// username, password);

			HttpClient httpclient = new DefaultHttpClient();
			// ((AbstractHttpClient)httpclient).getCredentialsProvider().setCredentials(AuthScope.ANY,
			// creds);
			HttpResponse response;

			try {
				response = httpclient.execute(new HttpGet(url));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				// TODO Handle problems..
			} catch (IOException e) {
				// TODO Handle problems..
			}
			
			return responseString;
		}

	}
}