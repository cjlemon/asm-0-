package com.buildsrc

import com.android.build.api.transform.*
import com.android.build.api.transform.Context
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter


public class MyPlugin extends Transform implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println '==      ==    ===='
        println '==      ==    ===='
        println '==      ==        '
        println '==========    ===='
        println '==========    ===='
        println '==      ==    ===='
        println '==      ==    ===='
        println '==      ==    ===='
        BaseExtension android = project.extensions.getByType(BaseExtension);
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return 'ASMDemo'
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println "============================"
        println "=========transform=========="
        println "============================"
        inputs.each {
            TransformInput input ->
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        operationAllClass(directoryInput.file)
                        def dest = outputProvider.getContentLocation(directoryInput.name,
                                directoryInput.contentTypes, directoryInput.scopes,
                                Format.DIRECTORY)


                        FileUtils.copyDirectory(directoryInput.file, dest)
                }
                input.jarInputs.each {
                    JarInput jarInput ->
                        def jarName = jarInput.name
                        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }

                        def dest = outputProvider.getContentLocation(jarName + md5Name,
                                jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        FileUtils.copyFile(jarInput.file, dest)
                }
        }
    }

    void operationAllClass(File file){
        if (file.isDirectory()){
            file.eachFile {
                File file1 ->
                    operationAllClass(file1)
            }
            return
        }
        String name = file.name
        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                                            "R.class" != name && "BuildConfig.class" != name) {
            println name + "  ############## "
            ClassReader cr = new ClassReader(file.bytes)
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            ClassVisitor cv = new ChangeVisitor(Opcodes.ASM5, cw)
            cr.accept(cv, ClassReader.EXPAND_FRAMES)
            byte[] code = cw.toByteArray();
            FileOutputStream fos = new FileOutputStream(
                    file.parentFile.absolutePath + File.separator + name);
            fos.write(code);
            fos.close();
        }
    }

    class ChangeVisitor extends ClassVisitor{

        ChangeVisitor(int api) {
            super(api)
        }

        ChangeVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        void visitInnerClass(String s, String s1, String s2, int i) {
            println "*********************************"
            println s + "  ***  " + s1+ "  ***  " + s2+ "  ***  " + i
            println "*********************************"
            super.visitInnerClass(s, s1, s2, i)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            println "*********************************====================="
            println access + "  ***  " + name+ "  ***  " + desc+ "  ***  " + signature
            println "*********************************====================="
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            if ("onClick" == name){
                return new OnClickAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    class OnClickAdapter extends AdviceAdapter{

        /**
         * Creates a new {@link AdviceAdapter}.
         *
         * @param api
         *            the ASM API version implemented by this visitor. Must be one
         *            of {@link Opcodes#ASM4}, {@link Opcodes#ASM5} or {@link Opcodes#ASM6}.
         * @param mv
         *            the method visitor to which this adapter delegates calls.
         * @param access
         *            the method's access flags (see {@link Opcodes}).
         * @param name
         *            the method's name.
         * @param desc
         *            the method's descriptor (see {@link Type Type}).
         */
        protected OnClickAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
            println "onClickAdapter start=============================="
            println name + "  ******  " + desc
            println "onClickAdapter end================================"
        }

        @Override
        protected void onMethodEnter() {
            println "name start==========================="
            println methodDesc
            println "name end============================="
            super.onMethodEnter()
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESTATIC, "com/asmdemo/ToastUtils", "showToast", "(Landroid/view/View;)V", false)
        }
    }
}