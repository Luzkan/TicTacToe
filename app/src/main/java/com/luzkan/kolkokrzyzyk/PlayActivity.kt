package com.luzkan.kolkokrzyzyk

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : AppCompatActivity() {

    private var currentPlayer = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
    }

    fun turnManager(){
        if(currentPlayer == 1){
            currentPlayer = 2
        }else{
            currentPlayer = 1
        }
    }

    var player1 = ArrayList<Int>()
    var player2 = ArrayList<Int>()

    fun checkWin(){

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
        Toast.makeText(this, "Cell ID: " + cellID, Toast.LENGTH_LONG).show()
        madePlay(cellID, butSelected)
    }

    private fun madePlay(cellID: Int, butSelected: Button) {

        if(currentPlayer == 1){
            butSelected.text = "X"
            butSelected.setBackgroundColor(Color.BLUE)
            player1.add(cellID)
            turnManager()
        }else{
            butSelected.text = "O"
            butSelected.setBackgroundColor(Color.GREEN)
            player2.add(cellID)
            turnManager()
        }

        butSelected.isEnabled = false
    }


}
