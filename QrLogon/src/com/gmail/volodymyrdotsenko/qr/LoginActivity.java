package com.gmail.volodymyrdotsenko.qr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	protected static final String TAG = LoginActivity.class.getSimpleName();

	private ProgressDialog progressDialog;

	private boolean destroyed = false;
	
	private LoginActivity thisLoginActivity = this;

	// ***************************************
	// Public methods
	// ***************************************
	public void showLoadingProgressDialog() {
		this.showProgressDialog("Loading. Please wait...");
	}

	public void showProgressDialog(CharSequence message) {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setIndeterminate(true);
		}

		progressDialog.setMessage(message);
		progressDialog.show();
	}

	public void dismissProgressDialog() {
		if (progressDialog != null && !destroyed) {
			progressDialog.dismiss();
		}
	}

	// ***************************************
	// Activity methods
	// ***************************************
	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyed = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Initiate the request to the protected service
		final Button submitButton = (Button) findViewById(R.id.submit);
		submitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new FetchSecuredResourceTask().execute();
			}
		});
	}

	// ***************************************
	// Private classes
	// ***************************************
	private class FetchSecuredResourceTask extends
			AsyncTask<Void, Void, Message> {

		private String username;

		private String password;

		// public FetchSecuredResourceTask(String username, String password) {
		// this.username = username;
		// this.password = password;
		// }

		@Override
		protected void onPreExecute() {
			showLoadingProgressDialog();

			// build the message object
			EditText editText = (EditText) findViewById(R.id.username);
			this.username = editText.getText().toString();

			editText = (EditText) findViewById(R.id.password);
			this.password = editText.getText().toString();
		}

		@Override
		protected Message doInBackground(Void... params) {

			final String url = getString(R.string.base_uri) + "/getmessage";

			// UsernamePasswordCredentials creds = new
			// UsernamePasswordCredentials(
			// username, password);
			//
			// HttpClient httpclient = new DefaultHttpClient();
			// ((AbstractHttpClient)httpclient).getCredentialsProvider().setCredentials(AuthScope.ANY,
			// creds);
			// HttpResponse response;
			// String responseString = null;
			//
			// try {
			// response = httpclient.execute(new HttpGet(url));
			// StatusLine statusLine = response.getStatusLine();
			// if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			// response.getEntity().writeTo(out);
			// out.close();
			// responseString = out.toString();
			// } else {
			// // Closes the connection.
			// response.getEntity().getContent().close();
			// throw new IOException(statusLine.getReasonPhrase());
			// }
			// } catch (ClientProtocolException e) {
			// // TODO Handle problems..
			// } catch (IOException e) {
			// // TODO Handle problems..
			// }
			// return responseString;

			// Populate the HTTP Basic Authentitcation header with the username
			// and password
			HttpAuthentication authHeader = new HttpBasicAuthentication(
					username, password);
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAuthorization(authHeader);
			requestHeaders.setAccept(Collections
					.singletonList(MediaType.APPLICATION_JSON));

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(
					new MappingJacksonHttpMessageConverter());

			try {
				// Make the network request
				Log.d(TAG, url);
				ResponseEntity<Message> response = restTemplate.exchange(url,
						HttpMethod.GET, new HttpEntity<Object>(requestHeaders),
						Message.class);
				return response.getBody();
			} catch (HttpClientErrorException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
				return new Message(0, e.getStatusText(),
						e.getLocalizedMessage());
				// return e.getLocalizedMessage();
			} catch (ResourceAccessException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
				return new Message(0, e.getClass().getSimpleName(),
						e.getLocalizedMessage());
				// return e.getLocalizedMessage();
			}
		}

		@Override
		protected void onPostExecute(Message result) {
			dismissProgressDialog();
			displayResponse(result);
			
			if(result.getId() == 100) {
		    	Intent intent = new Intent(thisLoginActivity, StartActivity.class);
		    	
		    	startActivity(intent);
			}
		}
	}

	// ***************************************
	// Private methods
	// ***************************************
	private void displayResponse(Message response) {
		Toast.makeText(this, response.getText(), Toast.LENGTH_LONG).show();
	}

}