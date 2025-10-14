package hexis.habitclash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

@Composable
fun FriendListScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    viewModel: GameViewModel = viewModel()
) {

    val isDarkMode = themeViewModel.isDarkMode
    var username by remember { mutableStateOf("User") }
    var showFriendDialog by remember { mutableStateOf(false) }
    val friends = viewModel.friends ?: emptyList()
    val colors = getAppThemeColors(isDarkMode)

    LaunchedEffect(Unit) {
        viewModel.loadFriends()

    }

    Column(
        modifier = Modifier.fillMaxSize()


    ) {


        Column(
            modifier = Modifier
                .padding(24.dp)
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Headline using Inter SemiBold font
            Text(
                text = "Friend List", style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = { showFriendDialog = true }) {
                Text("Add Friend")
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(friends) { friendId ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = friendId)

                            Button(onClick = { viewModel.deleteFriend(friendId) }) {

                                Text("Delete")
                            }

                        }
                    }
                }
            }
        }
        BottomNavigationBar(navController, isDarkMode)

    }

    if (showFriendDialog) {
        Dialog(onDismissRequest = { showFriendDialog = false }) {
            AnimatedVisibility(
                visible = showFriendDialog,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
            ) {

                AlertDialog(
                    onDismissRequest = { showFriendDialog = false },
                    title = { Text("Add a Friend") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Enter player's name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            viewModel.addFriendMessage?.let {
                                Text(
                                    text = it,
                                    color = if (it.contains(
                                            "success", true
                                        )
                                    ) Color.Green else Color.Red,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (username.isNotBlank()) {
                                viewModel.addFriendByUsername(username.trim())
                                username = ""
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showFriendDialog = false }) {
                            Text("Cancel")
                        }
                    })
            }
        }
    }

}

class GameRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getFriends(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onResult(emptyList())
            return
        }

        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            if (friendIds.isEmpty()) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            // Firestore whereIn supports up to 10 items — if <=10 we do a single query
            if (friendIds.size <= 10) {
                db.collection("users").whereIn(FieldPath.documentId(), friendIds).get()
                    .addOnSuccessListener { snapshot ->
                        val usernames =
                            snapshot.documents.mapNotNull { it.getString("username") }
                        onResult(usernames)
                    }.addOnFailureListener {
                        onResult(emptyList())
                    }
            } else {
                // simple batching for >10 friends
                val chunks = friendIds.chunked(10)
                val names = mutableListOf<String>()
                var remaining = chunks.size

                chunks.forEach { chunk ->
                    db.collection("users").whereIn(FieldPath.documentId(), chunk).get()
                        .addOnSuccessListener { snapshot ->
                            names += snapshot.documents.mapNotNull { it.getString("username") }
                            remaining -= 1
                            if (remaining == 0) onResult(names)
                        }.addOnFailureListener {
                            // fail-fast or you can continue; here we return empty list
                            onResult(emptyList())
                        }
                }
            }
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }


    fun addFriendByUsername(username: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onError("The username does not exist.")
                    return@addOnSuccessListener
                }

                val friendDoc = snapshot.documents.first()
                val friendId = friendDoc.id

                if (friendId == currentUserId) {
                    onError("You can not add yourself.")
                    return@addOnSuccessListener
                }

                // Add friend UID to current user’s 'friends' array
                db.collection("users").document(currentUserId)
                    .update("friends", FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to add friend") }
            }.addOnFailureListener { e ->
                onError(e.message ?: "Error searching username")
            }
    }

    fun deleteFriendByUserId(friendId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {

        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .update("friends", FieldValue.arrayRemove(friendId)).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete friend.")
            }
    }

    fun getFriendsLeaderboard(onResult: (List<LeaderboardEntry>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            val allIds = friendIds.toMutableList().apply { add(currentUserId) }
            if (allIds.isEmpty()) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            val tempList = mutableListOf<LeaderboardEntry>()
            var processedCount = 0

            allIds.forEach { friendId ->
                db.collection("users").document(friendId).get()
                    .addOnSuccessListener { friendDoc ->
                        val username = friendDoc.getString("username") ?: "Unknown"

                        friendDoc.reference.collection("completion_logs").get()
                            .addOnSuccessListener { logs ->
                                val score = logs.size()
                                tempList.add(LeaderboardEntry(name = username, score = score))
                                processedCount++

                                if (processedCount == allIds.size) {
                                    val ranked = tempList.sortedByDescending { it.score }
                                        .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
                                    onResult(ranked)
                                }
                            }
                    }
            }
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }
}