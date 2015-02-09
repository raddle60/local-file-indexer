package com.raddle.index.lucene.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * 类NoSymbolTokenizer.java的实现描述：任意字符分隔
 * @author raddle60 2013-5-7 下午10:03:02
 */
public class FullTwoTokenizer extends Tokenizer {

    public FullTwoTokenizer(Reader in){
        super(in);
    }

    public FullTwoTokenizer(AttributeSource source, Reader in){
        super(source, in);
    }

    public FullTwoTokenizer(AttributeFactory factory, Reader in){
        super(factory, in);
    }

    private int bufferIndex = 0, dataLen = 0;
    private final static int MAX_WORD_LEN = 255;
    private final static int IO_BUFFER_SIZE = 1024;
    private final char[] buffer = new char[MAX_WORD_LEN];
    private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

    private int length;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private Character preChar;
    private Character prePreChar;
    private Character curChar;

    // 对搜索进行分词优化，有双分词的，就不产生单的，减少分词个数
    private boolean forSearch = false;

    private List<String> extraTokens = new ArrayList<String>();

    private final void push(char c) {
        buffer[length++] = c; // buffer it
    }

    private final boolean flush() {
        if (length > 0) {
            // 如果前一个字是中文，做双字切分
            if (preChar != null && Character.getType(preChar) == Character.OTHER_LETTER && Character.getType(curChar) == Character.OTHER_LETTER) {
                if (!forSearch) {
                    // 单字分词
                    termAtt.copyBuffer(buffer, 0, length);
                }
                // 额外的双字分词
                StringBuilder sb = new StringBuilder();
                sb.append(preChar).append(curChar);
                extraTokens.add(sb.toString());
            } else {
                // 如果是单个中文或英文，但是有非字母分割符，也做双字切分
                String content = new String(buffer, 0, length);
                if (needSplit(content)) {
                    // 单双分词
                    List<String> splits = split(content);
                    if (splits.size() > 0) {
                        // 当前输出一个，不然当前就是空token了
                        termAtt.append(splits.remove(0));
                        extraTokens.addAll(splits);
                    } else {
                        // 原样输出，说名是全符号，没字母和数字
                        termAtt.copyBuffer(buffer, 0, length);
                    }
                } else {
                    // 完整单词
                    termAtt.copyBuffer(buffer, 0, length);
                    // 大写字母分割
                    splitCamel(content);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean needSplit(String content) {
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private List<String> split(String content) {
        List<String> list = new ArrayList<String>();
        List<CharRange> charRanges = new ArrayList<CharRange>();
        // 搜索出所有字母段
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (start == -1) {
                    start = i;
                }
            } else {
                // 碰到非数字字母
                if (start != -1) {
                    // 前面有过数字字母
                    charRanges.add(new CharRange(start, i - 1));
                    start = -1;
                }
            }
            if (i == content.length() - 1) {
                // 最后一个
                if (start != -1) {
                    // 前面有过数字字母
                    charRanges.add(new CharRange(start, i));
                    start = -1;
                }
            }
        }
        if (charRanges.size() > 0) {
            CharRange first = charRanges.get(0);
            if (first.start != 0) {
                // 说明前面有非数字字母
                // 只保留字母前一个字符，防止有多个，复制时漏了，就会查不到
                // 保留字母前一个字符已经足够精确
                // 例子见字母后只保留一个字符
                list.add(content.substring(first.start - 1, first.end + 1));
            }
            if (charRanges.size() > 0) {
                // 做单个和二分
                for (int i = 0; i < charRanges.size(); i++) {
                    CharRange charRange = charRanges.get(i);
                    // 单个的
                    if (!forSearch) {
                        // 用于索引，必须产生单个的，不然单词就查不到了
                        list.add(content.substring(charRange.start, charRange.end + 1));
                        // 大写字母分割
                        splitCamel(content.substring(charRange.start, charRange.end + 1));
                    } else {
                        if (list.size() == 0 && charRanges.size() == 1) {
                            // first没产生分词，而且只有一个字母分词
                            // last就不判断了，最多多一个分词
                            // 比如constant_type，就不不会产生单独的分词constant和type
                            list.add(content.substring(charRange.start, charRange.end + 1));
                        }
                    }
                    // 双切的
                    if (i > 0) {
                        CharRange pre = charRanges.get(i - 1);
                        if (charRange.start - pre.end > 2) {
                            // 说明中间隔了多个字符,只取最近的一个，因为连起来有些情况查不到
                            // 比如node.setName("abc")，会分成setname("abc
                            // 但是一般输入是"abc"，分词后会是"abc, abc"，"abc匹配不上
                            list.add(content.substring(pre.start, pre.end + 2));
                            list.add(content.substring(charRange.start - 1, charRange.end + 1));
                        } else {
                            list.add(content.substring(pre.start, charRange.end + 1));
                        }
                    }
                }
            }
            CharRange last = charRanges.get(charRanges.size() - 1);
            if (last.end != content.length() - 1) {
                // 说明后面有非数字字母
                // 只保留字母后一个字符，复制时漏了，就会查不到
                // 比如getCode(), s，一般只会复制getCode()搜索，后面逗号就会漏掉
                list.add(content.substring(last.start, last.end + 2));
            }
        }
        //System.out.println(list);
        return list;
    }

    private static class CharRange {

        private int start = -1;
        private int end = -1;

        private CharRange(int start, int end){
            this.start = start;
            this.end = end;
        }
    }

    private void splitCamel(String content) {
        List<String> splited = new ArrayList<String>();
        int preType = -1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            int type = Character.getType(c);
            if (preType == -1 || preType == type) {
                sb.append(c);
                preType = type;
                continue;
            } else if (sb.length() > 0) {
                if (preType == Character.UPPERCASE_LETTER && type == Character.LOWERCASE_LETTER) {
                    if (sb.length() == 1) {
                        sb.append(c);
                        preType = type;
                        continue;
                    } else {
                        splited.add(sb.substring(0, sb.length() - 1));
                        // 回到前两个字符
                        type = Character.UPPERCASE_LETTER;
                        i--;
                    }
                } else {
                    splited.add(sb.toString());
                }
                sb = new StringBuilder();
                // 已经到了下一个字母了，往回倒一个
                i--;
            }
            preType = type;
        }
        if (splited.size() > 0 && sb.length() > 0) {
            // 最后剩下的一个
            splited.add(sb.toString());
        }
        if (splited.size() > 1) {
            // 有两个才是发生了分割，做二分
            for (int i = 0; i < splited.size(); i++) {
                if (!forSearch) {
                    // 搜索用，只需要双分
                    // 索引用必须要单个也索引
                    extraTokens.add(splited.get(i));
                }
                if (i > 0 && splited.size() > 2) {
                    // 大于两个才拼，不然拼出来刚好是原串
                    extraTokens.add(splited.get(i - 1) + splited.get(i));
                }
            }
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        if (extraTokens.size() > 0) {
            Iterator<String> iterator = extraTokens.iterator();
            termAtt.append(iterator.next());
            iterator.remove();
            return true;
        }
        length = 0;

        while (true) {
            final char c;

            if (bufferIndex >= dataLen) {
                dataLen = input.read(ioBuffer);
                bufferIndex = 0;
            }

            if (dataLen == -1) {
                return flush();
            } else {
                prePreChar = preChar;
                preChar = curChar;
                c = ioBuffer[bufferIndex++];
                curChar = c;
                //System.out.println(preChar + " - " + curChar);
            }
            if (!Character.isWhitespace(c)) {
                if (Character.getType(c) == Character.OTHER_LETTER) {
                    if (length > 0) {
                        // s时,当发现中文时，要把s先输出掉
                        // 因为当前已经到“时”了，所以要往前去一个字符
                        // 不能把bufferIndex-2，因为可能就2个字符
                        bufferIndex--;
                        curChar = preChar;
                        preChar = prePreChar;
                        return flush();
                    }
                    push(c);
                    return flush();
                } else {
                    push(c);
                    if (length == MAX_WORD_LEN) {
                        return flush();
                    }
                }
            } else {
                if (length > 0) {
                    return flush();
                }
            }
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        bufferIndex = dataLen = 0;
    }

    public boolean isForSearch() {
        return forSearch;
    }

    public void setForSearch(boolean forSearch) {
        this.forSearch = forSearch;
    }
}
