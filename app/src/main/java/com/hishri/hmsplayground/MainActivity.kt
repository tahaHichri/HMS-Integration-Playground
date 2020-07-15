package com.hishri.hmsplayground

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.hishri.hmsplayground.extension.showSnackbar
import com.huawei.cloud.base.auth.DriveCredential
import com.huawei.cloud.base.http.FileContent
import com.huawei.cloud.base.util.StringUtils
import com.huawei.cloud.client.exception.DriveCode
import com.huawei.cloud.services.drive.Drive
import com.huawei.cloud.services.drive.DriveScopes
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {


    private lateinit var mCredential: DriveCredential
    private lateinit var accessToken: String
    private lateinit var unionId: String
    private var directoryCreated: com.huawei.cloud.services.drive.model.File? = null
    private var fileUploaded: com.huawei.cloud.services.drive.model.File? = null

    private lateinit var fileSearched: File

    private var pickedFile: File? = null


    // permissions for accessing storage and camera
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    companion object {
        private val MIME_TYPE_MAP: MutableMap<String, String> = HashMap()
        private const val REQUEST_SIGN_IN_LOGIN = 999
        private const val PICKFILE_RESULT_CODE = 991
        private const val TAG = "MainActivity"

        // accepted MIME types
        init {
            MIME_TYPE_MAP.apply {
                put(".doc", "application/msword")
                put(".jpg", "image/jpeg")
                put(".mp3", "audio/x-mpeg")
                put(".mp4", "video/mp4")
                put(".pdf", "application/pdf")
                put(".png", "image/png")
                put(".txt", "text/plain")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setting up the action bar
        setSupportActionBar(toolbar)
        toolbar.title = "HMS Playground"
        toolbar.subtitle = "HUAWEI ID and DriveKit"


        // Accessing the storage require explicit permission grant for API >= 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS_STORAGE, 1)
        }


        // learn more webpage button
        notice_learn_more.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/tahaHichri/HMS-Integration-Playground")
            )
            startActivity(browserIntent)
        }


        // Huawei login with ID
        huaweiLoginBtn.setOnClickListener {
            driveLogin()
        }

        // check if there is a file selected, and upload it to drive
        uploadToDriveBtn.setOnClickListener {
            pickedFile?.let {
                uploadFiles()
            } ?:run {
                uploadToDriveBtn.showSnackbar("Please select a file.", 5000)
            }
        }

        // select file for upload
        selectFileBtn.setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE)
        }

    }


    /**
     * Launch the login with ID process for result
     */
    private fun driveLogin() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val scopeList: MutableList<Scope> = ArrayList()

        scopeList.apply {
            add(Scope(DriveScopes.SCOPE_DRIVE))
            add(Scope(DriveScopes.SCOPE_DRIVE_READONLY))
            add(Scope(DriveScopes.SCOPE_DRIVE_FILE))
            add(Scope(DriveScopes.SCOPE_DRIVE_METADATA))
            add(Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY))
            add(Scope(DriveScopes.SCOPE_DRIVE_APPDATA))
            add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE)
        }

        val authParams = HuaweiIdAuthParamsHelper(
            HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM
        )
            .setAccessToken()
            .setIdToken()
            .setScopeList(scopeList)
            .createParams()
        val client = HuaweiIdAuthManager.getService(this, authParams)
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN_LOGIN)
    }

    // Exceptional process for obtaining account information. Obtain and save the related accessToken and unionID using this function.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult, requestCode = $requestCode, resultCode = $resultCode")
        when (requestCode) {

            // Login result
            REQUEST_SIGN_IN_LOGIN -> {
                val authHuaweiIdTask =
                    HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                if (authHuaweiIdTask.isSuccessful) {
                    val huaweiAccount = authHuaweiIdTask.result
                    accessToken = huaweiAccount.accessToken
                    unionId = huaweiAccount.unionId
                    val returnCode = init(unionId, accessToken, refreshAT)
                    if (DriveCode.SUCCESS == returnCode) {
                        notice_learn_more.showSnackbar("login ok", 9000)
                    } else if (DriveCode.SERVICE_URL_NOT_ENABLED == returnCode) {
                        notice_learn_more.showSnackbar("drive is not enabled", 9000)
                    } else {
                        notice_learn_more.showSnackbar("login error", 9000)
                    }
                } else {
                    Timber.d(
                        "onActivityResult, signIn failed: " + (authHuaweiIdTask.exception as ApiException).statusCode
                    )
                    notice_container.showSnackbar(
                        ":( Developer account still Pending validation. Please check the notice below.",
                        10000
                    )
                }
            }

            PICKFILE_RESULT_CODE ->{
                if (resultCode == -1) {
                    data?.data?.path?.let {
                        pickedFile = File(it)
                        selectedFileName.text= it
                    }

                }
            }
        }
    }

    private val refreshAT = DriveCredential.AccessMethod {
        /**
         * Simplified code snippet for demonstration purposes. For the complete code snippet,
         * please go to Client Development > Obtaining Authentication Information > Store Authentication Information
         * in the HUAWEI Drive Kit Development Guide.
         **/
        return@AccessMethod accessToken
    }

    /**
     * （unionId，countrycode，accessToken）drive。
     * accessTokenAccessMethod,accessToken。
     *
     * @param unionID   unionID from HwID
     * @param at        access token
     * @param refreshAT a callback to refresh AT
     */
    private fun init(unionID: String?, at: String?, refreshAT: DriveCredential.AccessMethod?): Int {
        return if (StringUtils.isNullOrEmpty(unionID) || StringUtils.isNullOrEmpty(at)) {
            DriveCode.ERROR
        } else {
            val builder = DriveCredential.Builder(unionID, refreshAT)
            mCredential = builder.build().setAccessToken(at)
            DriveCode.SUCCESS
        }
    }


    //Function to Upload files

    private fun uploadFiles() {
        GlobalScope.launch {
            try {
//                if (accessToken == null) {
//                    showTips("please click 'Login'.")
//                    return@launch
//                }

                val fileObject = pickedFile

                val appProperties: MutableMap<String, String> =
                    HashMap()
                appProperties["appProperties"] = "property"
                // create somepath directory
                com.huawei.cloud.services.drive.model.File().setFileName("somepath" + System.currentTimeMillis())
                    .setMimeType("application/vnd.huawei-apps.folder").appSettings =
                    appProperties
                directoryCreated = buildDrive()?.files()?.create(com.huawei.cloud.services.drive.model.File())?.execute()
                // create test.jpg on cloud
                val mimeType = mimeType(fileObject)
                val content = com.huawei.cloud.services.drive.model.File()
                    .setFileName(fileObject?.name)
                    .setMimeType(mimeType)
                    .setParentFolder(listOf(directoryCreated?.id))
                fileUploaded = buildDrive()?.files()
                    ?.create(content, FileContent(mimeType, fileObject))
                    ?.setFields("*")
                    ?.execute()
                notice_container.showSnackbar("upload success",9000)
            } catch (ex: Exception) {
                Log.d(TAG, "upload", ex)
                notice_container.showSnackbar("upload error, cause: $ex",9000)
            }

        }
    }

    private fun buildDrive() = Drive.Builder(mCredential, this).build()


    private fun mimeType(file: java.io.File?): String? {
        if (file != null && file.exists() && file.name.contains(".")) {
            val fileName = file.name
            val suffix = fileName.substring(fileName.lastIndexOf("."))
            if (MIME_TYPE_MAP.keys.contains(suffix)) {
                return MIME_TYPE_MAP[suffix]
            }
        }
        return "*/*"
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}