package mx.edu.utng.rpd.meditrack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.rpd.meditrack.repository.FirebaseRepository

class AuthViewModel(private val repo: FirebaseRepository = FirebaseRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val userId = repo.obtenerUsuarioActual()
        _authState.value = if (userId != null) {
            AuthState.Authenticated(userId)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun registrar(email: String, password: String, nombre: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.registrarUsuario(email, password, nombre)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.iniciarSesion(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Authenticated(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun cerrarSesion() {
        repo.cerrarSesion()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}