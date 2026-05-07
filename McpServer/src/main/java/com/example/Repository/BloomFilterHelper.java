package com.example.Repository;

import com.example.Mapper.BuildingsMapper;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.EnergyReadingsMapper;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BloomFilterHelper {

    @Autowired
    private BuildingsMapper buildingsMapper;
    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;


    @Autowired
    private RedissonClient redissonClient;
    //布隆过滤器名称
    public static final String BLOOM_FILTER_BUILDING="building:bloom:filter";
    public static final String BLOOM_FILTER_DEVICE="device:bloom:filter";

    public static final String BLOOM_FILTER_MCPENERGY="mcpenergy:bloom:filter";
    //预期插入的元素数量
    private static final int expectedInsertions = 100000;

    private static final double fpp=0.05;


    //启动服务器自动初始化过滤器
    @PostConstruct
    public void BloomFilterInit(){
        //初始化所有建筑信息
        List<Integer> allBuildingId = buildingsMapper.getAllBuildingId();
        initBloomFilter(BLOOM_FILTER_BUILDING,allBuildingId);
        List<String> allBuildingCode = buildingsMapper.getAllBuildingCode();
        initBloomFilter(BLOOM_FILTER_BUILDING,allBuildingCode);
        List<String> allBuildingName = buildingsMapper.getAllBuildingName();
        initBloomFilter(BLOOM_FILTER_BUILDING,allBuildingName);
        //初始化所有设备信息
        List<Integer> allDeviceId = devicesMapper.queryAllDeviceIds();
        initBloomFilter(BLOOM_FILTER_DEVICE,allDeviceId);
        List<String> allDeviceCode = devicesMapper.queryAllDeviceCode();
        initBloomFilter(BLOOM_FILTER_DEVICE,allDeviceCode);
        List<String> allDeviceName = devicesMapper.queryAllDeviceType();
        initBloomFilter(BLOOM_FILTER_DEVICE,allDeviceName);
    }

    /**
    初始化布隆过滤器
     **/
    public<T> void initBloomFilter(String filterName, List<T> dataList){

        //创建布隆过滤器
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        if(!bloomFilter.isExists()){
            //
            bloomFilter.tryInit(expectedInsertions, fpp);
            for (T t : dataList) {
                //添加元素
                bloomFilter.add(t);
            }
        }
    }
    //判断元素是否存在
    public<T> boolean mightContain(String filterName, T value){
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        //判断元素是否存在
        return bloomFilter.contains(value);
    }
    //新增元素
    public<T> void addValue(String filterName, T value){
        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(filterName);
        bloomFilter.add(value);
    }
    //删除元素，到那时在普通的布隆过滤中并没有删除元素的方法，可以使用计数布隆过滤器来代替





}
