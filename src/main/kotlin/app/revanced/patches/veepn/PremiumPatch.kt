package app.revanced.patches.veepn

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n

@Patch(
    name = "Premium Unlocked",
    description = "Bypass premium subscription checks automatically.",
    packagename = ["com.veepn.vpn"] // شرط: يجب أن يكون هذا هو الـ Package Name الدقيق للتطبيق
)
class PremiumPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        // 1. تحديد الكلاس ديناميكياً باستخدام البصمة النصية (بدل الاسم d00/c المعرض للتغيير)
        val targetClass = context.classes.find { 
            it.strings.contains("subscriptionGateway") && 
            it.strings.contains("premium end") 
        } ?: throw IllegalStateException("Patch failed: Class containing 'subscriptionGateway' not found.")

        // 2. فلترة الدوال المستهدفة: البحث عن كل دالة خاصة لا تقبل متغيرات وترجع ()Z
        val targetMethods = targetClass.methods.filter { 
            it.returnType == "Z" && it.parameters.isEmpty() 
        }

        // 3. حقن التعديل في Smali (const/4 v0, 0x1 ثم return v0)
        targetMethods.forEach { method ->
            val implementation = method.implementation ?: return@forEach
            val instructions = implementation.instructions
            
            // مسح الكود الأصلي بالكامل
            instructions.clear()
            
            // كتابة: const/4 v0, 0x1
            instructions.add(BuilderInstruction11n(Opcode.CONST_4, 0, 1))
            
            // كتابة: return v0
            instructions.add(BuilderInstruction10x(Opcode.RETURN, 0))
        }
    }
}
