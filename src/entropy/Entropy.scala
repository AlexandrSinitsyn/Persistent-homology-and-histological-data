/**package entropy

import java.awt.Color

import javax.imageio.ImageIO
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


    private def createArray[T](width: Int, height: Int): Array[T] = {
        //new Array[T](0)
        Array()
    }
}

import entropy.Entropy._

class Entropy {
    private var components = null
    private var matrix = null
    var s: ARR = _

    private var componentsInverted = null
    private var matrixInverted = null
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
        val w = image.getWidth
        val h = image.getHeight

        val pixels: ARR = Array()
        val pixelsInverted: ARR = Array()
        for (i: Int <- imgPixels.indices) {
            for (j: Int <- imgPixels.indices) {
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

    private def getComponent(pixels: ARR): (ARR, List[(Int, Int, Int)]) = {
        count = 1 - 1
        val r = new Array[Array[Int]](pixels.length, pixels(0).length)
        val was = new Array[Array[Boolean]](pixels.length, pixels(0).length)
        var component: List[(Int, Int, Int)] = Nil
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

    private def acrossComponent(startX: Int, startY: Int, map: ARR, was: Array[Array[Boolean]], imgPixels: ARR): List[(Int, Int, Int)] = {
        var component: List[(Int, Int, Int)] = Nil

        var first = true
        var toVisit: List[(Int, Int)] = Nil
        toVisit = (startX, startY) :: toVisit
        while (toVisit.nonEmpty) {
            val current = toVisit.head
            toVisit = toVisit match {
                case h :: t => t
                case h :: Nil => Nil
                case _ => Nil
            }
            val x = current._1
            val y = current._2
            if (imgPixels(y)(x) == 0) {
                if (first) {
                    first = false
                    count += 1
                    component = (x, y, count) :: component
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
            val scale = if (size == 1) image.getWidth
            else size
            image = scale(image, scale, scale)
        } catch {
            case _: IOException =>
                System.err.println(path + "\tF")
                System.exit(0)
        }
        imgPixels = new Array[Array[Int]](image.getWidth, image.getHeight)
        val listOfColors = new Array[Int](256)
        var i = 0
        while ( {
            i < imgPixels.length
        }) {
            var j = 0
            while ( {
                j < imgPixels(i).length
            }) {
                val c = new Color(image.getRGB(i, j))
                val r = c.getRed
                val g = c.getGreen
                val b = c.getBlue
                val color = (r + g + b) / 3
                imgPixels(i)(j) = color
                listOfColors(color) += 1

                {
                    j += 1;
                    j - 1
                }
            }

            {
                i += 1;
                i - 1
            }
        }
        val setOfColors = new ConcurrentHashMap[Int, Int]
        var color = 0
        while ( {
            color < listOfColors.length
        }) {
            if (listOfColors(color) != 0) setOfColors.put(color, listOfColors(color))

            {
                color += 1;
                color - 1
            }
        }
        val colors = getListOfColors(setOfColors, splitter)
        val img = image
        colors.forEach((color: Int, count: Int) => {
            def foo(color: Int, count: Int) = {
                val images = modifyImage(img, color)
                val comp = getComponent(images(0))
                val compInv = getComponent(images(1))
                var i = 0
                while ( {
                    i < count
                }) {
                    components.add(comp)
                    componentsInverted.addFirst(compInv)

                    {
                        i += 1;
                        i - 1
                    }
                }
            }

            foo(color, count)
        })
    }

    private var currentLine = 0
    private var fullLine = 0

    private def getListOfColors(setOfColors: util.Map[Int, Int], numberOfSpaces: Int) = {
        setOfColors.forEach((color: Int, count: Int) => fullLine += count)
        if (numberOfSpaces <= 50) System.exit(1)
        val splitter = fullLine / numberOfSpaces
        if (splitter == 0) System.exit(1)
        val result = new ConcurrentHashMap[Int, Int]
        result.put(0, 1)
        setOfColors.forEach((color: Int, count: Int) => {
            def foo(color: Int, count: Int) = {
                currentLine += count
                val current = currentLine / splitter
                currentLine -= current * splitter
                if (current != 0) result.put(color, current)
            }

            foo(color, count)
        })
        result.put(255, 1)
        result
    }

    private var countOfSpaces = 0

    def transitions(path: String, size: Int, splitter: Int): Four = {
        countOfSpaces = splitter + 2 - 1
        reset(countOfSpaces)
        getMatrix(path, size, splitter)
        var n = 0
        while ( {
            n <= countOfSpaces
        }) {
            var m = n
            while ( {
                m <= countOfSpaces
            }) { // todo     0-Entropy
                val first = components.get(n).j
                val arrival = List[Int]
                import scala.collection.JavaConversions._
                for (e <- first) {
                    arrival.add(components.get(m).i(e.y)(e.x))
                }
                matrix(m)(n) = new HashSet[Int](arrival).size

                // todo     1-Entropy
                val first = componentsInverted.get(n).j
                val arrival = List[Int]
                import scala.collection.JavaConversions._
                for (e <- first) {
                    arrival.add(componentsInverted.get(m).i(e.y)(e.x))
                }
                matrixInverted(m)(n) = new HashSet[Int](arrival).size

                if (m != n) {
                    s(m - 1)(n) = getSnm(n, m - 1, matrix)
                    holeLength += ((m - 1) - n) * s(m - 1)(n)

                    sInverted(m - 1)(n) = getSnm(n, m - 1, matrixInverted)
                    holeLengthInverted += ((m - 1) - n) * sInverted(m - 1)(n)

                }

                {
                    m += 1;
                    m - 1
                }
            }

            {
                n += 1;
                n - 1
            }
        }
        var n = 0
        while ( {
            n <= countOfSpaces
        }) {
            s(countOfSpaces)(n) = getSnm(n, countOfSpaces, matrix)
            holeLength += ((countOfSpaces - n) * s(countOfSpaces)(n))

            sInverted(countOfSpaces)(n) = getSnm(n, countOfSpaces, matrixInverted)
            holeLengthInverted += ((countOfSpaces - n) * sInverted(countOfSpaces)(n))


            {
                n += 1;
                n - 1
            }
        }
        val nameOfTheFile = path.split("/")
        val name = nameOfTheFile(nameOfTheFile.length - 1)
        System.out.print(name + ": ")
        val result = H(s, holeLength)
        val resultInverted = H(sInverted, holeLengthInverted)
        val resultCombined = getCombinedEntropy
        System.out.println(result + "\n" + resultInverted + "\n" + resultCombined)
        new Four(name, result, resultInverted, resultCombined)
    }

    private def getSnm(n: Int, m: Int, r: Array[Array[Int]]) = {
        var snm = r(m)(n)
        if (n - 1 >= 0) snm += -r(m)(n - 1) + (if (m + 1 < r.length) r(m + 1)(n - 1)
        else 0)
        snm += -
        if ((m + 1 < r.length)) r(m + 1)(n)
        else 0
        snm
    }

    def H(s: Array[Array[Int]], L: Double): Double = {
        var r = 0d
        var n = 0
        while ( {
            n <= countOfSpaces
        }) {
            var m = n + 1
            while ( {
                m <= countOfSpaces
            }) {
                if (s(m)(n) == 0) {
                    continue //todo: continue is not supported}
                    //System.out.println(r + " (" + n + ", " + m + ")");
                    val argument = (m - n) * s(m)(n) / L
                    //System.out.println(argument + " " + (m - n) + " " + s[m][n] + " " + L);
                    r += (argument * (Math.log(argument) / Math.log(2)))

                    {
                        m += 1;
                        m - 1
                    }
                }

                {
                    n += 1;
                    n - 1
                }
            }
            return -r
        }

        private def reset(splitter: Int): Unit

        =
        {
            splitter += 1
            components = List[pair[Array[Array[Int]], List[(Int, Int, Int)]]]
            matrix = new Array[Array[Int]](splitter, splitter)
            s = new Array[Array[Int]](splitter, splitter)
            componentsInverted = List[pair[Array[Array[Int]], List[(Int, Int, Int)]]]
            matrixInverted = new Array[Array[Int]](splitter, splitter)
            sInverted = new Array[Array[Int]](splitter, splitter)
            count = 1 - 1
            holeLength = 0
            holeLengthInverted = 0
            fullLine = 0
            currentLine = 0
        }

        def getCombinedEntropy = {
            val combined = new Array[Array[Int]](s.length, s(0).length)
            var len = 0
            var n = 0
            while ( {
                n <= countOfSpaces
            }) {
                var m = n
                while ( {
                    m <= countOfSpaces
                }) {
                    combined(m)(n) = s(m)(n) + sInverted(m)(n)
                    len += (m - n) * combined(m)(n)

                    {
                        m += 1;
                        m - 1
                    }
                }

                {
                    n += 1;
                    n - 1
                }
            }
            H(combined, len)
        }
    }
}
*/