/**
 * 
 */
package com.baidu.meet.talk;

/**
 * 最近聊天的人。
 */
public class RecentChatFriendData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3133087680118197014L;

	/** 我 */
	protected String ownerId;

	protected String ownerName;

	/** 与我聊天的朋友 */
	protected String friendId;

	protected String friendName;

	/** 朋友的头像加密串 */
	protected String friendPortrait;

	/**
	 * 消息状态
	 * 
	 * @see ChatMessageData#status
	 */
	protected int status;

	/** 本地发送时间。单位：毫秒。 */
	protected long localTime;

	/** 服务器的时间。单位：毫秒。 */
	protected long serverTime;

	/** 消息内容。Json串。 */
	protected String msgContent;

	/** 未读消息数 */
	protected int unReadCount;

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getFriendId() {
		return friendId;
	}

	public void setFriendId(String friendId) {
		this.friendId = friendId;
	}

	public String getFriendPortrait() {
		return friendPortrait;
	}

	public void setFriendPortrait(String friendPortrait) {
		this.friendPortrait = friendPortrait;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getLocalTime() {
		return localTime;
	}

	public void setLocalTime(long localTime) {
		this.localTime = localTime;
	}

	public long getServerTime() {
		return serverTime;
	}

	public void setServerTime(long serverTime) {
		this.serverTime = serverTime;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getFriendName() {
		return friendName;
	}

	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
}
