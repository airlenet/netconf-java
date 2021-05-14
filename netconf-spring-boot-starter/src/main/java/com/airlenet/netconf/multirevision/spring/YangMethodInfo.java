package com.airlenet.netconf.multirevision.spring;

import java.util.ArrayList;
import java.util.List;

public class YangMethodInfo {

    List<YangCapabilityInfo> yangCapabilityInfo;
    boolean ignore = false;
    boolean priority = false;
    Class<?>[] moduleClass;
    boolean moduleEnable;
    String versionRegexp;

    public YangMethodInfo(Class<?>[] prefixClass, boolean prefixEnable, boolean ignore, boolean priority,String versionPrefix) throws NoSuchFieldException, IllegalAccessException {
        this.moduleClass = prefixClass;
        this.moduleEnable = prefixEnable;
        this.ignore = ignore;
        this.priority = priority;
        this.versionRegexp= versionPrefix;
        yangCapabilityInfo = new ArrayList<>();
        for (Class<?> clazz : prefixClass) {
            Object revision = clazz.getDeclaredField("REVISION").get(null);
            Object namespace = clazz.getDeclaredField("NAMESPACE").get(null);
            Object moduleName = clazz.getDeclaredField("MODULE_NAME").get(null);
            yangCapabilityInfo.add(new YangCapabilityInfo(namespace.toString(),
                    moduleName == null ? null : moduleName.toString(), revision == null ? null : revision.toString(),versionPrefix,this))
            ;
        }
    }

    public List<YangCapabilityInfo> getYangCapabilities() {
        return yangCapabilityInfo;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public boolean isPriority() {
        return priority;
    }

    public boolean isModuleEnable() {
        return moduleEnable;
    }

    public Class<?>[] getModuleClass() {
        return moduleClass;
    }

    public static class YangCapabilityInfo {
        private String namespace;
        private String revision;
        private String module;
        private String versionRegexp;
        private YangMethodInfo yangMethodInfo;
        public String getCapabilityUri() {
            return namespace + ":" + module + ":" + revision;
        }


        public String getNamespace() {
            return namespace;
        }

        public String getModule() {
            return module;
        }

        public String getRevision() {
            return revision;
        }

        public YangMethodInfo getYangMethodInfo() {
            return yangMethodInfo;
        }

        public String getVersionRegexp() {
            return versionRegexp;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }


        public YangCapabilityInfo(String namespace, String module, String revision,String versionPrefix,YangMethodInfo yangMethodInfo) {
            this.namespace = namespace;
            this.module = module;
            this.revision = revision;
            this.versionRegexp = versionPrefix;
            this.yangMethodInfo = yangMethodInfo;
        }
    }
}
