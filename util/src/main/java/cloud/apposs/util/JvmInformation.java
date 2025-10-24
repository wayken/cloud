package cloud.apposs.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

public enum JvmInformation {
    /**
     * Represents HotSpot 32-bit
     */
    HOTSPOT_32_BIT  {
        /** default values are for this vm */
        @Override
        public String getJvmDescription() {
            return "32-Bit HotSpot JVM";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 32-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_32_BIT_WITH_CONCURRENT_MARK_AND_SWEEP {
        @Override
        public int getMinimumObjectSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "32-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM
     */
    HOTSPOT_64_BIT {
        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 8;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP {
        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 8;
        }

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Compressed OOPs
     */
    HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS {
        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Compressed OOPs";
        }
    },

    /**
     * Represents 64-Bit HotSpot JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC
     */
    HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP {
        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public int getMinimumObjectSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit HotSpot JVM with Compressed OOPs and Concurrent Mark-and-Sweep GC";
        }
    },

    /**
     * Represents 32-Bit JRockit JVM"
     */
    JROCKIT_32_BIT {
        @Override
        public int getAgentSizeOfAdjustment() {
            return 8;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 8;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "32-Bit JRockit JVM";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit JRockit JVM (with no reference compression)
     */
    JROCKIT_64_BIT {
        @Override
        public int getAgentSizeOfAdjustment() {
            return 8;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 8;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit JRockit JVM (with no reference compression)";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit JRockit JVM with 4GB Compressed References
     */
    JROCKIT_64_BIT_WITH_4GB_COMPRESSED_REFS {
        @Override
        public int getAgentSizeOfAdjustment() {
            return 8;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 8;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit JRockit JVM with 4GB Compressed References";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit JRockit JVM with 32GB Compressed References
     */
    JROCKIT_64_BIT_WITH_32GB_COMPRESSED_REFS {
        @Override
        public int getAgentSizeOfAdjustment() {
            return 8;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 8;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit JRockit JVM with 32GB Compressed References";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit JRockit JVM with 64GB Compressed References
     */
    JROCKIT_64_BIT_WITH_64GB_COMPRESSED_REFS {
        @Override
        public int getObjectAlignment() {
            return 16;
        }

        @Override
        public int getAgentSizeOfAdjustment() {
            return 16;
        }

        @Override
        public int getFieldOffsetAdjustment() {
            return 16;
        }

        @Override
        public int getObjectHeaderSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "64-Bit JRockit JVM with 64GB Compressed References";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit IBM JVM (with reference compression)
     */
    IBM_64_BIT_WITH_COMPRESSED_REFS {

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }

        @Override
        public String getJvmDescription() {
            return "IBM 64-Bit JVM with Compressed References";
        }
    },

    /**
     * Represents 64-Bit IBM JVM (with no reference compression)
     */
    IBM_64_BIT {

        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 8;
        }

        @Override
        public int getObjectHeaderSize() {
            return 24;
        }

        @Override
        public String getJvmDescription() {
            return "IBM 64-Bit JVM (with no reference compression)";
        }
    },

    /**
     * Represents IBM 32-bit
     */
    IBM_32_BIT  {
        /** default values are for this vm */
        @Override
        public String getJvmDescription() {
            return "IBM 32-Bit JVM";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }

        @Override
        public int getObjectHeaderSize() {
            return 16;
        }
    },

    /**
     * Represents Generic 32-bit
     */
    UNKNOWN_32_BIT  {

        /* default values are for this vm */

        @Override
        public String getJvmDescription() {
            return "Unrecognized 32-Bit JVM";
        }

        @Override
        public int getPointerSize() {
            return 4;
        }

        @Override
        public int getJavaPointerSize() {
            return 4;
        }
    },

    /**
     * Represents 64-Bit Generic JVM
     */
    UNKNOWN_64_BIT {
        @Override
        public int getPointerSize() {
            return 8;
        }

        @Override
        public int getJavaPointerSize() {
            return 8;
        }

        @Override
        public String getJvmDescription() {
            return "Unrecognized 64-Bit JVM";
        }
    };

    /**
     * The JvmInformation instance representing the current JVM
     */
    public static final JvmInformation CURRENT_JVM_INFORMATION;

    private static final long THREE_GB = 3L * 1024L * 1024L * 1024L;
    private static final long TWENTY_FIVE_GB = 25L * 1024L * 1024L * 1024L;
    private static final long FIFTY_SEVEN_GB = 57L * 1024L * 1024L * 1024L;


    static {
        CURRENT_JVM_INFORMATION = getJvmInformation();
    }

    /**
     * Size of a pointer in bytes on this runtime
     */
    public abstract int getPointerSize();

    /**
     * Size of a java pointer in bytes on this runtime (that differs when compressedOops are being used)
     */
    public abstract int getJavaPointerSize();

    /**
     * Minimal size an object will occupy on the heap in bytes.
     */
    public int getMinimumObjectSize() {
        return getObjectAlignment();
    }

    /**
     * Object alignment / padding in bytes
     */
    public int getObjectAlignment() {
        return 8;
    }

    /**
     * The size of an object header in bytes.
     */
    public int getObjectHeaderSize() {
        return getPointerSize() + getJavaPointerSize();
    }

    /**
     * The size of the jvm-specific field offset adjustment in bytes.
     */
    public int getFieldOffsetAdjustment() {
        return 0;
    }

    /**
     * The size of the jvm-specific agent result adjustment in bytes.
     */
    public int getAgentSizeOfAdjustment() {
        return 0;
    }

    /**
     * A human-readable description of the JVM and its relevant enabled options.Os
     */
    public abstract String getJvmDescription();

    /**
     * Determine the JvmInformation for the current JVM.
     */
    private static JvmInformation getJvmInformation() {
        JvmInformation jif = null;

        jif = detectHotSpot();

        if (jif == null) {
            jif = detectJRockit();
        }
        if (jif == null) {
            jif = detectIBM();
        }

        if (jif == null && is64Bit()) {
            // unknown 64-bit JVMs
            jif = UNKNOWN_64_BIT;
        } else if (jif == null) {
            // unknown 32-bit JVMs
            jif = UNKNOWN_32_BIT;
        }

        return jif;
    }

    private static JvmInformation detectHotSpot() {
        JvmInformation jif = null;

        if (isHotspot()) {
            if (is64Bit()) {
                if (isHotspotCompressedOops() && isHotspotConcurrentMarkSweepGC()) {
                    jif = HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS_AND_CONCURRENT_MARK_AND_SWEEP;
                } else if (isHotspotCompressedOops()) {
                    jif = HOTSPOT_64_BIT_WITH_COMPRESSED_OOPS;
                } else if (isHotspotConcurrentMarkSweepGC()) {
                    jif = HOTSPOT_64_BIT_WITH_CONCURRENT_MARK_AND_SWEEP;
                } else {
                    jif = HOTSPOT_64_BIT;
                }
            } else {
                jif = HOTSPOT_32_BIT;
            }
        }

        return jif;
    }
    private static JvmInformation detectJRockit() {
        JvmInformation jif = null;

        if (isJRockit()) {
            if (is64Bit()) {
                if (isJRockit4GBCompression()) {
                    jif = JROCKIT_64_BIT_WITH_4GB_COMPRESSED_REFS;
                } else if (isJRockit32GBCompression()) {
                    jif = JROCKIT_64_BIT_WITH_32GB_COMPRESSED_REFS;
                } else if (isJRockit64GBCompression()) {
                    jif = JROCKIT_64_BIT_WITH_64GB_COMPRESSED_REFS;
                } else {
                    jif = JROCKIT_64_BIT;
                }
            } else {
                jif = JROCKIT_32_BIT;
            }
        }

        return jif;
    }

    private static JvmInformation detectIBM() {
        JvmInformation jif = null;

        if (isIBM()) {
            if (is64Bit()) {
                if (isIBMCompressedRefs()) {
                    jif = IBM_64_BIT_WITH_COMPRESSED_REFS;
                } else {
                    jif = IBM_64_BIT;
                }
            } else {
                jif = IBM_32_BIT;
            }
        }

        return jif;
    }

    private static boolean isJRockit32GBCompression() {

        if (getJRockitVmArgs().contains("-XXcompressedRefs:enable=false")) {
            return false;
        }
        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=64GB") ||
                getJRockitVmArgs().contains("-XXcompressedRefs:size=4GB")) {
            return false;
        }

        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=32GB")) {
            return true;
        }
        if (Runtime.getRuntime().maxMemory() > THREE_GB && Runtime.getRuntime().maxMemory() <= TWENTY_FIVE_GB
                && getJRockitVmArgs().contains("-XXcompressedRefs:enable=true")) {
            return true;
        }

        return false;
    }

    private static boolean isJRockit64GBCompression() {
        if (getJRockitVmArgs().contains("-XXcompressedRefs:enable=false")) {
            return false;
        }
        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=4GB") ||
                getJRockitVmArgs().contains("-XXcompressedRefs:size=32GB")) {
            return false;
        }

        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=64GB")) {
            return true;
        }
        if (Runtime.getRuntime().maxMemory() > TWENTY_FIVE_GB && Runtime.getRuntime().maxMemory() <= FIFTY_SEVEN_GB
                && getJRockitVmArgs().contains("-XXcompressedRefs:enable=true")) {
            return true;
        }

        return false;
    }

    private static boolean isJRockit4GBCompression() {
        if (getJRockitVmArgs().contains("-XXcompressedRefs:enable=false")) {
            return false;
        }
        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=64GB") ||
                getJRockitVmArgs().contains("-XXcompressedRefs:size=32GB")) {
            return false;
        }

        if (getJRockitVmArgs().contains("-XXcompressedRefs:size=4GB")) {
            return true;
        }
        if (Runtime.getRuntime().maxMemory() <= THREE_GB) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if VM is JRockit
     * @return true, if JRockit
     */
    public static boolean isJRockit() {
        return System.getProperty("jrockit.version") != null
               || System.getProperty("java.vm.name", "").toLowerCase().indexOf("jrockit") >= 0;
    }

    /**
     * Returns true if VM vendor is Hotspot
     * @return true, if Hotspot
     */
    public static boolean isHotspot() {
        return System.getProperty("java.vm.name", "").toLowerCase().contains("hotspot");
    }

    /**
     * Returns true if VM vendor is IBM
     * @return true, if IBM
     */
    public static boolean isIBM() {
        return System.getProperty("java.vm.name", "").contains("IBM") && System.getProperty("java.vm.vendor").contains("IBM");
    }

    private static boolean isIBMCompressedRefs() {
        return System.getProperty("com.ibm.oti.vm.bootstrap.library.path", "").contains("compressedrefs");
    }

    private static boolean isHotspotCompressedOops() {
        String value = getHotSpotVmOptionValue("UseCompressedOops");
        if (value == null) {
            return false;
        } else {
            return Boolean.valueOf(value);
        }
    }

    private static String getHotSpotVmOptionValue(String name) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName beanName = ObjectName.getInstance("com.sun.management:type=HotSpotDiagnostic");
            Object vmOption = server.invoke(beanName, "getVMOption", new Object[] {name}, new String[] {"java.lang.String"});
            return (String)((CompositeData)vmOption).get("value");
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getPlatformMBeanAttribute(String beanName, String attrName) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance(beanName);
            Object attr = server.getAttribute(name, attrName).toString();
            if (attr != null) {
                return attr.toString();
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getJRockitVmArgs() {
        return getPlatformMBeanAttribute("oracle.jrockit.management:type=PerfCounters", "java.rt.vmArgs");
    }

    private static boolean isHotspotConcurrentMarkSweepGC() {
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if ("ConcurrentMarkSweep".equals(bean.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean is64Bit() {
        String systemProp;
        systemProp = System.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return systemProp.equals("64");
        }
        systemProp = System.getProperty("java.vm.version");
        if (systemProp != null) {
            return systemProp.contains("_64");
        }
        return false;
    }
    
    /**
     * Primitive types in the VM type system and their sizes
     * @author Alex Snaps
     */
    public static enum PrimitiveType {

        /**
         * boolean.class
         */
        BOOLEAN(boolean.class, 1),
        /**
         * byte.class
         */
        BYTE(byte.class, 1),
        /**
         * char.class
         */
        CHAR(char.class, 2),
        /**
         * short.class
         */
        SHORT(short.class, 2),
        /**
         * int.class
         */
        INT(int.class, 4),
        /**
         * float.class
         */
        FLOAT(float.class, 4),
        /**
         * double.class
         */
        DOUBLE(double.class, 8),
        /**
         * long.class
         */
        LONG(long.class, 8);

        private Class<?> type;
        private int size;


        private PrimitiveType(Class<?> type, int size) {
            this.type = type;
            this.size = size;
        }

        /**
         * Returns the size in memory this type occupies
         * @return size in bytes
         */
        public int getSize() {
            return size;
        }

        /**
         * The representing type
         * @return the type
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * The size of a pointer
         * @return size in bytes
         */
        public static int getReferenceSize() {
            return CURRENT_JVM_INFORMATION.getJavaPointerSize();
        }

        /**
         * The size on an array
         * @return size in bytes
         */
        public static long getArraySize() {
            return CURRENT_JVM_INFORMATION.getObjectHeaderSize() + INT.getSize();
        }

        /**
         * Finds the matching PrimitiveType for a type
         * @param type the type to find the PrimitiveType for
         * @return the PrimitiveType instance or null if none found
         */
        public static PrimitiveType forType(final Class<?> type) {
            for (PrimitiveType primitiveType : values()) {
                if (primitiveType.getType() == type) {
                    return primitiveType;
                }
            }
            return null;
        }
    }
}
