import kotlin.math.roundToInt

class Point2D {
    var x: Int = 0
    var y: Int = 0

    val isNull: Boolean
        get() = this.x or this.y == 0

    @JvmOverloads
    constructor(x: Int = 0, y: Int = x) {
        this.x = x
        this.y = y
    }

    @JvmOverloads
    constructor(list: List<Int>) {
        this.x = list[0]
        this.y = list[1]
    }


    constructor(vect: Point2D) {
        this.x = vect.x
        this.y = vect.y
    }


    override fun equals(other: Any?): Boolean {
        if (other == null || !(other is Point2D)) {
            return false
        }
        return this.x == other.x && this.y == other.y
    }

    operator fun set(x: Int, y: Int): Point2D {
        this.x = x
        this.y = y
        return this
    }

    inline fun set(a: Point2D): Point2D {
        this.x = a.x
        this.y = a.y
        return this
    }

    fun add(a: Point2D): Point2D {
        this.x += a.x
        this.y += a.y
        return this
    }

    fun add(a: Point2D, mul: Int): Point2D {
        this.x += a.x * mul
        this.y += a.y * mul
        return this
    }

    fun sub(a: Point2D): Point2D {
        this.x -= a.x
        this.y -= a.y
        return this
    }

    fun mult(a: Int): Point2D {
        this.x *= a
        this.y *= a
        return this
    }

    fun mul(d: Double): Point2D {
        x = (x * d).roundToInt()
        y = (x * d).roundToInt()
        return this
    }

    fun mul(d: Int): Point2D {
        x = (x * d)
        y = (x * d)
        return this
    }


    operator fun div(a: Int): Point2D {
        this.x /= a
        this.y /= a
        return this
    }

    fun negate(): Point2D {
        this.x = -this.x
        this.y = -this.y
        return this
    }


    inline fun manDist(): Int {
        return fastAbs(x) + fastAbs(y)
    }

    inline fun manDist(a: Point2D): Int {
        return fastAbs(this.x - a.x) + fastAbs(this.y - a.y)
    }

    inline fun manDist(x: Int, y: Int): Int {
        return fastAbs(this.x - x) + fastAbs(this.y - y)
    }


    fun tchebychevDistance(): Int {
        return Math.max(x, y)
    }

    fun tchebychevDistance(a: Point2D): Int {
        return Math.max(Math.abs(this.x - a.x), Math.abs(this.y - a.y))
    }

    fun euclidianDistance2(): Double {
        return (x * x + y * y).toDouble()
    }

    fun euclidianDistance2(a: Point2D): Int {
        val dx = this.x - a.x
        val dy = this.y - a.y
        return dx * dx + dy * dy;
    }

    fun euclidianDistance(): Double {
        return Math.sqrt(euclidianDistance())
    }

    fun euclidianDistance(a: Point2D): Double {
        return Math.sqrt(euclidianDistance2(a).toDouble())
    }

    inline fun applyDir(direction: Direction): Point2D {
        return plus(getPointByDir(direction))
    }

    inline operator fun plus(point: Point2D): Point2D {
        return plus(point.x, point.y)
    }

    inline fun plus(x: Int, y: Int): Point2D {
        this.x += x
        this.y += y
        return this
    }


    inline fun dirTo(target: Point2D): Direction {
        return when {
            x + 1 == target.x -> Direction.RIGHT
            x - 1 == target.x -> Direction.LEFT
            y + 1 == target.y -> Direction.DOWN
            y - 1 == target.y -> Direction.UP
            else -> {
                Direction.UP
            }
        }
    }



    fun copy(): Point2D {
        return Point2D(x, y)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    fun abs() {
        x = fastAbs(x)
        y = fastAbs(y)
    }

    companion object {

        fun add(a: Point2D, b: Point2D): Point2D {
            return Point2D(a).add(b)
        }

        fun sub(a: Point2D, b: Point2D): Point2D {
            return Point2D(a).sub(b)
        }

        fun mult(a: Point2D, b: Int): Point2D {
            return Point2D(a).mult(b)
        }

        fun div(a: Point2D, b: Int): Point2D {
            return Point2D(a).div(b)
        }

        val ZERO: Point2D = Point2D(0, 0)

        val UP = Point2D(0, -1)
        val RIGHT = Point2D(1, 0)
        val DOWN = Point2D(0, 1)
        val LEFT = Point2D(-1, 0)

        inline fun getPointByDir(direction: Direction): Point2D {
            return when (direction) {
                Direction.LEFT -> LEFT
                Direction.UP -> UP
                Direction.RIGHT -> RIGHT
                Direction.DOWN -> DOWN
            }
        }

        fun allDirections(): List<Point2D> {
            return listOf(
                UP,
                RIGHT,
                DOWN,
                LEFT
            )
        }

        inline fun manhDist(x: Int, y: Int, x1: Int, y1: Int): Int {
            return fastAbs(x - x1) + fastAbs(y - y1)
        }

        fun fromSdc(map: List<Int>) = Point2D(map[1] - 1, map[0] - 1)
        fun fromSdc(y: String, x: String): Point2D {
            return Point2D(x.toInt() - 1, y.toInt() - 1)
        }
    }


    inline fun down(): Point2D {
        return add(DOWN)
    }

    inline fun up(): Point2D {
        return add(UP)
    }

    inline fun left(): Point2D {
        return add(LEFT)
    }

    inline fun right(): Point2D {
        return add(RIGHT)
    }

    inline fun add(d: Int): Point2D {
        x += d
        y += d
        return this
    }

    fun flip() {
        val tmp = x
        x = y
        y = tmp
    }

    override fun toString(): String {
        return "(x=$x, y=$y)"
    }

/*    fun f(): Point2DF {
        return Point2DF(x.toDouble(), y.toDouble())
    }*/

    fun toSdcString(): String {
        return "${y + 1} ${x + 1}"
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun fastAbs(a: Int): Int {
    return if (a < 0) -a else a
}

@Suppress("NOTHING_TO_INLINE")
inline fun fastAbs(a: Double): Double {
    return if (a < 0) -a else a
}


enum class Direction {
    LEFT,
    UP,
    RIGHT,
    DOWN;

    fun toRight(): Direction {
        return next(1)
    }

    fun toLeft(): Direction {
        return next(-1)
    }


    private fun next(diff: Int): Direction {
        var indexOf = cValues.indexOf(this)
        indexOf += diff
        if (indexOf >= cValues.size) {
            indexOf = 0
        } else if (indexOf < 0) {
            indexOf = cValues.size - 1
        }
        return cValues.get(indexOf)
    }

    fun isOpposite(dirTo: Direction): Boolean {
        return when (dirTo) {
            LEFT -> this == RIGHT
            UP -> this == DOWN
            RIGHT -> this == LEFT
            DOWN -> this == UP
        }
    }

    fun toCommand() {

    }

    companion object {
        fun fromString(s: String): Direction {
            return when (s) {
                "left" -> LEFT
                "up" -> UP
                "right" -> RIGHT
                "down" -> DOWN
                else -> {
                    LEFT
                }
            }
        }

        val cValues = values()
    }
}

