package com.znaptag.tool.util;

import java.security.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class Digest
{
    private MessageDigest md = null;

    public Digest(String digest)
    throws NoSuchAlgorithmException
    {
        md = MessageDigest.getInstance(digest);
    }

    public byte[] hashFile(File file)
    throws FileNotFoundException, IOException
    {
        InputStream is = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }

        byte[] hash = md.digest();

        is.close();

        return hash;
    }

    public byte[] hashAsBytes(String[] dataToHash)
    {
        StringBuffer sb = new StringBuffer();
        for (String entry : dataToHash) {
            sb.append(entry);
        }
        md.update(sb.toString().getBytes(), 0, dataToHash.length);
        return md.digest();
    }

    public byte[] hashAsBytes(byte[] dataToHash)
    {
        md.update(dataToHash, 0, dataToHash.length);
        return md.digest();
    }

    public byte[] hashAsBytes(String dataToHash)
    {
        return hashAsBytes(dataToHash.getBytes());
    }

    public String hashAsString(String dataToHash)
    {
        return hexStringFromBytes(hashAsBytes(dataToHash.getBytes()));
    }

    public String hashAsString(byte[] dataToHash)
    {
        return hexStringFromBytes(hashAsBytes(dataToHash));
    }

    public static String hexStringFromBytes(byte[] b)
    {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            buffer.append(String.format("%02X", b[i]));
        }

        return buffer.toString();
    }
}
