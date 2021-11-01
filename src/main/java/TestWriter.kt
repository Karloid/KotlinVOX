class TestWriter {
    fun writeCoupleVoxels() {
        createCraters()
    }

    private fun createCraters() {
        val vox = Vox(126, 126, 126)


        val size = 3_000
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
                val vColorIndex = z % 20 + 1
                // println("z=$z vColorIndex=$vColorIndex")
                vox.addVoxel(x, y, z, vColorIndex)
            }
        }

        vox.saveToFile("craters.vox")
    }

}
