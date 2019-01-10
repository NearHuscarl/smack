package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.nearhuscarl.smack.Adapters.MessageAdapter
import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.AuthService
import com.nearhuscarl.smack.Services.MessageService
import com.nearhuscarl.smack.Services.UserDataService
import com.nearhuscarl.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import com.nearhuscarl.smack.Utilities.SIGN_IN_REQUEST_CODE
import com.nearhuscarl.smack.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setupAdapters()

        channel_list.setOnItemClickListener { _, _, position, _ ->
            selectedChannel = MessageService.channels[position]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        logIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,
                        "Signed in successfully. Welcome!",
                        Toast.LENGTH_LONG)
                        .show()

                val user = FirebaseAuth.getInstance().currentUser

                displayChatMessages(user?.uid, user?.displayName, user?.email)
            } else {
                Toast.makeText(this,
                        "Couldn't sign in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show()

                finish()
            }
        }

    }

    fun displayChatMessages(id: String?, name: String?, email: String?)
    {
        UserDataService.id = id.toString()
        UserDataService.name = name.toString()
        UserDataService.email = email.toString()

        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email

        // TODO: handle avatar
//        val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
//        userImageNavHeader.setImageResource(resourceId)
//        userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
        loginBtnNavHeader.text = "Log out"

        MessageService.getChannels { complete ->
            if (MessageService.channels.count() > 0) {
                selectedChannel = MessageService.channels[0]
                channelAdapter.notifyDataSetChanged()
                updateWithChannel()
            }
        }
    }

    fun updateWithChannel() {
        mainChannelName.text = selectedChannel?.toString()

        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if (complete) {
                    for (message in MessageService.messages) {
                        messageAdapter.notifyDataSetChanged()

                        if (messageAdapter.itemCount > 0) {
                            messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked(view: View) {
        if (App.sharedPrefs.isLoggedIn)
            logOut()
        else
            logIn()
    }

    fun addChannelClicked(view: View) {
        if (App.sharedPrefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton("Add") { _, _ ->
                        val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                        val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                        val channelName = nameTextField.text.toString()
                        val channelDesc = descTextField.text.toString()

                        socket.emit("newChannel", channelName, channelDesc)
                    }
                    .setNegativeButton("Cancel") { dialog, i ->
                    }
                    .show()
        }
    }

    private fun logIn()
    {
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            )
        } else {
            Toast.makeText(this,
                    "Welcome back " + FirebaseAuth.getInstance().currentUser?.displayName,
                    Toast.LENGTH_LONG)
                    .show()
            val user = FirebaseAuth.getInstance().currentUser

            displayChatMessages(user?.uid, user?.displayName, user?.email)
        }

        App.sharedPrefs.isLoggedIn = true
    }

    private fun logOut()
    {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener {
                    UserDataService.logout()

                    channelAdapter.notifyDataSetChanged()
                    messageAdapter.notifyDataSetChanged()

                    userNameNavHeader.text = ""
                    userEmailNavHeader.text = ""
                    userImageNavHeader.setImageResource(R.drawable.profiledefault)
                    userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
                    mainChannelName.text = "Please Log In"
                    loginBtnNavHeader.text = "Log in"

                    Toast.makeText(this,
                            "You have been signed out.",
                            Toast.LENGTH_LONG)
                            .show()
                }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if (App.sharedPrefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDesc = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDesc, channelId)

                println(newChannel.name)
                println(newChannel.description)
                println(newChannel.id)

                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.sharedPrefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String

                if (channelId == selectedChannel?.id) {
                    val msgBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(msgBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

    fun sendMessageBtnClicked(view: View) {
        if (App.sharedPrefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id

            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId,
                    UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)

            messageTextField.text.clear()
            hideKeyboard()
        }
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
