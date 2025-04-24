// Function to send the message to the backend server and get the response from Dialogflow
async function sendToDialogflow(message) {
    const response = await fetch('/sendMessage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            message: message,  // Message from user
            sessionId: '12345', // Session ID for tracking user sessions
        }),
    });

    // Parse the response from the server (Dialogflow's response)
    const result = await response.json();
    console.log('Dialogflow response:', result);  // Log the response from the server
    return result;
}

// Function to handle the user input and display the response in the chat box
async function handleUserInput() {
    const userMessage = document.getElementById('user-input').value;  // Get the message from the input box
    console.log('User message:', userMessage);  // Log the user input

    // Check if the user typed a message
    if (userMessage.trim() === "") {
        return; // Do nothing if the input is empty
    }

    // Send the message to Dialogflow through the backend server
    const dialogflowResponse = await sendToDialogflow(userMessage);  
    console.log('Dialogflow response received:', dialogflowResponse);

    // Get the bot's reply from Dialogflow's response
    const botMessage = dialogflowResponse.reply;

    // Add the user's message to the chat box
    document.getElementById('chat-box').innerHTML += `<p><strong>You:</strong> ${userMessage}</p>`;

    // Add the bot's reply to the chat box
    document.getElementById('chat-box').innerHTML += `<p><strong>Bot:</strong> ${botMessage}</p>`;

    // Scroll to the bottom of the chat box to show the latest messages
    const chatBox = document.getElementById('chat-box');
    chatBox.scrollTop = chatBox.scrollHeight;

    // Clear the input field after sending the message
    document.getElementById('user-input').value = '';
}

// Event listener for the 'send' button
document.getElementById('send-btn').addEventListener('click', handleUserInput);

// Optional: Allow the user to press Enter to send the message
document.getElementById('user-input').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        handleUserInput();
    }
});
