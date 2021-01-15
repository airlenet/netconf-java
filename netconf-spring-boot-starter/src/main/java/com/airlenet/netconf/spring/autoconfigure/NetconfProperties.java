package com.airlenet.netconf.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.netconf")
public class NetconfProperties {
    private String[] aopPatterns;
    private static final int DefaultTimeOut = 0;
    private int readTimeout = DefaultTimeOut;
    private int connectionTimeout = DefaultTimeOut;
    private int kexTimeout = DefaultTimeOut;
    private int maxPoolSize = 8;
    private boolean autoReconnect = true;
    private boolean exceptionRestart = true;
    /**
     * NOT_SET=0  SET=1   TEST_THEN_SET = 2 TEST_ONLY = 3
     */
    private TestOption testOption = TestOption.NOT_SET;

    private ErrorOption errorOption = ErrorOption.NOT_SET;

    private DefaultOperation defaultOperation = DefaultOperation.NOT_SET;

    private StatViewServlet statViewServlet = new StatViewServlet();

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getKexTimeout() {
        return kexTimeout;
    }

    public void setKexTimeout(int kexTimeout) {
        this.kexTimeout = kexTimeout;
    }

    public String[] getAopPatterns() {
        return this.aopPatterns;
    }

    public void setAopPatterns(String[] aopPatterns) {
        this.aopPatterns = aopPatterns;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public boolean isExceptionRestart() {
        return exceptionRestart;
    }

    public void setExceptionRestart(boolean exceptionRestart) {
        this.exceptionRestart = exceptionRestart;
    }

    public TestOption getTestOption() {
        return testOption;
    }

    public void setTestOption(TestOption testOption) {
        this.testOption = testOption;
    }

    public ErrorOption getErrorOption() {
        return errorOption;
    }

    public void setErrorOption(ErrorOption errorOption) {
        this.errorOption = errorOption;
    }

    public DefaultOperation getDefaultOperation() {
        return defaultOperation;
    }

    public void setDefaultOperation(DefaultOperation defaultOperation) {
        this.defaultOperation = defaultOperation;
    }

    public StatViewServlet getStatViewServlet() {
        return statViewServlet;
    }

    public void setStatViewServlet(StatViewServlet statViewServlet) {
        this.statViewServlet = statViewServlet;
    }

    public static enum TestOption {
        /**
         * NOT_SET=0  SET=1   TEST_THEN_SET = 2 TEST_ONLY = 3
         */
        NOT_SET, SET, TEST_THEN_SET, TEST_ONLY
    }

    public static enum ErrorOption {
        NOT_SET, STOP_ON_ERROR, CONTINUE_ON_ERROR, ROLLBACK_ON_ERROR
    }

    public static enum DefaultOperation {
        NOT_SET, MERGE, REPLACE, NONE
    }

    public static class StatViewServlet {
        private boolean enabled = true;
        private String urlPattern;
        private String allow;
        private String deny;
        private String loginUsername;
        private String loginPassword;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getAllow() {
            return allow;
        }

        public void setAllow(String allow) {
            this.allow = allow;
        }

        public String getDeny() {
            return deny;
        }

        public void setDeny(String deny) {
            this.deny = deny;
        }

        public String getLoginUsername() {
            return loginUsername;
        }

        public void setLoginUsername(String loginUsername) {
            this.loginUsername = loginUsername;
        }

        public String getLoginPassword() {
            return loginPassword;
        }

        public void setLoginPassword(String loginPassword) {
            this.loginPassword = loginPassword;
        }
    }
}
