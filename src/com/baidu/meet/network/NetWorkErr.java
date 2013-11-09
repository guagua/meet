package com.baidu.meet.network;

public class NetWorkErr {
	public static final int NETWORK_ERR = -1;
	public static final int NETWORK_OK = 0;
	public static final int NETWORK_IGNORE = -2;
	
	// 用户未登录
	public static final int USER_NOT_LOGIN = 1;
    // 用户名或密码错误
    public static final int USERNAME_OR_PASSWD_ERROR = 2;
    // 该吧不存在
    public static final int FORUM_NOT_EXIST = 3;
    // 该主题不存在
    public static final int THREAD_NOT_EXIST = 4;
    // 你浏览的主题已不存在，去看看其他贴子吧(和4无本质区别)
    public static final int THREAD_NOT_EXIST_1 = 28;
    // 这个楼层可能已被删除啦，去看看其他贴子吧
    public static final int POST_NOT_EXIST = 29;
    // 需要验证码
    public static final int NEED_VCODE = 5;
    // 验证码输入错误
    public static final int VCODE_INPUT_ERROR = 6;
    // 你的操作太频繁了
    public static final int OP_TOO_FAST = 7;
    // 为了维护贴吧的质量,你发表的贴子需要先经过系统审核,你可以先浏览一下别的贴子
    public static final int POST_NEED_AUDIT = 8;
    // 你的帐号由于不恰当操作已被封,你可以申请解封账号
    public static final int UID_PRISON = 9;
    // 你的网络地址由于不恰当操作已被封,你可以申请解封IP
    public static final int UIP_PRISON = 10;
    // 发贴重复
    public static final int POST_REPEAT = 11;
    // 发贴含有不适当内容或广告,请重新提交
    public static final int CONTENT_BUHEXIE = 12;
    // 插入表情数过多
    public static final int INSERT_TO_MANY_SMILE = 13;
    // 已顶过
    public static final int HAVE_DING = 14;
    // 图片太大
    public static final int FILE_UPLOAD_SIZE_ERROR = 15;
    // 此用户名已被注册，请另换一个
    public static final int USERNAME_HAS_REGED = 16;
    // 用户名最长不得超过7个汉字，或14个字节(数字，字母和下划线)
    public static final int USERNAME_IS_INVALID = 17;
    // 此用户名不可使用
    public static final int USERNAME_BU_HEXIE = 18;
    // 密码最少6个字符，最长不得超过14个字符
    public static final int PASSWD_IS_INVALID = 19;
    // 你的密码结构太过简单，请更换更复杂的密码，否则无法注册成功。
    public static final int PASSWD_IS_TOO_SIMPLE = 20;
    
    // 对不起，你现在无法登陆
    public static final int USER_CANNOT_LOGIN = 21;
    
    //用户已经喜欢过该吧
    public static final int USER_HAS_LIKE_FORUM = 22;
    
    //用户已经抢过粉
    public static final int USER_HAS_ADD_FAN = 120002;
    
    //注册相关错误
    public static final int REGIST_SMSCODE_ERROR = 26;
    public static final int REGIST_NAME_ERROR = 27;
    public static final int REGIST_PHONE_ERROR = 28;
    public static final int REGIST_PSW_ERROR = 29;
    
    //已有用户名
    public static final int USERNAME_HAVE_INPUT = 35;
    
    //用户名不可用
    public static final int USERNAME_CANT_USE = 36;
    
  //用户名不可用
    public static final int NO_MORE_IMAGE = 40;
    
    // 未知错误
    public static final int METHOD_MUST_POST =100;
    // 未知错误
    public static final int PARAM_NOT_ENOUGH = 101;
    // 未知错误
    public static final int INVALID_SIGN = 102;
    // 文件上传网络错误
    public static final int FILE_UPLOAD_NET_ERROR = 103;  
    
    // 未知错误
    public static final int INTERNAL_ERROR = 500;
    
    public static final int FRS_IMAGE_NO_EXIST = 2000;
    
    //server对于不重要页面停止服务（服务最小级）
    public static final int SERVER_DEAD = 110003;
    
    public static final int HAVE_SIGNED = 160002;

}
