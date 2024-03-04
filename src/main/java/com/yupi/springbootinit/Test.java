package com.yupi.springbootinit;

public class Test {
    public static void main(String[] args) {
        String str="【【【【【\n" +
                "{\n" +
                "    \"title\": {\n" +
                "        \"text\": \"网吧每日用户数量变化\"\n" +
                "    },\n" +
                "    \"tooltip\": {},\n" +
                "    \"legend\": {\n" +
                "        \"data\":[\"人数\"]\n" +
                "    },\n" +
                "    \"xAxis\": {\n" +
                "        \"data\": [\"1号\",\"2号\",\"3号\",\"4号\",\"5号\",\"6号\",\"7号\"]\n" +
                "    },\n" +
                "    \"yAxis\": {},\n" +
                "    \"series\": [{\n" +
                "        \"name\": \"人数\",\n" +
                "        \"type\": \"bar\",\n" +
                "        \"data\": [10, 15, 20, 15, 32, 145, 15]\n" +
                "    }]\n" +
                "}\n" +
                "】】】】";

        //System.out.println(str.split("【【【【【").length);
        String[] strings = str.split("【【【【【");
        System.out.println(strings[0]);
        System.out.println("======================");
        System.out.println(strings[1]);
        System.out.println("======================");
        System.out.println(strings[2]);
    }
}
