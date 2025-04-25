package org.example.fileservice.services;

public interface LZ77 {
    byte[] compress(byte[] data);
    byte[] decompress(byte[] data);
}
