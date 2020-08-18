package com.airlenet.netconf.nettyutil.handler;

import javax.xml.transform.*;

final class ThreadLocalTransformers {
    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();

    private static final ThreadLocal<Transformer> DEFAULT_TRANSFORMER = new ThreadLocal<Transformer>() {
        @Override
        protected Transformer initialValue() {
            return createTransformer();
        }

        @Override
        public void set(final Transformer value) {
            throw new UnsupportedOperationException();
        }
    };

    private static final ThreadLocal<Transformer> PRETTY_TRANSFORMER = new ThreadLocal<Transformer>() {
        @Override
        protected Transformer initialValue() {
            final Transformer ret = createTransformer();
            ret.setOutputProperty(OutputKeys.INDENT, "yes");
            ret.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            return ret;
        }

        @Override
        public void set(final Transformer value) {
            throw new UnsupportedOperationException();
        }
    };

    private ThreadLocalTransformers() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get the transformer with default configuration.
     *
     * @return A transformer with default configuration based on the default implementation.
     */
    static Transformer getDefaultTransformer() {
        return DEFAULT_TRANSFORMER.get();
    }

    /**
     * Get the transformer with default configuration, but with automatic indentation
     * and the XML declaration removed.
     *
     * @return A transformer with human-friendly configuration.
     */
    static Transformer getPrettyTransformer() {
        return PRETTY_TRANSFORMER.get();
    }

//    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
//            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static Transformer createTransformer() {
        try {
            return FACTORY.newTransformer();
        } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            throw new IllegalStateException("Unexpected error while instantiating a Transformer", e);
        }
    }
}
