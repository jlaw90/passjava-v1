import java.util.HashSet;

class Solution {

    private static HashSet<Integer> _stack = new HashSet<Integer>();

    // synchronize to block repeated access to _stack
    // if not wanted for this assignment, could make _stack a local var but would increase execution time
    // due to object alloc/dealloc
    public static synchronized int stone_wall(int[] H) {
        if (H == null || H.length == 0)
            return 0;

        int curY = H[0];
        _stack.add(curY);
        int stones = 1;

        for (int i = 1; i < H.length; i++) {
            // Y
            int y = H[i];

            // if height is the same as previously, we can extend along the x-axis so do nothing

            // if taller, add a new block
            if (y > curY) {
                stones += 1;
                _stack.add(y); // push our new block height onto the stack
            }
            // if smaller...
            else if (y < curY) {
                // remove all blocks on the stack above y...
                for (int j = curY; j > y; j--) {
                    _stack.remove(j);
                }

                // If we're still below the last block at this height, we need to add a new block below us
                if (!_stack.contains(y)) {
                    stones += 1;
                    _stack.add(y);
                }
            }
            curY = y;
        }
        _stack.clear();
        return stones;
    }

    public static void main(String[] args) {
        System.out.println(stone_wall(new int[]{1, 3, 1, 1, 3, 2, 2, 1, 5, 4, 3, 3, 1, 3, 4}));
    }
}