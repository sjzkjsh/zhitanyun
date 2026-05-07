package com.example.DataBaseController;

import com.example.Entity.ReultEntity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 按 key 清除指定缓存
     */
    @PostMapping("/evict")
    public Result<Void> evictCache(
            @RequestParam String cacheName,
            @RequestParam String key) {
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("清除缓存 [{}]: key={}", cacheName, key);
        }
        return Result.success();
    }

    /**
     * 清空整个缓存
     */
    @PostMapping("/clear")
    public Result<Void> clearCache(@RequestParam String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("清空缓存 [{}]", cacheName);
        }
        return Result.success();
    }

    /**
     * 清空所有缓存（激进）
     */
    @PostMapping("/clear/all")
    public Result<Void> clearAll() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
                log.info("清空缓存 [{}]", name);
            }
        });
        return Result.success();
    }

    /**
     * 查看缓存状态（调试用）
     */
    @GetMapping("/status")
    public Result<Set<String>> getCacheNames() {
        Set<String> names = cacheManager.getCacheNames().stream().collect(Collectors.toSet());
        return Result.success(names);
    }
}