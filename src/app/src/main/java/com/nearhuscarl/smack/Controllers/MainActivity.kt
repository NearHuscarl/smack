package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
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
import com.google.firebase.database.*
import com.nearhuscarl.smack.Adapters.MessageAdapter
import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.Models.MessagePayload
import com.nearhuscarl.smack.Models.User
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.Firebase
import com.nearhuscarl.smack.Services.MessageService
import com.nearhuscarl.smack.Services.UserDataService
import com.nearhuscarl.smack.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import com.google.firebase.database.DataSnapshot
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {

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
            drawer_layout.closeDrawer(GravityCompat.START)

            unsubscribeChannel(selectedChannel?.id)
            selectedChannel = MessageService.channels[position]
            updateChatMessages(selectedChannel?.id)
        }

        logIn()
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageService.clearChannels()
        Firebase.database.child(CHANNELS_REF).removeEventListener(onNewChannel)
    }

    fun displayChatMessages(id: String?, name: String?, email: String?, profileImageUrl: String?)
    {
        UserDataService.id = id.toString()
        UserDataService.name = name.toString()
        UserDataService.email = email.toString()

        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email

        // after adding all channels. chat messages will be loaded in the currently selected channel
        Firebase.database.child(CHANNELS_REF).addChildEventListener(onNewChannel)
        Picasso.get().load(profileImageUrl).into(userImageNavHeader)
        loginBtnNavHeader.text = "Log out"
    }

    private fun unsubscribeChannel(channelId: String?) {
        Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF")
                .removeEventListener(onNewMessage)
        MessageService.clearMessages()
        messageAdapter.notifyDataSetChanged()
    }

    private fun updateChatMessages(channelId: String?) {
        mainChannelName.text = selectedChannel?.toString()
        MessageService.clearMessages()

        Firebase.database.child("$CHANNELS_REF/${selectedChannel?.id}/$MESSAGES_REF")
                .addChildEventListener(onNewMessage)
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

    private fun logIn()
    {
        if (FirebaseAuth.getInstance().currentUser == null)
        {
            val Intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(Intent)
            finish()
        }else{
            Toast.makeText(this,
                    "Welcome back " + FirebaseAuth.getInstance().currentUser?.displayName,
                    Toast.LENGTH_LONG)
                    .show()
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val rootRef = FirebaseDatabase.getInstance().reference
            val uidRef = rootRef.child("users").child(uid)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user_new = dataSnapshot.getValue(User::class.java)

                    displayChatMessages(uid, user_new?.username,
                            FirebaseAuth.getInstance().currentUser?.email, user_new?.profileImageUrl)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("error", databaseError.message) //Don't ignore errors!
                }
            }
            uidRef.addListenerForSingleValueEvent(valueEventListener)
            loginBtnNavHeader.text = "Log out"
        }
        App.sharedPrefs.isLoggedIn = true
    }
//    private fun logIn()
//    {
//        if (FirebaseAuth.getInstance().currentUser == null) {
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .build(),
//                    SIGN_IN_REQUEST_CODE
//            )
//        } else {
//            Toast.makeText(this,
//                    "Welcome back " + FirebaseAuth.getInstance().currentUser?.displayName,
//                    Toast.LENGTH_LONG)
//                    .show()
//            val user = FirebaseAuth.getInstance().currentUser
//
//            displayChatMessages(user?.uid, user?.displayName, user?.email)
//        }
//
//        App.sharedPrefs.isLoggedIn = true
//    }

    private fun logOut()
    {
        AuthUI.getInstance().signOut(applicationContext)
                .addOnCompleteListener {
                    unsubscribeChannel(selectedChannel?.id)
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

    private val onNewChannel = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//            runOnUiThread {
            if (App.sharedPrefs.isLoggedIn) {
                val channelId = dataSnapshot.child(ID_REF).value.toString()
                val channelName = dataSnapshot.child(NAME_REF).value.toString()
                val channelDesc = dataSnapshot.child(DESCRIPTION_REF).value.toString()
                val newChannel = Channel(channelId, channelName, channelDesc)

                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
                updateChatMessages(selectedChannel?.id)
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("DEBUG", "onNewChannel:onChildChanged: ${dataSnapshot.key}")
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.d("DEBUG", "onNewChannel:onChildRemoved: ${dataSnapshot.key}")
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("DEBUG", "onNewChannel:onChildMoved: ${dataSnapshot.key}")
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewChannel:onCancelled", databaseError.toException())
        }
    }

    private val onNewMessage = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            if (App.sharedPrefs.isLoggedIn) {
                val channelId = dataSnapshot.child(CHANNEL_ID_REF).value.toString()

                if (channelId == selectedChannel?.id) {
                    val id = dataSnapshot.child(ID_REF).value.toString()
                    var msgBody = dataSnapshot.child(MESSAGE_BODY_REF).value.toString() //  TODO: change back to val
                    val userName = dataSnapshot.child(USER_NAME_REF).value.toString().trim()
                    val avatarUrl = dataSnapshot.child(AVATAR_URL_REF).value.toString()
                    val timeStamp = dataSnapshot.child(TIMESTAMP_REF).value.toString()

                    msgBody += "\nAvatar url: $avatarUrl" // TODO: add avatar and remove this line
                    val newMessage = Message(id, msgBody, channelId, userName, avatarUrl, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("DEBUG", "onNewMessage:onChildChanged: ${dataSnapshot.key}")
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.d("DEBUG", "onNewMessage:onChildRemoved:" + dataSnapshot.key!!)
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("DEBUG", "onNewMessage:onChildMoved:" + dataSnapshot.key!!)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewMessage:onCancelled", databaseError.toException())
        }
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

                        val channelsRef = Firebase.database.child(CHANNELS_REF)
                        val channelId = channelsRef.push().key

                        if (channelId != null) {
                            channelsRef.child(channelId).setValue(Channel(
                                    channelId, channelName, channelDesc
                            ))
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, i ->
                    }
                    .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessageBtnClicked(view: View) {
        if (App.sharedPrefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val channelId = selectedChannel?.id.toString()
            val messagesRef = Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF")
            val messageId = messagesRef.push().key

            if (messageId != null) {
                val messageBody = messageTextField.text.toString()
                val userName = UserDataService.name
                val serverTimeStamp = Firebase.GetServerTimeStamp()

                messagesRef.child(messageId).setValue(MessagePayload(
                        messageId,
                        messageBody,
                        channelId,
                        userName,
                        "https://$userName-avatar-url-link.jpg", // TODO: add avatar
                        serverTimeStamp
                ))

                messageTextField.text.clear()
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}