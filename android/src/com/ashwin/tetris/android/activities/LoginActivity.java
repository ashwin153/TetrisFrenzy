package com.ashwin.tetris.android.activities;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.R.id;
import com.ashwin.tetris.android.R.layout;
import com.ashwin.tetris.android.services.IncomingHandler;
import com.ashwin.tetris.android.services.ServerConnectionService;
import com.ashwin.tetris.android.services.ServiceConnectionClient;
import com.ashwin.tetris.command.Command;

public class LoginActivity extends Activity {
	
	private ServiceConnectionClient _serviceConnectionClient;
	private Typeface _typeface;
	
	public class LoginHandler extends IncomingHandler {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
					case ServerConnectionService.SERVICE_RESPONSE:
					case ServerConnectionService.SERVER_RESPONSE:
						Bundle data = msg.getData();
						for(String cmd : data.keySet())
							handleCommand(Command.valueOf(cmd), data.getString(cmd));
						break;
					
				}
			} catch(Exception e) {
				Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
		}
		
		public void handleCommand(Command cmd, String value) throws Exception {
			switch(cmd) {
				case LOGIN_SUCCESSFUL:
					Intent intent = new Intent(LoginActivity.this, InfoActivity.class);
					intent.putExtra("player_data", value);
					startActivity(intent);
					break;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		_serviceConnectionClient = new ServiceConnectionClient(this, new LoginHandler());
		_typeface = Typeface.createFromAsset(getAssets(), "Hobo.ttf");
		LinearLayout layout = (LinearLayout) findViewById(R.id.LoginActivity_layout);
		
		Button login = (Button) findViewById(R.id.LoginActivity_login);
		final EditText email = (EditText) findViewById(R.id.LoginActivity_username);
		final EditText password = (EditText) findViewById(R.id.LoginActivity_password);
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					HashMap<Command, String> commands = new HashMap<Command, String>();
					commands.put(Command.LOGIN, email.getText() + "," + password.getText());
					
					_serviceConnectionClient.sendMessage(ServerConnectionService.SEND_DATA, commands);
				} catch(RemoteException e) {
					Log.e("LoginActivity@onLoginButtonClick", "Cannot Access Server: " + e.toString());
				}
			}
		});
		
		setTypeface(layout, _typeface);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		_serviceConnectionClient.doBindService();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		_serviceConnectionClient.doUnbindService();
	}

	// Sets the typeface for a ViewGroup and all child ViewGroups
	public void setTypeface(View view, Typeface tf) {
		if(!(view instanceof ViewGroup)) {
			if(view instanceof TextView)
				((TextView) view).setTypeface(tf);
			return;
		}
		
		for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			setTypeface(((ViewGroup) view).getChildAt(i), tf);
	}
	
	// Called when on create account button is pressed
	public void onCreateAccount(View view) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_create_account);
		dialog.setTitle("Create Account");
		
		TextView title = (TextView) dialog.findViewById(android.R.id.title);
		title.setTypeface(_typeface);
		title.setTextSize(25);

		LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.CreateAccountDialog_layout);
		setTypeface(layout, _typeface);
		
		Button createAccount = (Button) dialog.findViewById(R.id.CreateAccountDialog_createAccount);
		createAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
			}
		});
		
		Button cancel = (Button) dialog.findViewById(R.id.CreateAccountDialog_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
}
