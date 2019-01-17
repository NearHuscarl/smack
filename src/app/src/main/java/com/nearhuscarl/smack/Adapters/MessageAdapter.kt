package com.nearhuscarl.smack.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nearhuscarl.smack.Models.Message
import com.nearhuscarl.smack.R
import com.nearhuscarl.smack.Services.UserDataService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.nav_header_main.*
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
        holder.bindMessage(context, messages[position])
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val userImage = itemView?.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView?.findViewById<TextView>(R.id.timestampLbl)
        val userName = itemView?.findViewById<TextView>(R.id.messageUserNameLbl)
        val messageBody = itemView?.findViewById<TextView>(R.id.messageBodyLbl)
        val messageImage = itemView?.findViewById<ImageView>(R.id.messageImage)

        fun bindMessage(context: Context, message: Message) {
            Picasso.get()
                    .load(message.avatarUrl)
                    .placeholder(R.drawable.profiledefault)
                    .into(userImage)

            userName?.text = message.userName
            timeStamp?.text = getFormattedDate(message.timeStamp)

            if (message.type == "text")
            {
                messageBody?.text = message.messageBody
                messageBody?.visibility = View.VISIBLE
                messageImage?.visibility = View.GONE
            }
            else
            {
                messageBody?.visibility = View.GONE
                messageImage?.visibility = View.VISIBLE
                Picasso.get()
                        .load(message.messageBody)
                        .resize(400, 0)
                        .placeholder(R.drawable.download)
                        .into(messageImage)
            }
        }

        private fun getFormattedDate(epochTime: String): String {
            val date = Date(epochTime.toLong())
            val formatter = SimpleDateFormat("EEE, h:mm:ss a", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()

            return formatter.format(date)
        }
    }
}