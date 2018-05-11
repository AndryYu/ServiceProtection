package com.example.km.ndk.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 *
 */
public class AuthenticationService extends Service {

    private AccountAuthenticator accountAuthenticator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        accountAuthenticator = new AccountAuthenticator(this);
    }


    static class AccountAuthenticator extends AbstractAccountAuthenticator {

        private Context mContext;
        private AccountManager accountManager;

        public AccountAuthenticator(Context context) {
            super(context);
            this.mContext = context;
            accountManager = AccountManager.get(context);
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        /**
         要求用户添加某个特定accountType的帐号，如没有则会引导注册（authenticator app自己的处理）
         注意：调用这个接口会调起authenticator app帐号登录的页面。传的activity参数就是用来启动这个intent的：如果传的activity参数不为null，则AccountManager会自动帮你start 登录的intent，否则你自己调用future.getResult()，返回的结果中会有AccountManager#KEY_INTENT对应的帐号登录页面的intent
         */
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                                 String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                         Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String
                authTokenType, Bundle options) throws NetworkErrorException {

            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                        String authTokenType, Bundle options) throws
                NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                                  String[] features) throws NetworkErrorException {
            return null;
        }
    }
}
