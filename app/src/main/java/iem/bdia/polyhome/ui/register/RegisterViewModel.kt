package iem.bdia.polyhome.ui.register

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

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun onRegisterClicked(login: String, password: String) {
        if (!validateInput(login, password)) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val user = User(login = login.trim(), password = password.trim())
                when (val result = userRepository.register(user)) {
                    is Result.Success -> {
                        _navigateToLogin.value = true
                    }

                    is Result.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Une erreur inattendue est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(login: String, password: String): Boolean {
        val email = login.trim()

        when {
            email.isBlank() -> {
                _error.value = "L'email ne doit pas être vide"
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _error.value = "L'email est erroné"
                return false
            }

            password.isBlank() -> {
                _error.value = "Le mot de passe ne doit pas être vide"
                return false
            }

            password.length < 7 -> {
                _error.value = "Le mot de passe doit faire au moins 7 caractères"
                return false
            }
        }
        return true
    }

    fun onNavigationDone() {
        _navigateToLogin.value = false
    }
}