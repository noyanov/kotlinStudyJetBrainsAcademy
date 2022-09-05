package processor
class Matrix {
    var columns:Int
    var rows:Int
    var data:Array<Double>

    constructor() {
        columns=0
        rows=0
        data = arrayOf<Double>()
    }
    constructor(rows:Int, columns:Int, v:Double) {
        this.rows = rows
        this.columns = columns
        data = Array<Double>(rows*columns) { v }
    }
    constructor(A:Matrix, B:Matrix, op: (Double, Double) -> Double) {
        rows = A.rows
        columns = A.columns
        data = Array<Double>(rows*columns) { 0.0 }
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                set(x, y, op(A.get(x,y), B.get(x,y)))
            }
        }
    }

    constructor(A:Matrix, K:Double) {
        rows = A.rows
        columns = A.columns
        data = Array<Double>(rows*columns) { 0.0 }
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                set(x, y, A.get(x,y) * K)
            }
        }
    }

    constructor(A:Matrix, B:Matrix) {
        rows = A.rows
        columns = B.columns
        data = Array<Double>(rows*columns) { 0.0 }
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                var c = 0.0
                for(i in 0..B.rows-1)
                    c += A.get(i, y)*B.get(x, i)
                set(x, y, c)
            }
        }
    }

    fun transpose(type:Int) {
        val m2 = Matrix(columns, rows, 0.0)
        for(y in 0..rows-1) {
            for (x in 0..columns - 1) {
                when(type) {
                    1 -> m2.set(y, x, get(x, y));
                    2 -> m2.set(rows-y-1, columns-x-1, get(x, y))
                    3 -> m2.set(columns-x-1, y, get(x,y))
                    4 -> m2.set(x, rows-y-1, get(x, y))
                }
            }
        }
        data = m2.data
        rows = m2.rows
        columns = m2.columns
    }

    private fun submatrix(n: Int, x0:Int, y0:Int) : Matrix {
    //println("submatrix: n=$n x0=$x0 y0=$y0")
    val m2 = Matrix(n-1, n-1, 0.0)
        for(y in 0..m2.rows-1) {
            var xx = 0
            for (x in 0..columns-1) {
                if (x != x0) {
                    m2.set(xx, y, get(x, y0+y))
                    xx++
                }
            }
        }
        return m2
    }
    fun determinant() : Double {
        if (rows == 2) {
            return get(0,0)*get(1,1) - get(1,0) * get(0,1);
        }
        var det = 0.0;
        val n = rows
        for (x in 0..n-1) {
            val sign = (if(x % 2 == 0) 1 else -1)
            det += sign * get(x,0) * submatrix(n, x, 1).determinant();
        }
        return det;
    }

    fun minor(x:Int, y:Int) : Matrix {
        val M = Matrix(rows-1, columns-1, 0.0)
        var yyy=0
        for(yy in 0..rows-1) {
            if(yy != y) {
                var xxx=0
                for(xx in 0..columns-1) {
                    if (xx != x) {
                        M.set(xxx, yyy, get(xx, yy))
                        xxx++
                    }
                }
                yyy++
            }
        }
        return M
    }

    fun adjacent() : Matrix {
        val AA = Matrix(rows, columns, 0.0)
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                val M = minor(x, y)
                val sign = if((x+y) % 2 == 0) 1.0 else -1.0
                AA.set(x, y,M.determinant()*sign)
            }
        }
        return AA
    }

    fun read(name:String) {
        print("Enter size of$name matrix:")
        val rc:List<String> = readln().split(" ")
        rows = rc[0].toInt()
        columns = rc[1].toInt()
        data = Array<Double>(rows*columns) { 0.0 }
        println("Enter$name matrix:")
        for(y in 0..rows-1) {
            val l = readln().split(" ")
            for(x in 0..columns-1) {
                set(x, y, l[x].toDouble())
            }
        }
    }

    fun get(x:Int, y:Int) : Double {
        return data[x+y*columns]
    }
    fun set(x:Int, y:Int, v:Double) {
        if(x+y*columns >= data.size) {
            println("Error: set x=$x y=$y. columns=$columns rows=$rows")
        }
        data[x+y*columns] = v
    }

    fun printInt() {
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                print(get(x, y).toInt())
                if(x != columns-1)
                    print(" ")
            }
            println("")
        }
    }
    fun printDouble() {
        for(y in 0..rows-1) {
            for(x in 0..columns-1) {
                print(get(x, y))
                if(x != columns-1)
                    print(" ")
            }
            println("")
        }
    }

}

class Matrixes {
    fun work() {
        var isOver = false
        while(!isOver) {
            print(
                "1. Add matrices\n" +
                "2. Multiply matrix by a constant\n" +
                "3. Multiply matrices\n" +
                "4. Transpose matrix\n" +
                "5. Calculate a determinant\n" +
                "6. Inverse matrix\n" +
                "0. Exit\n" +
                "Your choice:"
            )
            val action = readln().toInt()
            if(action == 0) {
                isOver = true
                break
            }
            when(action) {
                1 -> {
                    val m1 = Matrix()
                    m1.read(" first")
                    val m2 = Matrix()
                    m2.read(" second")
                    val m3 = Matrix(m1, m2, Double::plus)
                    println("The result is:")
                    m3.printDouble()
                }
                2 -> {
                    val m1 = Matrix()
                    m1.read("")
                    print("Enter constant:")
                    val K = readln().toDouble()
                    val m3 = Matrix(m1, K)
                    println("The result is:")
                    m3.printDouble()
                }
                3 -> {
                    val m1 = Matrix()
                    m1.read(" first")
                    val m2 = Matrix()
                    m2.read(" second")
                    val m3 = Matrix(m1, m2)
                    println("The result is:")
                    m3.printDouble()
                }
                4 -> {
                    print("1. Main diagonal\n" +
                            "2. Side diagonal\n" +
                            "3. Vertical line\n" +
                            "4. Horizontal line\n" +
                            "Your choice:")
                    val answer2 = readln().toInt()
                    val m1 = Matrix()
                    m1.read("")
                    m1.transpose(answer2);
                    println("The result is:")
                    m1.printDouble()
                }
                5 -> {
                    val m1 = Matrix()
                    m1.read("")
                    println("The result is:")
                    println(m1.determinant())
                }
                6 -> {
                    val A = Matrix()
                    A.read("")
                    val det1 = 1/A.determinant()
                    val C = A.adjacent()
                    C.transpose(1)
                    val m2 = Matrix(C, det1)
                    println("The result is:")
                    m2.printDouble()
                }
            }


        }
    }
}

fun main() {
    val m = Matrixes()
    m.work()
}
