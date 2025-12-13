package com.example.inventor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationPickerEditor(
    itemLocations: List<String>,
    selectedLocation: String?,
    onSelectedLocationChange: (String?) -> Unit,
    otherText: String,
    onOtherTextChange: (String) -> Unit,
    onSaveRequested: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Predefined locations
            itemLocations.forEach { location ->
                val isSelected = selectedLocation == location

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable {
                            onSelectedLocationChange(location)
                            onOtherTextChange("")
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelectedLocationChange(location)
                                onOtherTextChange("")
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // "Others" option
            val isOtherSelected = otherText.isNotBlank()

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .clickable {
                        onSelectedLocationChange(null)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isOtherSelected,
                        onClick = { onSelectedLocationChange(null) },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = {
                            onOtherTextChange(it)
                            if (it.isNotBlank()) onSelectedLocationChange(null)
                        },
                        label = {
                            Text(
                                "Enter Other Location",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
