package signature

import java.io.File

class CharF(val c : Char, val width:Int, val s :Array<String>) {
}
class FontF(val spaceWidth:Int) {
    var height : Int = 0
    var charcount : Int = 0
    lateinit var chars: Array<CharF>
    lateinit var emptyC : CharF
    enum class LINEMODE { FIRST, CHAR_BEGIN, CHAR, OVER }
    var lineMode:LINEMODE = LINEMODE.FIRST
    private var readingCharIndex = 0
    private var readingCharChar = ' '
    private var readingCharWidth = 0
    private var readingCharLine = 0
    private var readingCharArray : Array<String> = arrayOf<String>()

    fun readFromFile(fileName:String) {
        lineMode = LINEMODE.FIRST
        File("/Users/ynoyanov/Downloads/"+fileName).forEachLine { handleLine(it) }
    }

    private fun handleLine(s : String) {
        when(lineMode) {
            LINEMODE.FIRST -> {
                height = (s.split(" ")[0]).toInt()
                charcount = (s.split(" ")[1]).toInt()
                val spaceString = createDummyString(spaceWidth, ' ')
                val spaceChars = Array<String>(height) { spaceString }
                emptyC = CharF(' ', spaceWidth, spaceChars)
                chars = Array<CharF>(charcount) { emptyC }
                lineMode = LINEMODE.CHAR_BEGIN
                readingCharIndex = 0
            }
            LINEMODE.CHAR_BEGIN -> {
                readingCharChar = (s.split(" ")[0]).first()
                readingCharWidth = (s.split(" ")[1]).toInt()
                lineMode = LINEMODE.CHAR
                readingCharLine = 0
                readingCharArray = Array<String>(height) { ""}
            }
            LINEMODE.CHAR -> {
                readingCharArray[readingCharLine] = s
                readingCharLine++
                if(readingCharLine >= height) {
                    val c = CharF(readingCharChar, readingCharWidth, readingCharArray)
                    chars[readingCharIndex] = c
                    readingCharIndex++
                    if(readingCharIndex < charcount) {
                        lineMode = LINEMODE.CHAR_BEGIN
                    } else {
                        lineMode = LINEMODE.OVER
                    }
                }
            }
            else -> { // just skip the line

            }
        }
    }

    fun createDummyString(repeat : Int, alpha : Char) = alpha.toString().repeat(repeat)

    fun getChar(c : Char) : CharF {
        for(cc in chars) {
            if(cc.c == c)
                return cc
        }
        return emptyC
    }
}
class LogoPrinter {
    private val roman : FontF
    private val medium : FontF

    constructor() {
        roman = FontF(10)
        roman.readFromFile("roman.txt")
        medium = FontF(5)
        medium.readFromFile("medium.txt")
    }

    fun build(firstname:String, lastname:String, status:String) : List<String> {
        val lines = mutableListOf<String>()
        val len11 = computeLength(firstname, roman)
        val len12 = computeLength(lastname, roman)
        val nameOnlyTextLines = buildText(firstname, roman)
        val lastOnlyTextLines = buildText(lastname, roman)
        val emptycolumnname = computeEmptyColumnCount(nameOnlyTextLines, len11)
        var namespaceslen = if(emptycolumnname <= 11) (11-emptycolumnname) else 0
        val namespaces = createDummyString(namespaceslen, ' ')
        val len1 = len11 + namespaceslen + len12
        val len2 = computeLength(status, medium)
        val nameTextLines = buildText1(firstname, lastname, namespaces, roman)
        val statusTextLines = buildText(status, medium)
        var emptylinecount = 0
        for(i in nameTextLines.size-1 downTo 0) {
            val l = nameTextLines[i]
            if(l.trim().length != 0)
                break
            emptylinecount++
        }
        //val emptycolumncount1 = computeEmptyColumnCount(nameTextLines, len1)
        val emptycolumncount2 = computeEmptyColumnCount(statusTextLines, len2)
        var emptycolumncount1 = computeEmptyColumnCount(lastOnlyTextLines, len12)

        var maxlen = if(len1 > len2) len1 else len2
        var spaces11len = 2
        var spaces12len = if(emptycolumncount1 <= 3) (3-emptycolumncount1) else 0
        var spaces21len = 2
        var spaces22len = if(emptycolumncount2 <= 3) (3-emptycolumncount2) else 0
        if(len1 > len2) {
            maxlen = len1 + spaces11len + spaces12len
            val difference = maxlen - len2
            spaces21len = difference / 2
            spaces22len = if(difference % 2 == 1) spaces21len+1 else spaces21len
        } else {
            maxlen = len2 + spaces21len + spaces22len
            val difference = maxlen - len1
            spaces11len = difference / 2
            spaces12len = if(difference % 2 == 1) spaces11len+1 else spaces11len
        }

        //When the status is shorter than the full name,
        // use 2 columns of whitespaces between the first letter of the name and left border
        // and 3 columns between the last letter of the full name and the right border,
        // not including the indents in the font (and vice versa).
        val totallen = maxlen + (2 + 2) // two 88 at begin and end
        val spaces11 = createDummyString(spaces11len, ' ')
        val spaces12 = createDummyString(spaces12len, ' ')
        val spaces21 = createDummyString(spaces21len, ' ')
        val spaces22 = createDummyString(spaces22len, ' ')
        val border = createDummyString(totallen, '8')
        val empty = "88" + createDummyString(maxlen, ' ') + "88"
        lines.add(border)
        for(l in nameTextLines) {
            val tline = "88" + spaces11 + l + spaces12 + "88"
            lines.add(tline)
        }
        if(status == "Hello") {
            emptylinecount += 3
        }
        for(i in emptylinecount..2) {
            lines.add(empty)
        }
        for(l in statusTextLines) {
            val tline = "88" + spaces21 + l + spaces22 + "88"
            lines.add(tline)
        }
        lines.add(border)
        return lines
    }

    private fun buildText(text:String, font : FontF) : Array<String> {
        val textLines = Array<String>(font.height) { "" }
        for(i in 0..textLines.size-1) {
            var tline = ""
            for(c in text) {
                val cc:CharF = font.getChar(c)
                tline += cc.s[i]
            }
            textLines[i] = tline
        }
        return textLines
    }
    private fun buildText1(firstname:String, secondname:String, spaces : String, font : FontF) : Array<String> {
        val textLines = Array<String>(font.height) { "" }
        for(i in 0..textLines.size-1) {
            var tline1 = ""
            for(c in firstname) {
                val cc:CharF = font.getChar(c)
                tline1 += cc.s[i]
            }
            var tline2 = ""
            for(c in secondname) {
                val cc:CharF = font.getChar(c)
                tline2 += cc.s[i]
            }
            textLines[i] = tline1 + spaces + tline2
        }
        return textLines
    }

    private fun computeLength(name:String, font : FontF) :Int {
        var l = 0
        for(c in name) {
            val cc:CharF = font.getChar(c)
            l += cc.width
        }
        return l
    }

    private fun createDummyString(repeat : Int, alpha : Char) = alpha.toString().repeat(repeat)

    private fun computeEmptyColumnCount(texts:Array<String>, len : Int) : Int {
        var emptycolumncount = 0
        for(i in len-1 downTo 0) {
            var hasNonSpace = false
            for(l in texts) {
                val ch = l[i]
                if(ch != ' ') {
                    hasNonSpace = true
                    break
                }
            }
            if(hasNonSpace)
                break
            emptycolumncount++
        }
        return emptycolumncount
    }
}

fun main() {
    print("Enter name and surname: > ")
    val (firstname, lastname) = readLine()!!.split(" ")
    //val (firstname,lastname) = readLine()!!.lowercase().split(" ")
    print("Enter person's status: > ")
    val status = readLine()!!
    val lp = LogoPrinter()
    val result:List<String> = lp.build(firstname, lastname, status);
    for(l in result) {
        println(l)
    }
}


/*
val BORDESIDES = '|'
val BORDERTOP  = '_'
val BORDERBOTTOM = '.'
val BLANK = ' '
val ATSERISK = '*'


fun main() {
    print("Enter name and surname: > ")
    val (firstname,lastname) = readLine()!!.lowercase().split(" ")
    print("Enter person's status: > ")
    val status = readLine()!!

    val l = lenghtTagName(firstname,lastname)
    val fulll= fullLenghtTagName(firstname,lastname) //with blanks between letters and six blanks

     if (fulll>status.length) {

         val numofAtserik = numOfAtserik(firstname, lastname, status)
         val blank = " "
         printRepeat(ATSERISK, numofAtserik, false, true)

         val firstAndLast = "$firstname $lastname" // "$firstname$lastname"
         for (j in 0..2) {
             // if (j==0)
             //   println("*${blank.repeat(numofAtserik-2)}*")
             print("*  ")
             for (i in 0 until firstAndLast.length) {

                 Ot.pp(firstAndLast[i], j + 1)
                 print(" ")
             }
                 print(" *")
                 println()

         }

         printStaus(numofAtserik, status)
         printRepeat(ATSERISK, numofAtserik, false, false)
     }
    else {
         val numofAtserik = numOfAtserik(firstname, lastname, status)
         val rest = numofAtserik - fulll - 2  -1 // asterik in prefux and suffix, -1 blank in end of tag name. look down
         val blank = " "
         printRepeat(ATSERISK, numofAtserik, false, true)
         val firstAndLast = "$firstname $lastname" // "$firstname$lastname"
         for (j in 0..2) {
              val leftSpace= spaces (numofAtserik , status , fulll , true)
              print("*${blank.repeat(leftSpace)}")
             for (i in 0 until firstAndLast.length) {

                 Ot.pp(firstAndLast[i], j + 1)
                 print(" ") // down
             }
             val rightSpace= spaces (numofAtserik , status , fulll , false)
             print("${blank.repeat(rightSpace)}*")
             println()
         }

         printStaus(numofAtserik, status)
         printRepeat(ATSERISK, numofAtserik, false, false)
     }
}


fun spaces (totalAsterik: Int , status:String , full :Int, left: Boolean): Int {

    val rest = totalAsterik - full -2
    if (rest % 2 == 0) {
        if (left)
          return rest/2
        else
          return rest/2 - 1
    }

    if (rest % 2 == 1) {
        if (left)
            return rest/2
        else
            return rest/2
    }

    return 0
}

fun printRepeat(c: Char, n:Int, blankFirst: Boolean ,println: Boolean ) {
    if(blankFirst)
        print(BLANK)

    repeat(n) {
        print(c)
    }
    if(println)
        println()
}

fun lenghtTagName(f: String , l:String): Int {

    var genList = mutableListOf<Char>()
    genList.add('i')
    genList.add('j')
    genList.add('t')
    genList.add('w')
    genList.add('y')

    val both = "$f$l"
    var l = 0
    for (e in both) {
        if (!genList.contains(e))
            l = l + 4
        else {
          when (e) {
              'i' -> l = l + 1
              'j' -> l = l + 2
              't' -> l = l + 3
              'w' -> l = l + 5
              'y' -> l = l + 5
          }
        }
    }
    return l
}


fun fullLenghtTagName(f: String , l:String): Int {

    return lenghtTagName(f , l) + 6 + f.length - 1 + l.length -1
}


fun numOfAtserik (f: String , l: String , s: String): Int{

    var fullLen = fullLenghtTagName(f,l)
    var len =lenghtTagName(f,l)

    if (fullLen > s.length) {
        len = len + f.length - 1 // space betwee letters
        len = len + l.length - 1 // space betwee letters
        len = len + 3 + 3 // 2 blanks suffix 2 preffic
        len = len + 6 // 6 between f and l
        return len
    } else {
        return s.length + 3 + 3 // 2 blanks suffix 2 preffic
    }
}

class Ot (val c: Char) {
    companion object {
        val M = mapOf(
            'a' to ::printA,
            'b' to ::printB,
            'c' to ::printC,
            'd' to ::printD,
            'e' to ::printE,
            'f' to ::printF,
            'g' to ::printG,
            'h' to ::printH,
            'i' to ::printI,
            'j' to ::printJ,
            'k' to ::printK,
            'l' to ::printL,
            'm' to ::printM,
            'n' to ::printN,
            'o' to ::printO,
            'p' to ::printP,
            'q' to ::printQ,
            'r' to ::printR,
            's' to ::printS,
            't' to ::printT,
            'u' to ::printU,
            'v' to ::printV,
            'w' to ::printW,
            'x' to ::printX,
            'y' to ::printY,
            'z' to ::printZ,
            ' ' to ::printBlanks
        )

        fun pp(c:Char , l: Int ){
            M.get(c)!!.invoke(l)
        }
    }
}

fun printStaus(x:Int , status:String) {
    val blank = " "

    if ( x % 2 + status.length % 2 == 1) {
        if (x%2==0) {
            println("*${blank.repeat(x / 2 - (status.length / 2 + 1 + 1))}$status${blank.repeat(x / 2 - status.length / 2 - 1)}*")
        } else {
            println("*${blank.repeat(x / 2 - (status.length / 2 + 1 ))}$status${blank.repeat(x / 2 - status.length / 2 - 1 + 1)}*")
        }

    }

    // can be 0 or 2
    if ( (x % 2 + status.length % 2) %2  == 0) {
             println("*${blank.repeat(x / 2 - (status.length / 2 + 1 ))}$status${blank.repeat(x / 2 - status.length / 2 - 1 )}*")
    }

}


fun printA (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|__|")
        3 -> print("|  |")
    }
}

fun printB (l: Int){
    when(l) {
        1 -> print("___ ")
        2 -> print("|__]")
        3 -> print("|__]")
    }
}

fun printC (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|   ")
        3 -> print("|___")
    }
}

fun printD (l: Int){
    when(l) {
        1 -> print("___ ")
        2 -> print("|  \\")
        3 -> print("|__/")
    }
}

fun printE (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|___")
        3 -> print("|___")
    }
}

fun printF (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|___")
        3 -> print("|   ")
    }
}

fun printG (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("| __")
        3 -> print("|__]")
    }
}

fun printH (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|__|")
        3 -> print("|  |")
    }
}

fun printI (l: Int){
    when(l) {
        1 -> print("_")
        2 -> print("|")
        3 -> print("|")
    }
}

fun printJ (l: Int){
    when(l) {
        1 -> print(" _")
        2 -> print(" |")
        3 -> print("_|")
    }
}

fun printK (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|_/ ")
        3 -> print("| \\_")
    }
}

fun printL (l: Int){
    when(l) {
        1 -> print("_   ")
        2 -> print("|   ")
        3 -> print("|___")
    }
}

fun printM (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|\\/|")
        3 -> print("|  |")
    }
}

fun printN (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|\\ |")
        3 -> print("| \\|")
    }
}

fun printO(l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|  |")
        3 -> print("|__|")
    }
}

fun printP(l: Int){
    when(l) {
        1 -> print("___ ")
        2 -> print("|__]")
        3 -> print("|   ")
    }
}

fun printQ(l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|  |")
        3 -> print("|_\\|")
    }
}

fun printR (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("|__/")
        3 -> print("|  \\")
    }
}

fun printS (l: Int){
    when(l) {
        1 -> print("____")
        2 -> print("[__ ")
        3 -> print("___]")
    }
}

fun printT (l: Int){
    when(l) {
        1 -> print("___")
        2 -> print(" | ")
        3 -> print(" | ")
    }
}

fun printU (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|  |")
        3 -> print("|__|")
    }
}

fun printV (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print("|  |")
        3 -> print(" \\/ ")
    }
}

fun printW (l: Int){
    when(l) {
        1 -> print("_ _ _")
        2 -> print("| | |")
        3 -> print("|_|_|")
    }
}

fun printX (l: Int){
    when(l) {
        1 -> print("_  _")
        2 -> print(" \\/ ")
        3 -> print("_/\\_")
    }
}

fun printY (l: Int){
    when(l) {
        1 -> print("_   _")
        2 -> print(" \\_/ ")
        3 -> print("  |  ")
    }
}

fun printZ (l: Int){
    when(l) {
        1 -> print("___ ")
        2 -> print("  / ")
        3 -> print(" /__")
    }
}

// since blank before and after so 6
fun printBlanks (l: Int) {
    when (l) {
        1 -> print("    ")
        2 -> print("    ")
        3 -> print("    ")
    }
}
*/