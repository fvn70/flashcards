package flashcards

import java.io.File
import java.util.*
import kotlin.random.Random

val cards = mutableMapOf<String, String>()
val errors = mutableMapOf<String, Int>()
val logs = mutableListOf<String>()
var inFile = ""
var outFile = ""

fun main(args: Array<String>) {
    for (i in 0..args.lastIndex) {
        if (args[i] == "-import") inFile = args[i + 1]
        if (args[i] == "-export") outFile = args[i + 1]
    }
    if (!inFile.isBlank()) {
        import(inFile)
    }
    while (true) {
        try {
            printToLog("")
            printToLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val cmd = readLine()!!.toLog()
            when (cmd) {
                "?" -> dump()
                "add" -> add()
                "remove" -> remove()
                "import" -> import()
                "export" -> export()
                "ask" -> ask()
                "log" -> log()
                "hardest card" -> hard()
                "reset stats" -> reset()
                "exit" -> break
            }
        } catch (e: Exception) { printToLog(e.message!!) }
    }
    if (!outFile.isBlank()) {
        export(outFile)
    }
    printToLog("Bye bye!")
}

fun dump() {
    println("cards:")
    println(cards.toString())
    println("errors:")
    println(errors.toString())
}

fun String.toLog(): String {
    logs.add(this + "\n")
    return this
}

fun printToLog(str: String) {
    (str).toLog()
    println(str)
}

fun hard() {
    val max = errors.map { it.value }.maxOrNull()
    if (errors.isEmpty() || max == 0) {
        printToLog("There are no cards with errors.")
        return
    }
    val hk = errors.filter { it.value == max }
    if (hk.size == 1) {
        printToLog("The hardest card is \"${hk.keys.first()}\". You have $max errors answering it.")
    } else {
        val list = hk.keys.map { "\"${it}\"" }.joinToString()
        printToLog("The hardest cards are $list. You have $max errors answering them.")
    }
}

fun reset() {
    errors.clear()
    printToLog("Card statistics have been reset.")
}

fun add() {
    printToLog("The card:")
    val term = readLine()!!.toLog()
    if (cards.containsKey(term)) {
        throw Exception("The card \"$term\" already exists.")
    }
    printToLog("The definition of the card:")
    val def = readLine()!!.toLog()
    if (cards.containsValue(def)) {
        throw Exception("The definition \"$def\" already exists.")
    }
    cards.put(term, def)
    printToLog("The pair (\"$term\":\"$def\") has been added.")
}

fun remove() {
    printToLog("Which card?")
    val key = readLine()!!.toLog()
    if (cards.containsKey(key)) {
        cards.remove(key)
        errors.remove(key)
        printToLog("The card has been removed.")
    } else {
        throw Exception("Can't remove \"$key\": there is no such card.")
    }
}

fun ask() {
    printToLog("How many times to ask?")
    val num = readLine()!!.toLog().toInt()
    for (k in 1..num) {
        val i = Random.nextInt(cards.size)
        val key = cards.keys.toList()[i]
        val def = cards[key]
        printToLog("Print the definition of \"$key\":")
        val ans = readLine()!!.toLog()
        if (ans == def) {
            printToLog("Correct!")
        } else {
            errors[key] = errors[key]?.plus(1) ?: 1
            val s = "Wrong. The right answer is \"$def\""
            if (cards.containsValue(ans)) {
                val rcard = cards.filterValues { it == ans }.keys.first()
                printToLog("$s,  but your definition is correct for \"$rcard\".")
            } else printToLog("$s.")
        }
    }
}

fun import(fname: String = "") {
    var fn = fname
    if (fn.isBlank()) {
        printToLog("File name:")
        fn = readLine()!!.toLog()
    }
    if (File(fn).exists()) {
        val sc = Scanner(File(fn))
        var cnt = 0
        while (sc.hasNextLine()) {
            val (key, value, err) = sc.nextLine().split(" -> ")
            cards.put(key, value)
            errors.put(key, err.toInt())
            cnt++
        }
        printToLog("$cnt cards have been loaded.")
    } else {
        throw Exception("File not found.")
    }
}

fun export(fname: String = "") {
    var fn = fname
    if (fn.isBlank()) {
        printToLog("File name:")
        fn = readLine()!!.toLog()
    }
    File(fn).writeText("")
    for (c in cards) {
        File(fn).appendText("${c.key} -> ${c.value} -> ${errors[c.key] ?: 0}\n")
    }
    printToLog("${cards.size} cards have been saved.")
}

fun log() {
    printToLog("File name:")
    val fn = readLine()!!.toLog()
    File(fn).writeText("")
    for (log in logs) {
        File(fn).appendText(log)
    }
    printToLog("The log has been saved.")
}