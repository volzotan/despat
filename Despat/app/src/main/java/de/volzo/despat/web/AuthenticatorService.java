package de.volzo.despat.web;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by volzotan on 25.01.18.
 */


public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private ServpatAuthenticator servpatAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        servpatAuthenticator = new ServpatAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return servpatAuthenticator.getIBinder();
    }

}

