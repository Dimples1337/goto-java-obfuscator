package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.dictionary.impl.UnicodeDictionary
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.ASMUtils
import org.gotoobfuscator.utils.InstructionBuilder
import org.gotoobfuscator.utils.InstructionModifier
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier
import java.util.concurrent.ThreadLocalRandom

class StringEncryptor : Transformer("StringEncryptor") {
    private val data = ArrayList<EncryptData>()
    private val constFieldData = ArrayList<FieldData>()

    private var encryptedStringSize = 0

    override fun transform(node: ClassNode) {
        val dictionary = UnicodeDictionary(100)
        val key = IntArray(257) {
            ThreadLocalRandom.current().nextInt()
        }

        data.clear()
        constFieldData.clear()

        if (ASMUtils.isInterfaceClass(node)) {
            return
        }

        for (field in node.fields) {
            dictionary.addUsed(field.name)
        }

        for (method in node.methods) {
            dictionary.addUsed(method.name)
        }

        val fieldName = dictionary.get()
        val decryptMethodName = dictionary.get()

        val offset = ThreadLocalRandom.current().nextInt()

        var pos = 0

        MethodForeach@ for (method in node.methods) {
            val modifier = InstructionModifier()

            InstructionForeach@ for (instruction in method.instructions) {
                if (ASMUtils.isString(instruction)) {
                    modifier.replace(instruction,
                        FieldInsnNode(GETSTATIC,node.name,fieldName,"[Ljava/lang/String;"),
                        ASMUtils.createNumberNode(pos),
                        InsnNode(AALOAD)
                    )

                    data.add(EncryptData(encode((instruction as LdcInsnNode).cst.toString(),offset,key,pos),pos))

                    pos++
                    encryptedStringSize++
                }
            }

            modifier.apply(method)
        }

        for (field in node.fields) {
            if (Modifier.isStatic(field.access)) {
                val value = field.value

                if (value is String) {
                    field.value = null

                    data.add(EncryptData(encode(value, offset, key,pos), pos))
                    constFieldData.add(FieldData(pos, field))

                    pos++
                    encryptedStringSize++
                }
            }
        }

        if (pos == 0) return

        val field = FieldNode(ACC_PRIVATE or ACC_STATIC,fieldName,"[Ljava/lang/String;",null,null)
        node.fields.add(field)

        ASMUtils.getClinitMethodNodeOrCreateNew(node).also { staticMethod ->
            val modifier = InstructionModifier()

            val staticList = InstructionBuilder().apply {
                val startLabel = LabelNode()
                val foreachStartLabel = LabelNode()
                val endLabel = LabelNode()

                label(startLabel)
                number(data.size)
                type(ANEWARRAY, "java/lang/String")
                fieldInsn(PUTSTATIC, node.name, fieldName, "[Ljava/lang/String;")

                val stringsIndex = ++staticMethod.maxLocals
                val iIndex = ++staticMethod.maxLocals

                number(data.size)
                type(ANEWARRAY,"java/lang/String")
                insn(DUP)

                val lengthList = ArrayList<Int>(data.size)

                data.forEachIndexed { index, it ->
                    lengthList.add(it.encryptedString.length)

                    number(index)
                    ldc(it.encryptedString)
                    insn(AASTORE)

                    if (index != data.size - 1) {
                        insn(DUP)
                    }
                }

                varInsn(ASTORE, stringsIndex)

                insn(ICONST_0)
                varInsn(ISTORE, iIndex)
                label(foreachStartLabel)
                varInsn(ILOAD, iIndex)
                number(lengthList.size)
                jump(IF_ICMPGE, endLabel)
                varInsn(ALOAD, stringsIndex)
                varInsn(ILOAD, iIndex)
                insn(AALOAD)
                varInsn(ILOAD, iIndex)
                methodInsn(INVOKESTATIC, node.name, decryptMethodName, "(Ljava/lang/String;I)V", false)
                iinc(iIndex, 1)
                jump(GOTO, foreachStartLabel)

                label(endLabel)
                for (data in constFieldData) {
                    fieldInsn(GETSTATIC,node.name,fieldName,"[Ljava/lang/String;")
                    number(data.pos)
                    insn(AALOAD)
                    fieldInsn(PUTSTATIC,node.name,data.fieldNode.name,data.fieldNode.desc)
                }
            }.list

            modifier.prepend(staticMethod.instructions.first,staticList)
            modifier.apply(staticMethod)
        }

        MethodNode(ACC_PRIVATE or ACC_STATIC,decryptMethodName,"(Ljava/lang/String;I)V",null,null).also { decryptMethod ->
            val labels = newLabels(522)
            val inputString = 0
            val posLocal = 1
            val inputCharArray = 2
            val newCharArray = 3
            val finalNumber = 4
            val foreachLoopNum = 5

            decryptMethod.visitCode()
            decryptMethod.visitLabel(labels[0])
            decryptMethod.visitVarInsn(ALOAD,inputString)
            decryptMethod.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","toCharArray","()[C",false)
            decryptMethod.visitVarInsn(ASTORE,inputCharArray)
            decryptMethod.visitLabel(labels[1])
            decryptMethod.visitVarInsn(ALOAD,inputCharArray)
            decryptMethod.visitInsn(ARRAYLENGTH)
            decryptMethod.visitIntInsn(NEWARRAY,5)
            decryptMethod.visitVarInsn(ASTORE,newCharArray)
            decryptMethod.visitLabel(labels[2])
            decryptMethod.visitVarInsn(ALOAD,inputString)
            decryptMethod.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","length","()I",false)
            decryptMethod.visitIntInsn(SIPUSH,255)
            decryptMethod.visitInsn(IAND)
            decryptMethod.visitTableSwitchInsn(0,255,labels[515],labels[3],labels[5],labels[7],labels[9],labels[11],labels[13],labels[15],labels[17],labels[19],labels[21],labels[23],labels[25],labels[27],labels[29],labels[31],labels[33],labels[35],labels[37],labels[39],labels[41],labels[43],labels[45],labels[47],labels[49],labels[51],labels[53],labels[55],labels[57],labels[59],labels[61],labels[63],labels[65],labels[67],labels[69],labels[71],labels[73],labels[75],labels[77],labels[79],labels[81],labels[83],labels[85],labels[87],labels[89],labels[91],labels[93],labels[95],labels[97],labels[99],labels[101],labels[103],labels[105],labels[107],labels[109],labels[111],labels[113],labels[115],labels[117],labels[119],labels[121],labels[123],labels[125],labels[127],labels[129],labels[131],labels[133],labels[135],labels[137],labels[139],labels[141],labels[143],labels[145],labels[147],labels[149],labels[151],labels[153],labels[155],labels[157],labels[159],labels[161],labels[163],labels[165],labels[167],labels[169],labels[171],labels[173],labels[175],labels[177],labels[179],labels[181],labels[183],labels[185],labels[187],labels[189],labels[191],labels[193],labels[195],labels[197],labels[199],labels[201],labels[203],labels[205],labels[207],labels[209],labels[211],labels[213],labels[215],labels[217],labels[219],labels[221],labels[223],labels[225],labels[227],labels[229],labels[231],labels[233],labels[235],labels[237],labels[239],labels[241],labels[243],labels[245],labels[247],labels[249],labels[251],labels[253],labels[255],labels[257],labels[259],labels[261],labels[263],labels[265],labels[267],labels[269],labels[271],labels[273],labels[275],labels[277],labels[279],labels[281],labels[283],labels[285],labels[287],labels[289],labels[291],labels[293],labels[295],labels[297],labels[299],labels[301],labels[303],labels[305],labels[307],labels[309],labels[311],labels[313],labels[315],labels[317],labels[319],labels[321],labels[323],labels[325],labels[327],labels[329],labels[331],labels[333],labels[335],labels[337],labels[339],labels[341],labels[343],labels[345],labels[347],labels[349],labels[351],labels[353],labels[355],labels[357],labels[359],labels[361],labels[363],labels[365],labels[367],labels[369],labels[371],labels[373],labels[375],labels[377],labels[379],labels[381],labels[383],labels[385],labels[387],labels[389],labels[391],labels[393],labels[395],labels[397],labels[399],labels[401],labels[403],labels[405],labels[407],labels[409],labels[411],labels[413],labels[415],labels[417],labels[419],labels[421],labels[423],labels[425],labels[427],labels[429],labels[431],labels[433],labels[435],labels[437],labels[439],labels[441],labels[443],labels[445],labels[447],labels[449],labels[451],labels[453],labels[455],labels[457],labels[459],labels[461],labels[463],labels[465],labels[467],labels[469],labels[471],labels[473],labels[475],labels[477],labels[479],labels[481],labels[483],labels[485],labels[487],labels[489],labels[491],labels[493],labels[495],labels[497],labels[499],labels[501],labels[503],labels[505],labels[507],labels[509],labels[511],labels[513],)

            for (i in 0..255) {
                decryptMethod.visitLabel(labels[3 + i * 2])

                decryptMethod.instructions.add(ASMUtils.createNumberNode(((i + 1) xor offset.inv()) xor key[i]))

                decryptMethod.visitVarInsn(ISTORE,finalNumber)
                decryptMethod.visitLabel(labels[4 + i * 2])
                decryptMethod.visitJumpInsn(GOTO,labels[516])
            }

            //Default
            decryptMethod.visitLabel(labels[515])

            decryptMethod.instructions.add(ASMUtils.createNumberNode(257 xor offset.inv() xor key[256]))

            decryptMethod.visitVarInsn(ISTORE,finalNumber)
            decryptMethod.visitLabel(labels[516])
            decryptMethod.visitInsn(ICONST_0)
            decryptMethod.visitVarInsn(ISTORE,foreachLoopNum)
            decryptMethod.visitLabel(labels[517])
            decryptMethod.visitVarInsn(ILOAD,foreachLoopNum)
            decryptMethod.visitVarInsn(ALOAD,inputCharArray)
            decryptMethod.visitInsn(ARRAYLENGTH)
            decryptMethod.visitJumpInsn(IF_ICMPGE,labels[520])
            decryptMethod.visitLabel(labels[518])
            decryptMethod.visitVarInsn(ALOAD,newCharArray)
            decryptMethod.visitVarInsn(ILOAD,foreachLoopNum)
            decryptMethod.visitVarInsn(ALOAD,inputCharArray)
            decryptMethod.visitVarInsn(ILOAD,foreachLoopNum)
            decryptMethod.visitInsn(CALOAD)
            decryptMethod.visitVarInsn(ILOAD,foreachLoopNum)
            decryptMethod.visitInsn(IXOR)
            decryptMethod.visitVarInsn(ILOAD,finalNumber)
            decryptMethod.visitInsn(IXOR)
            decryptMethod.visitVarInsn(ILOAD,posLocal)
            decryptMethod.visitInsn(IXOR)
            decryptMethod.visitInsn(ICONST_M1)
            decryptMethod.visitInsn(IXOR)
            decryptMethod.visitInsn(I2C)
            decryptMethod.visitInsn(CASTORE)
            decryptMethod.visitLabel(labels[519])
            decryptMethod.visitIincInsn(foreachLoopNum,1)
            decryptMethod.visitJumpInsn(GOTO,labels[517])
            decryptMethod.visitLabel(labels[520])

            decryptMethod.visitFieldInsn(GETSTATIC,node.name,fieldName,"[Ljava/lang/String;")
            decryptMethod.visitVarInsn(ILOAD,posLocal)
            decryptMethod.visitTypeInsn(NEW,"java/lang/String")
            decryptMethod.visitInsn(DUP)
            decryptMethod.visitVarInsn(ALOAD,newCharArray)
            decryptMethod.visitMethodInsn(INVOKESPECIAL,"java/lang/String","<init>","([C)V",false)
            decryptMethod.visitMethodInsn(INVOKEVIRTUAL,"java/lang/String","intern","()Ljava/lang/String;",false)
            decryptMethod.visitInsn(AASTORE)

            decryptMethod.visitInsn(RETURN)
            decryptMethod.visitEnd()

            ASMUtils.computeMaxLocals(decryptMethod)

            node.methods.add(decryptMethod)
        }
    }

    private fun encode(s : String, offset : Int, key : IntArray, pos : Int) : String {
        val chars = s.toCharArray()
        val decode = CharArray(chars.size)

        for (i in chars.indices) {
            decode[i] = (chars[i].code.xor(i xor ((((s.length and 0xFF) + 1) xor offset.inv()) xor key[s.length and 0xFF]) xor pos)).inv().toChar()
        }

        return String(decode)
    }

    override fun finish(obfuscator : Obfuscator) {
        print("Encrypted $encryptedStringSize strings")
    }

    private data class EncryptData(val encryptedString : String,val pos : Int)

    private data class FieldData(val pos : Int,val fieldNode : FieldNode)
}
