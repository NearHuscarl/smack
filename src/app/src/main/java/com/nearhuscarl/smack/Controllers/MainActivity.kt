package com.nearhuscarl.smack.Controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nearhuscarl.smack.Adapters.MessageAdapter
import com.nearhuscarl.smack.Models.Channel
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.Models.User
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.RecyclerItemClickListener
import com.nearhuscarl.smack.RecyclerTouchListener
import com.nearhuscarl.smack.Services.Firebase
import com.nearhuscarl.smack.Services.MessageService
import com.nearhuscarl.smack.Services.UserDataService
import com.nearhuscarl.smack.SmoothActionBarDrawerToggle
import com.nearhuscarl.smack.Utilities.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    lateinit var drawerToggle: SmoothActionBarDrawerToggle
    var isDrawerIdle = true
    var selectedChannel: Channel? = null

    var typingStarted = false

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        resetMessageAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        drawerToggle = SmoothActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        setupAdapters()

        channel_list.setOnItemClickListener { _, _, position, _ ->
            changeChannel(position)
        }

        messageTextField.addTextChangedListener(messageTextChangeHandler)
        messageRecycleView.addOnItemTouchListener(object: RecyclerTouchListener(
                this, messageRecycleView, onChatMessageClick) {})

        logIn()
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageService.clearChannels()
        Firebase.database.child(CHANNELS_REF).removeEventListener(onNewChannel)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
/*
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,
                        "Signed in successfully. Welcome!",
                        Toast.LENGTH_LONG)
                        .show()

                val user = FirebaseAuth.getInstance().currentUser

                displayChatMessagesAtStartup(user?.uid, user?.displayName, user?.email)
            } else {
                Toast.makeText(this,
                        "Couldn't sign in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show()

                finish()
            }
        } else*/ if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {
            sendChatMessageImage(data)
        }
    }

    private fun resetMessageAdapter() {
        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageRecycleView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageRecycleView.layoutManager = layoutManager
    }

    private fun displayChatMessagesAtStartup(id: String?, name: String?, email: String?, profileImageUrl: String?) {
        UserDataService.id = id.toString()
        UserDataService.name = name.toString()
        UserDataService.email = email.toString()

        userNameNavHeader.text = UserDataService.name
        userEmailNavHeader.text = UserDataService.email

        // after adding all channels. chat messages will be loaded in the currently selected channel
        Firebase.database.child(CHANNELS_REF).addChildEventListener(onNewChannel)

        Picasso.get()
                .load(profileImageUrl)
                .placeholder(R.drawable.profiledefault)
                .into(userImageNavHeader)
        loginBtnNavHeader.text = "Log out"
    }

    private fun unsubscribeChannel(channelId: String?) {
        Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF").removeEventListener(onNewMessage)
        Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF").removeEventListener(onSomeoneTypingMessage)

        if (UserDataService.id == Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF/$USER_ID_REF").toString()) {
            Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF/$VALUE_REF").setValue(false)
        }

        MessageService.clearMessages()
        resetMessageAdapter()
    }

    private fun subscribeChannel(channelId: String?) {
        mainChannelName.text = selectedChannel?.toString()
        MessageService.clearMessages()

        Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF").addListenerForSingleValueEvent(onLoadingNewChannel)
        Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF").addChildEventListener(onNewMessage)
        Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF").addValueEventListener(onSomeoneTypingMessage)
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

    private fun logIn() {
        if (FirebaseAuth.getInstance().currentUser == null)
        {
            val Intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(Intent)
            finish()
        } else {
            Toast.makeText(this,
                    "Welcome back " + UserDataService.name,
                    Toast.LENGTH_LONG)
                    .show()
            val user = FirebaseAuth.getInstance().currentUser
            val userRef = Firebase.database.child(USERS_REF).child(user!!.uid)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userInfo = dataSnapshot.getValue(User::class.java)

                    displayChatMessagesAtStartup(user.uid, userInfo?.name,
                            FirebaseAuth.getInstance().currentUser?.email, userInfo?.avatar)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("error", databaseError.message) //Don't ignore errors!
                }
            }
            userRef.addListenerForSingleValueEvent(valueEventListener)
            loginBtnNavHeader.text = "Log out"
        }
        App.sharedPrefs.isLoggedIn = true



/*
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

            displayChatMessagesAtStartup(user?.uid, user?.displayName, user?.email)
        }

        App.sharedPrefs.isLoggedIn = true
*/
    }

    private fun logOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener {
                    unsubscribeChannel(selectedChannel?.id)
                    UserDataService.logout()

                    channelAdapter.notifyDataSetChanged()
                    messageAdapter.notifyDataSetChanged()

                    userNameNavHeader.text = ""
                    userEmailNavHeader.text = ""
                    userImageNavHeader.setImageResource(R.drawable.profiledefault)
                    userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
                    mainChannelName.text = resources.getString(R.string.please_log_in)
                    loginBtnNavHeader.text = "Log in"

                    Toast.makeText(this,
                            "You have been signed out.",
                            Toast.LENGTH_LONG)
                            .show()
                }
    }

    private val messageTextChangeHandler = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!TextUtils.isEmpty(s.toString()) && selectedChannel != null && s.toString().trim().length == 1)
                setTypingStatus(true)
            else if (s.toString().trim().isEmpty() && typingStarted)
                setTypingStatus(false)
        }

        private fun setTypingStatus(value: Boolean) {
            typingStarted = value

            val channelId = selectedChannel?.id.toString()

            Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF")
                    .child(USER_ID_REF).setValue(UserDataService.id)
            Firebase.database.child("$CHANNELS_REF/$channelId/$SOMEONE_TYPING_REF")
                    .child(VALUE_REF).setValue(value)
        }
    }

    private val onLoadingNewChannel = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (!dataSnapshot.exists()) // no message in this channel, stop showing spinner
                enableSpinner(false)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewChannel:onCancelled", databaseError.toException())
        }
    }

    private val onSomeoneTypingMessage = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (!dataSnapshot.exists())
                return

            if (UserDataService.id != dataSnapshot.child(USER_ID_REF).value) {
                val someoneElseTyping = dataSnapshot.child(VALUE_REF).value as Boolean

                if (someoneElseTyping)
                    typingIndicator.text = resources.getString(R.string.someone_is_typing)
                else
                    typingIndicator.text = ""
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("ERROR", "onNewChannel:onCancelled", databaseError.toException())
        }
    }

    private val onNewChannel = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            runOnUiThread {
                val channelId = dataSnapshot.child(ID_REF).value.toString()
                val channelName = dataSnapshot.child(NAME_REF).value.toString()
                val channelDesc = dataSnapshot.child(DESCRIPTION_REF).value.toString()
                val newChannel = Channel(channelId, channelName, channelDesc)

                drawer_layout.closeDrawer(GravityCompat.START)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
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
            val channelId = dataSnapshot.child(CHANNEL_ID_REF).value.toString()

            if (channelId == selectedChannel?.id) {
                val id = dataSnapshot.child(ID_REF).value.toString()
                val type = dataSnapshot.child(TYPE_REF).value.toString()
                val msgBody = dataSnapshot.child(MESSAGE_BODY_REF).value.toString()
                val userName = dataSnapshot.child(USER_NAME_REF).value.toString()
                val userId = dataSnapshot.child(USER_ID_REF).value.toString()
                val avatarUrl = dataSnapshot.child(AVATAR_URL_REF).value.toString()
                val timeStamp = dataSnapshot.child(TIMESTAMP_REF).value.toString()
                val newMessage = Message(id, type, msgBody, channelId, userId, userName, avatarUrl, timeStamp)
                MessageService.messages.add(newMessage)
            }

            // Only run those lines of code AFTER the drawer closing completely
            // to avoid making the closing animation stutter
            // https://stackoverflow.com/questions/18343018/optimizing-drawer-and-activity-launching-speed
            if (!isDrawerIdle)
                drawerToggle.runWhenIdle(Runnable {
                    updateChatMessage(dataSnapshot)
                    isDrawerIdle = true
                })
            else
                updateChatMessage(dataSnapshot)
        }

        private fun updateChatMessage(dataSnapshot: DataSnapshot) {
            messageAdapter.notifyDataSetChanged()
            messageRecycleView.smoothScrollToPosition(messageAdapter.itemCount - 1)
            enableSpinner(false)
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

    private fun changeChannel(newChannelIndex: Int) {
        if (newChannelIndex > MessageService.channels.count() - 1)
            return

        drawer_layout.closeDrawer(GravityCompat.START)
        val newChannel = MessageService.channels[newChannelIndex]

        if (newChannel.id != selectedChannel?.id || selectedChannel == null) {
            isDrawerIdle = false
            enableSpinner(true)
            unsubscribeChannel(selectedChannel?.id)
            selectedChannel = newChannel
            subscribeChannel(selectedChannel?.id)
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

    fun sendMessageBtnClicked(view: View) {
        if (App.sharedPrefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val channelId = selectedChannel?.id.toString()
            val messagesRef = Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF")
            val messageId = messagesRef.push().key ?: return
            val userId = UserDataService.id

            Firebase.database.child("$USERS_REF/$userId").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(databaseError: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val messageMap = HashMap<String, Any>()
                    val messageBody = messageTextField.text.toString()
                    val userName = UserDataService.name
                    val serverTimeStamp = Firebase.getServerTimeStamp()

                    messageMap[ID_REF] = messageId
                    messageMap[TYPE_REF] = "text"
                    messageMap[MESSAGE_BODY_REF] = messageBody
                    messageMap[CHANNEL_ID_REF] = channelId
                    messageMap[USER_ID_REF] = userId
                    messageMap[USER_NAME_REF] = userName
                    messageMap[AVATAR_URL_REF] = dataSnapshot.child("$AVATAR_REF").value.toString()
                    messageMap[TIMESTAMP_REF] = serverTimeStamp

                    messagesRef.child(messageId).setValue(messageMap)

                    messageTextField.text.clear()
                    hideKeyboard()
                }
            })
        }
    }

    fun addImageBtnClicked(view: View) {
        val galleryIntent = Intent()

        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
    }

    private fun sendChatMessageImage(data: Intent?) {
        if (App.sharedPrefs.isLoggedIn && selectedChannel != null) {
            if (data == null)
                return

            enableSpinner(true)

            val imageUri = data.data
            val channelId = selectedChannel?.id.toString()
            val messagesRef = Firebase.database.child("$CHANNELS_REF/$channelId/$MESSAGES_REF")
            val messageId = messagesRef.push().key ?: return
            val imageUploadPathRef = Firebase.storage.child(MESSAGE_IMAGES_REF).child("$messageId.jpg")

            imageUploadPathRef.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful)
                    throw task.exception!!
                imageUploadPathRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    val messageMap = HashMap<String, Any>()
                    val userId = UserDataService.id
                    val userName = UserDataService.name
                    val serverTimeStamp = Firebase.getServerTimeStamp()

                    messageMap[ID_REF] = messageId
                    messageMap[TYPE_REF] = "image"
                    messageMap[MESSAGE_BODY_REF] = downloadUrl
                    messageMap[CHANNEL_ID_REF] = channelId
                    messageMap[USER_ID_REF] = userId
                    messageMap[USER_NAME_REF] = userName
                    messageMap[AVATAR_URL_REF] = "https://$userName-avatar-url-link.jpg" // TODO: add avatar
                    messageMap[TIMESTAMP_REF] = serverTimeStamp

                    messagesRef.child(messageId).setValue(messageMap)

                    messageTextField.text.clear()
                    hideKeyboard()
                }
            }
        }
    }

    fun userImageNavHeaderClicked(view: View) {
        startUserSettingActivity()
    }

    fun userNameNavHeaderClicked(view: View) {
        startUserSettingActivity()
    }

    fun userEmailNavHeaderClicked(view: View) {
        startUserSettingActivity()
    }

    private fun startUserSettingActivity() {
        val intent = Intent(this, UserSettingActivity::class.java)
        startActivity(intent)
    }

    private fun startUserProfileActivity(position: Int) {
        val intent = Intent(this, UserProfileActivity::class.java)

        intent.putExtra(USER_ID_REF, messageAdapter.messages[position].userId)
        startActivity(intent)
    }

    private val onChatMessageClick = object: RecyclerItemClickListener {
        override fun onClick(view: View, position: Int) {
            val userAvatarImageView = view.findViewById<ImageView>(R.id.messageUserImage)

            userAvatarImageView.setOnClickListener{ _ ->
                startUserProfileActivity(position)
            }
        }

        override fun onLongClick(view: View, position: Int) {
        }

    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable)
            spinner.visibility = View.VISIBLE
        else
            spinner.visibility = View.INVISIBLE
    }
}
