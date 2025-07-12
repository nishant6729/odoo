package com.example.skillswaps.Screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillswaps.R


@Composable
fun OnBoardingScreen(onContinue: () -> Unit) {
    Column(modifier=Modifier.fillMaxSize().background(Brush.verticalGradient(
        colors = listOf(
            Color(254,222,99,255),
            Color(255,211,60,255),                   //Vertical Gradienting Color
            Color(254,195,20,255),
            Color(255,194,10,255)
        )

    ))){
        val montserratFont = FontFamily(
            Font(R.font.modified_text, FontWeight.Normal),

            )

        Column(modifier=Modifier.fillMaxSize().padding(16.dp)){
            Image(painter = painterResource(R.drawable.on_boarding_bg),
                contentDescription = null,
                modifier=Modifier.fillMaxHeight(0.42f).fillMaxWidth())
            Spacer(modifier=Modifier.height(8.dp))
            Text(text = "Let's  Get", fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                modifier=Modifier.fillMaxWidth().
                padding(start = 8.dp),
                letterSpacing = 5.sp, fontFamily = montserratFont)
            Spacer(modifier=Modifier.height(4.dp))
            Text(text = "Started", fontWeight = FontWeight.ExtraBold, fontSize = 42.sp,modifier=Modifier.fillMaxWidth().padding(start = 8.dp), letterSpacing = 5.sp)
            Spacer(modifier=Modifier.height(40.dp))
            Text(text = "Unlock a world of limitless skill and knowledge with our free skill swapping app,where sharing is caring.",
                letterSpacing = 0.8.sp, lineHeight =22.sp,
                modifier=Modifier.fillMaxWidth().padding(start = 8.dp),
                fontWeight = FontWeight.Light,
                fontSize = 15.sp,
                minLines = 3, color = Color.Black)
            Spacer(modifier=Modifier.height(100.dp))
            Button(onClick = {
                onContinue()
            }, shape = RoundedCornerShape(10.dp),
                modifier=Modifier.fillMaxWidth().padding(8.dp).
                height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(76,61,61,255))) {
                Text("Get Started",color=Color.White, fontSize = 18.sp, textAlign = TextAlign.Center, letterSpacing = 1.sp)
            }

        }
    }


}