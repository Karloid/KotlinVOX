import java.lang.Math.abs

class TestWriter {
    fun writeCoupleVoxels() {
        val vox = Vox(126, 126, 126)


        val size = 1_000
        val center = Point2D(size / 2, size / 2)

        val cur = Point2D(0, 0)
        repeat(size) { x ->
            repeat(size) { y ->
                cur[x] = y
                val z = -cur.euclidianDistance(center).toInt() + size * 2
                vox.addVoxel(x, y, z, z % 255)
            }
        }

        vox.saveToFile("test.vox")

    }

}
