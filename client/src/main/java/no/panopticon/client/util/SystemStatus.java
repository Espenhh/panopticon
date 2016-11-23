package no.panopticon.client.util;

import com.sun.management.UnixOperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@SuppressWarnings("restriction")
public class SystemStatus {

	private static final Logger LOG = LoggerFactory.getLogger(SystemStatus.class);

	private static final long DEFAULT_MEM_USAGE = -1L;

	private final Optional<MemoryPoolMXBean> eden;
	private final Optional<MemoryPoolMXBean> survivor;
	private final Optional<MemoryPoolMXBean> old;
	private final OperatingSystemMXBean osBean;

	public SystemStatus() {
		eden = findBeanWithName("Eden Space");
		survivor = findBeanWithName("Survivor Space");
		old = findBeanWithName("Old Gen", "Tenured", "Tenured Gen");
		osBean = ManagementFactory.getOperatingSystemMXBean();
	}

	public long heapUsed() {
		return edenUsed() + survivorUsed() + oldUsed();
	}

	public long heapMax() {
		return edenMax() + survivorMax() + oldMax();
	}

	public long heapAfterGC() {
		return edenGC() + survivorAfterGC() + oldAfterGC();
	}

	public long edenUsed() {
		return getMemUsage(eden, getUsed);
	}

	public long edenMax() {
		return getMemUsage(eden, getMax);
	}

	public long edenGC() {
		return getMemUsage(eden, getUsedAfterGC);
	}

	public long survivorUsed() {
		return getMemUsage(survivor, getUsed);
	}

	public long survivorMax() {
		return getMemUsage(survivor, getMax);
	}

	public long survivorAfterGC() {
		return getMemUsage(survivor, getUsedAfterGC);
	}

	public long oldUsed() {
		return getMemUsage(old, getUsed);
	}

	public long oldMax() {
		return getMemUsage(old, getMax);
	}

	public long oldAfterGC() {
		return getMemUsage(old, getUsedAfterGC);
	}

	public double load() {
		return osBean.getSystemLoadAverage();
	}

	public long openFileHandles() {
		if (osBean instanceof UnixOperatingSystemMXBean) {
			return ((UnixOperatingSystemMXBean) osBean).getOpenFileDescriptorCount();
		}
		return -1;
	}

	private Optional<MemoryPoolMXBean> findBeanWithName(final String... names) {

		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean memoryPool : memoryPoolMXBeans) {
			for (String name : names) {
				if (memoryPool.getName().endsWith(name)) {
					return Optional.of(memoryPool);
				}
			}
		}
		LOG.error("Could not fetch MemoryPoolMXBean for {}. These beans exists: {}",
				Stream.of(names).collect(joining(", ")),
				memoryPoolMXBeans.stream().map(MemoryPoolMXBean::getName).collect(joining(", ")));

		return Optional.empty();
	}

	private long getMemUsage(final Optional<MemoryPoolMXBean> mxBean, final Function<MemoryPoolMXBean, Long> function) {
		return mxBean.map(function).orElse(DEFAULT_MEM_USAGE);
	}

	private final Function<MemoryPoolMXBean, Long> getUsed = memoryPool -> memoryPool.getUsage().getUsed();
	private final Function<MemoryPoolMXBean, Long> getMax = memoryPool -> memoryPool.getUsage().getMax();
	private final Function<MemoryPoolMXBean, Long> getUsedAfterGC = memoryPool -> memoryPool.getCollectionUsage().getUsed();
}
