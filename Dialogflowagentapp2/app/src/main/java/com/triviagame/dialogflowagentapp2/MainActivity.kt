package com.triviagame.dialogflowagentapp2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.*
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private val projectID = "gametriviaagent-vewc"
    private val sessionID = "unique-session-id"
    private lateinit var tts: TextToSpeech


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                if (status == TextToSpeech.SUCCESS) {
                    val hindiLocale = Locale("hi", "IN")
                    val result = tts.setLanguage(hindiLocale)

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this@MainActivity, "Hindi TTS not supported", Toast.LENGTH_LONG).show()
                    } else {
                        // Look for a male Hindi voice
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            val maleVoice = tts.voices.find { voice ->
                                voice.locale == hindiLocale && voice.name.contains("male", ignoreCase = true)
                            }

                            if (maleVoice != null) {
                                tts.voice = maleVoice
                                Log.d("TTS", "Using male Hindi voice: ${maleVoice.name}")
                            } else {
                                Log.w("TTS", "No male Hindi voice found. Using default.")
                            }
                        }

                        tts.setPitch(1.0f) // Natural pitch
                        tts.setSpeechRate(0.85f) // Slightly slower
                    }
                } else {
                    Toast.makeText(this@MainActivity, "TTS init failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
        val userInput = findViewById<EditText>(R.id.userInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val botReply = findViewById<TextView>(R.id.botReply)
        val connectivityStatus = findViewById<TextView>(R.id.connectivityStatus)

    //  val micButton = findViewById<Button>(R.id.micButton)
        val micButton = findViewById<ImageButton>(R.id.micButton)


        sendButton.setOnClickListener {
            val message = userInput.text.toString()
            botReply.text = "Sending..."

            if (isInternetAvailable()) {
                connectivityStatus.text = "Internet: Connected"
                sendToDialogflow(message, botReply)
            } else {
                connectivityStatus.text = "No Internet Connection"
                botReply.text = "Please check your internet connection."
            }
        }
//Mic code begins here
        micButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")

            try {
                startActivityForResult(intent, 100)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
            }
        }
        // Mic code ends here
        val googleLink = findViewById<TextView>(R.id.googleLink)
        googleLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
        }
    }

    //Mic function begins here
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            findViewById<EditText>(R.id.userInput).setText(spokenText)
        }
    }
    //Mic functionality ends here
    // Safe internet connectivity check
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private fun sendToDialogflow(message: String, botReply: TextView) {
        Log.d("DF_DEBUG", "Message: $message")
        Log.d("DF_DEBUG", "Project ID: $projectID")
        Log.d("DF_DEBUG", "Session ID: $sessionID")

        CoroutineScope(Dispatchers.IO).launch {
            var botResponse = "Sending..."

            try {
                val credentials: InputStream = resources.openRawResource(R.raw.dialogflow_agent_newkey)
                val googleCredentials = GoogleCredentials.fromStream(credentials)

                val credentialsProvider = FixedCredentialsProvider.create(googleCredentials)
                val transportChannelProvider = InstantiatingGrpcChannelProvider.newBuilder()
                    .setEndpoint("dialogflow.googleapis.com:443")
                    .build()

                val sessionsSettings = SessionsSettings.newBuilder()
                    .setTransportChannelProvider(transportChannelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build()

                val sessionClient = SessionsClient.create(sessionsSettings)
                val session = SessionName.of(projectID, sessionID)

                val textInput = TextInput.newBuilder().setText(message).setLanguageCode("en").build()
                val queryInput = QueryInput.newBuilder().setText(textInput).build()

                val response: DetectIntentResponse = sessionClient.detectIntent(session, queryInput)
                Log.d("DF_DEBUG", "Dialogflow response: ${response.queryResult.fulfillmentText}")

                botResponse = response.queryResult.fulfillmentText

            } catch (e: Exception) {
                Log.e("DialogflowError", "Failed to connect", e)
                botResponse = "Error: ${e.localizedMessage}"
            }

            withContext(Dispatchers.Main) {
                botReply.text = botResponse
                tts.speak(botResponse, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }
}


