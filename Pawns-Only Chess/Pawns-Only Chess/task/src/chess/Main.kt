package chess

class POC {
    lateinit var firstName : String
    lateinit var secondName : String
    val board = CharArray(9*9) {' '}
    var isFirstPlayerMove : Boolean = true
    var isEnPassantAvailable = false

    init {
        prepareGame()
    }

    fun prepareGame() {
        for(x in 1..8)
            setFigure(x, 7, 'B')
        for(x in 1..8)
            setFigure(x, 2, 'W')
        isFirstPlayerMove = true
        isEnPassantAvailable = false
    }

    fun setFigure(x:Int, y:Int, c:Char) {
        val index = y*8+x
        if(index >= 0 && index < board.size)
            board[y*8+x] = c
    }
    fun getFigure(x:Int, y:Int) : Char {
        val index = y*8+x
        if(index >= 0 && index < board.size)
            return board[y*8+x]
        else
            return ' '
    }

    fun askPlayers()
    {
        println("Pawns-Only Chess")
        println("First Player's name:")
        firstName = readln()
        println("Second Player's name:")
        secondName = readln()
    }
    fun outputBoard() {
        for (line in 8 downTo 1) {
            print("   ")
            for (raw in 1..8) {
                print("+---")
            }
            println("+")
            print(" $line ")
            for (raw in 1..8) {
                print("| ")
                val c = getFigure(raw, line)
                print(c)
                print(" ")
            }
            println("|")
        }
        print("   ")
        for (raw in 1..8) {
            print("+---")
        }
        println("+")
        println("     a   b   c   d   e   f   g   h ")
    }

    fun game() {
        askPlayers()
        var isOver = false
        var isWrongMove = false
        while(!isOver) {
            if(!isWrongMove)
                outputBoard()
            if(isFirstPlayerMove)
                print(firstName)
            else
                print(secondName)
            println("'s turn:")
            val move = readln()
            if(move == "exit")
                break
            isWrongMove = checkAndDoMove(move)
            if(isGameOver())
                break;
        }
        println("Bye!")
    }

    fun game(moves : Array<String>) {
        firstName = "A"
        secondName = "B"
        var isOver = false
        var isWrongMove = false
        for(move in moves) {
            if(!isWrongMove)
                outputBoard()
            if(isFirstPlayerMove)
                print(firstName)
            else
                print(secondName)
            println("'s turn:")
            println(">"+move)
            if(move == "exit")
                break
            isWrongMove = checkAndDoMove(move)
            isOver = isGameOver()
        }
        println("Bye!")
    }

    fun checkAndDoMove(move : String) : Boolean {
        var isWrongMove = false
        val moveCheck = "[a-h][1-8][a-h][1-8]".toRegex()
        val isCorrectData = moveCheck.matches(move)
        val x0 = (move[0] - 'a') + 1
        val y0 = (move[1] - '1') + 1
        val x2 = (move[2] - 'a') + 1
        val y2 = (move[3] - '1') + 1

        val figure = if (isFirstPlayerMove) 'W' else 'B'
        val c0 = getFigure(x0, y0)
        val hasFigure = c0 == figure

        val isCorrectMoveForward = isCorrectMoveForward(x0, y0, x2, y2)
        val isCorrectMoveCapture = isCorrectMoveCapture(x0, y0, x2, y2)
        val isCorrectMoveEnPassant = isCorrectMoveEnPassant(x0, y0, x2, y2)

        if (isCorrectData && hasFigure && (isCorrectMoveForward || isCorrectMoveCapture || isCorrectMoveEnPassant)) {
            setFigure(x0, y0, ' ')
            setFigure(x2, y2, figure) // do move and capture
            if(!isCorrectMoveForward && !isCorrectMoveCapture && isCorrectMoveEnPassant) {
                setFigure(x2, y0, ' ') // do EnPassant capture
            }
            val cl = getFigure(x2-1, y2)
            val cr = getFigure(x2+1, y2)
            val figure2 = if (!isFirstPlayerMove) 'W' else 'B'
            isEnPassantAvailable = (cl == figure2) || (cr == figure2)
            isFirstPlayerMove = !isFirstPlayerMove
            isWrongMove = false
        } else {
            if(!hasFigure) {
                val startPosition = "" + move[0] + "" + move[1]
                if(isFirstPlayerMove)
                    println("No white pawn at $startPosition")
                else
                    println("No black pawn at $startPosition")
            } else {
                println("Invalid Input")
            }
            isWrongMove = true
        }
        return isWrongMove
    }

    fun isCorrectMoveForward(x0 : Int, y0 : Int, x2 : Int, y2 : Int) : Boolean {
        val c2 = getFigure(x2, y2)
        var isOK3 = c2 == ' '
        if(y2 - y0 > 1) {
            val c22 = getFigure(x0, y0+1)
            if(c22 != ' ')
                isOK3 = false
        }
        if(y2 - y0 < -1) {
            val c22 = getFigure(x0, y0-1)
            if(c22 != ' ')
                isOK3 = false
        }

        val isOK4 = (x0 == x2)
        var isOK5 = true
        if(isFirstPlayerMove && y0 == 2) {
            isOK5 = ((y2 - y0) == 1) || ((y2 - y0) == 2)
        } else if(isFirstPlayerMove && y0 != 2) {
            isOK5 = ((y2 - y0) == 1)
        } else if(!isFirstPlayerMove && y0 == 7) {
            isOK5 = ((y0 - y2) == 1) || ((y0 - y2) == 2)
        } else if(!isFirstPlayerMove && y0 != 7) {
            isOK5 = ((y0 - y2) == 1)
        }
        return isOK3 && isOK4 && isOK5
    }

    fun isCorrectMoveCapture(x0 : Int, y0 : Int, x2 : Int, y2 : Int) : Boolean {
        val c2 = getFigure(x2, y2)
        val figure2 = if (!isFirstPlayerMove) 'W' else 'B'
        val isOK3 = c2 == figure2

        val isOK4 = (x2 == x0+1) || (x2 == x0-1)
        var isOK5 = true
        if(isFirstPlayerMove) {
            isOK5 = ((y2 - y0) == 1)
        } else {
            isOK5 = ((y0 - y2) == 1)
        }
        return isOK3 && isOK4 && isOK5
    }

    fun isCorrectMoveEnPassant(x0 : Int, y0 : Int, x2 : Int, y2 : Int) : Boolean {
        val enpassantLine = if(isFirstPlayerMove) 5 else 4

        var isOK2 = y0 == enpassantLine
        var isOK3 = false
        var isOK4 = false
        var isOK5 = false
        if(isFirstPlayerMove) {
            isOK2 = y0 == 5
            isOK3 = (y2 - y0) == 1
            isOK4 = ((x2 - x0) == 1) || ((x2 - x0) == -1)
            val figureLeftRight = getFigure(x2, y0)
            isOK5 = figureLeftRight == 'B'
        } else {
            isOK2 = y0 == 4
            isOK3 = (y2 - y0) == -1
            isOK4 = ((x2 - x0) == 1) || ((x2 - x0) == -1)
            val figureLeftRight = getFigure(x2, y0)
            isOK5 = figureLeftRight == 'W'
        }

        return isEnPassantAvailable && isOK2 && isOK3 && isOK4 && isOK5
    }

    fun isWhiteWon() : Boolean {
        for(x in 1..8) {
            if(getFigure(x, 8) == 'W')
                return true
        }
        if(!hasFigure('B'))
            return true
        return false
    }
    fun isBlackWon() : Boolean {
        for(x in 1..8) {
            if(getFigure(x, 1) == 'B')
                return true
        }
        if(!hasFigure('W'))
            return true
        return false
    }
    fun hasFigure(figure : Char) : Boolean {
        for(y in 1..8) {
            for(x in 1..8) {
                val c = getFigure(x, y)
                if(c == figure)
                    return true
            }
        }
        return false
    }
    fun isSlatemate() : Boolean {
        val figure = if(isFirstPlayerMove) 'W' else 'B'
        val dy = if(isFirstPlayerMove) 1 else -1
        for(y in 1..8) {
            for(x in 1..8) {
                val c = getFigure(x, y)
                if(c == figure) {
                    val isOK1 = isCorrectMoveForward(x, y, x, y+dy)
                    val isOK2 = isCorrectMoveForward(x, y, x, y+dy*2)
                    val isOK3 = isCorrectMoveCapture(x, y, x-1, y+dy)
                    val isOK4 = isCorrectMoveCapture(x, y, x+1, y+dy)
                    val isOK5 = isCorrectMoveEnPassant(x, y, x-1, y+dy)
                    val isOK6 = isCorrectMoveEnPassant(x, y, x+1, y+dy)
                    if(isOK1 || isOK2 || isOK2 || isOK3 || isOK5 || isOK6)
                        return false
                }
            }
        }
        return true
    }

    fun isGameOver() : Boolean {
        if(isBlackWon()) {
            outputBoard()
            println("Black Wins!")
            return true
        }
        if(isWhiteWon()) {
            outputBoard()
            println("White Wins!")
            return true
        }
        if(isSlatemate()) {
            outputBoard()
            println("Stalemate!")
            return true
        }
        return false
    }

}

fun main() {
    val poc = POC()
//    val testMoves = arrayOf("e2e4", "d7d5", "e4d5", "c7c6", "a2a4", "c6d5", "exit")
//    val testEnOK = arrayOf("e2e4", "a7a6", "e4e5", "d7d5", "e5d6", "exit")
//    val testEnBad = arrayOf("e2e4", "a7a6", "e4e5", "d7d5", "a2a3", "a6a5", "e5d6", "exit")
//    val testSlatemate = arrayOf("a2a4", "b7b5", "a4b5", "c7c6", "b5c6", "a7a5", "c6d7", "a5a4", "d2d4", "e7e5", "d4e5", "f7f6", "e5f6", "h7h5", "f2f4", "a4a3", "b2a3", "h5h4", "f4f5", "g7f6", "g2g3", "h4g3", "h2g3")
//    val test1 = arrayOf("e2e4", "d7d5", "e4d5", "c7c6", "d5d6", "c6c5", "d6d7", "c5c4", "d7d8")
//    poc.game(test1)
    poc.game()
}