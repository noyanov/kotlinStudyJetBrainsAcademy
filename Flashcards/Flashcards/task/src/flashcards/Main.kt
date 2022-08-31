package flashcards

import java.io.File
import java.nio.charset.Charset
import kotlin.random.Random
import kotlin.random.nextInt

data class Card(val term:String, val definition:String) {
    var errors = 0
    var ok = 0
}

class FlashCards(val importfilename : String, val exportfilename : String)
{
    private var _cards : MutableList<Card> = mutableListOf()
    private var _log = ""

    fun interact() {
        if(importfilename.length > 0)
            doImport(importfilename)
        var isOver = false
        while (!isOver) {
            println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val action = readln()
            when (action) {
                "exit"   -> isOver = true
                "add"    -> doAdd()
                "remove" -> doRemove()
                "import" -> doImport("")
                "export" -> doExport("")
                "ask"    -> doAsk()
                "log"    -> doLog()
                "hardest card" -> doHardestCard()
                "reset stats" -> doResetStats()
            }
        }
        println("Bye bye!")
        if(exportfilename.length > 0)
            doExport(exportfilename)

    }

    fun doAdd() {
        println("The card:")
        var isTermAlreadyExist = true
        var term = ""
        while(isTermAlreadyExist) {
            term = readln()
            isTermAlreadyExist = findByTerm(term) != -1
            if(isTermAlreadyExist) {
                println("The card \"$term\" already exists.")
                return
            }
        }
        println("The definition of the card:")
        var definition = ""
        var isDefinitionAlreadyExist = true
        while(isDefinitionAlreadyExist) {
            definition = readln()
            isDefinitionAlreadyExist = findByDefinition(definition) != -1
            if(isDefinitionAlreadyExist) {
                isDefinitionAlreadyExist = true
                println("The definition \"$definition\" already exists.")
                return
            }
        }
        _cards.add( Card(term, definition) )
        println("The pair (\"$term\":\"$definition\") has been added.")
    }
    fun doRemove() {
        println("Which card?")
        val term = readln()
        val index = findByTerm(term)
        if(index != -1) {
            _cards.removeAt(index)
            println("The card has been removed.")
        } else {
            println("Can't remove \"$term\": there is no such card.")
        }
    }

    fun doExport(filename0 : String) {
        var filename = filename0
        if(filename.length == 0) {
            println("File name:")
            filename = readln()
        }
        val count = _cards.size
        var fileContent = ""
        for(c in _cards) {
            fileContent += c.term + "\t" + c.definition + "\t" + c.errors + "\t" + c.ok + "\n"
        }
        File(filename).writeText(fileContent)
        println("$count cards have been saved.")
    }

    fun doImport(filename0 : String) {
        var filename = filename0
        if(filename.length == 0) {
            println("File name:")
            filename = readln()
        }
        if(!File(filename).exists()) {
            println("File not found.")
            return
        }
        var term = ""
        var definition = ""
        var count = 0
        for(line in File(filename).readLines()) {
            val td = line.split("\t")
            val term = td[0]
            val definition = td[1]
            val error = td[2].toInt()
            val ok = td[3].toInt()
            val c = Card(term, definition)
            c.errors = error
            c.ok = ok
            val existCardIndex = findByTerm(term)
            if(existCardIndex != -1)
                _cards[existCardIndex] = c
            else
                _cards.add(c)
            count++
        }
        println("$count cards have been loaded.")
    }

    fun doAsk() {
        println("How many times to ask?")
        val n = readln().toInt()
        repeat(n) {
            val i = Random.nextInt(0.._cards.size-1)
            val term = _cards[i].term
            val definition = _cards[i].definition
            println("Print the definition of \"$term\":")
            val user = readln()
            if(definition == user) {
                _cards[i].ok++
                println("Correct!")
            } else {
                _cards[i].errors++
                val existTermIndex = findByDefinition(user)
                if(existTermIndex != -1) {
                    val existTerm = _cards[existTermIndex].term
                    println("Wrong. The right answer is \"$definition\", but your definition is correct for \"$existTerm\".")
                } else {
                    println("Wrong. The right answer is \"$definition\".")
                }
            }
        }
    }

    fun doHardestCard() {
        if(_cards.size == 0) {
            println("There are no cards with errors.")
            return
        }
        var hardest = _cards[0]
        for(c in _cards) {
            if(c.errors > hardest.errors) {
                hardest = c
            }
        }
        if(hardest.errors == 0) {
            println("There are no cards with errors.")
            return
        }
        var manuHardest = false;
        var hardestString = ""
        for(c in _cards) {
            if(c.errors == hardest.errors) {
                if(hardestString.length > 0) {
                    manuHardest = true
                    hardestString += ", "
                }
                hardestString += "\"" + c.term + "\""
            }
        }
        val errors = hardest.errors
        if(manuHardest) {
            println("The hardest cards are $hardestString. You have $errors errors answering them.")
        } else {
            println("The hardest card is $hardestString. You have $errors errors answering it.")
        }
    }

    fun doLog() {
        println("File name:")
        val filename = readln()
        File(filename).writeText(_log)
        println("The log has been saved.")
    }

    fun doResetStats() {
        for(c in _cards) {
            c.errors = 0
            c.ok = 0
        }
        println("Card statistics have been reset.")
    }

    fun findByTerm(term:String) : Int {
        for(j in 0.._cards.size-1) {
            if(term == _cards[j].term) {
                return j
            }
        }
        return -1
    }
    fun findByDefinition(definition: String) : Int {
        for(j in 0.._cards.size-1) {
            if(definition == _cards[j].definition) {
                return j
            }
        }
        return -1
    }

    fun println(s : String) {
        _log += s + "\n"
        System.out.println(s)
    }
    fun readln() : String {
        val res = readLine() ?: ""
        //LineReader.readLine(System.`in`, Charset.defaultCharset())        val res = System.console().readLine()
        _log += res + "\n"
        return res
    }
}


fun main(args:Array<String>) {
    var importfilename = ""
    var exportfilename = ""
    if(args.size > 0) {
        var command = ""
        for(arg in args) {
            if(arg == "-import" || arg == "-export")
                command = arg.substring(1)
            else {
                if(command == "import")
                    importfilename = arg
                if(command == "export")
                    exportfilename = arg
                command = ""
            }
        }
    }
    val fc = FlashCards(importfilename, exportfilename)
    fc.interact()
}
