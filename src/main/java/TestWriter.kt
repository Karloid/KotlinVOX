import java.lang.Math.abs

class TestWriter {
    fun writeCoupleVoxels() {
        val vox = Vox(126, 126, 126)



        repeat(100) { x ->
            repeat(200) { y ->
                vox.addVoxel(x, y, 1, 1)
               /* var z = abs(Math.sin((x + y) / 100.0) * 50)

                z += Math.random() * 5

                vox.addVoxel(x, y, z.toInt(), z.toInt())
                repeat(z.toInt()) { vZ ->
                 //   vox.addVoxel(x, y, vZ, z.toInt())
                }*/
            }
        }

        vox.saveToFile("test.vox")

    }

}
