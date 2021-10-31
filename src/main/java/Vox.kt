import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.floor
import kotlin.math.roundToInt

class Vox(
    maxVoxelPerCubeX: Int,
    maxVoxelPerCubeY: Int,
    maxVoxelPerCubeZ: Int
) {

    private val cubes = mutableListOf<VoxCube>()

    val m_MaxVoxelPerCubeX = maxVoxelPerCubeX.coerceIn(0, 126);
    val m_MaxVoxelPerCubeY = maxVoxelPerCubeY.coerceIn(0, 126);
    val m_MaxVoxelPerCubeZ = maxVoxelPerCubeZ.coerceIn(0, 126);
    val maxVolume = DAABBCC();
    val colors = mutableListOf<Int>();
    var maxCubeId = 0;
    var minCubeX: Int = 1e7.toInt();
    var minCubeY: Int = 1e7.toInt();
    var minCubeZ: Int = 1e7.toInt();
    var cubesId = mutableMapOf<Int, MutableMap<Int, MutableMap<Int, Int>>>()
    var voxelId = mutableMapOf<Int, MutableMap<Int, MutableMap<Int, Int>>>()

    init {
        maxVolume.lowerBound = Dvec3(1e7f)
        maxVolume.upperBound = Dvec3(0.0f)
    }

    fun saveToFile(filename: String) {

        val file = File(filename)
        file.delete()
        file.createNewFile()
        val channel = FileOutputStream(file).channel

        channel.write(getBB('V', 'O', 'X', ' '))
        channel.write(getBB(150))

        // main
        channel.write(getBB('M', 'A', 'I', 'N'))

        channel.write(getBB(0))
        val numBytesMainChunkPos = channel.position()

        val headerSize: Long = channel.position()
        channel.write(getBB(0))

        val cubesCount = cubes.size

        var nodeIds = 0

        val rootTransform: NTRN = NTRN(1)
        rootTransform.nodeId = nodeIds
        rootTransform.childNodeId = ++nodeIds

        val rootGroup = NGRP(cubesCount)
        rootGroup.nodeId = nodeIds
        rootGroup.nodeChildrenNodes = cubesCount

        val shapes = mutableListOf<NSHP>()
        val shapeTransforms = mutableListOf<NTRN>()

        repeat(cubesCount) { i ->
            val c = cubes[i]

            c.write(channel)

            val trans = NTRN(1)
            trans.nodeId = ++nodeIds
            rootGroup.childNodes[i] = nodeIds
            trans.childNodeId = ++nodeIds
            trans.layerId = 0

            c.tx = kotlin.math.floor((c.tx - minCubeX + 0.5f) * m_MaxVoxelPerCubeX - maxVolume.lowerBound.x - maxVolume.getSize().x * 0.5).roundToInt();
            c.ty = kotlin.math.floor((c.ty - minCubeY + 0.5f) * m_MaxVoxelPerCubeY - maxVolume.lowerBound.y - maxVolume.getSize().y * 0.5).roundToInt();
            c.tz = kotlin.math.floor((c.tz - minCubeZ + 0.5f) * m_MaxVoxelPerCubeZ).roundToInt();

            trans.frames[0].add("_t", c.tx.toString() + " " + c.ty.toString() + " " + c.tz)

            shapeTransforms.add(trans)

            val shape = NSHP(1)
            shape.nodeId = nodeIds
            shape.models[0].modelId = i
            shapes.add(shape)
        }

        rootTransform.write(channel)
        rootGroup.write(channel)

        repeat(cubesCount) { i ->
            shapeTransforms[i].write(channel)
            shapes[i].write(channel)
        }

        // rgba
        // TODO

        val mainChildChunkSize = channel.position() - headerSize
        channel.position(numBytesMainChunkPos)
        val mainChildChunkBb = getBB(mainChildChunkSize.toInt())
        channel.write(mainChildChunkBb).also {
            println("mainChildChunkSize written=$it " +
                    "mainChildChunkBb=${mainChildChunkBb.array().joinToString { Integer.toHexString(it.toInt()) }} channelPos=${channel.position()}"
            )
        }

        channel.close()
    }

    fun addVoxel(vX: Int, vY: Int, vZ: Int, vColorIndex: Int) {
        // cube pos
        val ox = floor(vX / m_MaxVoxelPerCubeX.toDouble()).roundToInt()
        val oy = floor(vY / m_MaxVoxelPerCubeY.toDouble()).roundToInt()
        val oz = floor(vZ / m_MaxVoxelPerCubeZ.toDouble()).roundToInt()

        minCubeX = minOf(minCubeX, ox);
        minCubeY = minOf(minCubeX, oy);
        minCubeZ = minOf(minCubeX, oz);

        val cube = getCube(ox, oy, oz);

        mergeVoxelInCube(vX, vY, vZ, vColorIndex, cube);
    }

    private fun mergeVoxelInCube(vX: Int, vY: Int, vZ: Int, vColorIndex: Int, vCube: VoxCube) {
        maxVolume.combine(Dvec3(vX.toDouble(), vY.toDouble(), vZ.toDouble()));

        var exist = false;

        voxelId[vX]?.get(vY)?.get(vZ)
            ?.let {
                exist = true
            }

        if (exist == false) {
            vCube.xyzi.voxels.add((vX % m_MaxVoxelPerCubeX).toByte()); // x
            vCube.xyzi.voxels.add((vY % m_MaxVoxelPerCubeY).toByte()); // y
            vCube.xyzi.voxels.add((vZ % m_MaxVoxelPerCubeZ).toByte()); // y

            // correspond a la loc de la couleur du voxel en question
            if (voxelId[vX] == null) {
                voxelId[vX] = mutableMapOf()
            }

            if (voxelId[vX]!![vY] == null) {
                voxelId[vX]!![vY] = mutableMapOf()
            }
            voxelId[vX]!![vY]!![vZ] = vCube.xyzi.voxels.size;

            vCube.xyzi.voxels.add(vColorIndex.toByte()); // color index
        } else {

        }
    }

    private fun getCube(vX: Int, vY: Int, vZ: Int): VoxCube {
        val id = getCubeId(vX, vY, vZ);

        if (id == cubes.size) {
            val c = VoxCube();

            c.id = id;

            c.tx = vX;
            c.ty = vY;
            c.tz = vZ;

            c.size.sizex = m_MaxVoxelPerCubeX + 1;
            c.size.sizey = m_MaxVoxelPerCubeY + 1;
            c.size.sizez = m_MaxVoxelPerCubeZ + 1;

            cubes.add(c);
        }

        return cubes.get(id)
    }

    private fun getCubeId(vX: Int, vY: Int, vZ: Int): Int {
        cubesId[vX]?.get(vY)?.get(vZ)?.let { return it }

        if (cubesId[vX] == null) {
            cubesId[vX] = mutableMapOf()
        }

        if (cubesId[vX]!![vY] == null) {
            cubesId[vX]!![vY] = mutableMapOf()
        }

        val newValue = maxCubeId++
        cubesId[vX]!![vY]!![vZ] = newValue

        return newValue;
    }

    class VoxCube {

        var id = 0

        var size = SIZE()

        var tx = 0;
        var ty = 0;
        var tz = 0;

        var xyzi = XYZI()

        fun write(channel: FileChannel) {
            size.write(channel)
            xyzi.write(channel)
        }
    }
}

class DAABBCC {
    fun getSize(): Dvec3 {
        return Dvec3(upperBound - lowerBound)
    }

    fun combine(pt: Dvec3) {
        lowerBound.x = minOf(lowerBound.x, pt.x);
        lowerBound.y = minOf(lowerBound.y, pt.y);
        lowerBound.z = minOf(lowerBound.z, pt.z);
        upperBound.x = maxOf(upperBound.x, pt.x);
        upperBound.y = maxOf(upperBound.y, pt.y);
        upperBound.z = maxOf(upperBound.z, pt.z);
    }

    var upperBound: Dvec3 = Dvec3(0.0)
    var lowerBound: Dvec3 = Dvec3(0.0)

    //TODO
    /*
    * dvec3 lowerBound;	///< the lower left vertex
		dvec3 upperBound;	///< the upper right vertex

		dAABBCC() : lowerBound(0.0), upperBound(0.0) {}
		dAABBCC(dvec3 vlowerBound, dvec3 vUpperBound)
		{
			lowerBound.x = mini(vlowerBound.x, vUpperBound.x);
			lowerBound.y = mini(vlowerBound.y, vUpperBound.y);
			lowerBound.z = mini(vlowerBound.z, vUpperBound.z);
			upperBound.x = maxi(vlowerBound.x, vUpperBound.x);
			upperBound.y = maxi(vlowerBound.y, vUpperBound.y);
			upperBound.z = maxi(vlowerBound.z, vUpperBound.z);
		}
		/// Add a vector to this vector.
		void operator += (const dvec3& v){lowerBound += v; upperBound += v;}

		/// Subtract a vector from this vector.
		void operator -= (const dvec3& v){lowerBound -= v; upperBound -= v;}

		/// Multiply this vector by a scalar.
		void operator *= (double a){lowerBound *= a; upperBound *= a;}

		/// Divide this vector by a scalar.
		void operator /= (double a){lowerBound /= a; upperBound /= a;}

		/// Get the center of the AABB.
		dvec3 GetCenter() const { return (lowerBound + upperBound) * 0.5; }

		/// Get the extents of the AABB (half-widths).
		dvec3 GetExtents() const {return (upperBound - lowerBound) * 0.5;}

		/// Get the perimeter length
		double GetPerimeter() const
		{
			double wx = upperBound.x - lowerBound.x;
			double wy = upperBound.y - lowerBound.y;
			double wz = upperBound.z - lowerBound.z;
			return 2.0 * (wx + wy + wz);
		}

		/// Combine a point into this one.
		void Combine(dvec3 pt)
		{
			lowerBound.x = mini<double>(lowerBound.x, pt.x);
			lowerBound.y = mini<double>(lowerBound.y, pt.y);
			lowerBound.z = mini<double>(lowerBound.z, pt.z);
			upperBound.x = maxi<double>(upperBound.x, pt.x);
			upperBound.y = maxi<double>(upperBound.y, pt.y);
			upperBound.z = maxi<double>(upperBound.z, pt.z);
		}

		/// Does this aabb contain the provided vec2.
		bool ContainsPoint(const dvec3& pt) const
		{
			bool result = true;
			result = result && lowerBound.x <= pt.x;
			result = result && lowerBound.y <= pt.y;
			result = result && lowerBound.z <= pt.z;
			result = result && pt.x <= upperBound.x;
			result = result && pt.y <= upperBound.y;
			result = result && pt.z <= upperBound.z;
			return result;
		}

		bool Intersects(const dAABBCC& other)
		{
			bool result = true;
			result = result || lowerBound.x <= other.lowerBound.x;
			result = result || lowerBound.y <= other.lowerBound.y;
			result = result || lowerBound.z <= other.lowerBound.z;
			result = result || other.upperBound.x <= upperBound.x;
			result = result || other.upperBound.y <= upperBound.y;
			result = result || other.upperBound.z <= upperBound.z;
			return result;
		}

		const dvec3 Size()
		{
			return dvec3(upperBound - lowerBound);
		}
		* */

}

class Dvec3() {
    operator fun minus(other: Dvec3): Dvec3 {
        return Dvec3(x - other.x, y - other.y, z - other.z)
    }

    var x = 0.0
    var y = 0.0
    var z = 0.0

    constructor(vxyz: Double) : this() {
        x = vxyz
        y = vxyz
        z = vxyz
    }

    constructor(vx: Double, vy: Double, vz: Double) : this() {
        x = vx
        y = vy
        z = vz
    }

    constructor(other: Dvec3) : this() {
        x = other.x
        y = other.y
        z = other.z
    }

    constructor(fl: Float) : this(fl.toDouble())
}

class XYZI {
    var numVoxels = 0
    val voxels = mutableListOf<Byte>()

    fun write(channel: FileChannel) {
        // chunk header

        channel.write(getBB('X', 'Y', 'Z', 'I'))
        channel.write(getBB(getSize()))
        channel.write(getBB(0))

        // data

        channel.write(getBB(numVoxels))
        val voxelss = ByteBuffer.wrap(voxels.toByteArray())
        println("voxels=$voxels")
        //voxelss.flip() // TODO CHECK
        val written = channel.write(voxelss)
        println(">>>>>>> ${voxelss.hexString()}")
        println("written voxels=$written")

    }

    private fun getSize(): Int {
        numVoxels = voxels.size / 4
        return 4 * (1 + numVoxels)
    }
    //TODO
}

class SIZE {
    var sizex = 0;
    var sizey = 0;
    var sizez = 0;


    fun write(channel: FileChannel) {
        // chunk header
        val tmpBB = getBB('S', 'I', 'Z', 'E')
        channel.write(tmpBB).also {
            println("SIZE written=$it bb=${tmpBB.hexString()}")
        }
        channel.write(getBB(getSize()))
        channel.write(getBB(0))

        // data
        channel.write(getBB(sizex))
        channel.write(getBB(sizey))
        channel.write(getBB(sizez))

    }

    private fun getSize(): Int {
        return 4 * 3
    }
}

private fun ByteBuffer.hexString(): String {
    return array().joinToString(separator = "") { Integer.toHexString(it.toInt()).padStart(2, '0') }
}

class NSHP(vCount: Int) {
    var nodeId = 0

    val numModels = vCount
    val models = MutableList(numModels) { MODEL() }
    val nodeAttribs = DICT()

    fun write(channel: FileChannel) {
        // chunk header

        channel.write(getBB('n', 'S', 'H', 'P'))
        channel.write(getBB(getSize()))
        channel.write(getBB(0))

        // data

        channel.write(getBB(nodeId))
        nodeAttribs.write(channel)
        channel.write(getBB(numModels))
        repeat(numModels) { i ->
            models[i].write(channel)
        }

    }

    private fun getSize(): Int {
        var s = 4 * 2 + nodeAttribs.getSize()
        repeat(numModels) {
            s += models[it].getSize()
        }
        return s
    }
}

class MODEL {
    var modelId = 0

    var modelAttribs = DICT()

    fun getSize(): Int {
        return 4 + modelAttribs.getSize()
    }

    fun write(channel: FileChannel) {
        channel.write(getBB(modelId))
        modelAttribs.write(channel)
    }
}

class NGRP(cubesCount: Int) {
    val nodeAttribs = DICT()

    val childNodes = MutableList(cubesCount, { 0 });
    var nodeChildrenNodes: Int = cubesCount


    var nodeId: Int = 0

    fun write(channel: FileChannel) {
        channel.write(getBB('n', 'G', 'R', 'P'))
        val contentSize = getSize()
        channel.write(getBB(contentSize))
        val childSize = 0
        channel.write(getBB(childSize))

        // data
        channel.write(getBB(nodeId))
        nodeAttribs.write(channel)
        channel.write(getBB(nodeChildrenNodes))
        for (childNode in childNodes) {
            channel.write(getBB(childNode))
        }
    }

    private fun getSize(): Int {
        return 4 * (2 + nodeChildrenNodes) + nodeAttribs.getSize()
    }
}

private val bb = ByteBuffer.allocate(4).also {
    it.order(ByteOrder.LITTLE_ENDIAN)
}

private fun getBB(i: Int): ByteBuffer {
    println(">>>>>>> ${bb.hexString()}")
    bb.position(0)
    bb.putInt(i)
    bb.flip()
    return bb
}


private fun getBB(a: Char, b: Char, c: Char, d: Char): ByteBuffer {
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

class NTRN(countFrames: Int) {
    var nodeId = 0
    var nodeAttribs = DICT()
    var childNodeId = 0;
    var reservedId = -1;
    var layerId = -1;
    var numFrames = countFrames
    var frames = List(numFrames) { DICT() };

    fun write(channel: FileChannel) {
        channel.write(getBB('n', 'T', 'R', 'N'))
        val contentSize = getSize()
        channel.write(getBB(contentSize))
        val childSize = 0
        channel.write(getBB(childSize))

        // data
        channel.write(getBB(nodeId))
        nodeAttribs.write(channel)
        channel.write(getBB(childNodeId))
        channel.write(getBB(reservedId))
        channel.write(getBB(layerId))
        channel.write(getBB(numFrames))
        for (frame in frames) {
            frame.write(channel)
        }


        /*   int32_t id = GetMVID('n', 'T', 'R', 'N');
           fwrite(&id, sizeof(int32_t), 1, fp);
           size_t contentSize = getSize();
           fwrite(&contentSize, sizeof(int32_t), 1, fp);
           size_t childSize = 0;
           fwrite(&childSize, sizeof(int32_t), 1, fp);

           // datas's
           fwrite(&nodeId, sizeof(int32_t), 1, fp);
           nodeAttribs.write(fp);
           fwrite(&childNodeId, sizeof(int32_t), 1, fp);
           fwrite(&reservedId, sizeof(int32_t), 1, fp);
           fwrite(&layerId, sizeof(int32_t), 1, fp);
           fwrite(&numFrames, sizeof(int32_t), 1, fp);
           for (int i=0;i<numFrames;i++)
           frames[i].write(fp);*/
    }

    fun getSize(): Int {
        var s = 4 * 5 + nodeAttribs.getSize()
        repeat(numFrames) {
            s += frames[it].getSize()
        }
        return s
    };
}

class DICT {

    var count = 0

    val keys = mutableListOf<DICTitem>();

    fun getSize(): Int {
        count = keys.size

        var s = 4
        repeat(count) {
            s += keys[it].getSize()
        }
        return s
    }

    fun write(channel: FileChannel) {
        count = keys.size
        val tmpBB = getBB(count)
        channel.write(tmpBB)
        println("DICT write=${tmpBB.hexString()}")
        repeat(count) {
            keys[it].write(channel)
        }
    }

    fun add(vKey: String, vValue: String) {
        keys.add(DICTitem(vKey, vValue))
    }
}

class DICTitem(val vKey: String, val vValue: String) {
    fun write(channel: FileChannel) {
        vKey.write(channel)
        vValue.write(channel)
    }

    fun getSize(): Int {
        return vKey.getSize() + vValue.getSize()
    }
}

private fun String.getSize(): Int {
    return 4 + length
}

private fun String.write(channel: FileChannel) {
    channel.write(getBB(length))
    val localBB = ByteBuffer.allocate(length)

    localBB.order(ByteOrder.LITTLE_ENDIAN)
    repeat(length) {
        localBB.put(get(it).toByte())
    }
    localBB.flip()

    channel.write(localBB)
}

