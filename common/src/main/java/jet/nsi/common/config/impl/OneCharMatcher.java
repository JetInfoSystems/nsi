package jet.nsi.common.config.impl;

import com.google.common.base.CharMatcher;

public class OneCharMatcher extends CharMatcher {

    private final char t;

    public OneCharMatcher(char t) {
        this.t = t;
    }

    @Override
    public boolean matches(char c) {
        return t == c;
    }

}
