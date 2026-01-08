package hr.unipu.journals.feature.manuscript.core

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.RandomAccessFile
import java.nio.file.Files

@Service
class ZipService {
    private fun findEndOfCentralDirectory(raf: RandomAccessFile): Long {
        val fileSize = raf.length()
        val maxComment = 65535
        val start = maxOf(0, fileSize - (22 + maxComment))

        for (pos in fileSize - 22 downTo start) {
            raf.seek(pos)
            if (Integer.reverseBytes(raf.readInt()) == END_OF_CENTRAL_DIRECTORY)
                return pos
        }
        throw Exception("entry is malformed")
    }
    fun isOperable(file: MultipartFile): Boolean {
        val temp = Files.createTempFile("upload", ".zip").toFile().apply { deleteOnExit() }
        try {
            file.inputStream.use { it.copyTo(temp.outputStream()) }
            RandomAccessFile(temp, "r").use { randomAccessFile ->
                val endOfCentralDirectoryOffset = findEndOfCentralDirectory(randomAccessFile)
                randomAccessFile.seek(endOfCentralDirectoryOffset + 10) // skip to "total number of entries"
                val totalEntries = randomAccessFile.readUnsignedShort()

                randomAccessFile.seek(endOfCentralDirectoryOffset + 16)
                // zip files offsets are unsigned 32bit integers, but raf.readInt() returns a signed 32bit integer
                // .toLong() converts it to a signed 64bit Long
                // and bitwise `AND 0xffffffffL` masks the upper 32bits effectively treating the original Int as unsigned
                val centralDirOffset = randomAccessFile.readInt().toLong() and 0xffffffffL
                randomAccessFile.seek(centralDirOffset)

                repeat(totalEntries) {
                    // Central directory file header signature
                    if (Integer.reverseBytes(randomAccessFile.readInt()) != CENTRAL_DIRECTORY_FILE_HEADER_SIGNATURE)
                        return false // entry is corrupted

                    randomAccessFile.skipBytes(6) // versions
                    val flags = randomAccessFile.readUnsignedShort()
                    if ((flags and GENERAL_PURPOSE_BIT_FLAG) != 0) return false // entry is encrypted

                    randomAccessFile.skipBytes(24) // rest of fixed fields
                    val nameLen = randomAccessFile.readUnsignedShort()
                    val extraLen = randomAccessFile.readUnsignedShort()
                    val commentLen = randomAccessFile.readUnsignedShort()

                    randomAccessFile.skipBytes(nameLen + extraLen + commentLen)
                }
            }
        } catch (_: Exception) {
            return false // entry is corrupted/malformed/encrypted
        }
        finally {
            temp.delete()
        }
        return true
    }
    companion object {
      /*|                      marks end of a zip file                     |
        | Offset | Size | Description                                      |
        | ------ | ---- | ------------------------------------------------ |
        | 0      | 4    | EOCD signature (`0x06054b50`)                    |
        | 4      | 2    | Number of this disk                              |
        | 6      | 2    | Disk where central directory starts              |
        | 8      | 2    | Total number of entries on this disk             |
        | 10     | 2    | Total number of entries in the central directory |
        | 12     | 4    | Size of central directory                        |
        | 16     | 4    | Offset of start of central directory             |
        | 20     | 2    | ZIP file comment length                          |
        | 22     | …    | ZIP file comment                                 |*/
        private const val END_OF_CENTRAL_DIRECTORY = 0x06054b50

      /*|                  marks a file entry in central directory               |
        | Offset | Size | Description                                            |
        | ------ | ---- | ------------------------------------------------------ |
        | 0      | 4    | Central directory file header signature (`0x02014b50`) |
        | 4      | 2    | Version made by                                        |
        | 6      | 2    | Version needed to extract                              |
        | 8      | 2    | General purpose bit flag                               |
        | 10     | 2    | Compression method                                     |
        | 12     | 2    | File last modification time                            |
        | 14     | 2    | File last modification date                            |
        | 16     | 4    | CRC-32                                                 |
        | 20     | 4    | Compressed size                                        |
        | 24     | 4    | Uncompressed size                                      |
        | 28     | 2    | File name length (n)                                   |
        | 30     | 2    | Extra field length (m)                                 |
        | 32     | 2    | File comment length (k)                                |
        | 34     | …    | File name, extra field, comment                        |*/
        private const val CENTRAL_DIRECTORY_FILE_HEADER_SIGNATURE = 0x02014b50

      /*|           marks the actual file data in the zip            |
        | Offset | Size | Description                                |
        | ------ | ---- | ------------------------------------------ |
        | 0      | 4    | Local file header signature (`0x04034b50`) |
        | 4      | 2    | Version needed to extract                  |
        | 6      | 2    | General purpose bit flag                   |
        | 8      | 2    | Compression method                         |
        | 10     | 2    | File last modification time                |
        | 12     | 2    | File last modification date                |
        | 14     | 4    | CRC-32                                     |
        | 18     | 4    | Compressed size                            |
        | 22     | 4    | Uncompressed size                          |
        | 26     | 2    | File name length (n)                       |
        | 28     | 2    | Extra field length (m)                     |
        | 30     | …    | File name and extra field                  |*/
        private const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50

      /*| Bit | Meaning                                                       |
        | --- | ------------------------------------------------------------- |
        | 0   | If set, file is **encrypted**                                 |
        | 1   | Compression option (usually ignored)                          |
        | …   | Other optional flags (UTF-8 filenames, data descriptor, etc.) |*/
        private const val GENERAL_PURPOSE_BIT_FLAG = 0x0001
    }
}