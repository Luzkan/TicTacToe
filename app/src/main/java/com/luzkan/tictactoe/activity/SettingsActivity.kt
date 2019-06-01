package com.luzkan.tictactoe.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.luzkan.tictactoe.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun logout(view: View) {
        Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show()
        Intent(this, MainActivity::class.java).also {
            it.putExtra("logout", true)
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
            finish()
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun changeEmail(view: View) {
    }

    fun changeUsername(view: View) {
    }

    // Sends an email with password reset link.
    fun passwordReset(view: View) {
        password_reset_button.isEnabled = false
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.email?.also { userEmail ->
            auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email sent.", Toast.LENGTH_SHORT).show()
                    Log.d("SETTINGS", "Email sent.")
                    password_reset_button.isEnabled = true
                } else {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show()
                    Log.d("SETTINGS", "Email sending error.")
                    password_reset_button.isEnabled = true
                }
            }
        }
    }

    // Deleting user dialog
    fun deleteUser(view: View) {
        delete_account_button.isEnabled = false
        val alertDialog = this.let { settingsActivity ->
            val builder = AlertDialog.Builder(settingsActivity)
            builder.apply {
                setPositiveButton(R.string.yes) { _, _ ->
                    // User clicked Yes button
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.delete()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                deleteUserFromDB(user)
                                Log.d("SETTINGS", "User account deleted.")
                                Toast.makeText(this.context, "User account deleted.", Toast.LENGTH_SHORT).show()
                                Intent(this.context, MainActivity::class.java).also {
                                    it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    startActivity(it)
                                }
                            } else {
                                Log.d("SETTINGS", "User account deletion error.")
                                Toast.makeText(this.context, "Something went wrong.", Toast.LENGTH_SHORT).show()
                                delete_account_button.isEnabled = true
                            }
                        }
                }
                setNegativeButton(R.string.no) { _, _ ->
                    // User cancelled the dialog
                    delete_account_button.isEnabled = true
                }
            }
            builder.setMessage("Are you sure? This operation cannot be rewind!")
            builder.setTitle("Delete my account")

            // Create the AlertDialog
            builder.create()
        }
        alertDialog.show()
    }

    // Deleting user function
    private fun deleteUserFromDB(user: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userID = user.uid

        db.collection("Users").document(userID).delete().addOnSuccessListener {
            Toast.makeText(this, "User deleted from Users collection.", Toast.LENGTH_SHORT).show()
            Log.d("SETTINGS", "User deleted from Users collection.")
        }
    }
}
