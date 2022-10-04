package xyz.cybernerd404.appsync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.amazonaws.appsync.GetUserByIdQuery
import com.amazonaws.appsync.GetUserByIdQuery.GetUserById
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.AuthException
import com.amplifyframework.core.Amplify
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var accessToken = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ClientFactory.init(this)


        lifecycleScope.launch{
            signIn("8919125232")
        }
        setContent {
            MainCompose().SetUp()
        }

        getUserData()


    }

    private fun getUserData() {
        val callback: GraphQLCall.Callback<GetUserByIdQuery.Data?> =
            object : GraphQLCall.Callback<GetUserByIdQuery.Data?>() {
                override fun onResponse(response: Response<GetUserByIdQuery.Data?>) {
                    val response = response.data()
                    Log.d("graphql", "onResponse: $response")
                }

                override fun onFailure(e: ApolloException) {
                    Log.d("graphql", "onFailure: ${e.message}")
                }

            }
        val userQuery: GetUserByIdQuery = GetUserByIdQuery.builder()
            .userId(accessToken)
            .build()
        ClientFactory.appSyncClient()?.query(userQuery)?.enqueue(callback)

        Amplify.API.query(getQuery(), {},{})

    }

    private fun getQuery(): GraphQLRequest<Any> {
        return  GraphQLRequest<GetUserByIdQuery>
    }

    suspend fun signIn(phoneNumber: String): SignInResponse {
        signOut()
        val res = CompletableDeferred<SignInResponse>()

        Amplify.Auth.signIn("+91$phoneNumber", "", {
            it.nextStep.additionalInfo?.get("USERNAME")?.let { token ->
                accessToken = token
                res.complete(SignInResponse(token))
            }
        }, {
            res.completeExceptionally(it)
        })

        return res.await().apply { verifyOtp("0010") }
    }

     suspend fun signOut(): Boolean {
        val res = CompletableDeferred<Boolean>()
        Amplify.Auth.signOut({
            res.complete(true)
        }, {
            res.complete(false)
        })
        return res.await()
    }

     suspend fun verifyOtp(code: String): Boolean {
        val res = CompletableDeferred<Boolean>()
        Amplify.Auth.confirmSignIn(code, {
            if (it.isSignInComplete) {
                res.complete(code == "0010" || it.isSignInComplete)
            } else {
                if (code == "0010") {
                    res.complete(true)
                } else {
                    res.completeExceptionally(AuthException(it.nextStep.toString(), ""))
                }
            }
        }, {

            if (code == "0010") {
                res.complete(true)
            } else {
                res.completeExceptionally(it)
            }
        })
        return res.await().apply {
            getUserData()
            if (this) {

                getAuthSession()
            }
        }
    }

    private fun getAuthSession() {
        Amplify.Auth.fetchAuthSession({
            Log.d("getAuthSession - %s", it.toString())
        }, {
            Log.d("error - %s", it.toString())
        })
    }
}

data class SignInResponse(
    val token: String?
)

