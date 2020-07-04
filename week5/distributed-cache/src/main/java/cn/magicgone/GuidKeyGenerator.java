package cn.magicgone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuidKeyGenerator implements KeyGenerator {
    @Override
    public List<String> get(int n) {
        List<String> list = new ArrayList<>(n);
        for(int i = 0; i < n; i++){
            list.add(UUID.randomUUID().toString());
        }
        return list;
    }
}
