package com.example.allenare_mobile.screens.leaderboards_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaderBoardRunningCard (index: Int?, name: String?, cantidad: Double?, timepoSegundos: Double?){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ){
        Text(index.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Distancia: ${cantidad.toString()}     ", fontSize = 14.sp, color = Color.Gray)
            Text("Tiempo en segundos: ${timepoSegundos.toString()}", fontSize = 14.sp, color = Color.Gray)

        }
    }
}


@Preview(showBackground = true)
@Composable
fun LeaderBoardRunningPreview(){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            LeaderBoardRunningCard(
                index = 160,
                name = "martin",
                cantidad = 10.67,
                timepoSegundos = 40.89
            )
        }
    }
}