package com.example.Repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {
    private final Map<String, List<String>> chatHistoryMap = new HashMap<>();
    @Override//将id添加到type类型的列表中
    public void save(String type, String id) {
        if(!chatHistoryMap.containsKey(type)){
            chatHistoryMap.put(type,new ArrayList<>());
        }
        List<String> strings = chatHistoryMap.get(type);//获取type类型的所有id
        if(strings.contains(id)){//判断这个类型中的id是否存在，如果已经存在，则不重复添加
            return;
        }
        strings.add(id);
    }

    @Override//获取type类型的所有id
    public List<String> get(String type) {
        return chatHistoryMap.getOrDefault(type,new ArrayList<>());
    }

    @Override
    public void delete(String type, String id) {
        if(!chatHistoryMap.containsKey(type)){
            chatHistoryMap.put(type,new ArrayList<>());
        }
        List<String> strings = chatHistoryMap.get(type);
        if(!strings.contains(id)){//判断这个类型中的id是否存在，如果已经存在，则删除
            return;
        }
        strings.remove(id);
    }
}
