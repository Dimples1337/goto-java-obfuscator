package org.gotoobfuscator.exceptions;

public final class MissingClassException extends RuntimeException {
    private final String missingClassName;

    public MissingClassException(String missingClassName) {
        super("Missing class " + missingClassName);

        this.missingClassName = missingClassName;
    }

    public String getMissingClassName() {
        return missingClassName;
    }
}
