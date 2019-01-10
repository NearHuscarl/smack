package com.nearhuscarl.smack.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindMessage(context, messages[position])
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView?.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView?.findViewById<TextView>(R.id.timestampLbl)
        val userName = itemView?.findViewById<TextView>(R.id.messageUserNameLbl)
        val messageBody = itemView?.findViewById<TextView>(R.id.messageBodyLbl)

        fun bindMessage(context: Context, message: Message) {
            // TODO: add avatar and remove those lines
            val resourceId = context.resources.getIdentifier("dark1", "drawable", context.packageName)
            userImage?.setImageResource(resourceId)
            userImage?.setBackgroundColor(UserDataService.returnAvatarColor("[0.5, 0.5, 0.5, 1]"))

            userName?.text = message.userName
            timeStamp?.text = getFormattedDate(message.timeStamp)
            messageBody?.text = message.messageBody
        }

        private fun returnDateString(isoString: String): String {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoString)
            } catch (e: ParseException) {
                Log.d("PARSE", "Cannot parse date")
            }

            val outDateString = SimpleDateFormat("EEE, h:mm:ss a", Locale.getDefault())
            return outDateString.format(convertedDate)
        }

        private fun getFormattedDate(epochTime: String): String {
            val date = Date(epochTime.toLong())
            val formatter = SimpleDateFormat("EEE, h:mm:ss a", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()

            return formatter.format(date)
        }
    }
}