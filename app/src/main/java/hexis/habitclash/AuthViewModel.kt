package hexis.habitclash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password : String){

        _authState.value = AuthState.Loading

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password must be filled")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){

                    _authState.value = AuthState.Authenticated

                }else{

                    _authState.value = AuthState.Error(task.exception?.message?:"Incorrect email or password")

                }

            }

    }

    fun registration(email: String, password : String){

        _authState.value = AuthState.Loading

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password must be filled")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){

                    _authState.value = AuthState.Authenticated

                }else{

                    _authState.value = AuthState.Error(task.exception?.message?:"Incorrect email or password")

                }

            }

    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class  AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState ()
}