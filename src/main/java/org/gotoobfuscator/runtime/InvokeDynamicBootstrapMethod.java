package org.gotoobfuscator.runtime;

import java.lang.invoke.*;

public class InvokeDynamicBootstrapMethod {
    public static final String STATIC_DESC = MethodType.methodType(CallSite.class,
            MethodHandles.Lookup.class,
            String.class,
            MethodType.class,
            Class.class
    ).toMethodDescriptorString();

    public static final String VIRTUAL_DESC = MethodType.methodType(CallSite.class,
            MethodHandles.Lookup.class,
            String.class,
            MethodType.class
    ).toMethodDescriptorString();

    public static CallSite modeStatic(MethodHandles.Lookup lookup, String name, MethodType methodType, Class<?> target) throws Throwable {
        final MutableCallSite callSite = new MutableCallSite(methodType);

        callSite.setTarget(lookup.findStatic(target,name,methodType));

        return callSite;
    }

    public static CallSite modeVirtual(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
        final Class<?> owner = methodType.parameterType(0);

        final MutableCallSite callSite = new MutableCallSite(methodType);

        methodType = methodType.dropParameterTypes(0, 1);

        callSite.setTarget(lookup.findVirtual(owner,name,methodType));

        return callSite;
    }
}