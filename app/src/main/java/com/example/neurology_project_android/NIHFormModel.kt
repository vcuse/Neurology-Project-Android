// In a new file: app/src/main/java/com/example/neurology_project_android/NIHFormModel.kt

package com.example.neurology_project_android

// Represents a single answer option for a question
data class FormOption(
    val displayText: String, // The text shown in the UI, e.g., "Both arms held for 10 seconds"
    val score: Int           // The value stored for this option, e.g., 0
)

// Represents a single question in the NIH Stroke Scale
data class FormQuestion(
    val id: Int,                  // A unique identifier, e.g., "1a"
    val questionText: String,        // The main question header, e.g., "1a. Level of Consciousness"
    val instructionText: String,     // The sub-header or instructions
    val options: List<FormOption>    // The list of possible answers for this question
)

// This object holds the complete definition of your form, replacing the old StrokeScaleQuestions
object NIHFormModel {
    val questions: List<FormQuestion> = listOf(
        FormQuestion(
            id = 1,
            questionText = "1a. Level of Consciousness",
            instructionText = "Assess the patient's response to stimulation.",
            options = listOf(
                FormOption("Alert; keenly responsive.", 0),
                FormOption("Not alert, but arousable by minor stimulation.", 1),
                FormOption("Not alert; requires repeated stimulation.", 2),
                FormOption("Responds only with reflex motor or autonomic effects.", 3)
            )
        ),
        FormQuestion(
            id = 2,
            questionText = "1b. LOC Questions",
            instructionText = "Ask the patient: What is the month? What is your age?",
            options = listOf(
                FormOption("Answers both questions correctly.", 0),
                FormOption("Answers one question correctly.", 1),
                FormOption("Answers neither question correctly.", 2)
            )
        ),
        FormQuestion(
            id = 3,
            questionText = "1c. LOC Commands",
            instructionText = "Ask the patient to: Open and close your eyes. Grip and release your non-paretic hand.",
            options = listOf(
                FormOption("Performs both tasks correctly.", 0),
                FormOption("Performs one task correctly.", 1),
                FormOption("Performs neither task correctly.", 2)
            )
        ),
        FormQuestion(
            id = 4,
            questionText = "2. Best Gaze",
            instructionText = "Test horizontal eye movements.",
            options = listOf(
                FormOption("Normal.", 0),
                FormOption("Partial gaze palsy; gaze is abnormal in one or both eyes.", 1),
                FormOption("Forced deviation, or total gaze paresis.", 2)
            )
        ),
        FormQuestion(
            id = 5,
            questionText = "3. Visual",
            instructionText = "Test visual fields by confrontation (upper and lower quadrants).",
            options = listOf(
                FormOption("No visual loss.", 0),
                FormOption("Partial hemianopia.", 1),
                FormOption("Complete hemianopia.", 2),
                FormOption("Bilateral hemianopia (blindness).", 3)
            )
        ),
        FormQuestion(
            id = 6,
            questionText = "4. Facial Palsy",
            instructionText = "Ask patient to show teeth or raise eyebrows and close eyes.",
            options = listOf(
                FormOption("Normal symmetrical movements.", 0),
                FormOption("Minor paralysis (flattened nasolabial fold, asymmetry on smiling).", 1),
                FormOption("Partial paralysis (total or near-total paralysis of lower face).", 2),
                FormOption("Complete paralysis of one or both sides (absence of facial movement in the upper and lower face).", 3)
            )
        ),
        FormQuestion(
            id = 7,
            questionText = "5. Motor Arm (Left)",
            instructionText = "Extend the arm (palms down) 90 degrees (if sitting) or 45 degrees (if supine). Hold for 10 seconds.",
            options = listOf(
                FormOption("No drift; arm holds 90 (or 45) degrees for full 10 seconds.", 0),
                FormOption("Drift; arm holds but drifts down before full 10 seconds.", 1),
                FormOption("Some effort against gravity; arm cannot get to or maintain (if cued) 90 (or 45) degrees, drifts down to bed.", 2),
                FormOption("No effort against gravity; arm falls.", 3),
                FormOption("No movement.", 4),
                FormOption("Amputation or joint fusion, explain:", 9)
            )
        ),
        FormQuestion(
            id = 8,
            questionText = "6. Motor Arm (Right)",
            instructionText = "Extend the arm (palms down) 90 degrees (if sitting) or 45 degrees (if supine). Hold for 10 seconds.",
            options = listOf(
                FormOption("No drift; arm holds 90 (or 45) degrees for full 10 seconds.", 0),
                FormOption("Drift; arm holds but drifts down before full 10 seconds.", 1),
                FormOption("Some effort against gravity; arm cannot get to or maintain (if cued) 90 (or 45) degrees, drifts down to bed.", 2),
                FormOption("No effort against gravity; arm falls.", 3),
                FormOption("No movement.", 4),
                FormOption("Amputation or joint fusion, explain:", 9)
            )
        ),
        FormQuestion(
            id = 9,
            questionText = "7. Motor Leg (Left)",
            instructionText = "Raise the leg to 30 degrees (always supine). Hold for 5 seconds.",
            options = listOf(
                FormOption("No drift; leg holds 30-degree position for full 5 seconds.", 0),
                FormOption("Drift; leg falls by end of the 5-second period but does not hit bed.", 1),
                FormOption("Some effort against gravity; leg falls to bed by 5 seconds, but has some effort against gravity.", 2),
                FormOption("No effort against gravity; leg falls to bed immediately.", 3),
                FormOption("No movement.", 4),
                FormOption("Amputation or joint fusion, explain:", 9)
            )
        ),
        FormQuestion(
            id = 10,
            questionText = "8. Motor Leg (Right)",
            instructionText = "Raise the leg to 30 degrees (always supine). Hold for 5 seconds.",
            options = listOf(
                FormOption("No drift; leg holds 30-degree position for full 5 seconds.", 0),
                FormOption("Drift; leg falls by end of the 5-second period but does not hit bed.", 1),
                FormOption("Some effort against gravity; leg falls to bed by 5 seconds, but has some effort against gravity.", 2),
                FormOption("No effort against gravity; leg falls to bed immediately.", 3),
                FormOption("No movement.", 4),
                FormOption("Amputation or joint fusion, explain:", 9)
            )
        ),
        FormQuestion(
            id = 11,
            questionText = "9. Limb Ataxia",
            instructionText = "Test with finger-nose-finger and heel-shin tests on both sides.",
            options = listOf(
                FormOption("Absent.", 0),
                FormOption("Present in one limb.", 1),
                FormOption("Present in two limbs.", 2),
                FormOption("Does not understand or is paralyzed.", 9)
            )
        ),
        FormQuestion(
            id = 12,
            questionText = "10. Sensory",
            instructionText = "Test sensation or grimace to pinprick.",
            options = listOf(
                FormOption("Normal; no sensory loss.", 0),
                FormOption("Mild-to-moderate sensory loss; less sharp/dull on affected side.", 1),
                FormOption("Severe to total sensory loss; not aware of being touched.", 2)
            )
        ),
        FormQuestion(
            id = 13,
            questionText = "11. Best Language",
            instructionText = "Ask patient to describe what is happening in the provided picture, name items, and read sentences.",
            options = listOf(
                FormOption("No aphasia; normal.", 0),
                FormOption("Mild-to-moderate aphasia; some obvious loss of fluency or comprehension.", 1),
                FormOption("Severe aphasia; all communication is fragmented, listener cannot understand.", 2),
                FormOption("Mute, global aphasia; no usable speech or auditory comprehension.", 3)
            )
        ),
        FormQuestion(
            id = 14,
            questionText = "12. Dysarthria",
            instructionText = "Ask the patient to read or repeat words from an attached list.",
            options = listOf(
                FormOption("Normal.", 0),
                FormOption("Mild-to-moderate dysarthria; slurring of words but can be understood.", 1),
                FormOption("Severe dysarthria; so slurred it is unintelligible or worse.", 2),
                FormOption("Intubated or other physical barrier.", 9)
            )
        ),
        FormQuestion(
            id = 15,
            questionText = "13. Extinction and Inattention",
            instructionText = "Sufficient information to identify neglect from prior testing is acceptable.",
            options = listOf(
                FormOption("No neglect.", 0),
                FormOption("Visual, tactile, auditory, or personal inattention to one side.", 1),
                FormOption("Profound hemi-inattention or extinction to more than one modality.", 2)
            )
        )
    )
}
