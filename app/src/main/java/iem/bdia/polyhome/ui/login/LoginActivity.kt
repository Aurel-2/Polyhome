package iem.bdia.polyhome.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import iem.bdia.polyhome.R
import iem.bdia.polyhome.data.local.TokenStorage
import iem.bdia.polyhome.data.remote.api.RetrofitInstance
import iem.bdia.polyhome.data.remote.repository.UserRepoImpl
import iem.bdia.polyhome.ui.home.HomeActivity
import iem.bdia.polyhome.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val repo = UserRepoImpl(RetrofitInstance.apiService)
        viewModel = LoginViewModel(repo)

        val tokenStorage = TokenStorage(this)

        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            viewModel.onLoginRegisterClicked(
                inputEmail.text.toString(),
                inputPassword.text.toString()
            )
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        observeViewModel(tokenStorage)
    }

    private fun observeViewModel(tokenStorage: TokenStorage) {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                launch {
                    viewModel.token.collect { token ->
                        token?.let {
                            tokenStorage.saveToken(it)
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        }
                    }
                }
                launch {
                    viewModel.loading.collect { isLoading ->
                        btnLogin.isEnabled = !isLoading
                        btnRegister.isEnabled = !isLoading
                    }

                }
                launch {
                    viewModel.navigateToRegister.collect { shouldNavigate ->
                        if (shouldNavigate) {
                            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                            viewModel.onRegisterNavigated()
                        }
                    }
                }
            }
        }
    }
}
