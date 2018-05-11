package com.example.km.ndk.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class AccountHelper {

    private static final String TAG = "AccountHelper";
    private static final String ACCOUNT_TYPE = "com.example.km.ndk.account";

    /**
     * <p>addAccount</p>
     * @param context
     * @Description  添加账号
     */
    public static void addAccount(Context context) {
        AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        //获得此类型的账户
        Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.e(TAG, "账户已存在");
            return;
        }
        //给这个账户类型添加一个账户
        Account dongnao = new Account("android_zhang", ACCOUNT_TYPE);
        am.addAccountExplicitly(dongnao, "123456", new Bundle());
    }

    /**
     * <p>autoSync</p>
     * @Description 设置账号自动同步
     */
    public static void autoSync() {
        Account dongnao = new Account("android_zhang", ACCOUNT_TYPE);
        //设置同步
        ContentResolver.setIsSyncable(dongnao, "com.example.km.provider", 1);
        //自动同步
        ContentResolver.setSyncAutomatically(dongnao, "com.example.km.provider", true);
        //设置同步周期
        ContentResolver.addPeriodicSync(dongnao, "com.example.km.provider", new Bundle(), 1);
    }
}
