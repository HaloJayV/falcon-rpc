import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Solution {

    int size;
    Map<Integer, Integer> map;

    public Solution(int n, int[] blacklist) {
        map = new HashMap<>();
        size = n - blacklist.length;
        int last = n - 1;
        // 标记该索引已经有黑名单存在了
        for(int b : blacklist) {
            map.put(b, 666);
        }
        for(int b : blacklist) {
            // 已经在[size, n)内了，不操作
            if(b >= size) {
                continue;
            }
            // 已经存在了
            while(map.containsKey(last)) {
                last--;
            }
            // 放到数组尾部
            map.put(b, last);
            last--;
        }
    }

    public int pick() {
        Random random = new Random();
        int index = random.nextInt(size) % size;
        if(map.containsKey(index)) {
            // 映射到数组尾部
            return map.get(index);
        }
        return index;
    }
}

/**
 * Your Solution object will be instantiated and called as such:
 * Solution obj = new Solution(n, blacklist);
 * int param_1 = obj.pick();
 */