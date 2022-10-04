package xyz.cybernerd404.appsync

import android.app.Application
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

class AppSyncApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        runAmplify()
    }

    private fun runAmplify() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(
                AmplifyConfiguration.fromConfigFile(applicationContext, R.raw.awsconfiguration),
                applicationContext
            )
        } catch (amplifyException: AmplifyException) {
            amplifyException.printStackTrace()
        }
    }


}