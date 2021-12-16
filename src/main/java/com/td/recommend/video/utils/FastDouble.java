package com.td.recommend.video.utils;

public class FastDouble {
    private static final int[] POW10 = {1, 10, 100, 1000, 10000, 100000, 1000000};

    public static String format(double val, int precision) {
        StringBuilder sb = new StringBuilder();
        if (val < 0) {
            sb.append('-');
            val = -val;
        }
        int exp = POW10[precision];
        long lval = (long) (val * exp + 0.5);
        sb.append(lval / exp).append('.');
        long fval = lval % exp;
        for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
            sb.append('0');
        }
        sb.append(fval);
        return sb.toString();
    }

    public static double round(double val, int precision) {
        int exp = POW10[precision];
        double carry = val > 0 ? 0.5 : -0.5;
        long lval = (long) (val * exp + carry);
        return (double) lval / exp;
    }
}

