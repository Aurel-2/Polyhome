package iem.bdia.polyhome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import iem.bdia.polyhome.data.local.TokenStorage
import iem.bdia.polyhome.ui.home.HomeActivity
import iem.bdia.polyhome.ui.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenStorage = TokenStorage(this)

        lifecycleScope.launch {
            val token = tokenStorage.getToken()
            if (token != null) {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}