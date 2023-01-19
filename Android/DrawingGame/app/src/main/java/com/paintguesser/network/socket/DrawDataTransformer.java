package com.paintguesser.network.socket;

public interface DrawDataTransformer<Input, Output> {
    Output transform(Input input);
}
