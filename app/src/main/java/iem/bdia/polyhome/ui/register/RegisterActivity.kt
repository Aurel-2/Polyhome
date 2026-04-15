package iem.bdia.polyhome.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import iem.bdia.polyhome.R
import iem.bdia.polyhome.data.remote.api.RetrofitInstance
import iem.bdia.polyhome.data.remote.repository.UserRepoImpl
import iem.bdia.polyhome.ui.login.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    private lateinit var inputEmail: EditText
    private lateinit var inputPass: EditText
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val repo = UserRepoImpl(RetrofitInstance.apiService)
        viewModel = RegisterViewModel(repo)

        inputEmail = findViewById(R.id.inputEmail)
        inputPass = findViewById(R.id.inputPassword)
        btn = findViewById(R.id.btnCreateAccount)

        findViewById<android.view.View>(R.id.btnBackToLogin).setOnClickListener {
            finish()
        }

        btn.setOnClickListener {
            viewModel.onRegisterClicked(inputEmail.text.toString(), inputPass.text.toString())
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.error.collect {
                        it?.let {
                            Toast.makeText(this@RegisterActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    viewModel.navigateToLogin.collect {
                        if (it) {
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            viewModel.onNavigationDone()
                            finish()
                        }
                    }
                }

                launch {
                    viewModel.loading.collect {
                        btn.isEnabled = !it
                    }
                }
            }
        }
    }
}