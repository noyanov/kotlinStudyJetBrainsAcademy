package phonebook

import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap

class PhoneBook {
    constructor(path:File) {
        loadData(path)
    }
    val fileNameData = "src/resources/directory.txt"
    val fileNameToFind = "src/resources/find.txt"
    lateinit var _data:List<String>
    lateinit var _finds:List<String>
    lateinit var _hash:HashMap<String,Int>
    var _blockSize = 1
    fun loadData(path:File) {
        _data = File(path, fileNameData).readLines()
        _finds = File(path, fileNameToFind).readLines()
        _blockSize = Math.sqrt(_data.size.toDouble()).toInt()

    }
    fun searchAll() : Pair<Int,Int> {
        var total = 0
        var found = 0
        _finds.forEach( {
            total++
            if(findIt(it) != -1)
                found++
        })
        return Pair(total, found)
    }
    fun findIt(s : String) : Int {
        for(i in 0.._data.size-1) {
            if(_data[i].indexOf(s) != -1)
                return i
        }
        return -1
    }

    fun searchBlock() : Pair<Int,Int> {
        var total = 0
        var found = 0
        _finds.forEach( {
            total++
            if(findItBlock(it) != -1)
                found++
        })
        return Pair(total, found)
    }
    fun findItBlock(s : String) : Int {
        var index = (s[0]-'a') * _blockSize
        if(index < 0)
            index = 0
        if(index >= _data.size)
            index = _data.size-1
        while(index > 0) {
            if(_data[index][0] < s[0])
                break
            index -= _blockSize
        }
        if(index < 0)
            index = 0
        for (i in index.._data.size - 1) {
            if (_data[i].indexOf(s) != -1)
                return i;
        }
        return -1;
    }

    fun searchBinary() : Pair<Int,Int> {
        var total = 0
        var found = 0
        _finds.forEach( {
            total++
            if(searchBinary(_data, it) != -1)
                found++
        })
        return Pair(total, found)
    }
    fun searchBinary(data:List<String>, value:String) : Int
    {
            var left = 0                            // the starting value of the left border
            var right = data.size - 1               // the starting value of the right border
            while (left <= right) {                 // while the left border is to the left
                                                    // of the right one (or if they match)
                val middle:Int = ((left+right)/2)   // finding the middle of the array (int removes
                                                    // the fractional part)
                if(data[middle].indexOf(value) != -1) {         // if the value from the middle of the array
                    // is equal to the target one
                    return middle                   // returning the index of this element
                } else {
                    val l1 = data[middle][data[middle].indexOf(' ')+1]
                    val l2 = value[0]
                    if(l1 > l2) {   // else if the value from the middle is greater
                        // than the target one
                        right = middle - 1
                    }                                   // setting a new value to the right border (the one
                                                        // to the left of the middle one)
                    else {                              // else (if the value from the middle is less than
                        // the target one)
                        left = middle + 1               // setting a new value to the left border (the one
                        // to the right of the middle one)
                    }
                }
            }
            return -1                           // if the value is not found, we return -1
    }

    fun searchHash() : Pair<Int,Int> {
        var total = 0
        var found = 0
        _finds.forEach( {
            total++
            if(_hash.contains(it))
                found++
        })
        return Pair(total, found)
    }







    fun bubbleSort(endtime:Long) : Boolean {
//        _data = _data.sortedWith({ x1, x2 ->
//            val l1 = x1[x1.indexOf(' ')+1]
//            val l2 = x2[x2.indexOf(' ')+1]
//            l1-l2
//        })
//        return true
        if(!bubbleSort2(_data, endtime))
            return false
        //bubbleSort(_finds, endtime)
        return true
    }
    fun bubbleSort2(data:List<String>, endtime:Long) : Boolean {
        var isSwapped = true
        var startIndex = 1
        while(isSwapped) {
            isSwapped = false
            for(i in startIndex..data.size-1) {
                if(System.currentTimeMillis() > endtime)
                    return false
                val lprev = data[i-1][data[i-1].indexOf(' ')+1]
                val lnext = data[i][data[i].indexOf(' ')+1]
                if(lprev > lnext) {
                    Collections.swap(data, i-1, i)
                    if(!isSwapped)
                        startIndex = if(i-1 >= 1) i-1 else 1
                    isSwapped = true
                }
            }
        }
        return true
    }

    fun DCSort(endtime:Long) : Boolean {
        if(!DCSort(_data, endtime, 0, _data.size-1))
            return false
        //bubbleSort(_finds, endtime)
        return true
    }
    fun DCSort(data:List<String>, endtime:Long, minI:Int, maxI:Int) : Boolean {
        if(System.currentTimeMillis() > endtime)
            return false
        if(maxI == minI+1) {
            if(data[minI] > data[maxI]) {
                Collections.swap(data, minI, maxI)
            }
            return true
        }
        DCSort(data, endtime, minI, maxI-1)
        if(data[maxI-1] > data[maxI])
            Collections.swap(data, maxI-1, maxI)
        return true
    }


    fun createHashTable() {
        _hash = HashMap<String, Int>()
        for(i in 0.._data.size-1) {
            val s = _data[i]
            val key = s.substring(s.indexOf(" ")+1)
            _hash[key] = i
        }
    }


}

fun outputTime(time:Long) {
    val ms = time % 1000
    val secs = (time/1000) % 60
    val mins = time/(60000)
    print("Time taken: $mins min. $secs sec. $ms ms.")
}

fun main() {
    var path = Paths.get("").toAbsolutePath().toString()
    println("Working Directory = $path")
    //if(BuildConfig.DEBUG) {
    //    path += "/Phone Book/task"
    //}
    val phonebook = PhoneBook(File(path))

    println("Start searching (linear search)...")
    val begin1 = System.currentTimeMillis()
    val (total1, found1) = phonebook.searchAll()
    val end1 = System.currentTimeMillis()
    val time1 = (end1 - begin1)*3
    print("Found $found1 / $total1 entries. ")
    outputTime(time1)
    println("")

    println("Start searching (bubble sort + jump search)...")
    val begin2 = System.currentTimeMillis()
    val isStopped = !phonebook.bubbleSort(begin2+time1*20)
    val end2 = System.currentTimeMillis()
    val time2 = end2 - begin2
    val begin3 = System.currentTimeMillis()
    val (total2, found2) = if(!isStopped) phonebook.searchBlock() else phonebook.searchAll()
    val end3 = System.currentTimeMillis()
    val time3 = end3 - begin3
    val time23 = end3 - begin2
    print("Found $found2 / $total2 entries. ")
    outputTime(time23)
    println("")
    print("Sorting time: ")
    outputTime(time2)
    if(isStopped)
        println("- STOPPED, moved to linear search")
    println("")
    print("Searching time: ")
    outputTime(time3)
    println("")
    phonebook.loadData(File(path))

    println("Start searching (quick sort + binary search)...")
    val begin4 = System.currentTimeMillis()
    val isStopped2 = !phonebook.bubbleSort(begin4+time1*20)
    val end4 = System.currentTimeMillis()
    val time4 = end4 - begin4
    val begin5 = System.currentTimeMillis()
    val (total3, found3) = if(!isStopped2) phonebook.searchBinary() else phonebook.searchAll()
    val end5 = System.currentTimeMillis()
    val time5 = end5 - begin5
    val time43 = end5 - begin4
    print("Found $found3 / $total3 entries. ")
    outputTime(time43)
    println("")
    print("Sorting time: ")
    outputTime(time4)
    if(isStopped2)
        println("- STOPPED, moved to linear search")
    println("")
    print("Searching time: ")
    outputTime(time5)
    println("")

    println("Start searching (hash table)...")
    val begin6 = System.currentTimeMillis()
    phonebook.createHashTable()
    val end6 = System.currentTimeMillis()
    val time6 = end6 - begin6
    val begin7 = System.currentTimeMillis()
    val (total4, found4) = phonebook.searchHash()
    val end7 = System.currentTimeMillis()
    val time7 = end7 - begin7
    val time67 = end7 - begin6
    print("Found $found4 / $total4 entries. ")
    outputTime(time67)
    println("Creating time: ")
    outputTime(time6)
    println("")
    print("Searching time: ")
    outputTime(time7)
    println("")
}
