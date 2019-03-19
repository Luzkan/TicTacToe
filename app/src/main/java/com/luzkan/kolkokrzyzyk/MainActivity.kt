package com.luzkan.kolkokrzyzyk

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playAI.setOnClickListener{
            val intent = Intent(this,PlayActivity::class.java)
            intent.putExtra("aiMode", true)
            startActivity(intent)
        }

        playVS.setOnClickListener{
            val intent = Intent(this,PlayActivity::class.java)
            intent.putExtra("aiMode", false)
            startActivity(intent)
        }

    }
}
