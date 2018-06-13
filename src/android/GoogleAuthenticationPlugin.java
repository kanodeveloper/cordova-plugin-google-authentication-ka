package com.kanoapps.cordova.google;

import android.util.Log;

import android.support.annotation.NonNull;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class GoogleAuthenticationPlugin extends CordovaPlugin {
    private static final String TAG = "GoogleAuthentication";

    private static final int RC_SIGN_IN = 9001;
    private CallbackContext signinCallback;
    private GoogleApiClient googleApiClient;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Google Authentication plugin");

        Context context = this.cordova.getActivity().getApplicationContext();
        String defaultClientId = getDefaultClientId(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(defaultClientId)
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("signInWithGoogle")) {
            signInWithGoogle(callbackContext);
            return true;
        }

        return false;
    }

    private void signInWithGoogle(final CallbackContext callbackContext) {

        this.signinCallback = callbackContext;

        final CordovaPlugin plugin = this;

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                cordova.startActivityForResult(plugin, signInIntent, RC_SIGN_IN);
            }
        });
    }

    private String getDefaultClientId(Context context) {

        String packageName = context.getPackageName();
        int id = context.getResources().getIdentifier("default_web_client_id", "string", packageName);
        return context.getString(id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result != null && result.isSuccess()) {
                // Google Sign In was successful
                GoogleSignInAccount account = result.getSignInAccount();

                JSONObject resultObj = new JSONObject();

                try {
                    resultObj.put("token", account.getIdToken());
                    this.signinCallback.success(resultObj);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to process getIdToken", e);
                    this.signinCallback.error("failed");
                }

            } else {
                this.signinCallback.error("failed");
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
}
