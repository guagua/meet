package com.baidu.meet.dbcache;

import com.baidu.meet.cache.BdCacheService;
import com.baidu.meet.cache.BdCacheService.CacheEvictPolicy;
import com.baidu.meet.cache.BdCacheService.CacheStorage;
import com.baidu.meet.cache.BdKVCache;

public class DBKVCacheManager {

	private static DBKVCacheManager mInstance = null;
	
	private static final String ACCOUNT_INFO = "meet.account";
	private static final int ACCOUNT_NUM = 1;
	private BdKVCache<String> mAccountCache = null;

	public static DBKVCacheManager getInstance() {
		if (mInstance == null) {
			mInstance = new DBKVCacheManager();
		}
		return mInstance;
	}

	private DBKVCacheManager() {

	}

	/**
	 * 获取账号信息Cache
	 * 
	 * @return
	 */
	public BdKVCache<String> getAccountCache() {
		if (mAccountCache == null) {
			BdCacheService service = BdCacheService.sharedInstance();
			mAccountCache = service.getAndStartTextCache(ACCOUNT_INFO,
					CacheStorage.SQLite_CACHE_PER_TABLE,
					CacheEvictPolicy.LRU_ON_INSERT, ACCOUNT_NUM);
		}
		return mAccountCache;
	}
}
