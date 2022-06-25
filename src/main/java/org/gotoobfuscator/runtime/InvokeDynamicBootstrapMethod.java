package org.gotoobfuscator.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class InvokeDynamicBootstrapMethod {
    public static final String DESC = MethodType.methodType(CallSite.class,
            MethodHandles.Lookup.class,
            String.class,
            MethodType.class,
            String.class,
            String.class,
            int.class
    ).toMethodDescriptorString();

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType methodType, String target, String fieldDesc, int mode) throws Throwable {
        final Class<?> targetClass = Class.forName(target);
        final Class<?> fieldClass;

        if (fieldDesc.isEmpty()) {
            fieldClass = null;
        } else {
            switch (fieldDesc) {
                case "B": {
                    fieldClass = byte.class;
                    break;
                }
                case "S": {
                    fieldClass = short.class;
                    break;
                }
                case "I": {
                    fieldClass = int.class;
                    break;
                }
                case "J": {
                    fieldClass = long.class;
                    break;
                }
                case "C": {
                    fieldClass = char.class;
                    break;
                }
                case "F": {
                    fieldClass = float.class;
                    break;
                }
                case "D": {
                    fieldClass = double.class;
                    break;
                }
                case "Z": {
                    fieldClass = boolean.class;
                    break;
                }
                default: {
                    fieldClass = Class.forName(fieldDesc);
                    break;
                }
            }
        }

        switch (mode) {
            case 0:
                return new ConstantCallSite(lookup.findStatic(targetClass, name, methodType).asType(methodType));
            case 1:
                return new ConstantCallSite(lookup.findVirtual(methodType.parameterType(0), name, methodType.dropParameterTypes(0, 1)).asType(methodType));
            case 2:
                return new ConstantCallSite(lookup.findSpecial(targetClass, name, methodType, lookup.lookupClass()).asType(methodType));
            case 3:
                return new ConstantCallSite(lookup.findConstructor(targetClass, methodType).asType(methodType));
            case 4:
                return new ConstantCallSite(lookup.findGetter(targetClass, name, fieldClass).asType(methodType));
            case 5:
                return new ConstantCallSite(lookup.findSetter(targetClass, name, fieldClass).asType(methodType));
            case 6:
                return new ConstantCallSite(lookup.findStaticGetter(targetClass, name, fieldClass).asType(methodType));
            case 7:
                return new ConstantCallSite(lookup.findStaticSetter(targetClass, name, fieldClass).asType(methodType));
        }

        throw new IllegalArgumentException("Unknown mode: " + mode);
    }
}
