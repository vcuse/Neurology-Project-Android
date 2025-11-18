package com.example.neurology_project_android

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID




@Serializable
data class NIHForm(


    // Other string/text fields (all nullable)
    @SerialName("username")
    val username: String? = null,

    @SerialName("form_date")
    val formDate: String? = null, // You can parse this into a Date object later

    @SerialName("patient_name")
    val patientName: String? = null,

    @SerialName("sessionid")
    val sessionId: String? = null,

    // --- NIHSS Item Scores (all nullable) ---

    @SerialName("item_1a_loc_level")
    val item1aLocLevel: Int? = null,

    @SerialName("item_1b_loc_commands")
    val item1bLocCommands: Int? = null,

    @SerialName("item_1c_loc_best_gaze")
    val item1cLocBestGaze: Int? = null,

    @SerialName("item_2_best_motor_gaze") // As per your schema
    val item2BestMotorGaze: Int? = null,

    @SerialName("item_3_visual")
    val item3Visual: Int? = null,

    @SerialName("item_4_facial_palsy")
    val item4FacialPalsy: Int? = null,

    @SerialName("item_5_left_arm_motor")
    val item5LeftArmMotor: Int? = null,

    @SerialName("item_6_right_arm_motor")
    val item6RightArmMotor: Int? = null,

    @SerialName("item_7_left_leg_motor")
    val item7LeftLegMotor: Int? = null,

    @SerialName("item_8_right_leg_motor")
    val item8RightLegMotor: Int? = null,

    @SerialName("item_9_ataxia")
    val item9Ataxia: Int? = null,

    @SerialName("item_10_sensory")
    val item10Sensory: Int? = null,

    @SerialName("item_11_language")
    val item11Language: Int? = null,

    @SerialName("item_12_dysarthria")
    val item12Dysarthria: Int? = null,

    @SerialName("item_13_extinction_inattention")
    val item13ExtinctionInattention: Int? = null,

    // --- Total Score ---

    @SerialName("total_nihss_score")
    val totalNihssScore: Int? = null


)

    public fun NIHForm.toJson(): String {
        return Json.encodeToString(this)
    }