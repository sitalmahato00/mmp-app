package com.example.mmp_app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.mmplogo),
                contentDescription = "MMP Logo",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "MMP Portal",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            
            Text(
                text = "Modern Management Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
        }
    }
}
