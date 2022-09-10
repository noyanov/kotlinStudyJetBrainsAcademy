package gitinternals

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.time.Instant
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

fun toHex(h:Int) : String {
//    if(h > 256)
//        println("" + h + " " + (256*256) + " " + (h-256) + " " + (h-255*256))
    val hh = if(h > 256) (h-65533)+193 else h
    var hs = Integer.toHexString(hh)
    if(hh < 16)
        hs = "0" + hs
    return hs
}

fun indexOf(bytes:ByteArray, v:Byte, begin:Int) : Int {
    for(i in begin..bytes.size-1) {
        if(bytes[i] == v)
            return i
    }
    return -1
}

fun outputFile(file : File) {
    val iis = InflaterInputStream(FileInputStream(file))
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val sbytes = bytes.clone()
    for(i in 0.. sbytes.size-1)
        if(sbytes[i].toInt() == 0)
            sbytes[i] = '\n'.code.toByte()

    val s = String(sbytes, Charsets.US_ASCII)

    val s1 = s.substring(0, s.indexOf("\n"))
    val s2 = s.substring(s.indexOf("\n"))
    val sss = s2.split("\n")
    val ss = s1.split(" ")
    //println(s)
    val type = ss[0]
    val length = ss[1]
    //println("type:$type length:$length")
    if(type == "blob") {
        println("*BLOB*")
        for(s in sss) {
            if(s.length > 0)
                println(s)
        }
    } else if(type == "tree") {
        println("*TREE*")
        val begin = bytes.indexOf(0.toByte()) + 1
        var j = begin
        while(j < bytes.size) {
            val endofpermission = indexOf(bytes, ' '.toByte(), j)
            val permission = String(bytes, j, endofpermission-j)
            j = endofpermission+1
            val endoffilename = indexOf(bytes, 0.toByte(), j)
            val filename = String(bytes, j, endoffilename-j)
            j = endoffilename+1
            var hashs = ""
            for(k in 0..19) {
                val b = bytes[j + k].toInt() and 255
                hashs += toHex(b)
            }
            j += 20
            println(permission + " " + hashs + " " + filename)
        }
    } else {
        println("*COMMIT*")
        var isMessage = false
        var parents = ""
        for(s in sss) {
            if(s.length > 0) {
                val pref = if(s.indexOf(" ") > 0) s.substring(0, s.indexOf(" ")).trim() else ""
                val value = if(s.indexOf(" ") > 0) s.substring(s.indexOf(" ")).trim() else s.trim()
                when(pref) {
                    "tree" ->
                        println("tree: "+value)
                    "parent" -> {
                        if(parents.length > 0)
                            parents += " | "
                        parents += value
                    }
                    "author" -> {
                        if(parents.length > 0)
                            println("parents: " + parents)
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        println("author: "+dt[0]+" "+ mail +" original timestamp: " + date + " " + time + " " + zone)
                    }
                    "committer" -> {
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        println("committer: "+dt[0]+" "+mail+" commit timestamp: " + date + " " + time + " " + zone)
                    }
                    else -> {
                        if(!isMessage) {
                            isMessage = true
                            println("commit message:")
                        }
                        println(s)
                    }
                }
            }
        }
    }
}


fun outputFile2(file : File, commit:String, merged:String) : String {
    var parents = ""
    val iis = InflaterInputStream(FileInputStream(file))
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val sbytes = bytes.clone()
    for(i in 0.. sbytes.size-1)
        if(sbytes[i].toInt() == 0)
            sbytes[i] = '\n'.code.toByte()

    val s = String(sbytes, Charsets.US_ASCII)

    val s1 = s.substring(0, s.indexOf("\n"))
    val s2 = s.substring(s.indexOf("\n"))
    val sss = s2.split("\n")
    val ss = s1.split(" ")
    //println(s)
    val type = ss[0]
    val length = ss[1]
    //println("type:$type length:$length")
    if(type == "blob") {
        println("*BLOB*")
        for(s in sss) {
            if(s.length > 0)
                println(s)
        }
    } else if(type == "tree") {
        println("*TREE*")
        val begin = bytes.indexOf(0.toByte()) + 1
        var j = begin
        while(j < bytes.size) {
            val endofpermission = indexOf(bytes, ' '.toByte(), j)
            val permission = String(bytes, j, endofpermission-j)
            j = endofpermission+1
            val endoffilename = indexOf(bytes, 0.toByte(), j)
            val filename = String(bytes, j, endoffilename-j)
            j = endoffilename+1
            var hashs = ""
            for(k in 0..19) {
                val b = bytes[j + k].toInt() and 255
                hashs += toHex(b)
            }
            j += 20
            println(permission + " " + hashs + " " + filename)
        }
    } else {
        println("Commit: " + commit + merged)
        var isMessage = false
        //println(sss)
        for(s in sss) {
            if(s.length > 0) {
                val pref = if(s.indexOf(" ") > 0) s.substring(0, s.indexOf(" ")).trim() else ""
                val value = if(s.indexOf(" ") > 0) s.substring(s.indexOf(" ")).trim() else s.trim()
                when(pref) {
                    "tree" -> {
//                        println("tree: " + value)
                    }
                    "parent" -> {
                        if(parents.length > 0)
                            parents += " | "
                        parents += value
                    }
                    "author" -> {
                        //println(parents)
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        //println("author: "+dt[0]+" "+ mail +" original timestamp: " + date + " " + time + " " + zone)
                    }
                    "committer" -> {
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        println(dt[0]+" "+mail+" commit timestamp: " + date + " " + time + " " + zone)
                    }
                    else -> {
                        if(!isMessage) {
                            isMessage = true
                            //println(s)
                        }
                        println(s)
                    }
                }
            }
        }
    }
    println("")
    return parents;
}


fun outputFile3(git_directory: String, git_hash:String, folder:String) {
    val file = getFileForHash(git_directory, git_hash)
    val iis = InflaterInputStream(FileInputStream(file))
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val sbytes = bytes.clone()
    for(i in 0.. sbytes.size-1)
        if(sbytes[i].toInt() == 0)
            sbytes[i] = '\n'.code.toByte()

    val s = String(sbytes, Charsets.US_ASCII)

    val s1 = s.substring(0, s.indexOf("\n"))
    val s2 = s.substring(s.indexOf("\n"))
    val sss = s2.split("\n")
    val ss = s1.split(" ")
    //println(s)
    val type = ss[0]
    val length = ss[1]
    //println("type:$type length:$length")
    if(type == "blob") {
        println("*BLOB*")
        for(s in sss) {
            if(s.length > 0)
                println(s)
        }
    } else if(type == "tree") {
        //println(s)
        //println("*TREE*")
        val begin = bytes.indexOf(0.toByte()) + 1
        var j = begin
        while(j < bytes.size) {
            val endofpermission = indexOf(bytes, ' '.toByte(), j)
            val permission = String(bytes, j, endofpermission-j)
            j = endofpermission+1
            val endoffilename = indexOf(bytes, 0.toByte(), j)
            val filename = String(bytes, j, endoffilename-j)
            j = endoffilename+1
            var hashs = ""
            for(k in 0..19) {
                val b = bytes[j + k].toInt() and 255
                hashs += toHex(b)
            }
            j += 20
            if(permission == "40000") {
                outputFile3(git_directory, hashs, filename)
            } else {
                if(folder.length > 0)
                    print(folder + "/")
                println(filename)
            }
        }
    } else {
        println("*COMMIT*")
        var isMessage = false
        var parents = ""
        for(s in sss) {
            if(s.length > 0) {
                val pref = if(s.indexOf(" ") > 0) s.substring(0, s.indexOf(" ")).trim() else ""
                val value = if(s.indexOf(" ") > 0) s.substring(s.indexOf(" ")).trim() else s.trim()
                when(pref) {
                    "tree" ->
                        println("tree: "+value)
                    "parent" -> {
                        if(parents.length > 0)
                            parents += " | "
                        parents += value
                    }
                    "author" -> {
                        if(parents.length > 0)
                            println("parents: " + parents)
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        println("author: "+dt[0]+" "+ mail +" original timestamp: " + date + " " + time + " " + zone)
                    }
                    "committer" -> {
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        println("committer: "+dt[0]+" "+mail+" commit timestamp: " + date + " " + time + " " + zone)
                    }
                    else -> {
                        if(!isMessage) {
                            isMessage = true
                            println("commit message:")
                        }
                        println(s)
                    }
                }
            }
        }
    }
}


fun getCurrentBranch(git_directory: String) : String {
    val file = File(File(git_directory), "HEAD")
    val iis = FileInputStream(file)
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val s = String(bytes)
    val cur = s.substring(s.lastIndexOf("/")+1).replace("\n", "")
    return cur
}

fun getListOfBranches(git_directory:String) : Array<String> {
    val file:File = File(File(git_directory), "refs/heads")
    val branches:Array<String> = file.list()
    return branches
}

fun getBranchId(git_directory: String, branch:String) : String {
    val file = File(File(File(git_directory), "refs/heads"), branch)
    val iis = FileInputStream(file)
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val s = String(bytes).replace("\n", "")
    return s
}

fun printObject(git_directory:String, git_hash:String) {
    //var parent = git_hash;
    //while(parent.length > 0) {
        val foldername = git_hash.substring(0, 2)
        val objects = File(File(git_directory, "objects"), foldername)
        val hash = git_hash.substring(2)
        val file = File(objects, hash)
        outputFile(file)
    //}
}

fun printObjects2(git_directory:String, git_hash:String, merged:String, commits:MutableList<String>) : String
{
    var parent = git_hash;
    if(git_hash.length < 2)
        return "";
    if(commits.indexOf(git_hash) != -1)
        return ""// already printed
    commits.add(git_hash)
    val foldername = parent.substring(0, 2)
    val objects = File(File(git_directory, "objects"), foldername)
    val hash = parent.substring(2)
    val file = File(objects, hash)

    val newparents = outputFile2(file, parent, merged)
    return newparents
}


fun log(git_directory:String, git_hash:String)
{
    val commits = mutableListOf<String>()
    var parent = git_hash;
    while(parent.length > 0) {
        val parents = parent.split(" | ")
        var newparents = ""
        for(i in parents.size-1 downTo 0) {
            val p = parents[i]
            val merged2 = if(i != 0) " (merged)" else ""
            val newparents2 = printObjects2(git_directory, p, merged2, commits)
            if(i == 0)
                newparents = newparents2
        }
        parent = newparents
    }
}

fun getTreeForHash(file:File) : String
{
    var tree = ""
    val iis = InflaterInputStream(FileInputStream(file))
    val bytes:ByteArray = iis.readAllBytes()
    iis.close()
    val sbytes = bytes.clone()
    for(i in 0.. sbytes.size-1)
        if(sbytes[i].toInt() == 0)
            sbytes[i] = '\n'.code.toByte()

    val s = String(sbytes, Charsets.US_ASCII)

    val s1 = s.substring(0, s.indexOf("\n"))
    val s2 = s.substring(s.indexOf("\n"))
    val sss = s2.split("\n")
    val ss = s1.split(" ")
    //println(s)
    val type = ss[0]
    val length = ss[1]
    //println("type:$type length:$length")
    if(type == "blob") {
        println("*BLOB*")
        for(s in sss) {
            if(s.length > 0)
                println(s)
        }
    } else if(type == "tree") {
        println("*TREE*")
        val begin = bytes.indexOf(0.toByte()) + 1
        var j = begin
        while(j < bytes.size) {
            val endofpermission = indexOf(bytes, ' '.toByte(), j)
            val permission = String(bytes, j, endofpermission-j)
            j = endofpermission+1
            val endoffilename = indexOf(bytes, 0.toByte(), j)
            val filename = String(bytes, j, endoffilename-j)
            j = endoffilename+1
            var hashs = ""
            for(k in 0..19) {
                val b = bytes[j + k].toInt() and 255
                hashs += toHex(b)
            }
            j += 20
            println(permission + " " + hashs + " " + filename)
        }
    } else {
        //println("*COMMIT*")
        var isMessage = false
        var parents = ""
        for(s in sss) {
            if(s.length > 0) {
                val pref = if(s.indexOf(" ") > 0) s.substring(0, s.indexOf(" ")).trim() else ""
                val value = if(s.indexOf(" ") > 0) s.substring(s.indexOf(" ")).trim() else s.trim()
                when(pref) {
                    "tree" -> {
                        tree = value
                        //println("tree: " + value)
                    }
                    "parent" -> {
                        if(parents.length > 0)
                            parents += " | "
                        parents += value
                    }
                    "author" -> {
                        //if(parents.length > 0)
                        //    println("parents: " + parents)
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        //println("author: "+dt[0]+" "+ mail +" original timestamp: " + date + " " + time + " " + zone)
                    }
                    "committer" -> {
                        val dt = value.split(" ")
                        val secs = (dt[3].toDouble()*3600/100.0).toLong()
                        val inst = Instant.ofEpochSecond(dt[2].toLong()+secs, 0)
                        val timestamp = inst.toString()
                        val date = timestamp.substring(0, timestamp.indexOf("T"))
                        val time = timestamp.substring(timestamp.indexOf("T")+1).replace("Z", "")
                        val zone = dt[3].substring(0,3)+":"+dt[3].substring(3)
                        val mail = dt[1].replace("<", "").replace(">", "")
                        //println("committer: "+dt[0]+" "+mail+" commit timestamp: " + date + " " + time + " " + zone)
                    }
                    else -> {
                        if(!isMessage) {
                            isMessage = true
                            //println("commit message:")
                        }
                        //println(s)
                    }
                }
            }
        }
    }
    return tree
}

fun getFileForHash(git_directory: String, git_hash:String) : File {
    val foldername = git_hash.substring(0, 2)
    val objects = File(File(git_directory, "objects"), foldername)
    val hash = git_hash.substring(2)
    val file = File(objects, hash)
    return file
}

fun logFiles(git_directory: String, git_hash:String) {
    val commits = mutableListOf<String>()
    val file = getFileForHash(git_directory, git_hash)
    val tree = getTreeForHash(file)
    outputFile3(git_directory, tree, "")
}

fun main() {
    println("Enter .git directory location:")
    var git_directory = readln()
    //git_directory += "/.git"
    //git_directory = "/Users/ynoyanov/Work/kotlinStudyJetBrainsAcademy/.git"
    println("Enter command:")
    val command = readln()
    if(command == "list-branches") {
        val currentBranch = getCurrentBranch(git_directory)
        val listOfBranches = getListOfBranches(git_directory)
        for(b in listOfBranches) {
            if(b != currentBranch) {
                print("  ")
                println(b)
            }
        }
        println("* "+currentBranch)
        return
    } else if(command == "log") {
        println("Enter branch name:")
        val branch = readln()
        val bid = getBranchId(git_directory, branch)
        log(git_directory, bid)
    } else if(command == "commit-tree") {
        println("Enter commit-hash:")
        val hash = readln()
        logFiles(git_directory, hash)
    } else if(command == "cat-file") {
        println("Enter git object hash:")
        var git_hash = readln()
        //git_hash = "019a134bf6b55e14b664716484ec095cfbb13115"// "009dace518703ffaa4fe179985848426779e61de"
        printObject(git_directory, git_hash)
    }
}
