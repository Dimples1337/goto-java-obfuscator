package org.gotoobfuscator;

import com.google.gson.annotations.SerializedName;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Collections;
import java.util.List;

final class Config {
    @SerializedName("InputFile")
    public String input = "";

    @SerializedName("OutputFile")
    public String output = "";

    @SerializedName("MainClass")
    public String mainClass = "";

    @SerializedName("Libraries")
    public List<String> libraries = Collections.emptyList();

    @SerializedName("ExtractZips")
    public List<String> extractZips = Collections.emptyList();

    @SerializedName("SkipClasses")
    public List<String> skipClasses = Collections.emptyList();

    @SerializedName("ExcludeClasses")
    public List<String> excludeClasses = Collections.emptyList();

    @SerializedName("DictionaryOptions")
    public DictionaryOptions dictionaryOptions = new DictionaryOptions();

    @SerializedName("StaticLibraryLoaderOptions")
    public StaticLibraryLoaderOptions staticLibraryLoaderOptions = new StaticLibraryLoaderOptions();

    @SerializedName("LoadLibraryMode")
    public int libMode = 0;

    @SerializedName("PreVerify")
    public boolean preVerify = true;

    @SerializedName("ClassRenameRemoveMetadata")
    public boolean classRenameRemoveMetadata = true;

    @SerializedName("CorruptCRC")
    public boolean corruptCRC;

    @SerializedName("CorruptDate")
    public boolean corruptDate;

    @SerializedName("ClassFolder")
    public boolean classFolder;

    @SerializedName("DuplicateResource")
    public boolean duplicateResource;

    @SerializedName("Packer")
    public boolean packerEnable;

    @SerializedName("ConstantPacker")
    public boolean constantPackerEnable;

    @SerializedName("ExtractorMode")
    public boolean extractorMode;

    @SerializedName("UseComputeMaxs")
    public boolean useComputeMaxs;

    @SerializedName("ClassRename")
    public ClassRenameOptions classRenameOptions = new ClassRenameOptions();

    @SerializedName("StringEncryptor")
    public boolean stringEncryptorEnable;

    @SerializedName("HideCode")
    public boolean hideCodeEnable;

    @SerializedName("NumberEncryptor")
    public boolean numberEncryptorEnable;

    @SerializedName("JunkCode")
    public boolean junkCodeEnable;

    @SerializedName("SourceRename")
    public boolean sourceRenameEnable;

    @SerializedName("BadAnnotation")
    public boolean badAnnotationEnable;

    @SerializedName("Crasher")
    public boolean crasherEnable;

    @SerializedName("InvalidSignature")
    public boolean invalidSignatureEnable;

    @SerializedName("VariableRename")
    public boolean variableRenameEnable;

    @SerializedName("InvokeProxy")
    public boolean invokeProxyEnable;

    @SerializedName("FlowObfuscation")
    public boolean flowObfuscationEnable;

    @SerializedName("FakeClasses")
    public boolean fakeClassesEnable;

    @SerializedName("DecompilerCrasher")
    public boolean decompilerCrasherEnable;

    public Config() {

    }

    public static class StaticLibraryLoaderOptions {
        @SerializedName("ThreadPoolSize")
        public int threadPoolSize = 5;

        @SerializedName("MultiThreadLoadLibraries")
        public boolean multiThreadLoadLibraries = true;
    }

    public static class DictionaryOptions {
        @SerializedName("CustomFile")
        public String customFile = "";
        @SerializedName("Mode")
        public int mode = 0;
        @SerializedName("RepeatTimeBase")
        public int repeatTimeBase = 1;
    }

    public static class ClassRenameOptions {
        @SerializedName("Enable")
        public boolean enable = false;
        @SerializedName("PackageName")
        public String packageName = "";
        @SerializedName("Prefix")
        public String prefix = "";
        @SerializedName("Exclude")
        public List<String> exclude = Collections.emptyList();
    }
}
