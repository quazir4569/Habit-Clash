package hexis.habitclash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Authentication ViewModel for handling user authentication.
 * Manages login, registration, and authentication state.
 */
class AuthViewModel : ViewModel() {
    // Firebase auth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Authentication state LiveData
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        // Check auth status when ViewModel is created
        checkAuthStatus()
    }

    /**
     * Checks if the user is currently authenticated.
     * Updates the authState based on Firebase Auth state.
     */
    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    /**
     * Signs in a user with email and password.
     * Validates input and updates authentication state.
     */
    fun login(email: String, password: String) {
        // Set loading state
        _authState.value = AuthState.Loading

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password must be filled")
            return
        }

        // Attempt Firebase login
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
     * Creates a new user account and profile.
     * Validates inputs, creates auth account, and stores user profile.
     */
    fun registration(email: String, password: String, username: String) {
        // Set loading state
        _authState.value = AuthState.Loading

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }

        // Attempt to create Firebase account
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated

                    // Store user profile in Firestore
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
     * Updates authentication state to unauthenticated.
     */
    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

/**
 * Authentication state sealed class.
 * Represents all possible auth states in the app.
 */
sealed class AuthState {
    object Authenticated : AuthState()        // User is logged in
    object Unauthenticated : AuthState()      // User is logged out
    object Loading : AuthState()              // Authentication in progress
    data class Error(val message: String) : AuthState()  // Authentication error with message
}