package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import kotlinx.datetime.TimeZone
import java.io.File
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : JsonAdapter<LocalDateTime>() {
    override fun toJson(writer: JsonWriter, value: LocalDateTime?) {
        //value?.let { writer?.value(it.format(Locale.US, formatter)) }
        value?.let { writer?.value(it.toString()) }
    }
    override fun fromJson(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }    }
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    //private fun fromNonNullString(nextString: String) : LocalDateTime = LocalDateTime.parse(nextString, formatter)
    private fun fromNonNullString(nextString: String) : LocalDateTime = LocalDateTime.parse(nextString)
}

class LocalDateTimeAdapter2 {
    @ToJson
    fun toJson(value: LocalDateTime): String {
        return value.toString()
    }

    @FromJson
    fun fromJson(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }

}


data class Task(var priority:String, var datetime:LocalDateTime, var task:String) {

}
class TaskList {
    var _tasks = mutableListOf<Task>()
    fun work() {
        doLoad()
        while(true) {
            println("Input an action (add, print, edit, delete, end):")
            val action = readln()
            when (action) {
                "add"   -> doAdd()
                "print" -> doPrint()
                "edit"  -> doEdit()
                "delete"-> doDelete()
                "save"  -> doSave()
                "load"  -> doLoad()
                "end"   -> {
                    println("Tasklist exiting!")
                    break
                }
                else    -> {
                    println("The input action is invalid")
                }
            }
        }
        doSave()
    }

    fun readPriority() : String {
        var priority = ""
        while (true) {
            println("Input the task priority (C, H, N, L):")
            priority = readln().uppercase().trim()
            if (setOf("C", "H", "N", "L").contains(priority))
                break
        }
        return priority
    }
    fun readDate() : LocalDate {
        var date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val dates = readln().trim()
            val date2 = toDate(dates)
            if(date2 != null) {
                date = date2
                break
            } else {
                println("The input date is invalid")
            }
        }
        return date
    }
    fun readTime(date:LocalDate) : LocalDateTime {
        var time = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0"))
        while(true) {
            println("Input the time (hh:mm):")
            val times = readln().trim()
            val time2 = toDateTime(date, times)
            if(time2 != null) {
                time = time2
                break
            } else {
                println("The input time is invalid")
            }
        }
        return time
    }
    fun readTaskLines() : String {
        println("Input a new task (enter a blank line to end):")
        var nonBlank = false
        var task = ""
        while(true) {
            val taskline = readln().trim()
            if(taskline.length == 0) {
                break
            }
            if(task.length > 0)
                task += "\n"
            task += taskline
        }
        return task
    }

    private fun doAdd() {
        val priority = readPriority()
        val date = readDate()
        val time = readTime(date)
        val task = readTaskLines()
        if(task.length > 0) {
            _tasks.add(Task(priority, time, task))
        } else {
            println("The task is blank")
        }
    }

    private fun doPrint() {
        if(_tasks.size > 0) {
            println("+----+------------+-------+---+---+--------------------------------------------+\n" +
                    "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                    "+----+------------+-------+---+---+--------------------------------------------+")
            for(i in 0.._tasks.size-1) {
                val task = _tasks[i]
                var number = ""+(i+1)
                if(i+1 < 10)
                    number += " "
                val dts = String.format("%04d", task.datetime.year)+
                        "-"+String.format("%02d", task.datetime.monthNumber)+
                        "-"+String.format("%02d", task.datetime.dayOfMonth)
                val tts = String.format("%02d", task.datetime.hour)+
                        ":"+String.format("%02d", task.datetime.minute)
                //val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val now = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
                val red = "\u001B[101m \u001B[0m"
                val yellow = "\u001B[103m \u001B[0m"
                val green = "\u001B[102m \u001B[0m"
                val blue = "\u001B[104m \u001B[0m"
                val daysUntil = if(task.datetime.date == now) "T" else if(task.datetime.date.compareTo(now) > 0) "I" else "O"
                //println(" "+dts+" "+task.priority+" "+daysUntil)
                val tasklines = task.task.split("\n")
                val c1 = when(task.priority) {
                    "C" -> red
                    "H" -> yellow
                    "N" -> green
                    "L" -> blue
                    else -> ""
                }
                val c2 = if(task.datetime.date == now) yellow else if(task.datetime.date.compareTo(now) > 0) green else red

                var isHeader = true
                for(j in 0..tasklines.size-1) {
                    var line = tasklines[j]
                    while(line.length > 0) {
                        var str = ""
                        if(line.length > 44) {
                            str = line.substring(0, 44)
                            line = line.substring(44)
                        } else {
                            str = line
                            line = ""
                            while(str.length < 44)
                                str += " "
                        }
                        if(isHeader) {
                            println("| $number | $dts | $tts | $c1 | $c2 |$str|")
                            isHeader = false
                        } else {
                            println("|    |            |       |   |   |$str|")
                        }
                    }
                    //println("   "+tasklines[j])
                }
                println("+----+------------+-------+---+---+--------------------------------------------+")
                //println("")
            }
        } else {
            println("No tasks have been input")
        }
    }

    private fun doEdit() {
        if(_tasks.size > 0) {
            val number = readTaskNumber()

                var field = ""
                while(true) {
                    println("Input a field to edit (priority, date, time, task):")
                    field = readln().trim()
                    if(listOf<String>("priority", "date", "time", "task").contains(field))
                        break
                    println("Invalid field")
                }
                val task = _tasks[number-1]
                    when(field) {
                        "priority" -> task.priority = readPriority()
                        "date" -> {
                            val date = readDate()
                            task.datetime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, task.datetime.hour, task.datetime.minute)
                        }
                        "time" -> {
                            val datetime = readTime(task.datetime.date)
                            task.datetime = datetime
                        }
                        "task" -> {
                            val tasklines = readTaskLines()
                            task.task = tasklines
                        }
                        else -> {

                        }
                }
                println("The task is changed")
        } else {
            println("No tasks have been input")
        }
    }

    fun readTaskNumber() : Int {
        doPrint()
        while(true) {
            val count = _tasks.size
            println("Input the task number (1-$count):")
            try {
                val number = readln().toInt()
                if (number >= 1 && number <= _tasks.size)
                    return number
            } catch(e: NumberFormatException) { }
            println("Invalid task number")
        }
    }

    private fun doDelete() {
        if(_tasks.size > 0) {
            val number = readTaskNumber()
            _tasks.removeAt(number-1)
            println("The task is deleted")
        } else {
            println("No tasks have been input")
        }
    }

    fun doLoad() {
        val file = file()
        var tasks:List<Task>? = null
        if(file.exists()) {
            val s = String(file.readBytes())
            tasks = moshi().fromJson(s)
        }
        if(tasks != null)
            _tasks = tasks.toMutableList<Task>()
        else
            _tasks = mutableListOf<Task>()

    }

    fun doSave() {
        val taskjson = moshi().toJson(_tasks)
        val file = file()
        file.writeText(taskjson)
    }

    fun file() : File {
        val jsonFile = File("tasklist.json")
        return jsonFile
    }
    fun moshi() : JsonAdapter<List<Task>> {
        val moshi = Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()) //KotlinJsonAdapterFactory())
            .add(LocalDateTimeAdapter2())
            .build()
        val type = Types.newParameterizedType(List::class.java, Task::class.java)
        val taskListAdapter = moshi.adapter<List<Task>>(type)
        return taskListAdapter
    }

    fun datetime() {
        // Create a LocalDate instance for 2017-4-29
        val date = LocalDate(2017, 4, 29)

        //Create a LocalDateTime instance for 2021-12-21 16:57
        val dateTime = LocalDateTime(2021, 12, 31, 16, 57)
        val year = dateTime.year         // Get year as an integer
        val month = dateTime.monthNumber // Get month as an integer
        val day = dateTime.dayOfMonth    // Get day as an integer
        val hour = dateTime.hour         // Get hour as an integer
        val minutes = dateTime.minute    // Get minutes as an integer

        // Create a LocalDateTime instance for the current date and time for UTC+0 timezone
        val dateTimeCurrent = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0"))
        // Create a LocalDate instance for the current date and time for UTC+0 timezone
        val dateCurrent = dateTimeCurrent.date

        // Get the date and time as string
        val dateTimeString = dateTime.toString()
        // Print the string above
        println(dateTimeString)  // Output 2021-12-31T16:57

        // Same result as the above
        println(dateTime)        // Output 2021-12-31T16:57
    }

    fun toDate(date:String) : LocalDate? {
        val items = date.split("-")
        if(items.size != 3)
            return null
//        if(items[0].toInt() < 1000 || items[0].toInt() > 3000)
//            return null
//        if(items[1].toInt() < 1 || items[1].toInt() > 12)
//            return null
//        val maxDays = arrayOf( 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 )
//        if(items[2].toInt() < 1 || items[2].toInt() > maxDays[items[1].toInt()])
//            return null
        try {
            val res = LocalDate(items[0].toInt(), items[1].toInt(), items[2].toInt())
            return res
        } catch(e:IllegalArgumentException) {
            return null
        }
    }
    fun toDateTime(date:LocalDate, times:String) : LocalDateTime? {
        val items = times.split(":")
        if(items.size != 2)
            return null
        if(items[0].toInt() < 0 || items[0].toInt() > 23)
            return null
        if(items[1].toInt() < 0 || items[1].toInt() > 59)
            return null
        return LocalDateTime(date.year, date.month, date.dayOfMonth, items[0].toInt(), items[1].toInt())
    }
}

fun main() {
    val tasklist = TaskList()
    tasklist.work()
}


