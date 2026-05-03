package com.example.metrotransit.nfc

class NfcCommandGenerator {
    fun generateReadCommand(idm: ByteArray, startBlock: Int = 0): ByteArray {
        val serviceCode = 0x220F
        val numBlocks = 15
        val commandLength = 14 + (numBlocks * 2)
        val command = ByteArray(commandLength)
        
        command[0] = commandLength.toByte()
        command[1] = 0x06.toByte() // Read Without Encryption Command
        idm.copyInto(command, 2)   // The card's unique IDm
        command[10] = 1.toByte()   // Service count
        command[11] = (serviceCode and 0xFF).toByte()
        command[12] = ((serviceCode shr 8) and 0xFF).toByte()
        command[13] = numBlocks.toByte()

        for (i in 0 until numBlocks) {
            command[14 + i * 2] = 0x80.toByte() // Control byte
            command[15 + i * 2] = (startBlock + i).toByte()
        }
        return command
    }
}
