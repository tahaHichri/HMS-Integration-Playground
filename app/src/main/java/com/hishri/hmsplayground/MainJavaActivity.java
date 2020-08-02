package com.hishri.hmsplayground;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.huawei.cloud.base.auth.DriveCredential;
import com.huawei.cloud.services.drive.DriveScopes;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;

import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import static com.huawei.hms.analytics.type.HAEventType.*;
import static com.huawei.hms.analytics.type.HAParamType.*;


import static com.huawei.hms.support.hwid.request.HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM;

public class MainJavaActivity extends AppCompatActivity {

    private final static String HmsTAG = "HMS:: ";
    private static int REQUEST_SIGN_IN_LOGIN = 1302;

    HiAnalyticsInstance instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);


        //huaweiLogin();


        HiAnalyticsTools.enableLog();
        // Generate the Analytics Instance
        instance = HiAnalytics.getInstance(this);

        instance.setUserProfile("sss", "mmm");
        reportAnswerEvt("");
        postScore();
    }


    private void reportAnswerEvt(String answer) {

        // Initialize parameters.
        Bundle bundle = new Bundle();

        bundle.putString("testanal","Ramm");

        // Report a customized event.
        instance.onEvent("Answer", bundle);
    }


    private void postScore() {

        Bundle bundle = new Bundle();
        bundle.putLong(SCORE, 666);

        // Report a predefined Event
        instance.onEvent(SUBMITSCORE, bundle);
    }


    private String accessToken;

    private String unionId;

    private DriveCredential.AccessMethod refreshAT = new DriveCredential.AccessMethod() {
        // Here is a simple process. For official use, please refer to the Huawei Cloud Space Service
        // Developer Guide-Client Development-Storage Authentication Information Chapter
        @Override
        public String refreshToken() {
            return accessToken;
        }
    };

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(HmsTAG, "onActivityResult, requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                accessToken = huaweiAccount.getAccessToken();
                unionId = huaweiAccount.getUnionId();
                //int returnCode = init(unionId, accessToken, refreshAT);

            } else {
                Log.d(HmsTAG, "onActivityResult, signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
                Toast.makeText(getApplicationContext(), "onActivityResult, signIn failed.", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void huaweiLogin() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        List<Scope> scopeList = new ArrayList<>();
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE));
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_READONLY));
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_FILE));
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA));
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY));
//        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_APPDATA));
        scopeList.add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE);

        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(DEFAULT_AUTH_REQUEST_PARAM)
                .setAccessToken()
                .setIdToken()
                .setScopeList(scopeList)
                .createParams();
        HuaweiIdAuthService client = HuaweiIdAuthManager.getService(this, authParams);
        startActivityForResult(client.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);
    }
}