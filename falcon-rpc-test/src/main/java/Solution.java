
import java.util.Arrays;

class Solution {
    public int superEggDrop(int k, int n) {
        memo = new int[k + 1][n + 1];
        for(int[] x : memo){
            Arrays.fill(x, -1);
        }
        return dp(k, n);
    }
    int[][] memo;

    // 状态：鸡蛋数、需要测试到第n层数
    // 选择：哪一层扔鸡蛋
    int dp(int k, int n) {
        if(k == 1) {
            return n;
        }
        if(n == 0) {
            return 0;
        }
        if(memo[k][n] != -1) {
            return memo[k][n];
        }
        int res = Integer.MAX_VALUE;
        int lo = 1, hi = n;
        while(lo <= hi) {
            int mid = lo + ((hi - lo) >> 1);
            int broken = dp(k - 1, mid - 1);
            int notBroken = dp(k, mid + 1);
            if(broken > notBroken) {
                hi= mid - 1;
                res = Math.min(res, broken + 1);
            } else {
                lo = mid + 1;
                res = Math.min(res, notBroken + 1);
            }
        }
        memo[k][n] = res;
        return res;
    }
}