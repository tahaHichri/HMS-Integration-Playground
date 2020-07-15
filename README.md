# HMS Playground



## Table of Contents

 * [Concepts](#concepts)
 * [Features](#features)
 * [Technical Details](#technical-details )
 * [Integration Process](#integration-process)
 * [Improvements & Limitations](#improvements--limitations)
 * [Sources Used](#sources-used)



## Concepts
This project is an Android application that showcases Huawei Mobile Services SDK(HMS) integration with Android.

The services that I showcased:
1. *HUAWEI Account Kit* Allowing users to login with Huawei. [Learn More](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/account-introduction-v4)
This is required also to use the Drive Kit and upload files
2. *HUAWEI Drive Kit* Allowing users to upload files to Huawei Drive. [Learn More](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/drivekit-introduction)


![screenshot](https://i.imgur.com/rTNqZUj.png)



## Features
1. Login with Huawei
2. Allows picking files from device internal storage.
3. Upload selected file to Huawei Drive

---


## Technical Details

### Dev Environment
*. Android Studio 4.0
*. A device with api level 28

### Libraries & Dependencies
1. Timber
2. Kotlin Coroutines
3. Huawei Core
4. Huawei DriveKit
5. Material Kit

### Test Requirements
*. A device with a Android 5.0 or higher (Lollipop or higher)
*. A device with HMS Core 4.0.0.300 or higher installed

## How to test?
I have build an apk that can be directly installed on any device meeting the requirements mentioned above.
1. Start by [Downloading this file](https://tahahichri.s3.amazonaws.com/hms_pg_test_app.apk)
2. Open the downloaded file and choose install [https://android.gadgethacks.com/how-to/android-101-sideload-apps-by-enabling-unknown-sources-install-unknown-apps-0161947/](You might need to allow apps from unknown sources) on some devices.
this allows you to side load apps manually.

_You can also clone this project and open the project with Android Studio._


---

## Integration Process

These are the steps I took in order to integrate HMS in my app
1. Gradle integration

app.gradle
```
// huawei gradle plugin
apply plugin: 'com.huawei.agconnect'


// ...

dependencies {

    /**
     * HMS dependencies
     */
    implementation 'com.huawei.hms:drive:4.0.0.301'         // driveKit
    implementation 'com.huawei.hms:hwid:4.0.0.300'          // Core

}

```

2. projects.gradle

```

buildscript {
    ext.kotlin_version = "1.3.72"
     repositories {

          // huawei deps repository
          maven { url 'https://developer.huawei.com/repo/' }

          // ...

        }
        dependencies {
          //...

          // Huawei connect
          classpath 'com.huawei.agconnect:agcp:1.2.1.301'

        }

}

allprojects {
    repositories {
        google()
        jcenter()

            maven {url 'http://developer.huawei.com/repo/'}
        }
}

```



3. _agconnect-services.json_ Under app/ directory


*Please see "Limitations" section for further description*


---





## Improvements & Limitations

Accessing Huawei Suite programmatically requires an activated developer account [https://developer.huawei.com/consumer/en/doc/drivekit-devpreparations](Check out the Preparation guide for more infos) . This can take 1-2 business days.
Since this is my own developer account, it took some time for the identity validation process to be done.

This will prevent the Account Kit from working. I will try to improve it once my account it validated.


---



## Sources Used
These are the sources I used for this project

1. [HMS Account Kit developer guide](https://developer.huawei.com/consumer/en/doc/account-preparation)
2. [HMS Drive Kit developer guide](https://developer.huawei.com/consumer/en/doc/drivekit-devpreparations)
3. [HMS Drive Kit Codelab](https://developer.huawei.com/consumer/en/codelab/HMSDriveKit-Kotlin/index.html#1)




