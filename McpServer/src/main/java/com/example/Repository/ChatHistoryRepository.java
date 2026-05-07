package com.example.Repository;

import java.util.List;

public interface ChatHistoryRepository {
    void save(String type,String id);

    List<String> get(String type);
    void delete(String type,String id);


}
