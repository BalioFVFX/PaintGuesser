package com.paintguesser.network.socket.server;

import com.paintguesser.network.socket.Constants;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.DrawDataTransformer;

public class SeverDrawDataTransformer implements DrawDataTransformer<DrawData, String> {

    /**
     * Transforms the provided data to string in the following format:
     * "{10.30&30.50&1234|55.32&94&3123|}1920&1080"
     * Where the items surrounded by the {} brackets are points defined as
     * xPosition&yPosition&color, each point separated by | symbol
     */
    @Override
    public String transform(DrawData data) {
        final StringBuilder builder = new StringBuilder();

        builder.append(Constants.MSG_DRAW);

        builder.append("{");

        data.points.forEach(point -> {
            builder.append(point.x).append("&")
                    .append(point.y).append("&")
                    .append(point.color).append("|");
        });

        if (builder.length() > Constants.MSG_DRAW.length() + 1) {
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append("}").append(data.screenWidth).append("&").append(data.screenHeight);

        return builder.toString();
    }
}
