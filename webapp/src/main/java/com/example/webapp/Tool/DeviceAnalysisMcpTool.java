package com.example.webapp.Tool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.webapp.Entity.BuildingDeviceId;
import com.example.webapp.Entity.ThresholdRange;
import com.example.webapp.Entity.customer;
import com.example.webapp.Entity.energyReadings;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.ThresholdRangeMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Util.LoginCustomerHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
/**
 * MCP工具：设备运行能耗分析器
 * 专注判断设备能耗是否在阈值范围内，若越界则结合知识库和百科给出诊断建议
 */
@Slf4j
@Service
public class DeviceAnalysisMcpTool {
	@Autowired
	private energyMapper energyReadingsMapper;
	@Autowired
	private ThresholdRangeMapper thresholdRangeMapper;
	@Autowired
	private LoginService loginService;
	@Autowired
	private McpBailianKnowledgeService knowledgeService;
	@Autowired
	private McpBaikeService baikeService;
	@Autowired
	private BuildingMapper buildingMapper;
	@Tool(name = "analyze_device_energy",
			description = """
              分析设备实时能耗是否正常，判断是否超出阈值。
              当用户询问以下问题时调用此工具：
              - 设备运行正常吗 / 有没有异常
              - 能耗是不是超标了 / 为什么这么费电
              - 帮我检查一下设备状态
              """)
	public Map<String, Object> analyzeDeviceEnergy() {
		// 1. 获取当前用户及绑定设备信息
		String name = LoginCustomerHolder.getLoginCustomer().getName();
		LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<customer>().eq(customer::getName, name);
		customer one = loginService.getOne(wrapper);
		// 假设从用户实体获取关联的建筑ID和设备ID
		BuildingDeviceId id = buildingMapper.getId(one.getDeviceCode(), one.getBuildingCode());

		int buildingId = id.getBuildingId();
		int deviceId = id.getDeviceId();
		log.info("开始设备能耗独立分析，buildingId={}, deviceId={}", buildingId, deviceId);
		Map<String, Object> result = new HashMap<>();
		try {
			// 2. 获取最新的能耗记录
			LambdaQueryWrapper<energyReadings> readingsWrapper = new LambdaQueryWrapper<>();
			readingsWrapper.eq(energyReadings::getDeviceId, deviceId)
					.eq(energyReadings::getBuildingId, buildingId)
					.orderByDesc(energyReadings::getMonitoringTime) // 按监控时间倒序
					.last("LIMIT 1");
			energyReadings latestReading = energyReadingsMapper.selectOne(readingsWrapper);
			if (latestReading == null) {
				result.put("status", "DATA_MISSING");
				result.put("message", "未找到该设备的任何能耗记录");
				return result;
			}
			// 提取当前电力能耗，并安全转换为 BigDecimal
			Double currentPowerDouble = latestReading.getPowerConsumption();
			BigDecimal currentConsumption = BigDecimal.valueOf(currentPowerDouble);
			result.put("deviceId", deviceId);
			result.put("buildingId", buildingId);
			result.put("monitoringTime", latestReading.getMonitoringTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			result.put("currentPowerConsumption", currentConsumption.toPlainString() + " kWh");
			// 3. 获取当前生效的"电力能耗"阈值范围
			LocalDateTime now = LocalDateTime.now();
			LambdaQueryWrapper<ThresholdRange> thresholdWrapper = new LambdaQueryWrapper<>();
			thresholdWrapper.eq(ThresholdRange::getDeviceId, deviceId)
					.eq(ThresholdRange::getMetricName, "电力能耗") // 匹配指标名称
					.le(ThresholdRange::getEffectiveFrom, now)     // 生效时间 <= 现在
					.ge(ThresholdRange::getEffectiveTo, now)       // 失效时间 >= 现在
					.last("LIMIT 1");
			ThresholdRange threshold = thresholdRangeMapper.selectOne(thresholdWrapper);
			if (threshold == null || threshold.getMinValue() == null || threshold.getMaxValue() == null) {
				result.put("status", "THRESHOLD_MISSING");
				result.put("message", "未找到当前生效的【电力能耗】阈值范围配置");
				return result;
			}
			// 记录阈值信息
			result.put("thresholdRange", threshold.getMinValue().toPlainString() + " ~ " + threshold.getMaxValue().toPlainString() + " " + threshold.getUnit());
			result.put("thresholdDescription", threshold.getDescription());
			// 4. 判断是否异常
			boolean isBelowMin = currentConsumption.compareTo(threshold.getMinValue()) < 0;
			boolean isAboveMax = currentConsumption.compareTo(threshold.getMaxValue()) > 0;
			boolean isAnomalous = isBelowMin || isAboveMax;
			if (isAnomalous) {
				String anomalyType = isAboveMax ? "OVER_MAX_LIMIT" : "BELOW_MIN_LIMIT";
				String anomalyDesc = isAboveMax ? "电力能耗超标(超出上限)" : "电力能耗过低(低于下限)";
				log.warn("设备 {} 能耗异常！类型: {}, 当前: {}, 范围: [{} ~ {}]",
						deviceId, anomalyDesc, currentConsumption, threshold.getMinValue(), threshold.getMaxValue());
				result.put("status", "ANOMALY");
				result.put("anomalyType", anomalyType);
				result.put("anomalyLevel", isAboveMax ? "HIGH" : "MEDIUM");
				// 计算偏离程度
				BigDecimal baseValue = isAboveMax ? threshold.getMaxValue() : threshold.getMinValue();
				BigDecimal deviation = currentConsumption.subtract(baseValue).abs();
				double deviationRatio = deviation.doubleValue() / baseValue.doubleValue() * 100;
				result.put("deviationRatio", String.format("%.2f%%", deviationRatio));
				// 4.1 查询专业规范知识库
				String kbQuery = isAboveMax ?
						"建筑电气设备能耗超标排查 供配电系统异常高负荷运行规范" :
						"建筑设备能耗异常偏低 供电不足故障排查规范";
				String knowledgeAdvice = knowledgeService.searchKnowledgeBase(kbQuery);
				// 4.2 查询百度百科获取概念性解释
				String baikeKeyword = isAboveMax ? "建筑设备高负荷运行" : "建筑设备低效运行异常";
				String baikeExplanation = baikeService.searchBaike(baikeKeyword);
				// 4.3 封装异常诊断报告
				Map<String, Object> anomalyReport = new HashMap<>();
				anomalyReport.put("detectionTime", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
				anomalyReport.put("anomalyDescription", anomalyDesc);
				anomalyReport.put("professionalNorms", knowledgeAdvice);
				anomalyReport.put("conceptExplanation", baikeExplanation);
				anomalyReport.put("suggestionPrompt",
						String.format("设备%s%s，偏离阈值%.2f%%。请结合上述《GB55024-2022》规范要求与百科知识，给出具体的排障步骤。",
								deviceId, anomalyDesc, deviationRatio));
				result.put("anomalyReport", anomalyReport);
			} else {
				// 无异常：直接反馈健康状态
				log.info("设备 {} 运行正常", deviceId);
				result.put("status", "NORMAL");
				result.put("healthStatus", "设备运行良好，电力能耗在合理阈值范围内");
				result.put("nextStepSuggestion", "当前设备无异常，如需结合天气生成节能优化策略，请调用建筑能源优化工具。");
			}
			return result;
		} catch (Exception e) {
			log.error("设备能耗分析失败", e);
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", true);
			errorResponse.put("message", "分析过程出错: " + e.getMessage());
			return errorResponse;
		}
	}
}