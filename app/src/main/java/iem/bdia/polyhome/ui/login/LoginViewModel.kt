package iem.bdia.polyhome.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iem.bdia.polyhome.domain.model.User
import iem.bdia.polyhome.domain.repository.UserRepository
import iem.bdia.polyhome.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigateToRegister = MutableStateFlow(false)
    val navigateToRegister: StateFlow<Boolean> = _navigateToRegister.asStateFlow()

    fun onLoginRegisterClicked(email: String, password: String) {
        if (!validateInput(email, password)) return

        val user = User(email.trim(), password.trim())

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (val result = userRepository.login(user)) {

                    is Result.Success -> {
                        _token.value = result.data.token
                    }

                    is Result.Error -> {
                        if (result.code == 401 || result.code == 404) {
                            _navigateToRegister.value = true
                        } else {
                            _error.value = result.message
                        }
                    }
                }

            } catch (e: Exception) {
                _error.value = "Erreur réseau"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onRegisterNavigated() {
        _navigateToRegister.value = false
    }

    private fun validateInput(login: String, password: String): Boolean {
        val email = login.trim()
        when {
            email.isBlank() -> {
                _error.value = "Email requis"
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _error.value = "Email invalide"
                return false
            }

            password.isBlank() -> {
                _error.value = "Mot de passe requis"
                return false
            }
        }

        return true
    }
}
