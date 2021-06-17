package pro.panopticon.client.util

import com.sun.management.UnixOperatingSystemMXBean
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.OperatingSystemMXBean
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class SystemStatus {
    private val eden: Optional<MemoryPoolMXBean>
    private val survivor: Optional<MemoryPoolMXBean>
    private val old: Optional<MemoryPoolMXBean>
    private val osBean: OperatingSystemMXBean

    fun heapUsed(): Long {
        return edenUsed() + survivorUsed() + oldUsed()
    }

    fun heapMax(): Long {
        return edenMax() + survivorMax() + oldMax()
    }

    fun heapAfterGC(): Long {
        return edenGC() + survivorAfterGC() + oldAfterGC()
    }

    fun edenUsed(): Long {
        return getMemUsage(eden, getUsed)
    }

    fun edenMax(): Long {
        return getMemUsage(eden, getMax)
    }

    fun edenGC(): Long {
        return getMemUsage(eden, getUsedAfterGC)
    }

    fun survivorUsed(): Long {
        return getMemUsage(survivor, getUsed)
    }

    fun survivorMax(): Long {
        return getMemUsage(survivor, getMax)
    }

    fun survivorAfterGC(): Long {
        return getMemUsage(survivor, getUsedAfterGC)
    }

    fun oldUsed(): Long {
        return getMemUsage(old, getUsed)
    }

    fun oldMax(): Long {
        return getMemUsage(old, getMax)
    }

    fun oldAfterGC(): Long {
        return getMemUsage(old, getUsedAfterGC)
    }

    fun load(): Double {
        return osBean.systemLoadAverage
    }

    fun openFileHandles(): Long {
        return if (osBean is UnixOperatingSystemMXBean) {
            osBean.openFileDescriptorCount
        } else -1
    }

    fun maxFileHandles(): Long {
        return if (osBean is UnixOperatingSystemMXBean) {
            osBean.maxFileDescriptorCount
        } else -1
    }

    private fun findBeanWithName(vararg names: String): Optional<MemoryPoolMXBean> {
        val memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans()
        for (memoryPool in memoryPoolMXBeans) {
            for (name in names) {
                if (memoryPool.name.endsWith(name)) {
                    return Optional.of(memoryPool)
                }
            }
        }
        LOG.error("Could not fetch MemoryPoolMXBean for {}. These beans exists: {}",
            Stream.of(*names).collect(Collectors.joining(", ")),
            memoryPoolMXBeans.stream().map { obj: MemoryPoolMXBean -> obj.name }.collect(Collectors.joining(", ")))
        return Optional.empty()
    }

    private fun getMemUsage(mxBean: Optional<MemoryPoolMXBean>, function: Function<MemoryPoolMXBean, Long>): Long {
        return mxBean.map(function).orElse(DEFAULT_MEM_USAGE)
    }

    private val getUsed = Function { memoryPool: MemoryPoolMXBean -> memoryPool.usage.used }
    private val getMax = Function { memoryPool: MemoryPoolMXBean -> memoryPool.usage.max }
    private val getUsedAfterGC = Function { memoryPool: MemoryPoolMXBean -> memoryPool.collectionUsage.used }

    companion object {
        private val LOG = LoggerFactory.getLogger(SystemStatus::class.java)
        private const val DEFAULT_MEM_USAGE = -1L
    }

    init {
        eden = findBeanWithName("Eden Space")
        survivor = findBeanWithName("Survivor Space")
        old = findBeanWithName("Old Gen", "Tenured", "Tenured Gen")
        osBean = ManagementFactory.getOperatingSystemMXBean()
    }
}
