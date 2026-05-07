package org.webSocketDemo.config;
import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.webSocketDemo.Entity.ChatMessage;
import org.webSocketDemo.Entity.User;
import org.webSocketDemo.Entity.customer;
import org.webSocketDemo.Mapper.ChatMessageMapper;
import org.webSocketDemo.Mapper.CustomerMapper;
import org.webSocketDemo.Mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
	private final ChatMessageMapper chatMessageMapper;
	private final UserMapper userMapper;
	private final CustomerMapper customerMapper;
	public ChatWebSocketHandler(ChatMessageMapper chatMessageMapper, UserMapper userMapper, CustomerMapper customerMapper) {
		this.chatMessageMapper = chatMessageMapper;
		this.userMapper = userMapper;
		this.customerMapper = customerMapper;
	}
	// 在线连接池：Key格式 USER:123 或 CUSTOMER:456
	private static final ConcurrentHashMap<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();
	// 顾客绑定关系：Key-顾客ID, Value-客服ID
	private static final ConcurrentHashMap<String, String> CUSTOMER_BINDING = new ConcurrentHashMap<>();
	/**
	 * 连接建立：更新数据库在线状态
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String senderType = getAttr(session, "senderType");
		String senderId = getAttr(session, "senderId");
		String sessionKey = getSessionKey(senderType, senderId);
		SESSION_MAP.put(sessionKey, session);
		log.info("上线: {}, 当前在线总数: {}", sessionKey, SESSION_MAP.size());
		// 如果是客服登录，更新数据库状态为在线
		if ("USER".equals(senderType)) {
			try {
				User user = userMapper.selectById(Long.valueOf(senderId));
				if (user != null) {
					user.setNowStatus("在线");
					userMapper.updateById(user);
				}
			} catch (NumberFormatException e) {
				log.error("客服ID格式转换失败: {}", senderId);
			}
		}
	}
	/**
	 * 消息路由与业务处理
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		ChatMessage chatMessage = JSON.parseObject(payload, ChatMessage.class);
		String senderType = getAttr(session, "senderType");
		String senderId = getAttr(session, "senderId");
		chatMessage.setSenderType(senderType);
		chatMessage.setSenderId(senderId);
		chatMessage.setCreateTime(LocalDateTime.now());
		// 自动从数据库查询真实姓名填充到实体类
		fillSenderName(chatMessage);
		// 1. 处理客服主动断开指令 (type = 3)
		if (chatMessage.getType() != null && chatMessage.getType() == 3 && "USER".equals(senderType)) {
			String customerId = chatMessage.getReceiverId();
			if (customerId != null && senderId.equals(CUSTOMER_BINDING.get(customerId))) {
				CUSTOMER_BINDING.remove(customerId);
				ChatMessage sysMsgToCust = buildSystemMsg(customerId, "客服已结束服务，正在为您重新分配...");
				sendMessage(getSessionKey("CUSTOMER", customerId), sysMsgToCust);
				chatMessageMapper.insert(sysMsgToCust);
				ChatMessage sysMsgToUsers = buildSystemMsg(null, "顾客[" + getCustomerName(customerId) + "]已释放，可重新接入");
				broadcastToUsers(sysMsgToUsers);
				chatMessageMapper.insert(sysMsgToUsers);
			}
			return;
		}
		// 2. 处理普通聊天消息 (type = 1)
		if (chatMessage.getType() != null && chatMessage.getType() == 1) {
			chatMessageMapper.insert(chatMessage); // 存库
			if ("CUSTOMER".equals(senderType)) {
				// --- 顾客发消息 ---
				String boundAdminId = CUSTOMER_BINDING.get(senderId);
				if (boundAdminId != null) {
					// 已绑定，私发给专属客服
					sendMessage(getSessionKey("USER", boundAdminId), chatMessage);
				} else {
					// 未绑定，广播给所有在线客服
					broadcastToUsers(chatMessage);
				}
			} else if ("USER".equals(senderType)) {
				// --- 客服发消息 ---
				String receiverId = chatMessage.getReceiverId();
				if (receiverId != null) {
					String boundAdmin = CUSTOMER_BINDING.get(receiverId);
					if (boundAdmin == null) {
						// 1. 顾客未被绑定，客服首次回复 -> 触发接入
						CUSTOMER_BINDING.put(receiverId, senderId);
						sendMessage(getSessionKey("CUSTOMER", receiverId), chatMessage);
						ChatMessage sysMsg = buildSystemMsg(null, "顾客[" + getCustomerName(receiverId) + "]已被客服["+ chatMessage.getSenderName() +"]接入");
						broadcastToUsers(sysMsg);
						chatMessageMapper.insert(sysMsg);
					} else if (senderId.equals(boundAdmin)) {
						// 2. 正常私聊
						sendMessage(getSessionKey("CUSTOMER", receiverId), chatMessage);
					} else {
						// 3. 抢占冲突
						ChatMessage sysMsg = buildSystemMsg(senderId, "接入失败：该顾客已被其他客服接入");
						sendMessage(getSessionKey("USER", senderId), sysMsg);
						chatMessageMapper.insert(sysMsg);
					}
				}
			}
		}
	}
	/**
	 * 连接关闭：更新数据库离线状态
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String senderType = getAttr(session, "senderType");
		String senderId = getAttr(session, "senderId");
		String sessionKey = getSessionKey(senderType, senderId);
		SESSION_MAP.remove(sessionKey);
		// 如果是客服下线，更新数据库状态为离线
		if ("USER".equals(senderType)) {
			try {
				User user = userMapper.selectById(Long.valueOf(senderId));
				if (user != null) {
					user.setNowStatus("离线");
					userMapper.updateById(user);
				}
			} catch (NumberFormatException e) { }
		}
		// 如果是顾客下线，可以清理绑定关系（根据业务需求决定是否保留）
		if ("CUSTOMER".equals(senderType)) {
			CUSTOMER_BINDING.remove(senderId);
		}
		log.info("下线: {}", sessionKey);
	}
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket异常", exception);
		if (session.isOpen()) session.close();
	}
	// ============ 业务辅助方法 ============
	// 自动填充发送者真实姓名
	private void fillSenderName(ChatMessage msg) {
		try {
			if ("USER".equals(msg.getSenderType())) {
				User user = userMapper.selectById(Long.valueOf(msg.getSenderId()));
				if (user != null) msg.setSenderName(user.getName());
			} else if ("CUSTOMER".equals(msg.getSenderType())) {
				customer cust = customerMapper.selectById(Long.valueOf(msg.getSenderId()));
				if (cust != null) msg.setSenderName(cust.getName());
			}
		} catch (NumberFormatException e) {
			msg.setSenderName("未知用户");
		}
	}
	// 获取顾客名字（用于系统提示）
	private String getCustomerName(String customerId) {
		try {
			customer cust = customerMapper.selectById(Long.valueOf(customerId));
			return cust != null ? cust.getName() : customerId;
		} catch (Exception e) {
			return customerId;
		}
	}
	// 构建系统提示消息
	private ChatMessage buildSystemMsg(String receiverId, String content) {
		ChatMessage msg = new ChatMessage();
		msg.setSenderType("SYSTEM");
		msg.setSenderId("SYSTEM");
		msg.setSenderName("系统提示");
		msg.setReceiverId(receiverId);
		msg.setContent(content);
		msg.setType(2); // 2表示系统提示
		msg.setCreateTime(LocalDateTime.now());
		return msg;
	}
	// 广播给所有在线客服
	private void broadcastToUsers(ChatMessage message) throws Exception {
		for (String key : SESSION_MAP.keySet()) {
			if (key.startsWith("USER:")) {
				sendMessage(key, message);
			}
		}
	}
	// 单发消息
	private void sendMessage(String receiverKey, ChatMessage chatMessage) throws Exception {
		WebSocketSession receiverSession = SESSION_MAP.get(receiverKey);
		if (receiverSession != null && receiverSession.isOpen()) {
			receiverSession.sendMessage(new TextMessage(JSON.toJSONString(chatMessage)));
		}
	}
	// ============ 基础工具方法 ============
	private String getAttr(WebSocketSession session, String key) {
		String uri = session.getUri().toString();
		String[] parts = uri.split("/");
		if ("senderType".equals(key)) return parts[parts.length - 2];
		if ("senderId".equals(key)) return parts[parts.length - 1];
		return null;
	}
	private String getSessionKey(String type, String id) {
		return type + ":" + id;
	}
}