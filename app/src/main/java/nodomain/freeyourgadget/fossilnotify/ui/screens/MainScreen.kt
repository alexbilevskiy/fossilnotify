package nodomain.freeyourgadget.fossilnotify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nodomain.freeyourgadget.fossilnotify.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    text: String,
    onClickCreateNotify: () -> Unit,
    onClickCount: () -> Unit,
    onClickClearText: () -> Unit,
    pebbleEnabled: Boolean,
    onPebbleToggle: (Boolean) -> Unit,
    fossilEnabled: Boolean,
    onFossilToggle: (Boolean) -> Unit
) {
    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(innerPadding) // Prevents content from going under status bar
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Actions",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Pebble enabled",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Checkbox(
                        checked = pebbleEnabled,
                        onCheckedChange = onPebbleToggle
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Fossil enabled",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Checkbox(
                        checked = fossilEnabled,
                        onCheckedChange = onFossilToggle
                    )
                    Button(
                        onClick = onClickCreateNotify
                    ) {
                        Text(
                            text = "Create Notify",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = onClickCount
                    ) {
                        Text(
                            text = "Count notifications",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = onClickClearText
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Preview",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}