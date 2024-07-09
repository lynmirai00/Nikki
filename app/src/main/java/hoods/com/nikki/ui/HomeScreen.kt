package hoods.com.nikki.ui

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import hoods.com.nikki.R
import kotlinx.coroutines.delay
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, email: String?) {
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val disabledDates = (1..36500).map { LocalDate.now().plusDays(it.toLong()) }
    val nameState = remember { mutableStateOf("") }
    val state = rememberRichTextState()
    val calendarState = rememberSheetState()
    val focusRequester = remember { FocusRequester() }
    val db = Firebase.firestore
    val dataSaved = remember { mutableStateOf(false) }

    // Lấy thông tin người dùng
    LaunchedEffect(email) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val names = document.getString("name")
                    if (names != null) {
                        Log.d("HomeScreen", "Name: $names")
                        nameState.value = names
                    } else {
                        Log.d("HomeScreen", "Name is null")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("HomeScreen", "Error getting documents: $exception")
            }
    }

    // Lấy dữ liệu từ Firestore khi selectedDate thay đổi
    LaunchedEffect(selectedDate.value) {
        selectedDate.value?.let { date ->
            db.collection("nikki")
                .whereEqualTo("email", email)
                .whereEqualTo("date", date.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents[0]
                        val text = document.getString("text")
                        Log.d("HomeScreen", "Document text: $text") // In ra giá trị của "text"
                        state.setHtml(text ?: "")
                    } else {
                        Log.d("HomeScreen", "No document found for the selected date")
                        state.setHtml("") // Không có dữ liệu thì để trống
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("HomeScreen", "Error getting documents: $exception")
                }
        }
    }

    fun saveData() {
        if (selectedDate.value == null) {
            Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val date = selectedDate.value.toString()
        val data = hashMapOf(
            "email" to email,
            "date" to date,
            "text" to state.toHtml()
        )

        db.collection("nikki")
            .whereEqualTo("email", email)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Tạo tài liệu mới
                    db.collection("nikki")
                        .add(data)
                        .addOnSuccessListener { documentReference ->
                            Log.d("HomeScreen", "DocumentSnapshot added with ID: ${documentReference.id}")
                            Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
                            dataSaved.value = true
                        }
                        .addOnFailureListener { e ->
                            Log.w("HomeScreen", "Error adding document", e)
                        }
                } else {
                    // Cập nhật tài liệu hiện có
                    val documentId = querySnapshot.documents[0].id
                    db.collection("nikki").document(documentId)
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("HomeScreen", "DocumentSnapshot updated with ID: $documentId")
                            Toast.makeText(context, "Data updated successfully", Toast.LENGTH_SHORT).show()
                            dataSaved.value = true
                        }
                        .addOnFailureListener { e ->
                            Log.w("HomeScreen", "Error updating document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("HomeScreen", "Error finding document", e)
            }
    }

    LaunchedEffect(dataSaved.value) {
        if (dataSaved.value) {
            delay(10) // Delay to allow Toast to be shown before moving focus
            focusRequester.requestFocus()
            dataSaved.value = false // Reset the flag
        }
    }
    fun logout() {
        navController.navigate("login_flow") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "NIKKI",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Save your memory",
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.padding(all = 8.dp)) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = nameState.value, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = email.toString(), fontStyle = FontStyle.Italic)
            }
        }
        Button(
            onClick = { calendarState.show() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            if (selectedDate.value == null) {
                Text(text = "Select Date")
            } else {
                selectedDate.value?.let { selected ->
                    Text(text = "$selected")
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        RichTextEditor(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .border(BorderStroke(2.dp, Color.Gray))
                .focusable(), // Ensure it can be focusable
            state = state,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { saveData() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Save")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { logout() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Logout")
        }

        // Invisible button to request focus
        Button(
            onClick = { },
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusTarget()
                .size(1.dp)
        ) {}

        CalendarDialog(
            state = calendarState,
            config = CalendarConfig(
                yearSelection = true,
                monthSelection = true,
                style = CalendarStyle.MONTH,
                disabledDates = disabledDates
            ),
            selection = CalendarSelection.Date { newDate ->
                if (newDate != null) {
                    selectedDate.value = newDate
                    Log.d("Calendar", "Selected date: $newDate")
                }
            }
        )
    }
}


