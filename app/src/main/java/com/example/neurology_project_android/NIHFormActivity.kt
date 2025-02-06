package com.example.neurology_project_android

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

class NewNIHFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewNIHFormScreen()
        }
    }
}

@Composable
fun NewNIHFormScreen() {
    val viewModel: NIHFormViewModel = viewModel()
    val context = LocalContext.current // Get context for Room and other operations
    var patientName by remember { mutableStateOf("") }
    val questions = remember { StrokeScaleQuestions.questions }
    val selectedOptions = remember { mutableStateListOf<Int?>().apply { repeat(questions.size) { add(null) } } }

    // Current date
    val date = remember {
        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E5F5)) // Light purple background
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title and Patient Name Input
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = "Date: $date",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
        }



        // Questions List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(questions) { question ->
                QuestionCard(question, selectedOptions)
            }
        }



        // Save and Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewModel.saveForm(context, patientName, selectedOptions) // Save form

                    // Navigate back to the ListNIHFormActivity
                    (context as? Activity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(text = "Save", color = Color.White)
            }


            Button(
                onClick = {

                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
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

class NIHFormViewModel : ViewModel() {

    fun saveForm(context: Context, patientName: String, selectedOptions: List<Int?>) {

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

