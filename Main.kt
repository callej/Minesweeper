package minesweeper

const val WIDTH = 9
const val HEIGHT = 9

fun gameInit(mines: Int): List<MutableList<String>> {
    val mineField = List(HEIGHT) { MutableList(WIDTH) { "." } }
    while (mineField.flatten().count { it == "X" } < mines) {
        mineField[(0 until HEIGHT).random()][(0 until WIDTH).random()] = "X"
    }
    return mineField
}

fun findAdjacentMines(row: Int, col: Int, mineField: List<MutableList<String>>): String {
    var count = 0
    for (r in (row - 1).coerceIn(0..mineField.lastIndex)..(row + 1).coerceIn(0..mineField.lastIndex)) {
        for (c in (col - 1).coerceIn(0..mineField[0].lastIndex)..(col + 1).coerceIn(0..mineField[0].lastIndex)) {
            if (mineField[r][c] == "X") count++
        }
    }
    return if (count == 0) "." else count.toString()
}

fun addHints(mineField: List<MutableList<String>>): List<MutableList<String>> {
    for (row in mineField.indices) {
        for (col in mineField[0].indices) {
            if (mineField[row][col] == ".") {
                mineField[row][col] = findAdjacentMines(row, col, mineField)
            }
        }
    }
    return mineField
}

fun printField(showField: List<MutableList<String>>) {
    println("\n │123456789│\n—│—————————│")
    for (row in 1..showField.size) {
        println("$row|${showField[row - 1].joinToString("").replace('X', '.')}|")
    }
    println("—│—————————│")
}

fun printFailed(showField: List<MutableList<String>>, mineField: List<MutableList<String>>) {
    for (row in showField.indices) {
        for (col in showField[0].indices) {
            if (mineField[row][col] == "X") showField[row][col] = "X"
        }
    }
    printField(showField)
}

fun runGame(mines: Int): String {
    var mineField = addHints(gameInit(mines))
    val showField = List(mineField.size) { MutableList(mineField[0].size) { "." } }
    var correctMarks = 0
    var wrongMarks = 0
    var firstFreeAction = true
    printField(showField)
    while (correctMarks + showField.flatten().count { it == "." } != mineField.flatten().count { it == "X" } || wrongMarks != 0) {
        print("Set/unset mines marks or claim a cell as free: ")
        val (xs, ys, action) = readln().split(" ")
        val x = xs.toInt() - 1
        val y = ys.toInt() - 1
        if (action == "mine") {
            when {
                showField[y][x] == "/" || Regex("\\d").matches(showField[y][x]) -> continue
                showField[y][x] == "*" -> { showField[y][x] = "."; if (mineField[y][x] == "X") correctMarks-- else wrongMarks-- }
                mineField[y][x] == "X" -> { correctMarks++; showField[y][x] = "*" }
                else -> { wrongMarks++; showField[y][x] = "*" }
            }
        } else {
            var mineFieldChanged = false
            while (firstFreeAction && mineField[y][x] == "X") {
                mineField = addHints(gameInit(mines))
                mineFieldChanged = true
            }
            firstFreeAction = false
            if (mineFieldChanged && correctMarks + wrongMarks != 0) {
                correctMarks = 0
                wrongMarks = 0
                for (row in 0..showField.lastIndex) {
                    for (col in 0..showField[0].lastIndex) {
                        if (showField[row][col] == "*") {
                            if (mineField[row][col] == "X") correctMarks++ else wrongMarks++
                        }
                    }
                }
            }
            when {
                showField[y][x] == "/" || Regex("\\d").matches(showField[y][x]) -> continue
                mineField[y][x] == "X" -> { printFailed(showField, mineField); return "failed" }
                Regex("\\d").matches(mineField[y][x]) -> showField[y][x] = mineField[y][x]
                else -> wrongMarks += free(x, y, mineField, showField)
            }
        }
        printField(showField)
    }
    return "win"
}

fun free(x: Int, y: Int, mineField: List<MutableList<String>>, showField: List<MutableList<String>>): Int {
    var wrongMarksChange = 0
    if (showField[y][x] == "*") wrongMarksChange -= 1
    if (Regex("\\d").matches(mineField[y][x])) {
        showField[y][x] = mineField[y][x]
        return wrongMarksChange
    }
    showField[y][x] = "/"
    for (row in (y - 1).coerceIn(0, y)..(y + 1).coerceIn(0, showField.lastIndex)) {
        for (col in (x - 1).coerceIn(0, x)..(x + 1).coerceIn(0, showField[0].lastIndex)) {
            if ((row != y || col != x) && showField[row][col] != "/") {
                wrongMarksChange += free(col, row, mineField, showField)
            }
        }
    }
    return wrongMarksChange
}

fun main() {
    print("How many mines do you want on the field? ")
    if (runGame(readln().toInt()) == "win") {
        println("Congratulations! You found all the mines!")
    } else {
        println("You stepped on a mine and failed!")
    }
}