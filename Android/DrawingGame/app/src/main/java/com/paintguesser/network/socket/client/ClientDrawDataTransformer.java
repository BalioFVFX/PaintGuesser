package com.paintguesser.network.socket.client;

import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.DrawDataTransformer;
import com.paintguesser.view.ui.Point;

import java.util.ArrayList;
import java.util.List;

public class ClientDrawDataTransformer implements DrawDataTransformer<String, DrawData> {

    /**
     * Transforms the provided string input {10.30&30.50&1234|55.32&94&3123|}1920&1080 to
     * DrawData object
     */
    @Override
    public DrawData transform(String input) {
        final StringBuilder builder = new StringBuilder();
        final List<Point> pointList = new ArrayList<>();

        int index = input.indexOf("{") + 1;
        float first;
        float second;
        int third;

        while(input.charAt(index) != '}') {
            while (input.charAt(index) != '&') {
                builder.append(input.charAt(index));
                index++;
            }

            first = Float.parseFloat(builder.toString());
            builder.delete(0, builder.length());

            index++;

            while (input.charAt(index) != '&') {
                builder.append(input.charAt(index));
                index++;
            }

            index++;

            second = Float.parseFloat(builder.toString());
            builder.delete(0, builder.length());

            while (input.charAt(index) != '|' && input.charAt(index) != '}') {
                builder.append(input.charAt(index));
                index++;
            }

            if (input.charAt(index) == '|') {
                index++;
            }

            third = Integer.parseInt(builder.toString());
            builder.delete(0, builder.length());

            pointList.add(new Point(first, second, third));
        }

        index++;

        while (input.charAt(index) != '&') {
            builder.append(input.charAt(index));
            index++;
        }

        first = Float.parseFloat(builder.toString());
        builder.delete(0, builder.length());

        index++;

        while (index < input.length()) {
            builder.append(input.charAt(index));
            index++;
        }

        second = Float.parseFloat(builder.toString());

        return new DrawData(pointList, first, second);
    }
}
