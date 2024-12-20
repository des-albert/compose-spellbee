package org.db.spellbee


import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.db.spellbee.ui.theme.SpellBeeTheme
import kotlin.system.measureTimeMillis


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      SpellBeeTheme {
        Scaffold(
          topBar = {
            SpellTopBar()
          },
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          WordFind(innerPadding)
        }
      }
    }
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellTopBar() {
  TopAppBar(
    title = {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          style = MaterialTheme.typography.headlineMedium,
          text = "DB'r SpellBee"
        )
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      titleContentColor = MaterialTheme.colorScheme.secondary
    )
  )
}

@Composable
fun WordFind(
  innerPadding: PaddingValues,
  wordFindModel: WordViewModel = viewModel(),

  ) {
  val context = LocalContext.current
  var base by remember { mutableStateOf("") }
  var center by remember { mutableStateOf("") }
  var resultWords by remember { mutableStateOf(listOf<String>()) }
  var count by remember { mutableStateOf(listOf<Int>()) }
  var score by remember { mutableIntStateOf(0) }
  var outer: Int = -1
  var common: Int = 0
  var resultVisible by remember { mutableStateOf(false) }
  var executionTime by remember { mutableLongStateOf(0L) }

  val wordLimit by wordFindModel.wordLimit

  LaunchedEffect(Unit) {
    wordFindModel.loadWords(context)
  }

  Surface(
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Box(
          modifier = Modifier
              .width(180.dp)
              .height(60.dp),
          contentAlignment = Alignment.Center
        ) {
          OutlinedTextField(
            value = base,
            onValueChange = { base = it },
            textStyle = TextStyle(
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center
            ),
            placeholder = {
              Text(
                text = "outer letters",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )
            },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.background,
              unfocusedBorderColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
              focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              focusedTextColor = MaterialTheme.colorScheme.error
            ),
          )
        }
        Box(
          modifier = Modifier
              .width(90.dp)
              .height(60.dp),
          contentAlignment = Alignment.Center
        ) {
          OutlinedTextField(
            value = center,
            onValueChange = { center = it },
            textStyle = TextStyle(
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.tertiary,
              textAlign = TextAlign.Center
            ),
            placeholder = {
              Text(
                text = "center",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
              )
            },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.background,
              unfocusedBorderColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
              focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              focusedTextColor = MaterialTheme.colorScheme.tertiary
            )
          )
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        ElevatedButton(
          modifier = Modifier
              .size(120.dp, 60.dp)
              .padding(8.dp),
          colors = ButtonDefaults.elevatedButtonColors(
            contentColor = MaterialTheme.colorScheme.background,
            containerColor = MaterialTheme.colorScheme.inversePrimary
          ),
          onClick = {
            resultVisible = true
            var chars: List<Int> = base.codePoints().toArray().toMutableList()
            outer = -1
            if (chars.size == 6) {
              for (p: Int in chars) {
                outer = if (p < 97) {
                  outer xor (1 shl (p - 65))
                } else {
                  outer xor (1 shl (p - 97))
                }
              }
            }

            val c = center[0].code
            common = if (c < 97) {
              common or (1 shl (c - 65))
            } else {
              common or (1 shl (c - 97))
            }
            outer = outer xor common
            val time = measureTimeMillis {
              resultWords = wordFindModel.findWords(outer, common)
            }
            executionTime = time
            count = wordFindModel.wordCount()
            score = wordFindModel.wordScore()
          }
        ) {
          Text(
            text = "Solve",
            fontSize = 16.sp
          )
        }
        ElevatedButton(
          modifier = Modifier
              .size(120.dp, 60.dp)
              .padding(8.dp),
          colors = ButtonDefaults.elevatedButtonColors(
            contentColor = MaterialTheme.colorScheme.background,
            containerColor = MaterialTheme.colorScheme.outline
          ),
          onClick = {
            resultVisible = false
            base = ""
            center = ""

          }
        ) {
          Text(
            text = "Clear",
            fontSize = 16.sp
          )
        }

      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
      ) {
        Text(text = "Word Length")

        Spacer(modifier = Modifier.width(8.dp))

        RadioButton(
          selected = wordLimit == 4,
          onClick = { wordFindModel.wordLimit.intValue = 4 },
          colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.errorContainer,
            unselectedColor = MaterialTheme.colorScheme.tertiary
          )
        )
        Text(text = "4")

        Spacer(modifier = Modifier.width(8.dp))

        RadioButton(
          selected = wordLimit == 5,
          onClick = { wordFindModel.wordLimit.intValue = 5 },
          colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.errorContainer,
            unselectedColor = MaterialTheme.colorScheme.tertiary
          )
        )
        Text(text = "5")
      }
      if (resultVisible) {
        Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${resultWords.size} words, score $score, time $executionTime mS",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onTertiary, // Equivalent to Colors.orange
            fontWeight = FontWeight.Normal
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Box(
            modifier = Modifier
                .width(350.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
          ) {
            LazyVerticalGrid(
              columns = GridCells.Fixed(3),
              contentPadding = PaddingValues(16.dp)
            ) {
              itemsIndexed(resultWords) { index, result ->
                Text(
                  text = result,
                  fontSize = 16.sp,
                  fontWeight = if (count[index] == 7)
                    FontWeight.Bold else FontWeight.Normal,
                  color = if (count[index] == 7) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface,
                  modifier = Modifier.padding(4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  name = "DefaultPreviewDark"
)
@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_NO,
  name = "DefaultPreviewLight"
)
@Composable
fun WordFindPreview() {
  SpellBeeTheme {
    Scaffold(
      topBar = {
        SpellTopBar()
      },
      modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
      WordFind(innerPadding)
    }
  }
}