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

    private var currentPlayer = 1
    private var aiMode = false

    private var online = false
    private lateinit var db: FirebaseDatabase
    private var first = false
    private var firstResume = false
    private var gameName = ""

    private var player1 = ArrayList<Int>()
    private var player2 = ArrayList<Int>()
    private var emptyCells = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        makeEmptyCellsList()

        val extras = intent.extras
        if (extras != null) {
            aiMode = extras.getBoolean("aiMode")
            online = extras.getBoolean("online")
            first = extras.getBoolean("first")
            firstResume = extras.getBoolean("first")
            gameName = extras.getString("gameName")
        }

        // The first player starts with locked touching events and they will swap
        if(online) {
            db = FirebaseDatabase.getInstance()
            val movesMade = ArrayList<String>()

            // Handle turns
            db.reference.child("Games").child(gameName).child("map")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (snapshot in dataSnapshot.children) {
                            if(!movesMade.contains(snapshot.key!!)) {
                                val test = "button" + snapshot.key
                                val resourceID = resources.getIdentifier(test, "id", packageName)
                                if (snapshot.getValue(Long::class.java).toString() == "1") {
                                    //Toast.makeText(applicationContext,"Player Blue moved: " + snapshot.key,Toast.LENGTH_SHORT).show()
                                    findViewById<Button>(resourceID).setBackgroundColor(Color.BLUE)
                                    player1.add(snapshot.key!!.toInt())
                                    emptyCells.remove(snapshot.key!!.toInt())
                                } else {
                                    //Toast.makeText(applicationContext,"Player Red moved: " + snapshot.key,Toast.LENGTH_SHORT).show()
                                    findViewById<Button>(resourceID).setBackgroundColor(Color.RED)
                                    player2.add(snapshot.key!!.toInt())
                                    emptyCells.remove(snapshot.key!!.toInt())
                                }
                                turnManager()
                                movesMade.add(snapshot.key!!)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) { /* */ }
                })

            // Fix first player if resuming game
            db.reference.child("Games").child(gameName).child("map")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {  }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(firstResume && dataSnapshot.childrenCount%2 == 1.toLong()){
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }

                    }
                })

            if(!first) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }

    fun butClick(view: View) {

        // Block ability to click screen immediately after action
        if(online){
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }

        val butSelected = view as Button
        var cellID = 0

        when (butSelected.id) {
            R.id.button11 -> cellID = 11
            R.id.button12 -> cellID = 12
            R.id.button13 -> cellID = 13
            R.id.button14 -> cellID = 14
            R.id.button15 -> cellID = 15

            R.id.button21 -> cellID = 21
            R.id.button22 -> cellID = 22
            R.id.button23 -> cellID = 23
            R.id.button24 -> cellID = 24
            R.id.button25 -> cellID = 25

            R.id.button31 -> cellID = 31
            R.id.button32 -> cellID = 32
            R.id.button33 -> cellID = 33
            R.id.button34 -> cellID = 34
            R.id.button35 -> cellID = 35

            R.id.button41 -> cellID = 41
            R.id.button42 -> cellID = 42
            R.id.button43 -> cellID = 43
            R.id.button44 -> cellID = 44
            R.id.button45 -> cellID = 45

            R.id.button51 -> cellID = 51
            R.id.button52 -> cellID = 52
            R.id.button53 -> cellID = 53
            R.id.button54 -> cellID = 54
            R.id.button55 -> cellID = 55
        }

        // Debug; to be removed later or rephrased to something cool for user
        // Toast.makeText(this, "ID: " + cellID, Toast.LENGTH_LONG).show()
        madePlay(cellID, butSelected)
    }

    private fun madePlay(cellID: Int, butSelected: Button) {
        if(currentPlayer == 1){
            //butSelected.text = "X"
            butSelected.setBackgroundColor(Color.BLUE)
            player1.add(cellID)
            emptyCells.remove(cellID)
            if(online) db.getReference("/Games/" + gameName + "/map/" + cellID).setValue(1)
        }else{
            //butSelected.text = "O"
            butSelected.setBackgroundColor(Color.RED)
            player2.add(cellID)
            emptyCells.remove(cellID)
            if(online) db.getReference("/Games/" + gameName + "/map/" + cellID).setValue(2)
        }

        if (!online) turnManager()
        butSelected.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    fun turnManager(){
        checkWin()
        if(currentPlayer == 1){
            currentPlayer = 2
            //whoseTurn.text = "Player Red"
            whoseTurn.background = ContextCompat.getDrawable(applicationContext, R.drawable.playerred)
            if(aiMode) {
                // Optional sleep for half a second to simulate hard calculations
                // Increases satisfaction from beating this tough AI by Human
                // sleep(1000)
                AILogic()
            }
        }else{
            currentPlayer = 1
            //whoseTurn.text = "Player Blue"
            whoseTurn.background = ContextCompat.getDrawable(applicationContext, R.drawable.playerblue)
        }

        // Unblock ability to play accordingly
        if(online) {
            first = if (!first) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                true
            } else {
                false
            }
        }
    }

    private fun checkWin(){
        var winner = 0
        // Cool algorithmTM by me
        // btw. The Game is super stupid if we look for threes so, im making the win condition for four cells
        if(currentPlayer == 1) {
            for (k in 10..50 step 10) {
                for (i in 1..5) {
                    if (i <= 2) {
                        if (player1.contains(k + i) && player1.contains(k + i + 1) && player1.contains(k + i + 2) && player1.contains(k + i + 3))
                            winner = 1
                    }
                    if (k <= 20) {
                        if (player1.contains(k + i) && player1.contains(k + i + 10) && player1.contains(k + i + 20) && player1.contains(k + i + 30))
                            winner = 1
                    }
                    if (k <= 20 && i <= 2) {
                        if (player1.contains(k + i) && player1.contains(k + i + 11) && player1.contains(k + i + 22) && player1.contains(k + i + 33))
                            winner = 1
                    }
                    if (k >= 40 && i <= 5) {
                        if (player1.contains(k + i) && player1.contains(k + i - 9) && player1.contains(k + i - 18) && player1.contains(k + i - 27))
                            winner = 1
                    }
                }
            }
        }

        if(currentPlayer == 2) {
            for (k in 10..50 step 10) {
                for (i in 1..5) {
                    if (i <= 2) {
                        if (player2.contains(k + i) && player2.contains(k + i + 1) && player2.contains(k + i + 2) && player2.contains(k + i + 3))
                            winner = 2
                    }
                    if (k <= 20) {
                        if (player2.contains(k + i) && player2.contains(k + i + 10) && player2.contains(k + i + 20) && player2.contains(k + i + 30))
                            winner = 2
                    }
                    if (k <= 20 && i <= 2) {
                        if (player2.contains(k + i) && player2.contains(k + i + 11) && player2.contains(k + i + 22) && player2.contains(k + i + 33))
                            winner = 2
                    }
                    if (k >= 40 && i <= 5) {
                        if (player2.contains(k + i) && player2.contains(k + i - 9) && player2.contains(k + i - 18) && player2.contains(k + i - 27))
                            winner = 2
                    }
                }
            }
        }

        if(winner != 0) {
            if(winner == 1){
                Toast.makeText(this, "Player Blue has won the game!", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Player Red has won the game!", Toast.LENGTH_LONG).show()
            }
            dialogPop(winner)

            if(online) {
                val deleteMappingFromDb = db.getReference("/Games/" + gameName + "/map")
                deleteMappingFromDb.removeValue()
                val deleteFirstFromDB = db.getReference("/Games/" + gameName + "/first")
                deleteFirstFromDB.removeValue()
            }
        }

        if(emptyCells.isEmpty()) {
            finish()
            Toast.makeText(this, "Game ended with a draw", Toast.LENGTH_LONG).show()
        }
    }

    private fun AILogic(){

        // Prevents game from crashing on draw
        if(emptyCells.isEmpty())
            return

        // Go for a random move
        val r = Random()
        val rand = r.nextInt(emptyCells.size-0)+0
        var cellID: Int
        val butSelected:Button

        // Change from random to algorithmic move if possible
        // Check for win
        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (i <= 3) {
                    if (player2.contains(k + i) && player2.contains(k + i + 1) && player2.contains(k + i + 2)) {
                        if (emptyCells.contains(k + i - 1)) {
                            if (k + i - 1 >= 11) {
                                cellID = k + i - 1
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 3)) {
                            if (k + i + 3 <= 55) {
                                cellID = k + i + 3
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 30) {
                    if (player2.contains(k + i) && player2.contains(k + i + 10) && player2.contains(k + i + 20)) {
                        if (emptyCells.contains(k + i - 10)) {
                            if (k + i - 10 >= 11) {
                                cellID = k + i - 10
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 30)) {
                            if (k + i + 30 <= 55) {
                                cellID = k + i + 30
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 30 && i <= 3) {
                    if (player2.contains(k + i) && player2.contains(k + i + 11) && player2.contains(k + i + 22)) {
                        if (emptyCells.contains(k + i - 11)) {
                            if (k + i - 11 >= 11) {
                                cellID = k + i - 11
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 33)) {
                            if (k + i + 33 <= 55) {
                                cellID = k + i + 33
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k >= 40 && i <= 5) {
                    if (player2.contains(k + i) && player2.contains(k + i - 9) && player2.contains(k + i + 18)) {
                        if (emptyCells.contains(k + i - 27)) {
                            if (k + i - 27 >= 11) {
                                cellID = k + i - 27
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 9)) {
                            if (k + i + 9 <= 55) {
                                cellID = k + i + 9
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
            }
        }

        // Check for player win
        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (i <= 3) {
                    if (player1.contains(k + i) && player1.contains(k + i + 1) && player1.contains(k + i + 2)) {
                        if (emptyCells.contains(k + i - 1)) {
                            if (k + i - 1 >= 11) {
                                cellID = k + i - 1
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 3)) {
                            if (k + i + 3 <= 55) {
                                cellID = k + i + 3
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 30) {
                    if (player1.contains(k + i) && player1.contains(k + i + 10) && player1.contains(k + i + 20)) {
                        if (emptyCells.contains(k + i - 10)) {
                            if (k + i - 10 >= 11) {
                                cellID = k + i - 10
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 30)) {
                            if (k + i + 30 <= 55) {
                                cellID = k + i + 30
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 30 && i <= 3) {
                    if (player1.contains(k + i) && player1.contains(k + i + 11) && player1.contains(k + i + 22)) {
                        if (emptyCells.contains(k + i - 11)) {
                            if (k + i - 11 >= 11) {
                                cellID = k + i - 11
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 33)) {
                            if (k + i + 33 <= 55) {
                                cellID = k + i + 33
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k >= 40 && i <= 5) {
                    if (player1.contains(k + i) && player1.contains(k + i - 9) && player1.contains(k + i - 18)) {
                        if (emptyCells.contains(k + i - 27)) {
                            if (k + i - 27 >= 11) {
                                cellID = k + i - 27
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 9)) {
                            if (k + i + 9 <= 55) {
                                cellID = k + i + 9
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
            }
        }

        // Check if player almost wins logic
        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (i <= 4) {
                    if (player1.contains(k + i) && player1.contains(k + i + 1)) {
                        if (emptyCells.contains(k + i - 1)) {
                            if (k + i - 1 >= 11) {
                                cellID = k + i - 1
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 2)) {
                            if (k + i + 3 <= 55) {
                                cellID = k + i + 2
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 40) {
                    if (player1.contains(k + i) && player1.contains(k + i + 10)) {
                        if (emptyCells.contains(k + i - 10)) {
                            if (k + i - 10 >= 11) {
                                cellID = k + i - 10
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 20)) {
                            if (k + i + 30 <= 55) {
                                cellID = k + i + 20
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 40 && i <= 4) {
                    if (player1.contains(k + i) && player1.contains(k + i + 11)) {
                        if (emptyCells.contains(k + i - 11)) {
                            if (k + i - 11 >= 11) {
                                cellID = k + i - 11
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 22)) {
                            if (k + i + 22 <= 55) {
                                cellID = k + i + 22
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 50 && i <= 5) {
                    if (player1.contains(k + i) && player1.contains(k + i - 9)) {
                        if (emptyCells.contains(k + i - 18)) {
                            if (k + i - 18 >= 11) {
                                cellID = k + i - 18
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 9)) {
                            if (k + i + 9 <= 55) {
                                cellID = k + i + 9
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
            }
        }

        // Check for own almost win
        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (i <= 4) {
                    if (player2.contains(k + i) && player2.contains(k + i + 1)) {
                        if (emptyCells.contains(k + i - 1)) {
                            if (k + i - 1 >= 11) {
                                cellID = k + i - 1
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 2)) {
                            if (k + i + 3 <= 55) {
                                cellID = k + i + 2
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 40) {
                    if (player2.contains(k + i) && player2.contains(k + i + 10)) {
                        if (emptyCells.contains(k + i - 10)) {
                            if (k + i - 10 >= 11) {
                                cellID = k + i - 10
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 20)) {
                            if (k + i + 30 <= 55) {
                                cellID = k + i + 20
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 40 && i <= 4) {
                    if (player2.contains(k + i) && player2.contains(k + i + 11)) {
                        if (emptyCells.contains(k + i - 11)) {
                            if (k + i - 11 >= 11) {
                                cellID = k + i - 11
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 22)) {
                            if (k + i + 22 <= 55) {
                                cellID = k + i + 22
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
                if (k <= 50 && i <= 5) {
                    if (player2.contains(k + i) && player2.contains(k + i - 9)) {
                        if (emptyCells.contains(k + i - 18)) {
                            if (k + i - 18 >= 11) {
                                cellID = k + i - 18
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                        if (emptyCells.contains(k + i + 9)) {
                            if (k + i + 9 <= 55) {
                                cellID = k + i + 9
                                butSelected = whatButton(cellID)
                                madePlay(cellID, butSelected)
                                return
                            }
                        }
                    }
                }
            }
        }

        // Well let's go random in the middle square otherwise
        // We gonna roll for it 15 times, so it will not always pick middle square
        // (if for example it makes unlucky roll 15 times on used cell

        val middleCells = ArrayList<Int>()
        for (k in 20..40 step 10)
            for (i in 2..4)
                middleCells.add(k+i)

        for (n in 1..15){
            val randMid = r.nextInt(middleCells.size-0)+0
            cellID = middleCells[randMid]
            if(emptyCells.contains(cellID)){
                butSelected = whatButton(cellID)
                madePlay(cellID, butSelected)
                return
            }
        }

        // TBA:  [x] [x] [_] [x] scenarios check
        // Logic was supposed to be simple, so i'm leaving it so
        // I can move on on another project

        cellID = emptyCells[rand]
        butSelected = whatButton(cellID)
        madePlay(cellID,butSelected)

    }

    private fun whatButton(cellID: Int): Button {
        val butSelected:Button
        when(cellID){
            11 -> butSelected = button11
            12 -> butSelected = button12
            13 -> butSelected = button13
            14 -> butSelected = button14
            15 -> butSelected = button15
            21 -> butSelected = button21
            22 -> butSelected = button22
            23 -> butSelected = button23
            24 -> butSelected = button24
            25 -> butSelected = button25
            31 -> butSelected = button31
            32 -> butSelected = button32
            33 -> butSelected = button33
            34 -> butSelected = button34
            35 -> butSelected = button35
            41 -> butSelected = button41
            42 -> butSelected = button42
            43 -> butSelected = button43
            44 -> butSelected = button44
            45 -> butSelected = button45
            51 -> butSelected = button51
            52 -> butSelected = button52
            53 -> butSelected = button53
            54 -> butSelected = button54
            55 -> butSelected = button55
            else -> butSelected = button55
        }
        return butSelected
    }

    private fun dialogPop(winner: Int) {
        val builder = AlertDialog.Builder(this@PlayActivity)

        if(winner == 1) {
            builder.setTitle("Player Blue won!")
        }else{
            builder.setTitle("Player Red won!")
        }
        builder.setMessage("Do you want to play again?")

        builder.setPositiveButton("HELL YES"){ _, _ ->

            Toast.makeText(applicationContext,"New background for even more entertainment!",Toast.LENGTH_SHORT).show()

            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val ll = findViewById<ConstraintLayout>(R.id.activity_play)
            ll.setBackgroundColor(color)

            restartGame()
        }

        builder.setNegativeButton("No"){ _, _ ->
            Toast.makeText(applicationContext,"See you next time!",Toast.LENGTH_SHORT).show()
            finish()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun restartGame() {

        // Awful code incoming
        // (just wanted to try dialog box with play again, so I didn't prepare beforehand for that
        // originally it was just finish() and Toast with toast for the winner)

        button11.setBackgroundResource(android.R.drawable.btn_default)
        button11.isEnabled=true
        button13.setBackgroundResource(android.R.drawable.btn_default)
        button13.isEnabled=true
        button12.setBackgroundResource(android.R.drawable.btn_default)
        button12.isEnabled=true
        button14.setBackgroundResource(android.R.drawable.btn_default)
        button14.isEnabled=true
        button15.setBackgroundResource(android.R.drawable.btn_default)
        button15.isEnabled=true

        button21.setBackgroundResource(android.R.drawable.btn_default)
        button21.isEnabled=true
        button22.setBackgroundResource(android.R.drawable.btn_default)
        button22.isEnabled=true
        button23.setBackgroundResource(android.R.drawable.btn_default)
        button23.isEnabled=true
        button24.setBackgroundResource(android.R.drawable.btn_default)
        button24.isEnabled=true
        button25.setBackgroundResource(android.R.drawable.btn_default)
        button25.isEnabled=true

        button31.setBackgroundResource(android.R.drawable.btn_default)
        button31.isEnabled=true
        button32.setBackgroundResource(android.R.drawable.btn_default)
        button32.isEnabled=true
        button33.setBackgroundResource(android.R.drawable.btn_default)
        button33.isEnabled=true
        button34.setBackgroundResource(android.R.drawable.btn_default)
        button34.isEnabled=true
        button35.setBackgroundResource(android.R.drawable.btn_default)
        button35.isEnabled=true

        button41.setBackgroundResource(android.R.drawable.btn_default)
        button41.isEnabled=true
        button42.setBackgroundResource(android.R.drawable.btn_default)
        button42.isEnabled=true
        button43.setBackgroundResource(android.R.drawable.btn_default)
        button43.isEnabled=true
        button44.setBackgroundResource(android.R.drawable.btn_default)
        button44.isEnabled=true
        button45.setBackgroundResource(android.R.drawable.btn_default)
        button45.isEnabled=true

        button51.setBackgroundResource(android.R.drawable.btn_default)
        button51.isEnabled=true
        button52.setBackgroundResource(android.R.drawable.btn_default)
        button52.isEnabled=true
        button53.setBackgroundResource(android.R.drawable.btn_default)
        button53.isEnabled=true
        button54.setBackgroundResource(android.R.drawable.btn_default)
        button54.isEnabled=true
        button55.setBackgroundResource(android.R.drawable.btn_default)
        button55.isEnabled=true

        player1.clear()
        player2.clear()
        emptyCells.clear()
        makeEmptyCellsList()
        currentPlayer = 1
    }

    private fun makeEmptyCellsList(){

        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (!(player1.contains(k + i) || player2.contains(k + i)))
                    emptyCells.add(k + i)
            }
        }

    }
}
