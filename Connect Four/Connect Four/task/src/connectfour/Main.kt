package connectfour

class Turn(x: Int, y: Int, c: Char) {

}
class CF {
    lateinit var firstname : String
    lateinit var secondname : String
    var rows : Int = 0
    var columns : Int = 0
    var board = Array<Char>(0) {' '}
    var isFirstPlayerTurn = true
    val CHAR1 = 'o'
    val CHAR2 = '*'
    var gameCount = 0
    var gameIndex = 1
    var firstscore = 0
    var secondscore = 0


    fun entrance() {
        println("Connect Four")
        println("First player's name:")
        firstname = readln()
        println("Second player's name:")
        secondname = readln()
    }

    fun readBoardSize() {
        var isBoardOK = false
        while (!isBoardOK) {
            println("Set the board dimensions (Rows x Columns)")
            println("Press Enter for default (6 x 7)")
            isBoardOK = false
            var boardSize = readln()
            if (boardSize == null || boardSize.length == 0)
                boardSize = "6x7"
            val xy = boardSize.lowercase().split("x")
            if (xy.size == 2) {
                if(xy[0].trim().toIntOrNull() != null && xy[1].trim().toIntOrNull() != null) {
                    rows = xy[0].trim().toIntOrNull() ?: -1
                    columns = xy[1].trim().toIntOrNull() ?: -1
                    if (rows !in 5..9) {
                        println("Board rows should be from 5 to 9")
                        continue
                    }
                    if (columns !in 5..9) {
                        println("Board columns should be from 5 to 9")
                        continue
                    }
                    isBoardOK = true
                }
            }
            if(!isBoardOK) {
                println("Invalid input")
            }
        }
        board = Array<Char>((rows+1)*(columns+1)) { ' ' }
    }

    fun askGameCount() : Int {
        var isError = true
        while(isError) {
            println("Do you want to play single or multiple games?")
            println("For a single game, input 1 or press Enter")
            println("Input a number of games:")
            var s = readln()
            if (s == null || s.isEmpty())
                s = "1"
            val agameCount = s.toIntOrNull() ?: 0
            if(agameCount > 0) {
                isError = false
                gameCount = agameCount
            } else {
                println("Invalid input")
            }
        }
        gameIndex = 0
        return gameCount
    }

    fun startNewGame() {
        gameIndex++
        if(gameIndex == 1) {
            println("$firstname VS $secondname")
            println("$rows X $columns board")
            if(gameCount > 1) {
                println("Total $gameCount games")
            }
        }
        if(gameCount > 1) {
            println("Game #$gameIndex")
        } else {
            println("Single game")
        }
        board = Array<Char>((rows+1)*(columns+1)) { ' ' }
    }

    fun printoutBoard() {
        // print board
        for(i in 1..columns) {
            print(" "+i)
        }
        println("")
        for(j in 1..rows) {
            print("║")
            for(i in 1..columns) {
                val c = get(j, i)
                print(c)
                print("║")
            }
            println("")
        }
        print("╚")
        for(i in 1..columns-1) {
            print("═╩")
        }
        println("═╝")
    }

    fun turn() : Boolean {
        var isOKTurn = false
        while(!isOKTurn) {
            isOKTurn = false
            val playername = if(isFirstPlayerTurn) firstname else secondname
            println("$playername's turn:")
            val ts = readln()
            if(ts == "end")
                return false
            val t = ts.toIntOrNull()
            if(t == null) {
                println("Incorrect column number")
                continue
            }
            if (t !in 1..columns) {
                println("The column number is out of range (1 - $columns)")
                continue
            }
            var c0 = -1;
            for(i in rows downTo 1) {
                if(get(i, t) == ' ') {
                    c0 = i
                    break
                }
            }
            if(c0 == -1) {
                println("Column $t is full")
                continue
            }
            isOKTurn = true
            val c = if(isFirstPlayerTurn) CHAR1 else CHAR2
            set(c0, t, c)
        }
        isFirstPlayerTurn = !isFirstPlayerTurn
        return true
    }


    fun isWon() : Boolean {
        if(isAllBoard()) {
            println("It is a draw")
            firstscore++
            secondscore++
            if(gameCount > 1)
                printResults()
            return true
        }
        val r = isWon12()
        if(r == 1) {
            firstscore += 2
            println("Player $firstname won")
            if(gameCount > 1)
                printResults()
            return true
        }
        if(r == 2) {
            secondscore += 2
            println("Player $secondname won")
            if(gameCount > 1)
                printResults()
            return true
        }
        return false
    }

    fun printResults() {
        println("Score")
        println("$firstname: $firstscore $secondname: $secondscore")

    }
    private fun isWon12() : Int {
        if(isWonColumns4(CHAR1))
            return 1
        if(isWonColumns4(CHAR2))
            return 2
        if(isWonRows4(CHAR1))
            return 1
        if(isWonRows4(CHAR2))
            return 2
        if(isWonDiagonals4(CHAR1))
            return 1
        if(isWonDiagonals4(CHAR2))
            return 2
        return 0
    }

    fun isWonColumns4(c : Char) : Boolean {
        // search horisontaly
        for(y in 1..rows) {
            var count = 0
            for (x in 1..columns) {
                if(get(x,y) == c) {
                    count++
                    if(count >= 4) {
                        return true
                    }
                } else {
                    count = 0
                }
            }
        }
        return false
    }

    fun isWonRows4(c : Char) : Boolean {
        // search vertically
        for (x in 1..columns) {
            var count = 0
            for(y in 1..rows) {
                if(get(x,y) == c) {
                    count++
                    if(count >= 4) {
                        return true
                    }
                } else {
                    count = 0
                }
            }
        }
        return false
    }

    fun isWonDiagonals4(c : Char) : Boolean {
        // search diagonally
        for (x in 1..columns) {
            for(y in 1..rows) {
                var count = 0
                for(i in 0..3) {
                    if(get(x+i, y+i) == c) {
                        count++
                    } else {
                        break
                    }
                }
                if(count >= 4) {
                    return true
                }
                count = 0
                for(i in 0..3) {
                    if(get(x+i, y-i) == c) {
                        count++
                    } else {
                        break
                    }
                }
                if(count >= 4) {
                    return true
                }
            }
        }
        return false
    }

    fun isAllBoard() : Boolean {
        for(x in 1..rows) {
            for(y in 1.. columns) {
                if(get(x,y) == ' ')
                    return false
            }
        }
        return true
    }


    private fun get(x: Int, y: Int) : Char {
        if(x < 1 || y < 1 || x > rows || y > columns)
            return '!'//0.toChar()
        return board.get(x*columns + y)
    }
    private fun set(x: Int, y: Int, c: Char) {
        if(x < 1 || y < 1 || x > rows || y > columns)
            return
        board.set(x*columns + y, c)
    }
}


fun main() {
    val cf = CF()
    cf.entrance()
    cf.readBoardSize()
    val gameCount = cf.askGameCount()
    repeat(gameCount) {
        cf.startNewGame()
        cf.printoutBoard()
        while (true) {
            if (!cf.turn())
                break
            cf.printoutBoard()
            if (cf.isWon())
                break
        }
    }
    println("Game over!")
}