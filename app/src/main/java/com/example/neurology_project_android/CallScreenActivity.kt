package com.example.neurology_project_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

class CallScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallScreen()
        }
    }
}

@Composable
fun CallScreen() {
    var isMuted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Bottom Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,

            ) {
                Column(
                    // Center the items horizontally within the column
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).background(Color.DarkGray).clickable(onClick = {
                        val intent = Intent(
                            context,
                            ListNIHFormActivity::class.java
                        )

                        context.startActivity(intent)
                        /* Open Stroke Scale Form */
                    })

                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(
                                context,
                                ListNIHFormActivity::class.java
                            )
                            // 3. Start the new Activity
                            context.startActivity(intent)
                            /* Open Stroke Scale Form */ },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.DarkGray)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.assignment_add_24px),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    // Add the Text composable right below the IconButton
                    Spacer(modifier = Modifier.height(4.dp)) // Optional: Add a small vertical space
                    Text(
                        text = "New Form/View Form",
                        modifier = Modifier.fillMaxWidth(),


                        fontSize = 16.sp,


                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,


                        color = Color.White,

                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    // Center the items horizontally within the column
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).background(Color.Gray).clickable(onClick = {

                    })

                ) {
                    IconButton(
                        onClick = {
                            if(isMuted){
                                isMuted = false
                            }
                            else{
                                isMuted = true
                            }

                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Gray)
                    ) {
                        Icon(
                        painter = painterResource(id = if (isMuted) R.drawable.mic_off_24px else R.drawable.mic_24px),
                        contentDescription = null,
                        tint = Color.White
                    )
                    }
                    // Add the Text composable right below the IconButton
                    Spacer(modifier = Modifier.height(4.dp)) // Optional: Add a small vertical space
                    Text(
                        text = "Mute/Unmute",
                        modifier = Modifier.fillMaxWidth(),


                        fontSize = 16.sp,


                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,


                        color = Color.White,

                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    // Center the items horizontally within the column
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).background(Color.Red).clickable(onClick = {
                        val intent = Intent(context, NewNIHFormActivity::class.java).apply {
                            putExtra(NewNIHFormActivity.EXTRA_IN_CALL, true)
                        }
                        context.startActivity(intent)

                        /* Open Stroke Scale Form */
                    })

                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, NewNIHFormActivity::class.java).apply {
                                putExtra(NewNIHFormActivity.EXTRA_IN_CALL, true)
                            }
                            context.startActivity(intent)

                            /* Open Stroke Scale Form */ },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red)
                    ) {
                        Icon(
                        painter = painterResource(R.drawable.call_end_24px),
                        contentDescription = null,
                        tint = Color.White
                    )
                    }
                    // Add the Text composable right below the IconButton
                    Spacer(modifier = Modifier.height(4.dp)) // Optional: Add a small vertical space
                    Text(
                        text = "End Call",
                        modifier = Modifier.fillMaxWidth(),


                        fontSize = 16.sp,


                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,

                       
                        color = Color.White,

                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                        )
                }

//                IconButton(
//                    onClick = { isMuted = !isMuted },
//                    modifier = Modifier
//                        .size(64.dp)
//                        .background(Color.DarkGray, shape = CircleShape)
//                ) {
//                    Icon(
//                        painter = painterResource(id = if (isMuted) R.drawable.mic_off_24px else R.drawable.mic_24px),
//                        contentDescription = null,
//                        tint = Color.White
//                    )
//                }
//
//                IconButton(
//                    onClick = { /* End Call */ },
//                    modifier = Modifier
//                        .size(64.dp)
//                        .background(Color.Red, shape = CircleShape)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.call_end_24px),
//                        contentDescription = null,
//                        tint = Color.White
//                    )
//                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CallScreenPreview() {
    CallScreen()
}
