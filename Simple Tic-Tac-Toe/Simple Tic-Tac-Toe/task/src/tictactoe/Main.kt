package tictactoe

class TTT {
    private val board : Array<Char>
    var playerChar : Char = 'X'
    constructor() {
        board = Array<Char>(3*3) { ' ' }
    }

    fun getC(x: Int, y:Int) : Char {
        return board.get((x-1)+(y-1)*3)
    }
    private fun setC(x: Int, y:Int, c: Char) {
        board.set((x-1)+(y-1)*3, c)
    }

    fun setBoardFromString(cels: String) {
        for(i in 0..8) {
            board[i] = if(cels[i] != '_') cels[i] else ' '
        }
    }
    fun printGame() {
        for(y in 1..3) {
            for(x in 1..3) {
                val c = getC(x,y)
                if(x != 1) {
                    print(" ")
                }
                print(c)
            }
            println("")
        }
    }

    fun printGameBoard() {
        repeat(9) {
            print("-")
        }
        println()
        for(y in 1..3) {
            print("|")
            for(x in 1..3) {
                print(" ")
                val c = getC(x,y)
                print(c)
            }
            println(" |")
        }
        repeat(9) {
            print("-")
        }
        println()
    }

    fun isWin() : Char? {
        if(isWinPlayer('O'))
            return 'O'
        if(isWinPlayer('X'))
            return 'X'
        return null
    }

    private fun isWinPlayer(c: Char) : Boolean {
        if(isWinH(c))
            return true
        if(isWinV(c))
            return true
        if(isWinD(c))
            return true
        return false
    }

    private fun isWinH(c : Char) : Boolean {
        for(i in 1..3) {
            if(getC(1, i) == c && getC(2, i) == c && getC(3, i) == c)
                return true
        }
        return false
    }
    private fun isWinV(c : Char) : Boolean {
        for(i in 1..3) {
            if(getC(i, 1) == c && getC(i, 2) == c && getC(i, 3) == c)
                return true
        }
        return false
    }
    private fun isWinD(c : Char) : Boolean {
        if(getC(1, 1) == c && getC(2, 2) == c && getC(3, 3) == c)
                return true
        if(getC(1, 3) == c && getC(2, 2) == c && getC(3, 1) == c)
            return true
        return false
    }

    fun isDraw() : Boolean {
        for(y in 1..3) {
            for(x in 1..3) {
                val c = getC(x,y)
                if(c != 'O' && c != 'X')
                    return false
            }
        }
        return true
    }

    fun isPossible() : Boolean {
        val x_count = board.filter { it == 'X' } . size
        val o_count = board.filter { it == 'O' } . size
        if(Math.abs(x_count - o_count) > 1) {
            return false
        }
        val wonx = isWinPlayer('X')
        val wono = isWinPlayer('O')
        if(wonx && wono) {
            return false
        }
        return true;
    }

    fun turn() : Boolean {
        var isError = true
        while(isError) {
            println("Enter the coordinates:")
            val inp = readln().split(" ")
            if (inp.size != 2 || inp[0].toIntOrNull() == null || inp[1].toIntOrNull() == null) {
                println("You should enter numbers!")
            } else {
                val y = inp[0].toInt()
                val x = inp[1].toInt()
                if (x !in 1..3 || y !in 1..3) {
                    println("Coordinates should be from 1 to 3!")
                } else if (getC(x, y) != ' ') {
                    println("This cell is occupied! Choose another one!")
                } else {
                    setC(x, y, playerChar)
                    playerChar = if(playerChar == 'X') 'O' else 'X'
                    isError = false
                }
            }
        }
        return true
    }

}

fun analyzeGame(tt : TTT) : Boolean {
    if(!tt.isPossible()) {
        println("Impossible")
        return false
    }
    val c = tt.isWin()
    if(c == 'O' || c == 'X') {
        println(c+" wins")
        return false
    } else if(tt.isDraw()) {
        println("Draw")
        return false
    } else {
        //println("Game in progress")
        return true
    }
}

fun readBoardFromConsole(tt : TTT) {
    print("Enter cells:")
    val cels = readln()
    tt.setBoardFromString(cels)
}

fun main() {
    val tt = TTT()
    //readBoardFromConsole()
    tt.printGameBoard()
    while(analyzeGame(tt)) {
        tt.turn()
        tt.printGameBoard()
    }
}