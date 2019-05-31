package com.luzkan.tictactoe.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luzkan.tictactoe.R
import com.luzkan.tictactoe.database.models.User
import com.luzkan.tictactoe.interfaces.UsernameClickListener
import com.luzkan.tictactoe.interfaces.Util.getCurrentUsername
import java.util.ArrayList

class OnlineAdapter(private val people: ArrayList<User>) : RecyclerView.Adapter<OnlineAdapter.PeopleViewHolder>() {

    private var context: Context? = null
    var userNameListener: UsernameClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_friends, parent,
            false
        )
        return PeopleViewHolder(view, people)
    }

    override fun getItemCount(): Int {
        return people.size
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {

        val user = people[position]
        holder.name.text = user.displayName

        holder.name.setOnClickListener {
            userNameListener?.onUsernameClick(user.displayName)
        }

        holder.onBindViews(position)
    }

    inner class PeopleViewHolder(val view: View, private val people: List<User>) : RecyclerView.ViewHolder(view) {

        fun onBindViews(position: Int) {
            if (itemCount != 0) {
                view.findViewById<TextView>(R.id.tvNick).text = people[position].displayName
                view.findViewById<Button>(R.id.invite).tooltipText = people[position].displayName

                val lobbyA = people[position].displayName + "-" + getCurrentUsername()
                val lobbyB = getCurrentUsername() + "-" + people[position].displayName

                FirebaseDatabase.getInstance().reference.child("Games")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                if(snapshot.key == lobbyA || snapshot.key == lobbyB) {
                                    if (snapshot.hasChild("connected")) {
                                        view.findViewById<Button>(R.id.invite).setText(R.string.join)
                                    } else if (snapshot.hasChild("map")) {
                                        view.findViewById<Button>(R.id.invite).setText(R.string.resume)
                                    } else {
                                        view.findViewById<Button>(R.id.invite).setText(R.string.invite)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })


                // Below is an attempt to make push notifications.
                // I don't have time for that anymore, as two other huge projects have very close deadline

//                view.findViewById<Button>(R.id.invite).setOnClickListener {
//                    val db = FirebaseDatabase.getInstance().reference
//                    db.child("Users")
//                        .child(getCurrentUsername()).child("info")
//                        .addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                val me = dataSnapshot.getValue(User::class.java)
//
//                                val client = OkHttpClient()
//
//                                val to = people[position].uid
//
//                                val request = Request.Builder()
//                                    .url(
//                                        String
//                                            .format(
//                                                "%s/sendNotification?to=%s&fromPushId=%s&fromId=%s&fromName=%s&type=%s",
//                                                "https://tictactoe-fb78f.firebaseio.com/",
//                                                to,
//                                                me!!.uid,
//                                                getCurrentUserId(),
//                                                me.displayName,
//                                                "invite"
//                                            )
//                                    )
//                                    .build()
//
//                                client.newCall(request).enqueue(object : Callback {
//                                    override fun onFailure(call: Call, e: IOException) {
//
//                                    }
//
//                                    @Throws(IOException::class)
//                                    override fun onResponse(call: Call, response: Response) {
//
//                                    }
//                                })
//                            }
//
//                            override fun onCancelled(databaseError: DatabaseError) {
//
//                            }
//                        })
//
//
//                }
            }

        }
        var name: TextView = itemView.findViewById(R.id.tvNick)
    }
}