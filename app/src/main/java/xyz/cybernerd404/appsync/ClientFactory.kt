package xyz.cybernerd404.appsync

import android.content.Context
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient


object ClientFactory {
    @Volatile
    private var client: AWSAppSyncClient? = null
    @Synchronized

    fun init(context: Context?) {
        if (client == null) {
            val awsConfig = AWSConfiguration(context)

            client = AWSAppSyncClient.builder()
                .context(context!!)
                .awsConfiguration(awsConfig)
                .build()
        }
    }

    @Synchronized
    fun appSyncClient(): AWSAppSyncClient? {
        return client
    }
}
