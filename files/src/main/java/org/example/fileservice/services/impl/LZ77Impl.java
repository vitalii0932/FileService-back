package org.example.fileservice.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.fileservice.services.LZ77;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

@Slf4j
@Component
public class LZ77Impl implements LZ77 {

    // Оптимизированные константы
    private static final int WINDOW_SIZE = 32768;    // 32KB окно поиска
    private static final int MAX_MATCH_LENGTH = 258; // Максимальная длина совпадения
    private static final int MIN_MATCH_LENGTH = 3;   // Минимальная длина для кодирования
    private static final int LOOKAHEAD_BUFFER = 258; // Буфер предпросмотра

    @Override
    public byte[] compress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        int cursor = 0;

        while (cursor < input.length) {
            int windowStart = Math.max(0, cursor - WINDOW_SIZE);
            int lookaheadLimit = Math.min(cursor + LOOKAHEAD_BUFFER, input.length);

            int[] match = findLongestMatch(input, windowStart, cursor, lookaheadLimit);

            // Если найденное совпадение достаточно длинное
            if (match[1] >= MIN_MATCH_LENGTH) {
                output.write(1);                      // Флаг кодированных данных
                writeShort(output, match[1]);         // Длина совпадения (2 байта)
                writeShort(output, match[0]);         // Расстояние (2 байта)
                cursor += match[1];
            } else {
                output.write(0);                      // Флаг литерала
                output.write(input[cursor]);          // Сам байт
                cursor++;
            }
        }

        byte[] result = output.toByteArray();
        log.debug("Compression: {} bytes -> {} bytes", input.length, result.length);
        return result;
    }

    @Override
    public byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length * 2);
        int cursor = 0;

        try {
            while (cursor < input.length) {
                int flag = input[cursor++] & 0xFF;

                if (flag == 0) {
                    if (cursor >= input.length) break;
                    output.write(input[cursor++]);
                } else {
                    if (cursor + 3 >= input.length) break;

                    int length = readShort(input, cursor);
                    cursor += 2;
                    int distance = readShort(input, cursor);
                    cursor += 2;

                    // Оптимизированное копирование
                    byte[] buffer = output.toByteArray();
                    int startPos = buffer.length - distance;

                    if (startPos < 0) {
                        throw new IllegalStateException("Invalid distance: " + distance);
                    }

                    if (length <= distance) {
                        // Простое копирование, если длина меньше расстояния
                        output.write(buffer, startPos, length);
                    } else {
                        // Оптимизация для перекрывающихся копий
                        for (int i = 0; i < length; i++) {
                            output.write(buffer[startPos + (i % distance)]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Decompression error at position {}: {}", cursor, e.getMessage());
            throw e;
        }

        return output.toByteArray();
    }

    private int[] findLongestMatch(byte[] data, int windowStart, int cursor, int lookaheadLimit) {
        int bestLength = 0;
        int bestDistance = 0;

        // Ограничиваем максимальную длину поиска
        int maxLength = Math.min(MAX_MATCH_LENGTH, lookaheadLimit - cursor);
        if (maxLength < MIN_MATCH_LENGTH) {
            return new int[]{0, 0};
        }

        byte firstByte = data[cursor];
        for (int i = windowStart; i < cursor; i++) {
            if (data[i] == firstByte) {
                int length = 1;
                while (length < maxLength &&
                        cursor + length < data.length &&
                        data[i + length] == data[cursor + length]) {
                    length++;
                }

                if (length > bestLength) {
                    bestLength = length;
                    bestDistance = cursor - i;
                }
            }
        }

        return new int[]{bestDistance, bestLength};
    }

    // Вспомогательные методы для работы с 16-битными значениями
    private void writeShort(ByteArrayOutputStream output, int value) {
        output.write((value >> 8) & 0xFF);
        output.write(value & 0xFF);
    }

    private int readShort(byte[] input, int position) {
        return ((input[position] & 0xFF) << 8) | (input[position + 1] & 0xFF);
    }
}