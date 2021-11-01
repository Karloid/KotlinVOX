class TestWriter {
    fun writeCoupleVoxels() {
        createCraters()
    }

    private fun createCraters() {
        val vox = Vox(126, 126, 126)

        val colorRed = 1
        val colorBlue = 2
        val colorGreen = 3
        val colorWhite = 4
        vox.addColor(255, 0, 0, 255, colorRed)
        vox.addColor(0, 0, 255, 255, colorBlue)
        vox.addColor(0, 255, 0, 255, colorGreen)
        vox.addColor(255, 255, 233, 255, colorWhite)

        val size = 1000
        val mountains = List(size / 10) {
            Point2D((Math.random() * size).toInt(), (Math.random() * size).toInt())
        }

        val cur = Point2D(0, 0)
        repeat(size) { x ->
            repeat(size) { y ->
                @Suppress("ReplaceGetOrSet")
                cur.set(x, y)
                val z = (mountains.map {
                    var dist = it.euclidianDistance(cur)
                    if (dist.isNaN()) {
                        dist = 10.0
                    }
                    val res = dist + 5
                    res
                }
                    .minOrNull()!!).toInt()

                val vColorIndex = when {
                    z < 50 -> colorRed
                    z < 75 -> colorGreen
                    z < 125 -> colorBlue
                    else -> colorWhite
                } + 1

               //  println("z=$z vColorIndex=$vColorIndex")
                vox.addVoxel(x, y, z, vColorIndex)
            }
        }

        vox.saveToFile("craters.vox")
    }

}
