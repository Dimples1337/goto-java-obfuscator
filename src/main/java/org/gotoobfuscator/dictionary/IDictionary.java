package org.gotoobfuscator.dictionary;

import org.gotoobfuscator.Obfuscator;
import org.gotoobfuscator.dictionary.impl.*;

public interface IDictionary {
    String get();

    static IDictionary newDictionary() {
        final int mode = Obfuscator.Instance.getDictionaryMode();

        switch (mode) {
            case 0:
                return new AlphaDictionary();
            case 1:
                return new NumberDictionary();
            case 2:
                return new UnicodeDictionary(Obfuscator.Instance.getDictionaryRepeatTimeBase());
            case 3:
                return new CustomDictionary();
            default:
                throw new IllegalArgumentException("Unknown dictionary mode: " + mode);
        }
    }
}
