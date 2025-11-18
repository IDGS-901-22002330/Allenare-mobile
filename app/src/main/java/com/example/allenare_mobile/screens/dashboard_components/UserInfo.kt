package com.example.allenare_mobile.screens.dashboard_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.allenare_mobile.R
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme

@Composable
fun UserInfo(username: String?, email: String?, photoUrl: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = photoUrl ?: R.drawable.ic_launcher_foreground,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text("Bienvenido, ${username ?: "Usuario"}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(email ?: "", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserInfoPreview() {
    AllenaremobileTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            UserInfo(
                username = "Oscar Octavio Alvarado Cornejo",
                email = "octavio@gmail.com",
                photoUrl = null
            )
        }
    }
}