package com.nico.bowling;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Game {

    public static final int NUMBER_OF_FRAMES = 10;
    public static final int SPARE_OR_STRIKE_PINS = 10;

    private final List<MutablePair<Integer, Integer>> frames = initFrames();

    private static List<MutablePair<Integer, Integer>> initFrames() {
        return IntStream.range(0,NUMBER_OF_FRAMES)
                .mapToObj(index -> MutablePair.of((Integer) null, (Integer) null))
                .toList();
    }

    private int currentFrameIndex = 0;
    private boolean firstFrameRoll = true;

    public void roll(int pins) {
        boolean secondFrameRoll = !firstFrameRoll;

        MutablePair<Integer, Integer> currentFrame = frames.get(currentFrameIndex);
        if(firstFrameRoll) {
            currentFrame.setLeft(pins);
        } else {
            currentFrame.setRight(pins);
        }
        if(isStrike(currentFrame) && !isLastFrame(currentFrameIndex)) {
            secondFrameRoll = true;
        }
        if(secondFrameRoll) {
            currentFrameIndex++;
        }
        firstFrameRoll = secondFrameRoll;
    }

    private boolean isLastFrame(int frameIndex) {
        return frameIndex >= NUMBER_OF_FRAMES - 1;
    }

    public int score() {
        return IntStream.range(0, NUMBER_OF_FRAMES)
                .map(this::computeFrameScore)
                .sum();
    }

    private int computeFrameScore(int frameIndex) {
        Pair<Integer, Integer> frame = frames.get(frameIndex);
        int frameScore = getFrameScore(frame);
        if(isStrike(frame)) {
            frameScore = SPARE_OR_STRIKE_PINS + twoNextRollsPins(frameIndex);
        } else if(isSpare(frame)) {
            frameScore = SPARE_OR_STRIKE_PINS + nextRollPins(frameIndex);
        }
        return frameScore;
    }

    private int twoNextRollsPins(int frameIndex) {
        if(isLastFrame(frameIndex)) {
            return getFrameScore(frames.get(frameIndex));
        }
        if(frameIndex == NUMBER_OF_FRAMES - 2) {
            Pair<Integer, Integer> nextFrame = frames.get(frameIndex + 1);
            return getFrameScore(nextFrame);
        }

        Pair<Integer, Integer> nextFrame = frames.get(frameIndex + 1);
        int result = getFrameFirstRollPins(nextFrame);
        if(!isStrike(nextFrame)) {
            return result + getFrameSecondRollPins(nextFrame);
        }
        nextFrame = frames.get(frameIndex + 2);
        return result + getFrameFirstRollPins(nextFrame);
    }

    private int nextRollPins(int frameIndex) {
        Pair<Integer, Integer> nextFrame = frames.get(frameIndex + 1);
        return getFrameFirstRollPins(nextFrame);
    }

    private boolean isSpare(Pair<Integer, Integer> frame) {
        return !isStrike(frame) && (getFrameScore(frame) == SPARE_OR_STRIKE_PINS);
    }

    private boolean isStrike(Pair<Integer, Integer> frame) {
        return getFrameFirstRollPins(frame) == SPARE_OR_STRIKE_PINS;
    }

    private static int getFrameScore(Pair<Integer, Integer> frame) {
        return getFrameFirstRollPins(frame) + getFrameSecondRollPins(frame);
    }

    private static int getFrameFirstRollPins(Pair<Integer, Integer> frame) {
        return Optional.ofNullable(frame).map(Pair::getLeft).orElse(0);
    }

    private static int getFrameSecondRollPins(Pair<Integer, Integer> frame) {
        return Optional.ofNullable(frame).map(Pair::getRight).orElse(0);
    }
}
