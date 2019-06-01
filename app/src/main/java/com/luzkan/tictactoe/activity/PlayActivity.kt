package com.luzkan.tictactoe.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luzkan.tictactoe.R
import kotlinx.android.synthetic.main.activity_play.*
import java.util.*

class PlayActivity : AppCompatActivity() {
    // Starting player
    private var currentPlayer = 1

    // Intents
    private var aiMode = false
    private var online = false
    private var first = false
    private var firstResume = false
    private var gameName = ""

    // Arrays
    private var player1 = ArrayList<Int>()
    private var player2 = ArrayList<Int>()
    private var emptyCells = ArrayList<Int>()
    private val allCells = ArrayList<Int>()
    private val middleCells = ArrayList<Int>()

    // Database init when online
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        // Prepare type of the game from taken intents
        val extras = intent.extras
        if (extras != null) {
            aiMode = extras.getBoolean("aiMode")
            online = extras.getBoolean("online")
            first = extras.getBoolean("first")
            firstResume = extras.getBoolean("first")
            gameName = extras.getString("gameName")
        }

        // Prepare cells arrays
        makeCellsLists()

        // The first player starts with locked touching events and they will swap
        if(online) {
            db = FirebaseDatabase.getInstance()
            val movesMade = ArrayList<String>()

            // Handle turns
            db.reference.child("Games").child(gameName).child("map").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        if(!movesMade.contains(snapshot.key!!)) {
                            val resourceID = resources.getIdentifier("button" + snapshot.key, "id", packageName)
                            if (snapshot.getValue(Long::class.java).toString() == "1") {
                                findViewById<Button>(resourceID).setBackgroundColor(Color.BLUE)
                                player1.add(snapshot.key!!.toInt())
                            }else{
                                findViewById<Button>(resourceID).setBackgroundColor(Color.RED)
                                player2.add(snapshot.key!!.toInt())
                            }
                            emptyCells.remove(snapshot.key!!.toInt())
                            turnManager()
                            movesMade.add(snapshot.key!!)
                        }
                    }
                }
                override fun onCancelled(p0: DatabaseError) {}
            })

            // Fix first player if resuming game
            db.reference.child("Games").child(gameName).child("map").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(firstResume && dataSnapshot.childrenCount%2 == 1.toLong())
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
                override fun onCancelled(p0: DatabaseError) {}
            })

            if(!first) window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    fun butClick(view: View) {
        // Block ability to click screen immediately after action
        if (online) window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val butSelected = view as Button
        val cellID = resources.getResourceEntryName(butSelected.id).takeLast(2).toInt()
        madePlay(cellID, butSelected)
    }

    private fun madePlay(cellID: Int, butSelected: Button) {
        if (currentPlayer == 1){
            butSelected.setBackgroundColor(Color.BLUE)
            player1.add(cellID)
            if (online) db.getReference("/Games/$gameName/map/$cellID").setValue(1)
        }else{
            butSelected.setBackgroundColor(Color.RED)
            player2.add(cellID)
            if (online) db.getReference("/Games/$gameName/map/$cellID").setValue(2)
        }
        emptyCells.remove(cellID)
        butSelected.isEnabled = false
        if (!online) turnManager()
    }

    @SuppressLint("SetTextI18n")
    fun turnManager(){
        checkWin()
        if(currentPlayer == 1){
            currentPlayer = 2
            whoseTurn.background = ContextCompat.getDrawable(applicationContext, R.drawable.playerred)
            if (aiMode) aiLogic()
        }else{
            currentPlayer = 1
            whoseTurn.background = ContextCompat.getDrawable(applicationContext, R.drawable.playerblue)
        }

        // Unblock ability to play accordingly
        if (online) first = if (!first) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            true
        } else false
    }

    // [2.1 Change] Since - changed win condition to five cells
    private fun checkWin(){
        var winner = 0
        if(currentPlayer == 1)
            for (k in 10..80 step 10) for (i in 1..8) {
                    if (player1.contains(k + i) && player1.contains(k + i + 1) && player1.contains(k + i + 2) && player1.contains(k + i + 3) && player1.contains(k + i + 4)) winner = 1
                    if (player1.contains(k + i) && player1.contains(k + i + 10) && player1.contains(k + i + 20) && player1.contains(k + i + 30) && player1.contains(k + i + 40)) winner = 1
                    if (player1.contains(k + i) && player1.contains(k + i + 11) && player1.contains(k + i + 22) && player1.contains(k + i + 33) && player1.contains(k + i + 44)) winner = 1
                    if (player1.contains(k + i) && player1.contains(k + i - 9) && player1.contains(k + i - 18) && player1.contains(k + i - 27) && player1.contains(k + i - 36)) winner = 1
            }

        if(currentPlayer == 2)
            for (k in 10..80 step 10) for (i in 1..8) {
                    if (player2.contains(k + i) && player2.contains(k + i + 1) && player2.contains(k + i + 2) && player2.contains(k + i + 3) && player2.contains(k + i + 4)) winner = 2
                    if (player2.contains(k + i) && player2.contains(k + i + 10) && player2.contains(k + i + 20) && player2.contains(k + i + 30) && player2.contains(k + i + 40)) winner = 2
                    if (player2.contains(k + i) && player2.contains(k + i + 11) && player2.contains(k + i + 22) && player2.contains(k + i + 33) && player2.contains(k + i + 44)) winner = 2
                    if (player2.contains(k + i) && player2.contains(k + i - 9) && player2.contains(k + i - 18) && player2.contains(k + i - 27) && player2.contains(k + i - 36)) winner = 2
            }

        if(winner != 0) {
            if (winner == 1) Toast.makeText(this, "Player Blue has won the game!", Toast.LENGTH_LONG).show()
            else             Toast.makeText(this, "Player Red has won the game!", Toast.LENGTH_LONG).show()
            dialogPop(winner)

            if(online) {
                val deleteMappingFromDb = db.getReference("/Games/$gameName/map")
                val deleteFirstFromDB = db.getReference("/Games/$gameName/first")
                deleteMappingFromDb.removeValue()
                deleteFirstFromDB.removeValue()
            }
        }

        if(emptyCells.isEmpty()) {
            Toast.makeText(this, "Game ended with a draw", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // [2.1] Updated Code and cleaned it up (upgraded from my "second app skills" to current knowledge of Kotlin)
    private fun aiLogic(){
        // Prevents game from crashing on draw
        if(emptyCells.isEmpty())
            return

        // Prepare
        var cellID: Int
        val butSelected:Button

        // Go for a random move...
        val r = Random()
        val rand = r.nextInt(emptyCells.size-0)+0
        // ... and change from random to algorithmic move if possible

        // Check for win - if got [4] pieces anywhere with empty space to finish
        for (k in 10..80 step 10) for (i in 1..8) {
            if (player2.contains(k+i) && player2.contains(k+i+1) && player2.contains(k+i+2) && player2.contains(k+i+3)) {
                if (emptyCells.contains(k+i-1) && allCells.contains(k+i-1))
                    return madePlay(k+i-1, butSelected = whatButton(k+i-1))
                if (emptyCells.contains(k+i+4) && allCells.contains(k+i+4))
                    return madePlay(k+i+4, butSelected = whatButton(k+i+4))
            }

            if (player2.contains(k+i) && player2.contains(k+i+10) && player2.contains(k+i+20) && player2.contains(k+i+30)) {
                if (emptyCells.contains(k+i-10) && allCells.contains(k+i-10))
                    return madePlay(k+i-10, butSelected = whatButton(k+i-10))
                if (emptyCells.contains(k+i+40) && allCells.contains(k+i+40))
                    return madePlay(k+i+40, butSelected = whatButton(k+i+40))
            }

            if (player2.contains(k+i) && player2.contains(k+i+11) && player2.contains(k+i+22) && player2.contains(k+i+33)) {
                if (emptyCells.contains(k+i-11) && allCells.contains(k+i-11))
                    return madePlay(k+i-11, butSelected = whatButton(k+i-11))
                if (emptyCells.contains(k+i+44) && allCells.contains(k+i+44))
                    return madePlay(k+i+44, butSelected = whatButton(k+i+44))
            }

            if (player2.contains(k+i) && player2.contains(k+i-9) && player2.contains(k+i-18) && player2.contains(k+i-27)) {
                if (emptyCells.contains(k+i+9) && allCells.contains(k+i+9))
                    return madePlay(k+i+9, butSelected = whatButton(k+i+9))
                if (emptyCells.contains(k+i-36) && allCells.contains(k+i-36))
                    return madePlay(k+i-36, butSelected = whatButton(k+i-36))
            }
        }

        // Check for player win
        for (k in 10..80 step 10) for (i in 1..8) {
            if (player1.contains(k+i) && player1.contains(k+i+1) && player1.contains(k+i+2) && player2.contains(k+i+3)) {
                if (emptyCells.contains(k+i-1) && allCells.contains(k+i-1))
                    return madePlay(k+i-1, butSelected = whatButton(k+i-1))
                if (emptyCells.contains(k+i+4) && allCells.contains(k+i+4))
                    return madePlay(k+i+4, butSelected = whatButton(k+i+4))
            }

            if (player1.contains(k+i) && player1.contains(k+i+10) && player1.contains(k+i+20) && player2.contains(k+i+30)) {
                if (emptyCells.contains(k+i-10) && allCells.contains(k+i-10))
                    return madePlay(k+i-10, butSelected = whatButton(k+i-10))
                if (emptyCells.contains(k+i+40) && allCells.contains(k+i+40))
                    return madePlay(k+i+40, butSelected = whatButton(k+i+40))
            }

            if (player1.contains(k+i) && player1.contains(k+i+11) && player1.contains(k+i+22) && player2.contains(k+i+33)) {
                if (emptyCells.contains(k+i-11) && allCells.contains(k+i-11))
                    return madePlay(k+i-11, butSelected = whatButton(k+i-11))
                if (emptyCells.contains(k+i+44) && allCells.contains(k+i+44))
                    return madePlay(k+i+44, butSelected = whatButton(k+i+44))
            }

            if (player1.contains(k+i) && player1.contains(k+i-9) && player1.contains(k+i-18) && player2.contains(k+i-27)) {
                if (emptyCells.contains(k+i+9) && allCells.contains(k+i+9))
                    return madePlay(k+i+9, butSelected = whatButton(k+i+9))
                if (emptyCells.contains(k+i-36) && allCells.contains(k+i-36))
                    return madePlay(k+i-36, butSelected = whatButton(k+i-36))
            }
        }

        // Check if player almost wins logic
        for (k in 10..80 step 10) for (i in 1..8) {
            if (player1.contains(k+i) && player1.contains(k+i+1)) {
                if (emptyCells.contains(k+i-1) && allCells.contains(k+i-1))
                    return madePlay(k+i-1, butSelected = whatButton(k+i-1))
                if (emptyCells.contains(k+i+2) && allCells.contains(k+i+2))
                    return madePlay(k+i+2, butSelected = whatButton(k+i+2))
            }

            if (player1.contains(k+i) && player1.contains(k+i+10)) {
                if (emptyCells.contains(k+i-10) && allCells.contains(k+i-10))
                    return madePlay(k+i-10, butSelected = whatButton(k+i-10))
                if (emptyCells.contains(k+i+20) && allCells.contains(k+i+20))
                    return madePlay(k+i+20, butSelected = whatButton(k+i+20))
            }

            if (player1.contains(k+i) && player1.contains(k+i+11)) {
                if (emptyCells.contains(k+i-11) && allCells.contains(k+i-11))
                    return madePlay(k+i-11, butSelected = whatButton(k+i-11))
                if (emptyCells.contains(k+i+22) && allCells.contains(k+i+22))
                    return madePlay(k+i+22, butSelected = whatButton(k+i+22))

            }

            if (player1.contains(k+i) && player1.contains(k+i-9)) {
                if (emptyCells.contains(k+i-18) && allCells.contains(k+i-18))
                    return madePlay(k+i-18, butSelected = whatButton(k+i-18))
                if (emptyCells.contains(k+i+9) && allCells.contains(k+i+9))
                    return madePlay(k+i+9, butSelected = whatButton(k+i+9))
            }
        }

        // Check for own almost win
        for (k in 10..80 step 10) for (i in 1..8) {
            if (player2.contains(k+i) && player2.contains(k+i+1)) {
                if (emptyCells.contains(k+i-1) && allCells.contains(k+i-1))
                    return madePlay(k+i-1, butSelected = whatButton(k+i-1))
                if (emptyCells.contains(k+i+2) && allCells.contains(k+i+2))
                    return madePlay(k+i+2, butSelected = whatButton(k+i+2))
            }

            if (player2.contains(k+i) && player2.contains(k+i+10)) {
                if (emptyCells.contains(k+i-10) && allCells.contains(k+i-10))
                    return madePlay(k+i-10, butSelected = whatButton(k+i-10))
                if (emptyCells.contains(k+i+20) && allCells.contains(k+i+20))
                    return madePlay(k+i+20, butSelected = whatButton(k+i+20))
            }

            if (player2.contains(k+i) && player2.contains(k+i+11)) {
                if (emptyCells.contains(k+i-11) && allCells.contains(k+i-11))
                    return madePlay(k+i-11, butSelected = whatButton(k+i-11))
                if (emptyCells.contains(k+i+22) && allCells.contains(k+i+22))
                    return madePlay(k+i+22, butSelected = whatButton(k+i+22))
            }

            if (player2.contains(k+i) && player2.contains(k+i-9)) {
                if (emptyCells.contains(k+i-18) && allCells.contains(k+i-18))
                    return madePlay(k+i-18, butSelected = whatButton(k+i-18))
                if (emptyCells.contains(k+i+9) && allCells.contains(k+i+9))
                    return madePlay(k+i+9, butSelected = whatButton(k+i+9))
            }
        }

        // If no "good" move was detected - let's go for middle of the map
        // Bot is going to roll it 10 times - if it fails, it goes for random move
        for (n in 1..10){
            val randMid = r.nextInt(middleCells.size-0)+0
            cellID = middleCells[randMid]
            if (emptyCells.contains(cellID)) return madePlay(cellID, butSelected = whatButton(cellID))
        }

        // Todo:  [x] [x] [_] [x] scenarios check
        cellID = emptyCells[rand]
        butSelected = whatButton(cellID)
        madePlay(cellID,butSelected)
    }

    private fun whatButton(cellID: Int): Button {
        val butSelected:Button
        val resourceID = resources.getIdentifier("button$cellID", "id", packageName)
        butSelected = findViewById(resourceID)
        return butSelected
    }

    private fun dialogPop(winner: Int) {
        val builder = AlertDialog.Builder(this@PlayActivity)

        if (winner == 1) builder.setTitle("Player Blue won!")
        else             builder.setTitle("Player Red won!")

        builder.setMessage("Do you want to play again?")
        builder.setPositiveButton("HELL YES"){ _, _ ->
            Toast.makeText(applicationContext,"New background for even more entertainment!",Toast.LENGTH_SHORT).show()
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val ll = findViewById<ConstraintLayout>(R.id.activity_play)
            ll.setBackgroundColor(color)
            restartGame(winner)
        }

        builder.setNegativeButton("No"){ _, _ ->
            Toast.makeText(applicationContext,"See you next time!",Toast.LENGTH_SHORT).show()
            finish()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun restartGame(winner: Int) {
        for (k in 10..80 step 10) for (i in 1..8) {
            val butSelected: Button
            val id = k+i
            val resourceID = resources.getIdentifier("button$id", "id", packageName)
            butSelected = findViewById<Button>(resourceID)
            butSelected.setBackgroundResource(android.R.drawable.btn_default)
            butSelected.isEnabled=true
        }
        makeCellsLists()
        turnManager()
    }

    private fun makeCellsLists(){
        player1.clear()
        player2.clear()
        emptyCells.clear()
        allCells.clear()
        middleCells.clear()

        for (k in 10..80 step 10) for (i in 1..8)
            if (!(player1.contains(k+i) || player2.contains(k+i)))
                emptyCells.add(k+i)
        for (k in 10..80 step 10)  for (i in 1..8)
            if (!(player1.contains(k+i) || player2.contains(k+i)))
                allCells.add(k+i)
        for (k in 40..50 step 10)
            for (i in 4..5)
                middleCells.add(k+i)
    }
}
