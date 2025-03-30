package hexis.habitclash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles user login, signup, and tracks auth status.
 * Uses Firebase for user management.
 */
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    /**
     * Checks if user is logged in and updates state.
     */
    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    /**
     * Logs in a user with email and password.
     * Updates auth state based on result.
     */
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password must be filled")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Incorrect email or password")
                }
            }
    }

    /**
     * Creates a new user account.
     * Updates auth state based on result.
     */
    fun registration(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated

                    // Save username as a background operation
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userDoc = FirebaseFirestore.getInstance().collection("users").document(userId)
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email
                        )
                        userDoc.set(userData)
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed")
                }
            }
    }

    /**
     * Signs out the current user.
     */
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

/**
 * All possible auth states in the app.
 */
sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}