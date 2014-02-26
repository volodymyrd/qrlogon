package com.gmail.volodymyrdotsenko.qr;

import java.util.Collections;

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

	private String userName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Initiate the request to the protected service
		final Button submitButton = (Button) findViewById(R.id.submit);
		submitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText editText = (EditText) findViewById(R.id.username);
				userName = editText.getText().toString();
				
				new FetchSecuredResourceTask().execute();
//		    	Intent intent = new Intent(thisLoginActivity, StartActivity.class);
//		    	intent.putExtra("userName", userName);
//		    	startActivity(intent);
			}
		});
	}

	// ***************************************
	// Private classes
	// ***************************************
	private class FetchSecuredResourceTask extends
			AsyncTask<Void, Void, Message> {

		private String password;

		// public FetchSecuredResourceTask(String username, String password) {
		// this.username = username;
		// this.password = password;
		// }

		@Override
		protected void onPreExecute() {
			showLoadingProgressDialog();

			EditText editText = (EditText) findViewById(R.id.password);
			this.password = editText.getText().toString();
		}

		@Override
		protected Message doInBackground(Void... params) {

			final String url = getString(R.string.base_uri) + "/getmessage";

			// Populate the HTTP Basic Authentitcation header with the username
			// and password
			HttpAuthentication authHeader = new HttpBasicAuthentication(
					userName, password);
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
		    	intent.putExtra("userName", userName);
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