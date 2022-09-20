package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException

import javax.imageio.ImageIO


fun transparency2String(t:Int) : String {
    return when(t) {
        1 -> "OPAQUE"
        2 -> "BITMASK"
        3 -> "TRANSLUCENT"
        else -> ""
    }
}

fun main() {
    println("Input the image filename:")
    val filename = readln()
    if(File(filename).exists()) {
        try {
            val image = ImageIO.read(File(filename))
//            if(filename.indexOf("bits16") != -1) {
//                println("The number of image color components="+image.colorModel.numColorComponents)
//                println("The image bits="+image.colorModel.pixelSize)
//            }
            if(image.colorModel.numColorComponents != 3) {
                println("The number of image color components isn't 3.")
                return
            }
            if(image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
                println("The image isn't 24 or 32-bit.")
                return
            }
            println("Input the watermark image filename:")
            val watermarkfilename = readln()
            if(File(watermarkfilename).exists()) {
                val watermark = ImageIO.read(File(watermarkfilename))
//                if(watermarkfilename.indexOf("watermark3.png") != -1) {
//                    println("The number of image color components="+watermark.colorModel.numColorComponents)
//                    println("The image bits="+watermark.colorModel.pixelSize)
//                }
                if(watermark.colorModel.numColorComponents != 3) {
                    println("The number of watermark color components isn't 3.")
                    return
                }
                if(watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) {
                    println("The watermark isn't 24 or 32-bit.")
                    return
                }
//                if(image.width != watermark.width || image.height != watermark.height) {
//                    println("The image and watermark dimensions are different.")
//                    return
//                }
                if(image.width < watermark.width || image.height < watermark.height) {
                    println("The watermark's dimensions are larger.")
                    return
                }
                var yesnoalpha = false
                var yesnotranspcolor = false
                var transpcolor = Color(0,0,0)
                if(watermark.transparency == 3) {
                    println("Do you want to use the watermark's Alpha channel?")
                    yesnoalpha = readln().toLowerCase() == "yes"
                } else {
                    println("Do you want to set a transparency color?")
                    yesnotranspcolor = readln().toLowerCase() == "yes"
                    if(yesnotranspcolor) {
                        println("Input a transparency color ([Red] [Green] [Blue]):")
                        try {
                            val rgb = readln().split(" ").map({it.toInt()})
                            if(rgb.size != 3) {
                                println("The transparency color input is invalid.")
                                return
                            }
                            transpcolor = Color(rgb[0], rgb[1], rgb[2])
                        } catch (e:Exception) {
                            println("The transparency color input is invalid.")
                            return
                        }
                    }
                }
                var weight=0
                while(true) {
                    println("Input the watermark transparency percentage (Integer 0-100):")
                    try {
                        weight = readln().toInt()
                        if(weight >= 0 && weight <= 100)
                            break
                        println("The transparency percentage is out of range.")
                        return
                    } catch(e:Exception) {
                        println("The transparency percentage isn't an integer number.")
                        return
                    }
                }

                println("Choose the position method (single, grid):")
                val method = readln().toLowerCase()
                if(method != "single" && method != "grid") {
                    println("The position method input is invalid.")
                    return
                }
                var single_x = 0
                var single_y = 0
                if(method == "single") {
                    val diffX = image.width - watermark.width
                    val diffY = image.height - watermark.height
                    println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
                    try {
                        val posxy = readln().split(" ").map { it.toInt() }
                        if(posxy.size != 2) {
                            println("The position input is invalid.")
                            return
                        }
                        if(posxy[0] < 0 || posxy[0] > diffX || posxy[1] < 0 || posxy[1] > diffY) {
                            println("The position input is out of range.")
                            return
                        }
                        single_x = posxy[0]
                        single_y = posxy[1]
                    } catch (e:Exception) {
                        println("The position input is invalid.")
                        return
                    }
                }

                println("Input the output image filename (jpg or png extension):")
                val outputfilename = readln()
                val jpgpng = if(outputfilename.toLowerCase().endsWith(".jpg")) "jpg"
                                else if(outputfilename.toLowerCase().endsWith(".png")) "png"
                                else ""
                if(jpgpng == "") {
                    println("The output file extension isn't \"jpg\" or \"png\".")
                    return
                }
                val issingle = method == "single"
                val isgrid = method == "grid"
                val output = BufferedImage(image.width, image.height, image.type)
                for(yi in 0..image.height-1) {
                    for(xi in 0..image.width-1) {
                        val i = Color(image.getRGB(xi, yi))
                        var x = xi - single_x
                        var y = yi - single_y
                        var color:Color = i
                        if(issingle) {
                            if(xi < single_x || xi >= single_x+watermark.width ||
                                    yi < single_y || yi >= single_y+watermark.height) {
                                output.setRGB(xi, yi, color.rgb)
                                continue
                            }
                        } else {
                            x = xi % watermark.width
                            y = yi % watermark.height
                        }
                        if(yesnoalpha) {
                            val wa = Color(watermark.getRGB(x, y), true)
                            if(wa.alpha == 0) {
                                color = i
                            } else if(wa.alpha == 255) {
                                val w = Color(watermark.getRGB(x, y))
                                color = Color(
                                    (weight * w.red + (100 - weight) * i.red) / 100,
                                    (weight * w.green + (100 - weight) * i.green) / 100,
                                    (weight * w.blue + (100 - weight) * i.blue) / 100
                                )
                            }
                        } else if(yesnotranspcolor) {
                            try {
                                val w = Color(watermark.getRGB(x, y))
                                if(w == transpcolor) {
                                    color = i
                                } else {
                                    color = Color(
                                        (weight * w.red + (100 - weight) * i.red) / 100,
                                        (weight * w.green + (100 - weight) * i.green) / 100,
                                        (weight * w.blue + (100 - weight) * i.blue) / 100
                                    )
                                }
                            } catch(e:Exception) {
                                println("watermark.width="+watermark.width+" watermark.height="+watermark.height)
                                println("x="+x+" y="+y)
                                throw e
                            }

                        } else {
                            val w = Color(watermark.getRGB(x, y))
                            color = Color(
                                (weight * w.red + (100 - weight) * i.red) / 100,
                                (weight * w.green + (100 - weight) * i.green) / 100,
                                (weight * w.blue + (100 - weight) * i.blue) / 100
                            )
                        }
                        output.setRGB(xi, yi, color.rgb)
                    }
                }
                ImageIO.write(output, jpgpng, File(outputfilename))
                println("The watermarked image $outputfilename has been created.")
            } else {
                println("The file $watermarkfilename doesn't exist.")
            }
//            println("Image file: $filename\n" +
//                    "Width: ${image.width}\n" +
//                    "Height: ${image.height}\n" +
//                    "Number of components: ${image.colorModel.numComponents}\n" +
//                    "Number of color components: ${image.colorModel.numColorComponents}\n" +
//                    "Bits per pixel: ${image.colorModel.pixelSize}\n" +
//                    "Transparency: ${transparency2String(image.transparency)}")
        } catch (e: IOException) {
        }
    } else {
        println("The file $filename doesn't exist.")
    }
}

