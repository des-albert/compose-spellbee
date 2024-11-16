package org.db.spellbee

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel: ViewModel() {
    var words: List<String> = emptyList()
    var results: MutableList<String> = emptyList<String>().toMutableList()
    var count: MutableList<Int> = emptyList<Int>().toMutableList()
    val wordLimit = mutableIntStateOf(5)
    var score = mutableIntStateOf(0)

    fun loadWords(context: Context) {
        viewModelScope.launch {
            words = withContext(Dispatchers.IO)     {
                readFileFromAssets(context, "words.txt").split("\\s+".toRegex())
            }
        }
    }
    fun readFileFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun findWords(outer: Int, common: Int): List<String> {
        var mask:Int = 0
        var match:Int = 0
        var center:Int = 0
        var total: Int = 0
        lateinit var chars: List<Int>

        results = emptyList<String>().toMutableList()
        count = emptyList<Int>().toMutableList()
        score.intValue = 0

        for (word: String in words) {
            mask = 0
            match = 0
            center = 0


            chars = word.codePoints().toArray().toList()
            for (p in chars) {
                mask = if (p < 97) {
                    mask or (1 shl (p - 65))
                } else {
                    mask or (1 shl (p - 97))
                }
            }

            match = mask and outer
            center = mask and common

            if (match == 0 && center != 0 && word.length >= wordLimit.intValue) {
                results.add(word)
                total = 0
                for (i in 0 until 26) {
                    if ((outer shr i) and 1 == 0 && (mask shr i) and 1 == 1)
                        total++
                }
                count.add(total)
                if (total == 7) {
                    score.value += 7
                }
                score.value += if (word.length > 4) {
                    word.length
                } else {
                    1
                }
            }
        }

        return results
    }

    fun wordCount(): List<Int> {
        return count
    }

    fun wordScore(): Int {
        return score.intValue
    }

}