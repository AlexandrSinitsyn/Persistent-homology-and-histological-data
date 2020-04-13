package entropy

import java.awt.Color

import javax.imageio.ImageIO

import scala.collection.mutable
//import java.awt._
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap


object Entropy {

    type ARR = Array[Array[Int]]


    private val zeroes = true

    def toString(mas: ARR): String = {
        var R = ""

        for (m: Int <- mas.indices) {
            var r = ""

            for (n: Int <- mas(m).indices)
                r += (if (m < n && !zeroes) " " else mas(m)(n) + " ")

            R += r + "\n"
        }

        R
    }

    def printList(mas: ARR): Unit = println(toString(mas))

    private def addEdge(array: ARR): ARR = {
        val r: ARR = Array()
        array.copyToArray(r)

        for (i: Int <- array.indices) {
            for (j: Int <- array(i).indices) {
                r(i + 1)(j + 1) = array(i)(j)
            }
        }

        r
    }
}

import entropy.Entropy._

class Entropy {
    private var components: List[(ARR, List[(Int, Int)])] = Nil
    private var matrix: ARR = _
    var s: ARR = _

    private var componentsInverted: List[(ARR, List[(Int, Int)])] = Nil
    private var matrixInverted: ARR = _
    var sInverted: ARR = _

    private var count = 1 - 1
    private var holeLength = 0
    private var holeLengthInverted = 0
    private var imgPixels: ARR = _


    private def scale(img: BufferedImage, width: Int, height: Int): BufferedImage = {
        val scaledImage = new BufferedImage(
            if (width > 0) width else 1,
            if (height > 0) height else 1,
            img.getType)
        val graphics2D = scaledImage.createGraphics

        graphics2D.drawImage(img, 0, 0, width, height, null)
        graphics2D.dispose()

        scaledImage
    }


    private def modifyImage(image: BufferedImage, color: Int): Array[ARR]  = {
        val w = image.getWidth()
        val h = image.getHeight()

        val pixels: ARR = Array.ofDim(h, w)
        val pixelsInverted: ARR = Array.ofDim(h, w)

        for (i: Int <- 0 until h) {
            for (j: Int <- 0 until w) {
                if (imgPixels(i)(j) > color) {
                    pixels(i)(j) = 255
                    pixelsInverted(i)(j) = 0
                }
                else {
                    pixels(i)(j) = 0
                    pixelsInverted(i)(j) = 255
                }
            }
        }

        Array(pixels, pixelsInverted)
    }

    private def getComponent(pixels: ARR): (ARR, List[(Int, Int)]) = {
        count = 1 - 1
        val r = Array.ofDim[Int](pixels.length, pixels(0).length)
        val was = Array.ofDim[Boolean](pixels.length, pixels(0).length)
        var component: List[(Int, Int)] = Nil

        var x = 0
        var y = 0
        while (y < pixels.length) {
            if (!was(y)(x)) {
                val color = pixels(y)(x)
                was(y)(x) = true
                if (color == 0) {
                    component = acrossComponent(x, y, r, was, pixels) ::: component
                }
            }

            x += 1

            if (x >= pixels(y).length) {
                x = 0
                y += 1
            }
        }

        (r, component)
    }

    private def acrossComponent(startX: Int, startY: Int, map: ARR, was: Array[Array[Boolean]], imgPixels: ARR): List[(Int, Int)] = {
        var component: List[(Int, Int)] = Nil

        var first = true
        var toVisit: List[(Int, Int)] = Nil
        toVisit = (startX, startY) :: toVisit
        while (toVisit.nonEmpty) {
            val current = toVisit.head
            toVisit = toVisit match {
                case _ :: t => t
                case _ => Nil
            }

            val x = current._1
            val y = current._2
            if (imgPixels(y)(x) == 0) {
                if (first) {
                    first = false
                    count += 1
                    component = (x, y) :: component
                }
                map(y)(x) = count

                for (i: Int <- -1 until 1) {
                    for (j: Int <- -1 until 1) {
                        if (y + i >= 0 && y + i < imgPixels.length &&
                            x + j >= 0 && x + j < imgPixels(y).length &&
                            !was(y + i)(x + j)) {
                            was(y + i)(x + j) = true

                            toVisit = (x + j, y + i) :: toVisit
                        }
                    }
                }
            }
        }

        component
    }

    private def getMatrix(path: String, size: Int, splitter: Int): Unit = {
        var image: BufferedImage = null
        try {
            image = javax.imageio.ImageIO.read(new File(path))
            val size$ = if (size == 1) image.getWidth else size

            image = scale(image, size$, size$)
        } catch {
            case _: IOException =>
                System.err.println(path + "\tF")
                System.exit(0)
        }
        imgPixels = Array.ofDim[Int](image.getWidth, image.getHeight)
        val listOfColors = new Array[Int](256)

        for (i: Int <- imgPixels.indices) {
            for (j: Int <- imgPixels(i).indices) {
                val c = new Color(image.getRGB(i, j))
                val r = c.getRed
                val g = c.getGreen
                val b = c.getBlue
                val color = (r + g + b) / 3

                imgPixels(i)(j) = color
                listOfColors(color) += 1
            }
        }

        val setOfColors = new ConcurrentHashMap[Int, Int]

        for (color: Int <- listOfColors.indices) {
            if (listOfColors(color) != 0)
                setOfColors.put(color, listOfColors(color))
        }

        val colors = getListOfColors(setOfColors, splitter)
        val img = image
        colors.forEach((color: Int, count: Int) => {
            val images = modifyImage(img, color)
            val comp = getComponent(images(0))
            val compInv = getComponent(images(1))

            for (_ <- 0 until count) {
                def push_back[T](list: List[T], a: T) : List[T] = list match {
                    case Nil => a :: Nil
                    case h :: Nil => h :: a :: Nil
                    case h :: t => h :: push_back(t, a)
                }
                components = push_back(components, comp)
                componentsInverted = compInv :: componentsInverted
            }
        })
    }

    private var currentLine = 0
    private var fullLine = 0

    private def getListOfColors(setOfColors: ConcurrentHashMap[Int, Int], numberOfSpaces: Int) = {
        setOfColors.forEach((_, count: Int) => fullLine += count)
        if (numberOfSpaces <= 50) System.exit(1)
        val splitter = fullLine / numberOfSpaces
        if (splitter == 0) System.exit(1)
        val result = new ConcurrentHashMap[Int, Int]
        result.put(0, 1)
        setOfColors.forEach((color, count) => {
            currentLine += count
            val current = currentLine / splitter
            currentLine -= current * splitter
            if (current != 0) result.put(color, current)
        })
        result.put(255, 1)
        result
    }

    private var countOfSpaces = 0

    def transitions(path: String, size: Int, splitter: Int): Four = {
        countOfSpaces = splitter + 2 - 1
        reset(countOfSpaces)


        getMatrix(path, size, splitter)


        for (n: Int <- 0 to countOfSpaces) {
            for (m: Int <- n to countOfSpaces) {
                // todo     0-Entropy
                {
                    val first = components(n)._2

                    var arrival: List[Int] = Nil
                    for (e <- first) arrival = components(m)._1(e._2)(e._1) :: arrival

                    matrix(m)(n) = mutable.HashSet(arrival).size
                }

                // todo     1-Entropy
                {
                    val first = componentsInverted(n)._2

                    var arrival: List[Int] = Nil
                    for (e <- first) arrival = componentsInverted(m)._1(e._2)(e._1) :: arrival

                    matrixInverted(m)(n) = mutable.HashSet(arrival).size
                }

                if (m != n) {
                    // todo     0-Entropy
                    {
                        s(m - 1)(n) = getSnm(n, m - 1, matrix)

                        holeLength += ((m - 1) - n) * s(m - 1)(n)
                    }

                    // todo     1-Entropy
                    {
                        sInverted(m - 1)(n) = getSnm(n, m - 1, matrixInverted)

                        holeLengthInverted += ((m - 1) - n) * sInverted(m - 1)(n)
                    }
                }
            }
        }

        for (n <- 0 to countOfSpaces) {
            // todo     0-Entropy
            {
                s(countOfSpaces)(n) = getSnm(n, countOfSpaces, matrix)

                holeLength += ((countOfSpaces - n) * s(countOfSpaces)(n))
            }

            // todo     1-Entropy
            {
                sInverted(countOfSpaces)(n) = getSnm(n, countOfSpaces, matrixInverted)

                holeLengthInverted += ((countOfSpaces - n) * sInverted(countOfSpaces)(n))
            }
        }


        val nameOfTheFile = path.split("/")
        val name = nameOfTheFile(nameOfTheFile.length - 1)
        print(name + ": ")

        val result = H(s, holeLength)
        val resultInverted = H(sInverted, holeLengthInverted)
        val resultCombined = getCombinedEntropy

        println(result + "\n" + resultInverted + "\n" + resultCombined)

        new Four(name, result, resultInverted, resultCombined)
    }


    private def getSnm(n: Int, m: Int, r: Array[Array[Int]]) = {
        var snm = r(m)(n)

        if (n - 1 >= 0)
            snm += -r(m)(n - 1) + (if (m + 1 < r.length) r(m + 1)(n - 1) else 0)

        snm += -(if (m + 1 < r.length) r(m + 1)(n) else 0)

        snm
    }

    def H(s: Array[Array[Int]], L: Double): Double = {
        var r = 0d

        for (n <- 0 to countOfSpaces) {
            for (m <- n + 1 to countOfSpaces) {
                if (s(m)(n) != 0) {
                    val argument = (m - n) * s(m)(n) / L
                    r += (argument * (Math.log(argument) / Math.log(2)))
                }
            }
        }

        -r
    }

    private def reset(splitter$: Int): Unit = {
        val splitter = splitter$ + 1

        components = Nil
        matrix = Array.ofDim[Int](splitter, splitter)
        s = Array.ofDim[Int](splitter, splitter)

        componentsInverted = Nil
        matrixInverted = Array.ofDim[Int](splitter, splitter)
        sInverted = Array.ofDim[Int](splitter, splitter)


        count = 1 - 1

        holeLength = 0
        holeLengthInverted = 0

        fullLine = 0
        currentLine = 0
    }

    def getCombinedEntropy: Double = {
        val combined = Array.ofDim[Int](s.length, s(0).length)

        var len = 0
        for (n <- 0 to countOfSpaces) {
            for (m <- n to countOfSpaces) {
                combined(m)(n) = s(m)(n) + sInverted(m)(n)

                len += (m - n) * combined(m)(n)
            }
        }

        H(combined, len)
    }
}
