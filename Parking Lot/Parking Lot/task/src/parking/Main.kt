package parking

class Car(var regnum:String, var color:String) {

}
class ParkingLot {
    val spots : Array<Car?>
    constructor(count:Int) {
        spots = Array<Car?>(count) { null }
    }

    fun command(inp:List<String>) : Boolean {
        val command = inp[0]
        if(command == "park") {
            val regnum = inp[1]
            val color = inp[2]
            commandPark(regnum, color)
        } else if(command == "leave") {
            val spot = inp[1].toInt()
            commandLeave(spot)
        } else if(command == "status") {
            commandStatus()
        } else if(command == "reg_by_color") {
            val color = inp[1]
            commandRegByColor(color)
        } else if(command == "spot_by_color") {
            val color = inp[1]
            commandSpotByColor(color)
        } else if(command == "spot_by_reg") {
            val reg = inp[1]
            commandSpotByReg(reg)
        } else if(command == "exit") {
            return false
        }
        return true
    }

    private fun commandPark(regnum:String, color:String) {
        val freeSpot = findFreeSpot()
        if(freeSpot == -1) {
            println("Sorry, the parking lot is full.")
            return
        }
        spots[freeSpot] = Car(regnum, color)
        println("$color car parked in spot ${freeSpot+1}.")
    }
    private fun commandLeave(spot:Int) {
        if(spots[spot-1] != null) {
            spots[spot-1] = null
            println("Spot $spot is free.")
        } else {
            println("There is no car in spot $spot.")
        }
    }
    private fun commandStatus() {
        var isEmpty = true
        for(i in 0..spots.size-1) {
            val car = spots[i]
            if(car != null) {
                println("" + (i+1) + " " + car.regnum + " " + car.color)
                isEmpty = false
            }
        }
        if(isEmpty) {
            println("Parking lot is empty.")
        }
    }
    private fun commandRegByColor(color: String) {
        var isNotFound = true
        var isFirstNum = true
        for(c in spots) {
            if(c != null && c.color.uppercase() == color.uppercase()) {
                if(!isFirstNum) {
                    print(", ")
                }
                print(c.regnum)
                isFirstNum = false
                isNotFound = false
            }
        }
        if(isNotFound) {
            println("No cars with color ${color.uppercase()} were found.")
        } else {
            println("")
        }
    }

    private fun commandSpotByReg(regnum: String) {
        for(i in 0..spots.size-1) {
            val c = spots[i]
            if(c != null && c.regnum.uppercase() == regnum.uppercase()) {
                println(i+1)
                return
            }
        }
        println("No cars with registration number ${regnum.uppercase()} were found.")
    }
    private fun commandSpotByColor(color: String) {
        var isNotFound = true
        var isFirst = true
        for(i in 0..spots.size-1) {
            val c = spots[i]
            if(c != null && c.color.uppercase() == color.uppercase()) {
                if(!isFirst) {
                    print(", ")
                }
                print(i+1)
                isFirst = false
                isNotFound = false
            }
        }
        if(isNotFound) {
            println("No cars with color ${color.uppercase()} were found.")
        } else {
            println("")
        }
    }

    private fun findFreeSpot() : Int {
        for(i in 0.. spots.size-1) {
            if(spots[i] == null)
                return i
        }
        return -1
    }
}

class PLFactory {
    companion object {
        fun createPL(spotCount: Int) : ParkingLot {
            return ParkingLot(spotCount)
        }
    }
}

fun main() {
    var PL : ParkingLot? = null
    var isWork = true
    while(isWork) {
        val inp = readln().split(" ")
        val command = inp[0]
        if(command == "create") {
            val spotCount = inp[1].toInt()
            PL = PLFactory.createPL(spotCount)
            println("Created a parking lot with $spotCount spots.")
        } else if(command == "exit") {
                isWork = false
                break
        } else {
            if(PL == null) {
                println("Sorry, a parking lot has not been created.")
            } else {
                if (!PL.command(inp)) {
                    break
                }
            }
        }
    }
}
