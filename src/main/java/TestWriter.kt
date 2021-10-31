class TestWriter {
    fun writeCoupleVoxels() {
        val vox = Vox(126, 126, 126)

        vox.addVoxel(1, 1, 1, 1)

        vox.saveToFile("test.vox")

    }

}
