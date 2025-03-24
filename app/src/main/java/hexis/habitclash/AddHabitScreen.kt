package hexis.habitclash

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.Habit

@Composable
fun AddHabitScreen(
    navController: NavController
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add a Habit", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Habit Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (e.g. 8:00 AM)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null && title.isNotBlank() && time.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    val habit = Habit(title = title, time = time)

                    db.collection("users")
                        .document(userId)
                        .collection("habits")
                        .add(habit)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddHabitScreen", "Error adding habit", e)
                            Toast.makeText(context, "Failed to save habit", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Make sure you're logged in and fields aren't empty", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Habit", color = Color.White)
        }
    }
}
