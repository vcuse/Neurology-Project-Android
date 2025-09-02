package com.example.neurology_project_android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*

class NewNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val formId = intent.getIntExtra("formId", -1)
        val patientName = intent.getStringExtra("patientName")
        val dob = intent.getStringExtra("dob")
        val date = intent.getStringExtra("date")
        val formData = intent.getStringExtra("formData")
        val username = intent.getStringExtra("username")
        val sessionManager = SessionManager(this) // Create the real instance
        val existingForm = if (patientName != null && dob != null && date != null && formData != null && username != null) {
            NIHForm(formId, patientName, dob, date, formData, username)
        } else null

        setContent {
            NewNIHFormScreen(existingForm, sessionManager)
        }
    }
}

@Composable
fun NewNIHFormScreen(existingForm: NIHForm? = null, sessionManager: ISessionManager) {
    val context = LocalContext.current

    val client = sessionManager.client
    val coroutineScope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableStateOf(0) }

    var patientName by remember { mutableStateOf(existingForm?.patientName ?: "") }
    var dob by remember { mutableStateOf(existingForm?.dob ?: "") }
    val questions = remember { StrokeScaleQuestions.questions }
    val selectedOptions = remember {
        mutableStateListOf<Int?>().apply { repeat(questions.size) { add(null) } }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val dobCalendar = remember { Calendar.getInstance() }
    val username = remember { sessionManager.fetchUsername() ?: "anonymous" }

    val date = remember {
        existingForm?.date ?: SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
    }

    // Populate selected options if editing
    LaunchedEffect(existingForm) {
        existingForm?.formData?.forEachIndexed { index, c ->
            val score = c.toString().toIntOrNull() ?: 9
            selectedOptions[index] = if (score != 9) score else null
        }
    }

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dobCalendar.set(year, month, dayOfMonth)
            dob = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(dobCalendar.time)
        },
        dobCalendar.get(Calendar.YEAR),
        dobCalendar.get(Calendar.MONTH),
        dobCalendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E5F5))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New NIH Stroke Scale Form",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Enter Patient Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false // Disables suggestions
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() } // Closes keyboard on Done press
                ),
                placeholder = { Text("") }, // Prevents showing hints
            )

            OutlinedTextField(
                value = dob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { datePickerDialog.show() },
                enabled = false, //Disables editing, but still clickable due to Modifier
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )

            )


            Text(
                text = "Date: $date",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(questions) { question ->
                QuestionCard(question, selectedOptions)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (patientName.isBlank()) {
                        Toast.makeText(context, "Please enter a patient name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        val formData = selectedOptions.joinToString("") { (it ?: 9).toString() }
                        val form = NIHForm(
                            patientName = patientName,
                            dob = dob,
                            date = date,
                            formData = formData,
                            username = username
                        )
                        FormManager.submitFormToServer(form, client as OkHttpClient) { success ->
                            (context as? ComponentActivity)?.runOnUiThread {
                                if (success) {
                                    Toast.makeText(context, "Form saved successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, ListNIHFormActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Failed to save form", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Save", color = Color.White)
            }

            Button(
                onClick = {
                    val intent = Intent(context, ListNIHFormActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel", color = Color.White)
            }
        }
    }
}


@Composable
fun QuestionCard(question: StrokeScaleQuestion, selectedOptions: MutableList<Int?>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Question Header
        Text(
            text = question.questionHeader,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Subheader
        if (!question.subHeader.isNullOrEmpty()) {
            Text(
                text = question.subHeader,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Options
        question.options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (selectedOptions[question.id] == index) Color(0xFFD1C4E9) else Color(0xFFF3E5F5)
                    )
                    .padding(8.dp)
                    .clickable { selectedOptions[question.id] = index },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = option.title)
                Text(text = if (option.score > 0) "+${option.score}" else "${option.score}")
            }
        }
    }
}

data class StrokeScaleQuestion(
    val id: Int,
    val questionHeader: String,
    val subHeader: String?,
    val options: List<Option>
)

data class Option(
    val title: String,
    val score: Int
)

object StrokeScaleQuestions {
    val questions = listOf(
        StrokeScaleQuestion(
            id = 0,
            questionHeader = "1A: Level of Consciousness",
            subHeader = "May be assessed casually while taking history",
            options = listOf(
                Option("Alert; keenly responsive", 0),
                Option("Arouses to minor stimulation", 1),
                Option("Requires repeated stimulation to arouse", 2),
                Option("Movements to pain", 2),
                Option("Postures or unresponsive", 3)
            )
        ),
        StrokeScaleQuestion(
            id = 1,
            questionHeader = "1B: Ask month and age",
            subHeader = null,
            options = listOf(
                Option("Both questions right", 0),
                Option("1 question right", 1),
                Option("0 questions right", 2),
                Option("Dysarthric/intubated/trauma/language barrier", 1),
                Option("Aphasic", 2)
            )
        ),
        StrokeScaleQuestion(
            id = 2,
            questionHeader = "1C: 'Blink eyes' & 'Squeeze hands'",
            subHeader = "Pantomime commands if communication barrier",
            options = listOf(
                Option("Performs both tasks", 0),
                Option("Performs 1 task", 1),
                Option("Performs 0 tasks", 2)
            )
        ),
        StrokeScaleQuestion(
            id = 3,
            questionHeader = "2: Horizontal extraocular movements",
            subHeader = "Only assess horizontal gaze",
            options = listOf(
                Option("Normal", 0),
                Option("Partial gaze palsy: corrects with oculocephalic reflex", 1),
                Option("Forced gaze palsy: cannot be overcome", 2)
            )
        ),
        StrokeScaleQuestion(
            id = 4,
            questionHeader = "3: Visual Fields",
            subHeader = null,
            options = listOf(
                Option("No visual loss", 0),
                Option("Partial hemianopia", 1),
                Option("Complete hemianopia", 2),
                Option("Patient is bilaterally blind", 3),
                Option("Bilateral hemianopia", 3)
            )
        ),
        StrokeScaleQuestion(
            id = 5,
            questionHeader = "4: Facial Palsy",
            subHeader = "Use grimace if obtunded",
            options = listOf(
                Option("Normal symmetry", 0),
                Option("Minor paralysis (flat nasolabial fold, smile asymmetry)", 1),
                Option("Partial paralysis (lower face)", 2),
                Option("Unilateral complete paralysis (upper/lower face)", 3),
                Option("Bilateral complete paralysis (upper/lower face)", 3)
            )
        ),
        StrokeScaleQuestion(
            id = 6,
            questionHeader = "5A: Left arm motor drift",
            subHeader = "Count out loud and use your fingers to show the patient your count",
            options = listOf(
                Option("No drift for 10 seconds", 0),
                Option("Drift, but doesn't hit bed", 1),
                Option("Drift, hits bed", 2),
                Option("Some effort against gravity", 2),
                Option("No effort against gravity", 3),
                Option("No movement", 4),
                Option("Amputation/joint fusion", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 7,
            questionHeader = "5B: Right arm motor drift",
            subHeader = "Count out loud and use your fingers to show the patient your count",
            options = listOf(
                Option("No drift for 10 seconds", 0),
                Option("Drift, but doesn't hit bed", 1),
                Option("Drift, hits bed", 2),
                Option("Some effort against gravity", 2),
                Option("No effort against gravity", 3),
                Option("No movement", 4),
                Option("Amputation/joint fusion", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 8,
            questionHeader = "6A: Left leg motor drift",
            subHeader = "Count out loud and use your fingers to show the patient your count",
            options = listOf(
                Option("No drift for 5 seconds", 0),
                Option("Drift, but doesn't hit bed", 1),
                Option("Drift, hits bed", 2),
                Option("Some effort against gravity", 2),
                Option("No effort against gravity", 3),
                Option("No movement", 4),
                Option("Amputation/joint fusion", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 9,
            questionHeader = "6B: Right leg motor drift",
            subHeader = "Count out loud and use your fingers to show the patient your count",
            options = listOf(
                Option("No drift for 5 seconds", 0),
                Option("Drift, but doesn't hit bed", 1),
                Option("Drift, hits bed", 2),
                Option("Some effort against gravity", 2),
                Option("No effort against gravity", 3),
                Option("No movement", 4),
                Option("Amputation/joint fusion", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 10,
            questionHeader = "7: Limb Ataxia",
            subHeader = "FNF/heel-shin",
            options = listOf(
                Option("No ataxia", 0),
                Option("Ataxia in 1 limb", 1),
                Option("Ataxia in 2 limbs", 2),
                Option("Does not understand", 0),
                Option("Paralyzed", 0),
                Option("Amputation/joint fusion", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 11,
            questionHeader = "8: Sensation",
            subHeader = null,
            options = listOf(
                Option("Normal; no sensory loss", 0),
                Option("Mild-moderate loss: less sharp/more dull", 1),
                Option("Mild-moderate loss: can sense being touched", 1),
                Option("Complete loss: cannot sense being touched at all", 2),
                Option("No response and quadriplegic", 2),
                Option("Coma/unresponsive", 2)
            )
        ),
        StrokeScaleQuestion(
            id = 12,
            questionHeader = "9: Language/Aphasia",
            subHeader = "Describe the scene; name the items; read the sentences",
            options = listOf(
                Option("Normal; no aphasia", 0),
                Option("Mild-moderate aphasia: some obvious changes, without significant limitation", 1),
                Option("Severe aphasia: fragmentary expression, inference needed, cannot identify materials", 2),
                Option("Mute/global aphasia: no usable speech/auditory comprehension", 3),
                Option("Coma/unresposive", 3)
            )
        ),
        StrokeScaleQuestion(
            id = 13,
            questionHeader = "10: Dysarthria",
            subHeader = "Read the words",
            options = listOf(
                Option("Normal", 0),
                Option("Mild-moderate dysarthria: slurring but can be understood", 1),
                Option("Severe dysarthria: unintelligible slurring or out of proportion to dysphasia", 2),
                Option("Mute/anarthic", 2),
                Option("Intubated/unable to test", 0)
            )
        ),
        StrokeScaleQuestion(
            id = 14,
            questionHeader = "11: Extinction/Inattention",
            subHeader = null,
            options = listOf(
                Option("No abnormality", 0),
                Option("Visual/tactile/auditory/spatial inattention", 1),
                Option("Extinction to bilateral simultaneous stimulation", 1),
                Option("Profound hemi-inattention", 2),
                Option("Extinction to >1 modality", 2)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CallNewFormPreview() {

    val formId = 33
    val patientName = "patientName"
    val dob = "test"
    val date = "33333"
    val formData = "334999999"
    val username = "Sim Username"

    val existingForm = if (patientName != null && dob != null && date != null && formData != null && username != null) {
        NIHForm(formId, patientName, dob, date, formData, username)
    } else null

    NewNIHFormScreen(existingForm, MockSessionManager())
}

