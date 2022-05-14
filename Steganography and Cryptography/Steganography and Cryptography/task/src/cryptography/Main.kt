package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import kotlin.experimental.and
import kotlin.experimental.xor

class CRYPTO {
    fun command() : Boolean {
        println("Task (hide, show, exit):")
        val cmd = readln()
        when(cmd) {
            "hide" -> commandHide()
            "show" -> commandShow()
            "exit" -> return false
            else -> println("Wrong task: $cmd")
        }
        return true
    }

    private fun commandHide() {
        println("Input image file:")
        val inputImageName = readln()
        println("Output image file:")
        val outputImageName = readln()
        println("Message to hide:")
        val message = readln()
        println("Password:")
        val password = readln()
        //println("Input Image: $inputImageName")
        //println("Output Image: $outputImageName")
        var outcount = 0
        val inputFile = File(inputImageName)
        try {
            val myImage: BufferedImage = ImageIO.read(inputFile)
            val data0 = message.encodeToByteArray()
            val mod = password.length
            for(i in 0..data0.size-1) {
                data0[i] = data0[i] xor (password[i % mod].toByte())
            }
            val end = byteArrayOf(0,0,3)
            val data = data0 + end
            if(data.size*8*4 < myImage.width*myImage.height) {
                var index = 0
                val V0:Byte = 0
                val V1:Byte = 1
                val V128 : Byte = -128
                var byteMask:Byte = V128
                var isOver = false
                for (y in 0 until myImage.height) {          // For every row
                    if(isOver) {
                        break
                    }
                    for (x in 0 until myImage.width) {               // For every column.
                        val color = Color(myImage.getRGB(x, y))  // Read color from the (x, y) position
                        val byteData = data[index]
                        val bitData : Int = if((byteData and byteMask) != V0) 1 else 0
                        val r = color.red             // Access the Green color value
                        val g = color.green           // Access the Green color value
                        val b = color.blue.and(254).or(bitData) % 256 // (color.blue and 1) or bitData             // Access the Blue color value
                        //val a = color.alpha              // Access the Blue color value
//                        if(outcount++ < 50) {
//                            println("byteData=" + byteData.toChar())
//                            println("byteMask=" + byteMask)
//                            println("bitData=" + bitData)
//                        }
                        val colorNew = Color(r, g, b)  // Create a new Color instance with the red value equal to 255
                        myImage.setRGB(x, y, colorNew.rgb)  // Set the new color at the (x, y) position
                        if(byteMask != V1) {
                            byteMask = byteMask.rotateRight(1)
                        } else {
                            byteMask = V128
                            index++
                            if(index >= data.size) {
                                isOver = true
                                break
                            }
                        }
                    }
                }
                val outputFile = File(outputImageName)  // Output the file
                ImageIO.write(myImage, "png", outputFile)  // Create an image using the BufferedImage instance data
                println("Message saved in $outputImageName image.")
            } else {
                println("The input image is not large enough to hold this message.")
            }
        } catch (e: IOException){
            println("Can't read input file!")
            return
        }
    }
    private fun commandShow() {
        println("Input image file:")
        val inputImageName = readln()
        println("Password:")
        val password = readln()
        val inputFile = File(inputImageName)
        try {
            var message = ""
            val myImage: BufferedImage = ImageIO.read(inputFile)
            val data = ByteArray(myImage.width*myImage.height) { 0 }
            val end = byteArrayOf(0,0,3)
            var index = 0
            var byteData = 0
            var bitCount = 7
            val V0:Byte = 0
            var isOver = false
            var outcount = 0
            for (y in 0 until myImage.height) {          // For every row
                if(isOver) {
                    break
                }
                for (x in 0 until myImage.width) {               // For every column.
                    val color = Color(myImage.getRGB(x, y))  // Read color from the (x, y) position
                    //val r = color.red             // Access the Green color value
                    //val g = color.green           // Access the Green color value
                    val b = color.blue             // Access the Blue color value
                    //val a = color.alpha              // Access the Blue color value
                    val bitData = (b and 1)
                    val bitMask = 1.rotateLeft(bitCount)
                    if(bitData != 0) {
                        byteData = byteData or bitMask
                    }
//                    if(outcount++ < 20) {
//                        println("bitData="+bitData)
//                        println("bitMask="+bitMask)
//                        println("byteData="+byteData)
//                    }
                    if(bitCount > 0) {
                        bitCount--
                    } else {
                        if(outcount < 20) {
//                            println("byteData=" + byteData.toChar() + "(" + byteData + ")")
                        }
                        data[index] = byteData.toByte()
                        byteData = 0
                        bitCount = 7
                        if(index > 3) {
                            if(data[index-2] == end[0] && data[index-1] == end[1] && data[index] == end[2]) {
                                val mes =  Arrays.copyOfRange(data, 0, data.size-3)
                                val mod = password.length
                                for(i in 0..mes.size-1) {
                                    mes[i] = mes[i] xor (password[i % mod].toByte())
                                }
                                message = mes.toString(Charsets.UTF_8)
                                isOver = true
                                break
                            }
                        }
                        index++
                    }
                }
            }
            println("Message:")
            println(message)
        } catch (e: IOException){
            println("Can't read input file!")
            return
        }
    }
}
fun main() {
    val cr = CRYPTO()
    while(cr.command()) {
    }
    println("Bye!")
}

