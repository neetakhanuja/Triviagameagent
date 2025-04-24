package com.triviagame.dialogflowagentapp2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.TextInput
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.google.api.gax.core.FixedCredentialsProvider
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val projectID = "gametriviaagent-vewc"  // Your Dialogflow Project ID
    private val sessionID = "unique-session-id"    // Unique session ID (you can change this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userInput = findViewById<EditText>(R.id.userInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val botReply = findViewById<TextView>(R.id.botReply)

        sendButton.setOnClickListener {
            val message = userInput.text.toString()
            botReply.text = "Sending..."

            // Call Dialogflow API
            val response = sendToDialogflow(message)
            botReply.text = response
        }
    }

    /*private fun sendToDialogflow(message: String): String {
        return try {
            // Load the service account key from res/raw


            val credentials: InputStream = resources.openRawResource(R.raw.dialogflow_key)
            val googleCredentials = GoogleCredentials.fromStream(credentials)
            Log.d("Dialogflow", "Credentials loaded successfully!")

            // Create credentials provider
            val credentialsProvider = FixedCredentialsProvider.create(googleCredentials)

            // Create a Dialogflow session with authenticated credentials
            val sessionClient = SessionsClient.create(
                SessionsSettings.newBuilder()
                .setCredentialsProvider { credentialsProvider }
                .build())

            val session = SessionName.of(projectID, sessionID)

            // Build the text input to send to Dialogflow
            val textInput = TextInput.newBuilder().setText(message).setLanguageCode("en-US").build()
            val queryInput = QueryInput.newBuilder().setText(textInput).build()

            // Get the response from Dialogflow
            val response: DetectIntentResponse = sessionClient.detectIntent(session, queryInput)

            return response.queryResult.fulfillmentText // Return the bot's reply
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: Could not connect to Dialogflow."
        }
    }*/

    private fun sendToDialogflow(message: String): String {
        // Run the Dialogflow request in a background thread to avoid UI blocking
        var botResponse = "Sending..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load the service account key from res/raw
                val credentials: InputStream = resources.openRawResource(R.raw.dialogflow_key)
                val googleCredentials = GoogleCredentials.fromStream(credentials)
                Log.d("Dialogflow", "Credentials loaded successfully!")

                // Create credentials provider
                val credentialsProvider = FixedCredentialsProvider.create(googleCredentials)

                // Create a Dialogflow session with authenticated credentials
                val sessionClient = SessionsClient.create(
                    SessionsSettings.newBuilder()
                        .setCredentialsProvider(credentialsProvider)
                        .build()
                )

                val session = SessionName.of(projectID, sessionID)

                // Build the text input to send to Dialogflow
                val textInput = TextInput.newBuilder().setText(message).setLanguageCode("en-US").build()
                val queryInput = QueryInput.newBuilder().setText(textInput).build()

                // Get the response from Dialogflow
                val response: DetectIntentResponse = sessionClient.detectIntent(session, queryInput)

                botResponse = response.queryResult.fulfillmentText // Update the bot response with the reply

            } catch (e: Exception) {
                e.printStackTrace()
                botResponse = "Error: Could not connect to Dialogflow."
            }

            // Update the UI with the result (back on the main thread)
            withContext(Dispatchers.Main) {
                val botReply = findViewById<TextView>(R.id.botReply)
                botReply.text = botResponse
            }
        }

        return botResponse // Initially return a "Sending..." message
    }
}