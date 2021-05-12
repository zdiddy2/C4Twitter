package c4

import java.lang.StringBuilder

object BoardConstants {
    const val columns = 7
    const val rows = columns - 1
    const val boardLength = columns * rows
}

data class Board(var board : MutableList<Piece> = MutableList(BoardConstants.boardLength) {Piece.EMPTY}){

    /** 7 columns, 6 rows
     * Accepted characters E/e empty, P/p player, C/c computer, anything else defaults to empty.
     * BoardString can be separated into columns for readability
     */
    constructor(vararg boardString: String) : this() {

        val bs = boardString.joinToString("").filter { !it.isWhitespace() }.toLowerCase()
        if(bs.length != BoardConstants.boardLength){
            throw java.lang.IllegalArgumentException("Invalid Board")
        }

        bs.forEachIndexed { index, c ->
            when(c){
                'c' -> board[index] = Piece.COMPUTER
                'p' -> board[index] = Piece.PLAYER
                else -> board[index] = Piece.EMPTY
            }
        }

        validateBoard()
    }

    constructor(b: Board) : this(){
        board = mutableListOf(*b.board.toTypedArray())
    }

    private fun validateBoard(){
        for(i in ((BoardConstants.rows - 1) * BoardConstants.columns) until BoardConstants.boardLength){
            drop(i, 0)
        }
    }

    private fun drop(columnBottom : Int, depth: Int){
        //base cases
        if(depth >= BoardConstants.rows - 1/* -1 for board indexing, not needed for columns*/) return
        //skip any valid filled spots
        if(board[columnBottom - (depth * BoardConstants.columns)] != Piece.EMPTY) return drop(columnBottom, depth + 1)

        //loop from depth up, for rows - depth
        for(i in (columnBottom - depth * BoardConstants.columns) downTo 0 step BoardConstants.columns){
            //move invalid piece down
            if(board[i] != Piece.EMPTY){
                board[columnBottom - depth * BoardConstants.columns] = board[i]
                board[i] = Piece.EMPTY
                //check for any more pieces
                return drop(columnBottom, depth + 1)
            }
        }
    }

    /**
     * Adds piece, column is between  1 - BoardConstants.columns (7)
     */
    fun addPiece(type : Piece, column : Int){
        //drop piece down column, board is row major

        for(i in BoardConstants.rows - 1 downTo 0){
            val index = (BoardConstants.columns * i) + (column).coerceIn(1, BoardConstants.columns) - 1
            if(board[index] == Piece.EMPTY){
                board[index] = type
                break
            }
        }
    }

    override fun toString() : String{
        val sb = StringBuilder()
        for(i in board.indices){
            sb.append(board[i].character + " ")
            if ((i + 1)% BoardConstants.columns == 0) sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Returns winning Piece and Piece.EMPTY on no win
     */
    fun hasWon() : Piece {

        var inRow = 0
        var lastPiece = Piece.EMPTY
        //check horizontal
        for(i in board.indices){

            if(lastPiece == board[i] && lastPiece != Piece.EMPTY) inRow++ else inRow = 0
            lastPiece = board[i]

            if(inRow >= 3) return lastPiece

            //reset at the end of each row
            if((i + 1) % BoardConstants.columns == 0) {
                inRow = 0
                lastPiece = Piece.EMPTY
            }
        }


        //check vertical
        inRow = 0
        lastPiece = Piece.EMPTY
        for(i in 0 until BoardConstants.columns){
            for(j in 0 until BoardConstants.rows){
                val index = (j * BoardConstants.columns) + i

                if(lastPiece == board[index] && lastPiece != Piece.EMPTY) inRow++ else inRow = 0
                lastPiece = board[index]

                if(inRow >= 3) return lastPiece
            }
            inRow = 0
            lastPiece = Piece.EMPTY
        }

        //down right upper half
        //iterates columns starts (except 0)
        var iterations = BoardConstants.rows - 1
        for(start in 1 until BoardConstants.columns){
            val piece = iDiagonal(start, iterations, BoardConstants.columns + 1)
            if(piece != Piece.EMPTY) return piece
            iterations--
        }

        //down right lower half
        iterations = BoardConstants.rows - 1
        //for loop traverses down the first column
        for(start in 0..(BoardConstants.columns * (BoardConstants.rows - 1)) step BoardConstants.columns){
            val piece = iDiagonal(start, iterations, BoardConstants.columns + 1)
            if(piece != Piece.EMPTY) return piece
            iterations--
        }


        //down left upper half
        iterations = BoardConstants.rows - 1

        //traverse column starts
        for(start in BoardConstants.columns - 2 downTo 0){
            val piece = iDiagonal(start, iterations, BoardConstants.columns - 1)
            if(piece != Piece.EMPTY) return piece
            iterations--
        }

        //down left lower half
        iterations = BoardConstants.rows - 1
        for(start in (BoardConstants.columns - 1)..(BoardConstants.columns * (BoardConstants.rows - 1)) step BoardConstants.columns){
            val piece = iDiagonal(start, iterations, BoardConstants.columns - 1)
            if(piece != Piece.EMPTY) return piece
            iterations--
        }

        return Piece.EMPTY
    }

    private fun iDiagonal(start: Int, iterations: Int, diagonalStep : Int) : Piece{

        var inRow = 0
        var lastPiece = Piece.EMPTY

        for (add in 0..(iterations * diagonalStep) step diagonalStep) {
            val index = start + add

            if (lastPiece == board[index] && lastPiece != Piece.EMPTY) inRow++ else inRow = 0
            lastPiece = board[index]

            if (inRow >= 3) return lastPiece
        }

        return Piece.EMPTY
    }

    /**
     * Evaluation function, computer is minimizer
     */
    fun evaluate() : Int {
        val win = hasWon()
        if(win == Piece.PLAYER) return 1000
        else if(win == Piece.COMPUTER) return -1000

        //count possible 4's




        return 0
    }

    /**
     * Returns list of indices in all valid directions
     */
    fun getDirectionsMatrix(i : Int) : List<Int>{
        val c = BoardConstants.columns

        val tLeft = listOf(-c - 1, -1, c - 1)
        val tCenter = listOf(-c, c)
        val tRight = listOf(-c + 1, 1, c + 1)

        val tMatrix : MutableList<Int> = when{
            i % c == 0 -> (tRight + tCenter) as MutableList<Int>
            (i + 1) % c == 0 -> (tLeft + tCenter) as MutableList<Int>
            else -> (tLeft + tCenter + tRight) as MutableList<Int>
        }

        return tMatrix.map { it + i }.filter { it >= 0 && it < BoardConstants.boardLength }.sorted()
    }

}


enum class Piece(val character: String) {
    EMPTY("T"), PLAYER("P"), COMPUTER("C")
}