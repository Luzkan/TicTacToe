package com.luzkan.kolkokrzyzyk

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_play.*
import java.lang.Thread.sleep
import java.util.*

class PlayActivity : AppCompatActivity() {

    private var currentPlayer = 1
    private var aiMode = false

    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()
    var emptyCells = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if(!(player1.contains(k+i) || player2.contains(k+i)))
                    emptyCells.add(k + i)
            }
        }

        val extras = intent.extras
        if (extras != null) {
            aiMode = extras.getBoolean("aiMode")
        }
    }

    fun butClick(view: View) {

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
            turnManager()
        }else{
            //butSelected.text = "O"
            butSelected.setBackgroundColor(Color.RED)
            player2.add(cellID)
            emptyCells.remove(cellID)
            turnManager()
        }

        butSelected.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    fun turnManager(){
        checkWin()
        if(currentPlayer == 1){
            currentPlayer = 2
            whoseTurn.text = "Player Red"
            if(aiMode) {
                // Optional sleep for half a second to simulate hard calculations
                // Increases satisfaction from beating this tough AI by Human
                // sleep(1000)
                AILogic()
            }
        }else{
            currentPlayer = 1
            whoseTurn.text = "Player Blue"
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
                }
            }
        }

        if(winner != 0) {
            if(winner == 1){
                finish()
                Toast.makeText(this, "Player 1 has won the game!", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Player 2 has won the game!", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        if(emptyCells.isEmpty()) {
            finish()
            Toast.makeText(this, "Game ended with a draw", Toast.LENGTH_LONG).show()
        }
    }

    private fun AILogic(){

        // Go for a random move
        val r = Random()
        val rand = r.nextInt(emptyCells.size-0)+0
        var cellID = emptyCells[rand]
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
            }
        }

        /*
        // Check for almost win
        for (k in 10..50 step 10) {
            for (i in 1..5) {
                if (i <= 4) {
                    if (player2.contains(k + i) && player2.contains(k + i + 1))
                        if(emptyCells.contains(k+i-1)){
                            madePlay(k+i-1, butSelected)
                        }
                        if(emptyCells.contains(k+i+3)){
                            madePlay(k+i+3, butSelected)
                        }
                }
                if (k <= 40) {
                    if (player2.contains(k + i) && player2.contains(k + i + 10)) {
                        if (emptyCells.contains(k + i - 10)) {
                            madePlay(k + i - 10, butSelected)
                        }
                        if (emptyCells.contains(k + i + 20)) {
                            madePlay(k + i + 20, butSelected)
                        }
                    }
                }
                if (k <= 40 && i <= 4) {
                    if (player2.contains(k + i) && player2.contains(k + i + 11)) {
                        if (emptyCells.contains(k + i - 11)) {
                            madePlay(k + i - 11, butSelected)
                        }
                        if (emptyCells.contains(k + i + 22)) {
                            madePlay(k + i + 22, butSelected)
                        }
                    }
                }
            }
        }
        */

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
}
