import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Vox {
    fun writeToFile(filename: String) {


        val file = File(filename)
        file.delete()
        file.createNewFile()
        val channel = FileOutputStream(file).channel

        channel.write(getIdBB('V', 'O', 'X', ' '))
        channel.write(getBBInt(150))

        channel.close()

    }

    private fun getBBInt(i: Int): ByteBuffer {
        bb.position(0)
        bb.putInt(i)
        bb.flip()
        return bb
    }

    val bb = ByteBuffer.allocate(4).also {
        it.order(ByteOrder.LITTLE_ENDIAN)
    }

    private fun getIdBB(a: Char, b: Char, c: Char, d: Char): ByteBuffer {
        bb.position(0)
        bb.putInt(getId(a, b, c, d))
        bb.flip()
        return bb
    }

    private fun getId(a: Int, b: Int, c: Int, d: Int): Int {
        return a or (b.shl(8)) or (c.shl(16)) or (d.shl(24))
    }

    private fun getId(a: Char, b: Char, c: Char, d: Char): Int {
        return getId(a.code, b.code, c.code, d.code)
    }

}
