
class Solution {
    public int[] searchRange(int[] nums, int target) {
        if (nums.length == 0) return new int[]{-1,-1};
        int[] res = new int[2];
        res[0] = searchLeft(nums, target);
        res[1] = searchRight(nums, target);
        return res;
    }

    int searchLeft(int[] nums, int target) {
        int lo = 0, hi = nums.length;
        while(lo < hi) {
            int mid = lo + ((hi - lo) >> 1);
            if(nums[mid] == target) {
                hi = mid;
            } else if(nums[mid] > target) {
                hi = mid;
            } else if(nums[mid] < target) {
                lo = mid + 1;
            }
        }

        return lo;
    }

    int searchRight(int[] nums, int target) {
        int lo = 0, hi = nums.length;
        while(lo < hi) {
            int mid = lo + ((hi - lo) >> 1);
            if(nums[mid] == target) {
                lo = mid + 1;
            } else if(nums[mid] > target) {
                hi = mid;
            } else if(nums[mid] < target) {
                lo = mid + 1;
            }
        }
        return lo - 1;
    }
}