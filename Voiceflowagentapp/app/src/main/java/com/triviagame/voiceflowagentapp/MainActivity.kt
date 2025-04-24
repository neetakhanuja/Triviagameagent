package com.triviagame.voiceflowagentapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    //  Your Voiceflow info
    private val projectID = "64fb29bde26bb80007a97c27" // ✅ Public demo bot
    private val apiKey = "VF.eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhIjoiNjgwODFiNTkxNjliNmUwZGEzZTE1NDM2IiwiaWF0IjoxNjkxMDc3MzUxfQ.N1En-RbU2DahZHrkf9eAL35ZzCJInKv1gBiCE7QQfDw"
    private val userID = "user_android_demo"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userInput = findViewById<EditText>(R.id.userInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val botReply = findViewById<TextView>(R.id.botReply)

        sendButton.setOnClickListener {
            val message = userInput.text.toString()
            botReply.text = "Sending..."

            CoroutineScope(Dispatchers.IO).launch {
                val response = sendToVoiceflow(message)
                withContext(Dispatchers.Main) {
                    botReply.text = response
                }
            }
        }
    }

    private fun sendToVoiceflow(message: String): String {
        return try {
            val url = URL("https://general-runtime.voiceflow.com/state/$projectID/user/$userID/interact")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", apiKey)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestJson = JSONObject()
            val requestContent = JSONObject()
            requestContent.put("type", "text")
            requestContent.put("payload", message)
            requestJson.put("request", requestContent)

            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(requestJson.toString())
            outputStreamWriter.flush()

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return "Voiceflow is unreachable. Please try again later."
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.has("payload")) {
                    return obj.getJSONObject("payload").getString("message")
                }
            }

            return "Bot didn't send a reply."
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: Could not connect to Voiceflow."
        }
    }

}
