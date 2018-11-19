package formula;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class Reader {
    private PushbackReader reader;

    private int position = 1;

    public Reader(InputStream inputStream) {
        reader = new PushbackReader(new InputStreamReader(inputStream));
    }

    public Reader(String formula) {
        this(new ByteArrayInputStream(formula.getBytes()));
    }

    public char nextChar() throws IOException {
        while (reader.ready()) {
            char nextChar = rawRead();
            switch (nextChar) {
            case ' ':
            case '\n':
            case '\t':
                continue;
            default:
                return nextChar;
            }
        }
        throw new IOException("Unexpected EOF.");
    }

    public void unread(char charIn) throws IOException {
        reader.unread(charIn);
        position--;
    }

    public char rawRead() throws IOException {
        char nextChar = (char) reader.read();
        position++;
        return nextChar;
    }

    public boolean ready() throws IOException {
        return reader.ready();
    }

    public int getPosition() {
        return position;
    }

}
