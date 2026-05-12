package com.example.Service.Opt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.Entity.AlertRecord;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Entity.Devices;
import com.example.Entity.energyReadings;
import com.example.Enum.DeviceStatusConstants;
import com.example.Mapper.AlertRecordMapper;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.AlertRecordService;
import com.example.Service.AnalysisService.ThresholdRangeService;
import com.example.Service.DevicesService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.example.Util.ThresholdCheckUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeviceMonitorTask {

	@Autowired
	private EnergyReadingsMapper energyReadingsMapper;

	@Autowired
	private ThresholdRangeService thresholdRangeService;

	@Autowired
	private DevicesService devicesService;

	@Autowired
	private AlertRecordMapper alertRecordMapper;

	@Autowired
	private AlertRecordService alertRecordService;

	@Autowired
	private RedissonClient redissonClient;

	private static final int LATEST_RECORDS_COUNT = 7;
	private static final int CONSECUTIVE_OUT_OF_RANGE = 2;
	private static final int ALERT_DEDUP_MINUTES = 10;

	/**
	 * 可配置的 cron 表达式，默认每 4 小时执行一次
	 * 在 application.yml 中配置: monitor.task.cron=0 0 0/4 * * ?
	 */
	@Scheduled(cron = "${monitor.task.cron:0 0 0/4 * * ?}")
	public void checkEnergyThresholds() {
		// 分布式锁：防止多实例同时执行
		RLock lock = redissonClient.getLock("lock:device-monitor-task");
		try {
			if (!lock.tryLock(10,TimeUnit.MINUTES)) {
				log.info("另一个实例正在执行监控任务，本次跳过");
				return;
			}
		} catch (InterruptedException e) {
			log.warn("获取分布式锁被中断", e);
			Thread.currentThread().interrupt();
			return;
		}

		try {
			executeTask();
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	/**
	 * 实际执行逻辑，加事务保护
	 */
	@Transactional(rollbackFor = Exception.class)
	public void executeTask() {
		log.info("========== 设备能耗监控任务开始 ==========");
		long startTime = System.currentTimeMillis();

		// 1. 获取最新读数
		List<energyReadings> latestReadings = energyReadingsMapper.selectLatestNByDevice(LATEST_RECORDS_COUNT);
		if (CollectionUtils.isEmpty(latestReadings)) {
			log.info("无任何设备的最新能耗数据，任务结束。");
			return;
		}

		long deviceCount = latestReadings.stream().map(energyReadings::getDeviceId).distinct().count();
		log.info("共获取到 {} 个设备的最新 {} 条能耗记录", deviceCount, latestReadings.size());

		// 2. 生效阈值
		LocalDateTime now = LocalDateTime.now();
		List<ThresholdRange> activeThresholds = thresholdRangeService.list(
				new LambdaQueryWrapper<ThresholdRange>()
						.and(w -> w.isNull(ThresholdRange::getEffectiveFrom).or().le(ThresholdRange::getEffectiveFrom, now))
						.and(w -> w.isNull(ThresholdRange::getEffectiveTo).or().ge(ThresholdRange::getEffectiveTo, now))
		);
		log.info("当前生效阈值规则数量：{}", activeThresholds.size());
		if (CollectionUtils.isEmpty(activeThresholds)) {
			log.info("无生效中的阈值规则，任务结束。");
			return;
		}

		// 3. 阈值Map
		Map<String, List<ThresholdRange>> thresholdMap = activeThresholds.stream()
				.collect(Collectors.groupingBy(t ->
						(t.getDeviceId() != null ? t.getDeviceId() : "null") + "_" +
								(t.getBuildingId() != null ? t.getBuildingId() : "null")
				));

		// 4. 设备读数分组
		Map<Integer, List<energyReadings>> deviceReadingsMap = latestReadings.stream()
				.collect(Collectors.groupingBy(energyReadings::getDeviceId));

		// 5. 设备信息
		Set<Integer> deviceIdSet = deviceReadingsMap.keySet();
		Map<Integer, Devices> deviceMap = devicesService.listByIds(deviceIdSet)
				.stream()
				.collect(Collectors.toMap(Devices::getDeviceId, d -> d));

		Map<Integer, String> statusUpdates = new HashMap<>();
		List<AlertRecord> alertRecordsToInsert = new ArrayList<>();
		Set<Long> alertIdsToResolve = new HashSet<>();
		int processedDevices = 0;
		int skippedDevices = 0;

		// 6. 遍历设备（每个设备独立 try-catch，一个失败不影响其他）
		for (Map.Entry<Integer, List<energyReadings>> entry : deviceReadingsMap.entrySet()) {
			Integer deviceId = entry.getKey();
			List<energyReadings> readings = entry.getValue();

			try {
				Devices device = deviceMap.get(deviceId);
				if (device == null) {
					log.warn("设备 {} 在devices表中不存在，跳过", deviceId);
					skippedDevices++;
					continue;
				}

				log.debug("------ 检查设备 {} (状态: {})，读数数量: {} ------",
						deviceId, device.getDeviceStatus(), readings.size());

				if (DeviceStatusConstants.STATUS_MAINTENANCE.equals(device.getDeviceStatus())) {
					log.debug("设备 {} 处于维护保养状态，跳过监控", deviceId);
					skippedDevices++;
					continue;
				}

				Integer buildingId = readings.get(0).getBuildingId();

				// 阈值匹配
				List<ThresholdRange> thresholds = thresholdMap.get(deviceId + "_" + buildingId);
				if (CollectionUtils.isEmpty(thresholds)) {
					thresholds = thresholdMap.get("null_" + buildingId);
				}
				if (CollectionUtils.isEmpty(thresholds)) {
					thresholds = thresholdMap.get("null_null");
				}
				if (CollectionUtils.isEmpty(thresholds)) {
					log.debug("设备 {} 无任何匹配阈值，跳过", deviceId);
					skippedDevices++;
					continue;
				}

				// 按时间降序
				readings.sort((r1, r2) -> r2.getMonitoringTime().compareTo(r1.getMonitoringTime()));
				energyReadings latest = readings.get(0);
				log.debug("设备 {} 最新读数时间: {}", deviceId, latest.getMonitoringTime());

				boolean deviceAlert = false;
				for (ThresholdRange range : thresholds) {
					Double actualValue = ThresholdCheckUtil.getFieldValueByName(latest, range.getMetricName());
					log.debug("检查指标: {}，实际值: {}，阈值范围: [{}, {}]",
							range.getMetricName(), actualValue, range.getMinValue(), range.getMaxValue());

					boolean metricAlert = ThresholdCheckUtil.checkMetricConsecutiveOutOfRange(readings, range, CONSECUTIVE_OUT_OF_RANGE);
					if (metricAlert) {
						log.warn("设备 {} 指标 {} 连续越界，触发告警", deviceId, range.getMetricName());
						deviceAlert = true;
						// 构建告警记录
						AlertRecord record = new AlertRecord();
						record.setDeviceId(deviceId);
						record.setDeviceCode(device.getDeviceCode());
						record.setBuildingId(buildingId);
						record.setReadingId(latest.getReadingId());
						record.setThresholdId(range.getId());
						record.setMetricName(range.getMetricName());
						record.setAbnormalValue(BigDecimal.valueOf(actualValue));
						record.setMinValue(range.getMinValue());
						record.setMaxValue(range.getMaxValue());
						record.setUnit(range.getUnit());
						record.setAlertType(AlertRecord.AlertType.valueOf(ThresholdCheckUtil.determineAlertType(actualValue, range)));
						record.setAlertLevel(ThresholdCheckUtil.calculateAlertLevel(actualValue, range));
						record.setStatus(AlertRecord.STATUS_PENDING);
						alertRecordsToInsert.add(record);
					}
				}

				// 状态变更
				String currentStatus = device.getDeviceStatus();
				String targetStatus = deviceAlert ? DeviceStatusConstants.STATUS_FAULT : DeviceStatusConstants.STATUS_NORMAL;
				if (!targetStatus.equals(currentStatus)) {
					statusUpdates.put(deviceId, targetStatus);
					log.info("设备 {} 状态变更: {} -> {}", deviceId, currentStatus, targetStatus);
				}

				if (!deviceAlert) {
					log.debug("设备 {} 本次检查正常，未触发告警", deviceId);
					List<AlertRecord> pendingAlerts = alertRecordMapper.selectList(
							new LambdaQueryWrapper<AlertRecord>()
									.eq(AlertRecord::getDeviceId, deviceId)
									.eq(AlertRecord::getStatus, AlertRecord.STATUS_PENDING)
					);
					pendingAlerts.forEach(alert -> alertIdsToResolve.add(alert.getId()));
				}

				processedDevices++;
			} catch (Exception e) {
				log.error("处理设备 {} 异常，跳过继续处理其他设备", deviceId, e);
				skippedDevices++;
			}
		}

		// 7. 批量插入告警（去重后）
		int newAlertCount = 0;
		if (!alertRecordsToInsert.isEmpty()) {
			List<AlertRecord> deduped = deduplicateAlerts(alertRecordsToInsert);
			if (!deduped.isEmpty()) {
				// 批量插入，一次 SQL
				alertRecordService.saveBatch(deduped);
				newAlertCount = deduped.size();
				log.info("本次任务新增 {} 条告警记录", newAlertCount);
			}
		}

		// 8. 批量更新设备状态
		int statusUpdateCount = 0;
		if (!statusUpdates.isEmpty()) {
			List<Devices> updateList = statusUpdates.entrySet().stream()
					.map(e -> {
						Devices d = new Devices();
						d.setDeviceId(e.getKey());
						d.setDeviceStatus(e.getValue());
						return d;
					})
					.collect(Collectors.toList());
			devicesService.updateBatchById(updateList);
			statusUpdateCount = updateList.size();
			log.info("本次任务共更新 {} 台设备状态", statusUpdateCount);
		}

		// 9. 批量关闭告警
		int closedAlertCount = 0;
		if (!alertIdsToResolve.isEmpty()) {
			AlertRecord updateRecord = new AlertRecord();
			updateRecord.setStatus(AlertRecord.STATUS_RESOLVED);
			updateRecord.setHandledAt(LocalDateTime.now());
			updateRecord.setRemark("设备状态恢复正常，系统自动关闭");
			alertRecordMapper.update(updateRecord,
					new LambdaUpdateWrapper<AlertRecord>().in(AlertRecord::getId, alertIdsToResolve));
			closedAlertCount = alertIdsToResolve.size();
			log.info("本次任务共关闭 {} 条告警记录", closedAlertCount);
		}

		long costTime = System.currentTimeMillis() - startTime;
		log.info("========== 设备能耗监控任务结束 ==========");
		log.info("汇总：检查 {} 台设备（成功 {}，跳过 {}），新增告警 {} 条，更新设备状态 {} 台，关闭告警 {} 条，耗时 {}ms",
				deviceCount, processedDevices, skippedDevices, newAlertCount, statusUpdateCount, closedAlertCount, costTime);
	}

	/**
	 * 批量去重：一次查询已有告警，在内存中过滤（替代 N+1 查询）
	 */
	private List<AlertRecord> deduplicateAlerts(List<AlertRecord> newAlerts) {
		if (newAlerts.isEmpty()) return newAlerts;

		LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(ALERT_DEDUP_MINUTES);

		// 一次查询获取所有相关的已有 PENDING 告警
		Set<String> existingKeys = alertRecordMapper.selectList(
				new LambdaQueryWrapper<AlertRecord>()
						.eq(AlertRecord::getStatus, AlertRecord.STATUS_PENDING)
						.ge(AlertRecord::getCreatedAt, thresholdTime)
						.select(AlertRecord::getDeviceId, AlertRecord::getMetricName)
		).stream()
				.map(a -> a.getDeviceId() + "_" + a.getMetricName())
				.collect(Collectors.toSet());

		// 内存中过滤
		return newAlerts.stream()
				.filter(alert -> !existingKeys.contains(alert.getDeviceId() + "_" + alert.getMetricName()))
				.collect(Collectors.toList());
	}

}
