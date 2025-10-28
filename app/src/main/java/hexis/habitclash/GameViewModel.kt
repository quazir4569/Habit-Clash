package hexis.habitclash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private val repo = GameRepository()


    var friends by mutableStateOf<List<String>>(emptyList())
        private set

    var addFriendMessage by mutableStateOf<String?>(null)
        private set

    var deleteFriendMessage by mutableStateOf("")
        private set

    var leaderboard = mutableStateOf<List<LeaderboardEntry>>(emptyList())
        private set


    fun loadFriends() {
        repo.getFriends { list -> friends = list }
    }

    fun loadFriendsLeaderboard() {
        repo.getFriendsLeaderboard { leaderboard.value = it }
    }


    fun addFriendByUsername(username: String) {
        repo.addFriendByUsername(username, onSuccess = {
            addFriendMessage = "Friend added successfully!"
            loadFriends()
        }, onError = { msg ->
            addFriendMessage = msg
        })
    }

    fun deleteFriend(friendId: String) {
        repo.deleteFriendByUserId(friendId, onSuccess = {
            friends = friends.filterNot { it == friendId }
            deleteFriendMessage = "Friend deleted successfully!"


        }, onError = { msg ->
            deleteFriendMessage = msg
        })
    }
}
