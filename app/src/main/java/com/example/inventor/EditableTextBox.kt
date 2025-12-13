package com.example.inventor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditableTextBox(
    label: String,
    textValue: String,
    onValueChange: (String) -> Unit,
    onSaveClicked: (() -> Unit)? = null,
    editingComponents: (@Composable ((String) -> Unit, () -> Unit) -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempValue by remember { mutableStateOf(textValue) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .padding(12.dp)
    ) {
        if (isEditing) {
            Column {
                if (editingComponents == null) {
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = { tempValue = it },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else editingComponents(
                    { tempValue = it },
                    { isEditing = false }
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onSaveClicked?: {
                            onValueChange(tempValue)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            }

        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                    Text(textValue, style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit $label")
                }
            }
        }
    }
}
