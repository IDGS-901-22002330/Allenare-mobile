package com.example.allenare_mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val user = Firebase.auth.currentUser

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(16.dp)
    ) {
        item {
            UserInfo(username = user?.displayName, email = user?.email, photoUrl = user?.photoUrl?.toString())
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            TrainingStats()
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            RecentWorkouts()
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            MeasureRoute()
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
             Button(onClick = {
                Firebase.auth.signOut()
                onLogout()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Log Out")
            }
        }
    }
}

@Composable
fun UserInfo(username: String?, email: String?, photoUrl: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (photoUrl != null) {
            // Aquí usarías una librería como Coil para cargar la imagen desde la URL
            // Image(painter = rememberImagePainter(photoUrl), ...)
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text("Bienvenido, ${username ?: "Usuario"}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(email ?: "", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TrainingStats() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatCard(
            title = "Días de entrenamiento",
            value = "2/3",
            icon = Icons.Default.FitnessCenter,
            backgroundColor = Color(0xFFFFCDD2),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        StatCard(
            title = "Total de km recorridos",
            value = "24 km",
            icon = Icons.Default.DirectionsRun,
            backgroundColor = Color(0xFFF9E79F),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun RecentWorkouts() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Entrenamientos recientes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            WorkoutItem(icon = Icons.Default.FitnessCenter, text = "Gimnasio: 'Pierna' - 12/09/25 - 01:17:20")
            Spacer(modifier = Modifier.height(8.dp))
            WorkoutItem(icon = Icons.Default.DirectionsRun, text = "Running: 'Easy run' - 11/09/25 - 01:17:20")
            Spacer(modifier = Modifier.height(8.dp))
            WorkoutItem(icon = Icons.Default.FitnessCenter, text = "Gimnasio: 'Pecho' - 10/09/25 - 01:17:20")
        }
    }
}

@Composable
fun WorkoutItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun MeasureRoute() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Medir ruta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray)
            ) {
                // Placeholder for the map
                 Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Placeholder
                    contentDescription = "Map Placeholder",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
